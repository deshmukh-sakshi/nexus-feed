package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Admin.DTO.AdminReportResponse;
import com.nexus.feed.backend.Email.Service.EmailService;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Report;
import com.nexus.feed.backend.Entity.ReportReason;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.DuplicateReportException;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.PostRepository;
import com.nexus.feed.backend.Repository.ReportRepository;
import com.nexus.feed.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of ReportService for managing post reports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public Report createReport(UUID postId, UUID reporterId, ReportReason reason, String additionalDetails) {
        // Check for duplicate report
        if (reportRepository.existsByPostIdAndReporterId(postId, reporterId)) {
            log.warn("Duplicate report attempt: postId={}, reporterId={}", postId, reporterId);
            throw new DuplicateReportException("You have already reported this post");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Users reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        Report report = Report.builder()
                .post(post)
                .reporter(reporter)
                .reason(reason)
                .additionalDetails(additionalDetails)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report created: id={}, postId={}, reporterId={}, reason={}", 
                savedReport.getId(), postId, reporterId, reason);

        // Send confirmation email asynchronously
        sendReportConfirmationEmail(reporter, post.getTitle());

        return savedReport;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReportedPost(UUID postId, UUID reporterId) {
        return reportRepository.existsByPostIdAndReporterId(postId, reporterId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReportCountForPost(UUID postId) {
        return reportRepository.countByPostId(postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAllWithDetails(pageable)
                .map(this::mapToAdminReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReportsByReason(ReportReason reason, Pageable pageable) {
        return reportRepository.findByReasonWithDetails(reason, pageable)
                .map(this::mapToAdminReportResponse);
    }

    /**
     * Maps a Report entity to an AdminReportResponse DTO.
     */
    private AdminReportResponse mapToAdminReportResponse(Report report) {
        return new AdminReportResponse(
                report.getId(),
                report.getPost().getId(),
                report.getPost().getTitle(),
                report.getReporter().getId(),
                report.getReporter().getUsername(),
                report.getReason(),
                report.getReason().getDisplayName(),
                report.getAdditionalDetails(),
                report.getCreatedAt()
        );
    }

    /**
     * Sends a confirmation email to the reporter.
     */
    private void sendReportConfirmationEmail(Users reporter, String postTitle) {
        if (reporter.getAppUser() == null || reporter.getAppUser().getEmail() == null) {
            log.debug("Cannot send report confirmation email - no email address for user: {}", reporter.getId());
            return;
        }

        String email = reporter.getAppUser().getEmail();
        String username = reporter.getUsername();
        String subject = "Thanks for your report";
        String body = composeReportConfirmationEmail(username, postTitle);

        try {
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            // Log but don't fail the report creation if email fails
            log.error("Failed to send report confirmation email to {}: {}", email, e.getMessage());
        }
    }

    /**
     * Composes the report confirmation email body.
     */
    private String composeReportConfirmationEmail(String username, String postTitle) {
        return String.format(
            "Hey %s,\n\n" +
            "Thanks for reporting the post \"%s\".\n\n" +
            "We take reports seriously and will review this content to ensure it meets our " +
            "community guidelines. If we find that it violates our policies, we'll take " +
            "appropriate action.\n\n" +
            "We appreciate you helping keep Nexus Feed a safe and welcoming community.\n\n" +
            "The Nexus Feed Team",
            username,
            postTitle
        );
    }
}

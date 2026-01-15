package com.nexus.feed.backend.Service;

import com.nexus.feed.backend.Admin.DTO.AdminReportResponse;
import com.nexus.feed.backend.Email.Service.EmailService;
import com.nexus.feed.backend.Entity.Comment;
import com.nexus.feed.backend.Entity.Post;
import com.nexus.feed.backend.Entity.Report;
import com.nexus.feed.backend.Entity.Report.ReportableType;
import com.nexus.feed.backend.Entity.ReportReason;
import com.nexus.feed.backend.Entity.Users;
import com.nexus.feed.backend.Exception.DuplicateReportException;
import com.nexus.feed.backend.Exception.ResourceNotFoundException;
import com.nexus.feed.backend.Repository.CommentRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public Report createPostReport(UUID postId, UUID reporterId, ReportReason reason, String additionalDetails) {
        if (reportRepository.existsByReportableIdAndReportableTypeAndReporterId(postId, ReportableType.POST, reporterId)) {
            log.warn("Duplicfate report attempt: postId={}, reporterId={}", postId, reporterId);
            throw new DuplicateReportException("You have already reported this post");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Users reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        Report report = Report.builder()
                .reportableId(postId)
                .reportableType(ReportableType.POST)
                .reporter(reporter)
                .reason(reason)
                .additionalDetails(additionalDetails)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Post report created: id={}, postId={}, reporterId={}, reason={}", 
                savedReport.getId(), postId, reporterId, reason);

        sendReportConfirmationEmail(reporter, "post", post.getTitle());
        return savedReport;
    }

    @Override
    public Report createCommentReport(UUID commentId, UUID reporterId, ReportReason reason, String additionalDetails) {
        if (reportRepository.existsByReportableIdAndReportableTypeAndReporterId(commentId, ReportableType.COMMENT, reporterId)) {
            log.warn("Duplicate report attempt: commentId={}, reporterId={}", commentId, reporterId);
            throw new DuplicateReportException("You have already reported this comment");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        Users reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        Report report = Report.builder()
                .reportableId(commentId)
                .reportableType(ReportableType.COMMENT)
                .reporter(reporter)
                .reason(reason)
                .additionalDetails(additionalDetails)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Comment report created: id={}, commentId={}, reporterId={}, reason={}", 
                savedReport.getId(), commentId, reporterId, reason);

        String preview = comment.getBody().length() > 50 
                ? comment.getBody().substring(0, 50) + "..." 
                : comment.getBody();
        sendReportConfirmationEmail(reporter, "comment", preview);
        return savedReport;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReportedPost(UUID postId, UUID reporterId) {
        return reportRepository.existsByReportableIdAndReportableTypeAndReporterId(postId, ReportableType.POST, reporterId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReportedComment(UUID commentId, UUID reporterId) {
        return reportRepository.existsByReportableIdAndReportableTypeAndReporterId(commentId, ReportableType.COMMENT, reporterId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReportCountForPost(UUID postId) {
        return reportRepository.countByReportableIdAndReportableType(postId, ReportableType.POST);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReportCountForComment(UUID commentId) {
        return reportRepository.countByReportableIdAndReportableType(commentId, ReportableType.COMMENT);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getAllReports(Pageable pageable) {
        return reportRepository.findAllWithDetails(pageable).map(this::mapToAdminReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReportsByType(ReportableType type, Pageable pageable) {
        return reportRepository.findByReportableTypeWithDetails(type, pageable).map(this::mapToAdminReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReportsByReason(ReportReason reason, Pageable pageable) {
        return reportRepository.findByReasonWithDetails(reason, pageable).map(this::mapToAdminReportResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReportResponse> getReportsByReasonAndType(ReportReason reason, ReportableType type, Pageable pageable) {
        return reportRepository.findByReasonAndReportableTypeWithDetails(reason, type, pageable).map(this::mapToAdminReportResponse);
    }

    private AdminReportResponse mapToAdminReportResponse(Report report) {
        String contentTitle;
        String contentPreview;

        if (report.getReportableType() == ReportableType.POST) {
            Post post = postRepository.findById(report.getReportableId()).orElse(null);
            contentTitle = post != null ? post.getTitle() : "[Deleted Post]";
            contentPreview = post != null && post.getBody() != null 
                    ? (post.getBody().length() > 100 ? post.getBody().substring(0, 100) + "..." : post.getBody())
                    : null;
        } else {
            Comment comment = commentRepository.findById(report.getReportableId()).orElse(null);
            contentTitle = comment != null ? "Comment on: " + comment.getPost().getTitle() : "[Deleted Comment]";
            contentPreview = comment != null 
                    ? (comment.getBody().length() > 100 ? comment.getBody().substring(0, 100) + "..." : comment.getBody())
                    : null;
        }

        return new AdminReportResponse(
                report.getId(),
                report.getReportableId(),
                report.getReportableType(),
                contentTitle,
                contentPreview,
                report.getReporter().getId(),
                report.getReporter().getUsername(),
                report.getReason(),
                report.getReason().getDisplayName(),
                report.getAdditionalDetails(),
                report.getCreatedAt()
        );
    }

    private void sendReportConfirmationEmail(Users reporter, String contentType, String contentTitle) {
        if (reporter.getAppUser() == null || reporter.getAppUser().getEmail() == null) {
            return;
        }

        String email = reporter.getAppUser().getEmail();
        String subject = "Thanks for your report";
        String body = String.format(
            "Hey %s,\n\n" +
            "Thanks for reporting the %s \"%s\".\n\n" +
            "We take reports seriously and will review this content to ensure it meets our " +
            "community guidelines. If we find that it violates our policies, we'll take " +
            "appropriate action.\n\n" +
            "We appreciate you helping keep Nexus Feed a safe and welcoming community.\n\n" +
            "The Nexus Feed Team",
            reporter.getUsername(), contentType, contentTitle
        );

        try {
            emailService.sendEmail(email, subject, body);
        } catch (Exception e) {
            log.error("Failed to send report confirmation email to {}: {}", email, e.getMessage());
        }
    }
}

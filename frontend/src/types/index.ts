// User types
export interface User {
  userId: string
  username: string
  email: string
  profilePictureUrl?: string
  role?: 'USER' | 'ADMIN'
}

// Auth types
export interface AuthResponse {
  userId: string
  username: string
  email: string
  token: string
  role: 'USER' | 'ADMIN'
}

// Admin types
export interface AdminStats {
  totalUsers: number
  totalPosts: number
  totalComments: number
  totalVotes: number
  totalReports: number
  newUsersToday: number
  newPostsToday: number
}

export interface AdminUser {
  id: string
  username: string
  email: string
  role: 'USER' | 'ADMIN'
  karma: number
  profilePictureUrl?: string
  createdAt: string
  postCount: number
  commentCount: number
}

export interface AdminPost {
  id: string
  title: string
  body?: string
  userId: string
  username: string
  imageUrls: string[]
  tags: string[]
  upvotes: number
  downvotes: number
  commentCount: number
  reportCount: number
  createdAt: string
  updatedAt: string
}

export interface AdminComment {
  id: string
  body: string
  userId: string
  username: string
  postId: string
  postTitle: string
  upvotes: number
  downvotes: number
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegistrationRequest {
  username: string
  email: string
  password: string
}

export interface GoogleAuthResponse {
  needsUsername: boolean
  tempToken: string
  email: string
  suggestedName: string
  pictureUrl?: string
}

// Post types
export interface Post {
  id: string
  title: string
  body?: string
  createdAt: string
  updatedAt: string
  userId: string
  username: string
  profilePictureUrl?: string
  imageUrls?: string[]
  tags?: string[]
  commentCount: number
  upvotes: number
  downvotes: number
  userVote?: 'UPVOTE' | 'DOWNVOTE' | null
}

export interface PostCreateRequest {
  title: string
  body?: string
  imageUrls?: string[]
  tags?: string[]
}

// Tag types
export interface Tag {
  id: number
  name: string
  postCount: number
}

export interface TrendingTag {
  id: number
  name: string
  postCount: number
  trendingScore: number
}

// Comment types
export interface Comment {
  id: string
  body: string
  createdAt: string
  updatedAt: string
  userId: string
  username: string
  userProfilePictureUrl?: string
  postId: string
  parentCommentId?: string
  replies?: Comment[]
  upvotes: number
  downvotes: number
  userVote?: 'UPVOTE' | 'DOWNVOTE' | null
}

export interface CommentCreateRequest {
  body: string
  postId?: string
  parentCommentId?: string
}

export interface CommentUpdateRequest {
  body: string
}

export interface PostUpdateRequest {
  title?: string
  body?: string
  imageUrls?: string[]
  tags?: string[]
}

// Vote types
export interface VoteRequest {
  votableId: string
  votableType: 'POST' | 'COMMENT'
  voteValue: 'UPVOTE' | 'DOWNVOTE'
}

// Pagination types
export interface PageInfo {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

export interface PageResponse<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
}

// Badge types
export interface Badge {
  id: number
  name: string
  description: string
  iconUrl?: string
}

// User Profile types
export interface UserProfile {
  id: string
  username: string
  email: string
  bio?: string
  profilePictureUrl?: string
  createdAt: string
  updatedAt?: string
  karma: number
}

// Post Detail with Comments
export interface PostDetail {
  post: Post
  comments: Comment[]
}


// Report types
export type ReportReason =
  | 'SEXUAL_CONTENT'
  | 'VIOLENT_OR_REPULSIVE'
  | 'HATEFUL_OR_ABUSIVE'
  | 'HARASSMENT_OR_BULLYING'
  | 'HARMFUL_OR_DANGEROUS'
  | 'MISINFORMATION'
  | 'SPAM_OR_MISLEADING'
  | 'LEGAL_ISSUE'
  | 'OTHER'

export const REPORT_REASONS: { value: ReportReason; label: string; icon: string }[] = [
  { value: 'SEXUAL_CONTENT', label: 'Sexual content', icon: '🔞' },
  { value: 'VIOLENT_OR_REPULSIVE', label: 'Violent or repulsive content', icon: '⚠️' },
  { value: 'HATEFUL_OR_ABUSIVE', label: 'Hateful or abusive content', icon: '🚫' },
  { value: 'HARASSMENT_OR_BULLYING', label: 'Harassment or bullying', icon: '😠' },
  { value: 'HARMFUL_OR_DANGEROUS', label: 'Harmful or dangerous acts', icon: '☠️' },
  { value: 'MISINFORMATION', label: 'Misinformation', icon: '❌' },
  { value: 'SPAM_OR_MISLEADING', label: 'Spam or misleading', icon: '📧' },
  { value: 'LEGAL_ISSUE', label: 'Legal issue', icon: '⚖️' },
  { value: 'OTHER', label: 'Something else', icon: '💬' },
]

export interface ReportRequest {
  reason: ReportReason
  additionalDetails?: string
}

export interface ReportStatusResponse {
  hasReported: boolean
}

export interface AdminReport {
  id: string
  postId: string
  postTitle: string
  reporterId: string
  reporterUsername: string
  reason: ReportReason
  reasonDisplayName: string
  additionalDetails?: string
  createdAt: string
}

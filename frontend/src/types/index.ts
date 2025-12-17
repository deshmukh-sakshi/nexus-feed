// User types
export interface User {
  userId: string
  username: string
  email: string
  profilePictureUrl?: string
}

// Auth types
export interface AuthResponse {
  userId: string
  username: string
  email: string
  token: string
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

// Post types
export interface Post {
  id: string
  title: string
  url?: string
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
  url?: string
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
  url?: string
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

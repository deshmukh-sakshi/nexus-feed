// User types
export interface User {
  userId: string
  username: string
  email: string
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
  imageUrls?: string[]
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
}

// Comment types
export interface Comment {
  id: string
  body: string
  createdAt: string
  updatedAt: string
  userId: string
  username: string
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
}

// Vote types
export interface VoteRequest {
  votableId: string
  votableType: 'POST' | 'COMMENT'
  voteValue: 'UPVOTE' | 'DOWNVOTE'
}

// Pagination types
export interface PageInfo {
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
}

export interface PageResponse<T> {
  content: T[]
  pageable: {
    pageNumber: number
    pageSize: number
  }
  totalElements: number
  totalPages: number
  last: boolean
  first: boolean
  number: number
  size: number
  numberOfElements: number
  empty: boolean
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
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
}

// Post Detail with Comments
export interface PostDetail {
  post: Post
  comments: Comment[]
}

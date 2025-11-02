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
  upvotes: number
  downvotes: number
  userVote?: 'UPVOTE' | 'DOWNVOTE' | null
}

export interface CommentCreateRequest {
  body: string
  postId: string
}

// Vote types
export interface VoteRequest {
  voteType: 'UPVOTE' | 'DOWNVOTE'
}

// API Response wrapper
export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
}

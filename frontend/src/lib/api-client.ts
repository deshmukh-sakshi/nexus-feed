import { api } from './api'
import type {
  AuthResponse,
  LoginRequest,
  RegistrationRequest,
  Post,
  PostCreateRequest,
  Comment,
  CommentCreateRequest,
  VoteRequest,
} from '@/types'

// Auth API
export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/api/auth/login', data)
    return response.data
  },

  register: async (data: RegistrationRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/api/auth/register', data)
    return response.data
  },
}

// Posts API
export const postsApi = {
  getPosts: async (): Promise<Post[]> => {
    const response = await api.get<Post[]>('/api/posts')
    return response.data
  },

  getPost: async (id: string): Promise<Post> => {
    const response = await api.get<Post>(`/api/posts/${id}`)
    return response.data
  },

  createPost: async (data: PostCreateRequest): Promise<Post> => {
    const response = await api.post<Post>('/api/posts', data)
    return response.data
  },

  deletePost: async (id: string): Promise<void> => {
    await api.delete(`/api/posts/${id}`)
  },

  votePost: async (id: string, data: VoteRequest): Promise<void> => {
    await api.post(`/api/posts/${id}/vote`, data)
  },
}

// Comments API
export const commentsApi = {
  getComments: async (postId: string): Promise<Comment[]> => {
    const response = await api.get<Comment[]>(`/api/posts/${postId}/comments`)
    return response.data
  },

  createComment: async (data: CommentCreateRequest): Promise<Comment> => {
    const response = await api.post<Comment>('/api/comments', data)
    return response.data
  },

  deleteComment: async (id: string): Promise<void> => {
    await api.delete(`/api/comments/${id}`)
  },

  voteComment: async (id: string, data: VoteRequest): Promise<void> => {
    await api.post(`/api/comments/${id}/vote`, data)
  },
}

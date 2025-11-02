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
    const response = await api.post<AuthResponse>('/auth/login', data)
    return response.data
  },

  register: async (data: RegistrationRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', data)
    return response.data
  },
}

// Posts API
export const postsApi = {
  getPosts: async (): Promise<Post[]> => {
    const response = await api.get<Post[]>('/posts')
    return response.data
  },

  getPost: async (id: string): Promise<Post> => {
    const response = await api.get<Post>(`/posts/${id}`)
    return response.data
  },

  createPost: async (data: PostCreateRequest): Promise<Post> => {
    const response = await api.post<Post>('/posts', data)
    return response.data
  },

  deletePost: async (id: string): Promise<void> => {
    await api.delete(`/posts/${id}`)
  },

  votePost: async (id: string, data: VoteRequest): Promise<void> => {
    await api.post(`/posts/${id}/vote`, data)
  },
}

// Comments API
export const commentsApi = {
  getComments: async (postId: string): Promise<Comment[]> => {
    const response = await api.get<Comment[]>(`/posts/${postId}/comments`)
    return response.data
  },

  createComment: async (data: CommentCreateRequest): Promise<Comment> => {
    const response = await api.post<Comment>('/comments', data)
    return response.data
  },

  deleteComment: async (id: string): Promise<void> => {
    await api.delete(`/comments/${id}`)
  },

  voteComment: async (id: string, data: VoteRequest): Promise<void> => {
    await api.post(`/comments/${id}/vote`, data)
  },
}

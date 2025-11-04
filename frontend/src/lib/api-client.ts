import { api } from './api'
import type {
  AuthResponse,
  LoginRequest,
  RegistrationRequest,
  Post,
  PostCreateRequest,
  PostUpdateRequest,
  Comment,
  CommentCreateRequest,
  CommentUpdateRequest,
  VoteRequest,
  PageResponse,
  UserProfile,
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
  getPosts: async (page = 0, size = 10): Promise<PageResponse<Post>> => {
    const response = await api.get<PageResponse<Post>>('/posts', {
      params: { page, size },
    })
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

  updatePost: async (id: string, data: PostUpdateRequest): Promise<Post> => {
    const response = await api.put<Post>(`/posts/${id}`, data)
    return response.data
  },

  deletePost: async (id: string): Promise<void> => {
    await api.delete(`/posts/${id}`)
  },

  getUserPosts: async (
    userId: string,
    page = 0,
    size = 10
  ): Promise<PageResponse<Post>> => {
    const response = await api.get<PageResponse<Post>>(`/posts/user/${userId}`, {
      params: { page, size },
    })
    return response.data
  },
}

// Comments API
export const commentsApi = {
  getComments: async (postId: string): Promise<Comment[]> => {
    const response = await api.get<Comment[]>(`/comments/post/${postId}`)
    return response.data
  },

  createComment: async (
    postId: string,
    data: CommentCreateRequest
  ): Promise<Comment> => {
    const response = await api.post<Comment>(`/comments/post/${postId}`, data)
    return response.data
  },

  updateComment: async (
    id: string,
    data: CommentUpdateRequest
  ): Promise<Comment> => {
    const response = await api.put<Comment>(`/comments/${id}`, data)
    return response.data
  },

  deleteComment: async (id: string): Promise<void> => {
    await api.delete(`/comments/${id}`)
  },

  getUserComments: async (
    userId: string,
    page = 0,
    size = 10
  ): Promise<PageResponse<Comment>> => {
    const response = await api.get<PageResponse<Comment>>(
      `/comments/user/${userId}`,
      {
        params: { page, size },
      }
    )
    return response.data
  },
}

// Votes API
export const votesApi = {
  votePost: async (postId: string, data: VoteRequest): Promise<void> => {
    await api.post(`/votes`, {
      votableId: postId,
      votableType: 'POST',
      ...data,
    })
  },

  voteComment: async (commentId: string, data: VoteRequest): Promise<void> => {
    await api.post(`/votes`, {
      votableId: commentId,
      votableType: 'COMMENT',
      ...data,
    })
  },
}

// Users API
export const usersApi = {
  getUserByUsername: async (username: string): Promise<UserProfile> => {
    const response = await api.get<UserProfile>(`/users/username/${username}`)
    return response.data
  },

  getUserById: async (userId: string): Promise<UserProfile> => {
    const response = await api.get<UserProfile>(`/users/id/${userId}`)
    return response.data
  },
}

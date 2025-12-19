import { api } from './api'
import type {
  AuthResponse,
  LoginRequest,
  RegistrationRequest,
  GoogleAuthResponse,
  Post,
  PostCreateRequest,
  PostUpdateRequest,
  Comment,
  CommentCreateRequest,
  CommentUpdateRequest,
  PageResponse,
  UserProfile,
  PostDetail,
  Badge,
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

  googleLogin: async (idToken: string): Promise<AuthResponse | GoogleAuthResponse> => {
    const response = await api.post<AuthResponse | GoogleAuthResponse>('/auth/google', { idToken })
    return response.data
  },

  completeGoogleRegistration: async (tempToken: string, username: string): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/google/complete', { tempToken, username })
    return response.data
  },
}

// Posts API
export const postsApi = {
  getPosts: async (page = 0, size = 4): Promise<PageResponse<Post>> => {
    const response = await api.get<PageResponse<Post>>('/posts', {
      params: { page, size },
    })
    return response.data
  },

  getPostWithComments: async (id: string): Promise<PostDetail> => {
    const response = await api.get<PostDetail>(`/posts/${id}/with-comments`)
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
  votePost: async (postId: string, voteValue: 'UPVOTE' | 'DOWNVOTE'): Promise<void> => {
    await api.post(`/votes`, {
      votableId: postId,
      votableType: 'POST',
      voteValue,
    })
  },

  voteComment: async (commentId: string, voteValue: 'UPVOTE' | 'DOWNVOTE'): Promise<void> => {
    await api.post(`/votes`, {
      votableId: commentId,
      votableType: 'COMMENT',
      voteValue,
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

  updateUser: async (
    userId: string,
    data: { bio?: string; profilePictureUrl?: string }
  ): Promise<UserProfile> => {
    const response = await api.put<UserProfile>(`/users/id/${userId}`, data)
    return response.data
  },

  getTopUsers: async (limit = 5): Promise<UserProfile[]> => {
    const response = await api.get<UserProfile[]>('/users/top', {
      params: { limit },
    })
    return response.data
  },
}

// Badges API
export const badgesApi = {
  getUserBadges: async (userId: string): Promise<Badge[]> => {
    const response = await api.get<Badge[]>(`/badges/user/${userId}`)
    return response.data
  },

  getAllBadges: async (): Promise<Badge[]> => {
    const response = await api.get<Badge[]>('/badges')
    return response.data
  },
}

// Tags API
export const tagsApi = {
  searchTags: async (query?: string): Promise<import('@/types').Tag[]> => {
    const response = await api.get<import('@/types').Tag[]>('/tags/search', {
      params: query ? { query } : {},
    })
    return response.data
  },

  getTrendingTags: async (limit = 10): Promise<import('@/types').Tag[]> => {
    const response = await api.get<import('@/types').Tag[]>('/tags/trending', {
      params: { limit },
    })
    return response.data
  },

  getTrendingTagsScored: async (limit = 5): Promise<import('@/types').TrendingTag[]> => {
    const response = await api.get<import('@/types').TrendingTag[]>('/tags/trending-scored', {
      params: { limit },
    })
    return response.data
  },

  getAllTags: async (): Promise<import('@/types').Tag[]> => {
    const response = await api.get<import('@/types').Tag[]>('/tags')
    return response.data
  },

  getPostsByTag: async (
    tagName: string,
    page = 0,
    size = 10
  ): Promise<PageResponse<Post>> => {
    const response = await api.get<PageResponse<Post>>(`/posts/tag/${tagName}`, {
      params: { page, size },
    })
    return response.data
  },

  getPostsByTags: async (
    tags: string[],
    page = 0,
    size = 10
  ): Promise<PageResponse<Post>> => {
    const response = await api.get<PageResponse<Post>>('/posts/tags', {
      params: { tags, page, size },
    })
    return response.data
  },
}


// Admin API
export const adminApi = {
  getStats: async (): Promise<import('@/types').AdminStats> => {
    const response = await api.get<import('@/types').AdminStats>('/admin/stats')
    return response.data
  },

  getUsers: async (page = 0, size = 10): Promise<PageResponse<import('@/types').AdminUser>> => {
    const response = await api.get<PageResponse<import('@/types').AdminUser>>('/admin/users', {
      params: { page, size },
    })
    return response.data
  },

  updateUserRole: async (userId: string, role: string): Promise<import('@/types').AdminUser> => {
    const response = await api.put<import('@/types').AdminUser>(`/admin/users/${userId}/role`, { role })
    return response.data
  },

  deleteUser: async (userId: string): Promise<void> => {
    await api.delete(`/admin/users/${userId}`)
  },

  getPosts: async (page = 0, size = 10): Promise<PageResponse<import('@/types').AdminPost>> => {
    const response = await api.get<PageResponse<import('@/types').AdminPost>>('/admin/posts', {
      params: { page, size },
    })
    return response.data
  },

  deletePost: async (postId: string): Promise<void> => {
    await api.delete(`/admin/posts/${postId}`)
  },

  getComments: async (page = 0, size = 10): Promise<PageResponse<import('@/types').AdminComment>> => {
    const response = await api.get<PageResponse<import('@/types').AdminComment>>('/admin/comments', {
      params: { page, size },
    })
    return response.data
  },

  deleteComment: async (commentId: string): Promise<void> => {
    await api.delete(`/admin/comments/${commentId}`)
  },
}

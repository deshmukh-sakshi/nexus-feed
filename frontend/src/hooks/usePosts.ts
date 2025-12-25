import { useInfiniteQuery, useMutation, useQueryClient, useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { postsApi } from '@/lib/api-client'
import { getErrorMessage } from '@/types/errors'
import type { PostCreateRequest, PostUpdateRequest, Post } from '@/types'
import type { SortOption } from '@/stores/sortStore'

export const usePosts = (pageSize = 4, sortOption: SortOption = 'new') => {
  const queryClient = useQueryClient()

  const postsQuery = useInfiniteQuery({
    queryKey: ['posts', pageSize, sortOption],
    queryFn: ({ pageParam = 0 }) => postsApi.getPosts(pageParam, pageSize, sortOption),
    getNextPageParam: (lastPage) => {
      const isLastPage = lastPage.page.number >= lastPage.page.totalPages - 1
      if (isLastPage) return undefined
      return lastPage.page.number + 1
    },
    initialPageParam: 0,
    retry: false,
  })

  const createPostMutation = useMutation({
    mutationFn: postsApi.createPost,
    onMutate: async (data) => {
      await queryClient.cancelQueries({ queryKey: ['posts', pageSize, sortOption] })
      
      const previousPosts = queryClient.getQueryData(['posts', pageSize, sortOption])
      
      // Create temp ID for tracking
      const tempId = `temp-${Date.now()}-${Math.random()}`
      
      // Add skeleton post at the top
      const skeletonPost = {
        id: tempId,
        title: data.title,
        body: data.body,
        userId: 'temp',
        username: 'loading',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        upvotes: 0,
        downvotes: 0,
        commentCount: 0,
        userVote: null,
        imageUrls: data.imageUrls || [],
        isLoading: true, // Flag to show skeleton
      }
      
      queryClient.setQueryData(['posts', pageSize, sortOption], (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: unknown[]; [key: string]: unknown }[] }
        const firstPage = typedOld.pages[0]
        return {
          ...typedOld,
          pages: [
            { ...firstPage, content: [skeletonPost, ...firstPage.content] },
            ...typedOld.pages.slice(1),
          ],
        }
      })
      
      toast.loading('Creating post...', { id: 'create-post' })
      
      return { previousPosts, tempId }
    },
    onSuccess: (newPost, _variables, context) => {
      // Silently replace skeleton with real post
      if (context?.tempId) {
        queryClient.setQueryData(['posts', pageSize, sortOption], (old: unknown) => {
          if (!old) return old
          const typedOld = old as { pages: { content: { id: string; [key: string]: unknown }[]; [key: string]: unknown }[] }
          return {
            ...typedOld,
            pages: typedOld.pages.map((page) => ({
              ...page,
              content: page.content.map((p) => 
                p.id === context.tempId ? newPost : p
              ),
            })),
          }
        })
      }
      
      toast.dismiss('create-post')
      toast.success('Post created!')
    },
    onError: (error, _variables, context) => {
      if (context?.previousPosts) {
        queryClient.setQueryData(['posts', pageSize, sortOption], context.previousPosts)
      }
      toast.dismiss('create-post')
      toast.error(getErrorMessage(error))
    },
  })

  const updatePostMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: PostUpdateRequest }) =>
      postsApi.updatePost(id, data),
    onMutate: async ({ id, data }) => {
      // Cancel any outgoing refetches
      await queryClient.cancelQueries({ queryKey: ['posts'] })
      await queryClient.cancelQueries({ queryKey: ['postWithComments', id] })
      
      const previousPosts = queryClient.getQueryData(['posts', pageSize, sortOption])
      const previousPostDetail = queryClient.getQueryData(['postWithComments', id])
      
      const now = new Date().toISOString()
      
      // Update post in infinite query
      queryClient.setQueryData(['posts', pageSize, sortOption], (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: Post[]; [key: string]: unknown }[] }
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map((post: Post) => {
              if (post.id !== id) return post
              return {
                ...post,
                title: data.title || post.title,
                body: data.body !== undefined ? data.body : post.body,
                updatedAt: now,
              }
            }),
          })),
        }
      })
      
      // Update post in detail view
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: Post; comments: unknown[] }
        return {
          ...postDetail,
          post: {
            ...postDetail.post,
            title: data.title || postDetail.post.title,
            body: data.body !== undefined ? data.body : postDetail.post.body,
            updatedAt: now,
          },
        }
      })
      
      return { previousPosts, previousPostDetail }
    },
    onSuccess: (updatedPost, { id }) => {
      // Silently replace optimistic data with real server response
      queryClient.setQueryData(['posts', pageSize, sortOption], (old: unknown) => {
        if (!old) return old
        const typedOld = old as { pages: { content: Post[]; [key: string]: unknown }[] }
        return {
          ...typedOld,
          pages: typedOld.pages.map((page) => ({
            ...page,
            content: page.content.map((post: Post) => 
              post.id === id ? updatedPost : post
            ),
          })),
        }
      })
      
      queryClient.setQueryData(['postWithComments', id], (old: unknown) => {
        if (!old) return old
        const postDetail = old as { post: Post; comments: unknown[] }
        return {
          ...postDetail,
          post: updatedPost,
        }
      })
      
      toast.success('Post updated successfully!')
    },
    onError: (error, { id }, context) => {
      if (context?.previousPosts) {
        queryClient.setQueryData(['posts', pageSize, sortOption], context.previousPosts)
      }
      if (context?.previousPostDetail) {
        queryClient.setQueryData(['postWithComments', id], context.previousPostDetail)
      }
      toast.error(getErrorMessage(error))
    },
  })

  const deletePostMutation = useMutation({
    mutationFn: postsApi.deletePost,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['posts'] })
      toast.success('Post deleted successfully!')
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  const posts = postsQuery.data?.pages.flatMap((page) => page.content) ?? []
  const hasNextPage = postsQuery.hasNextPage
  const lastPageData = postsQuery.data?.pages[postsQuery.data.pages.length - 1]
  const isLastPage = lastPageData ? lastPageData.page.number >= lastPageData.page.totalPages - 1 : false

  return {
    posts,
    isLoading: postsQuery.isLoading,
    isRefetching: postsQuery.isRefetching,
    error: postsQuery.error,
    refetch: postsQuery.refetch,
    fetchNextPage: postsQuery.fetchNextPage,
    hasNextPage,
    isLastPage,
    isFetchingNextPage: postsQuery.isFetchingNextPage,
    createPost: (data: PostCreateRequest) => createPostMutation.mutate(data),
    updatePost: (id: string, data: PostUpdateRequest) =>
      updatePostMutation.mutate({ id, data }),
    deletePost: (id: string) => deletePostMutation.mutate(id),
    isCreating: createPostMutation.isPending,
  }
}

export const usePostWithComments = (id: string) => {
  return useQuery({
    queryKey: ['postWithComments', id],
    queryFn: () => postsApi.getPostWithComments(id),
    enabled: !!id,
  })
}

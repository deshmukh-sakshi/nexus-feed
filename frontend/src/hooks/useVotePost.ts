import { useOptimisticVote } from './useOptimisticVote'

/**
 * @deprecated Use useOptimisticVote directly instead.
 * This hook is kept for backward compatibility.
 */
export const useVotePost = () => {
  return useOptimisticVote()
}

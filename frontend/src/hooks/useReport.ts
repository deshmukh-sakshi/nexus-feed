import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { reportsApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { ReportRequest } from '@/types'

export const useReport = (postId: string) => {
  const queryClient = useQueryClient()
  const { isAuthenticated } = useAuthStore()

  const reportStatusQuery = useQuery({
    queryKey: ['reportStatus', postId],
    queryFn: () => reportsApi.getReportStatus(postId),
    enabled: !!postId && isAuthenticated,
    retry: false,
  })

  const submitReportMutation = useMutation({
    mutationFn: (data: ReportRequest) => reportsApi.submitReport(postId, data),
    onSuccess: () => {
      queryClient.setQueryData(['reportStatus', postId], { hasReported: true })
      queryClient.invalidateQueries({ queryKey: ['reportStatus', postId] })
    },
    onError: (error) => {
      toast.error(getErrorMessage(error))
    },
  })

  return {
    hasReported: reportStatusQuery.data?.hasReported ?? false,
    isLoadingStatus: reportStatusQuery.isLoading,
    submitReport: submitReportMutation.mutate,
    isSubmitting: submitReportMutation.isPending,
    isSuccess: submitReportMutation.isSuccess,
    reset: submitReportMutation.reset,
  }
}

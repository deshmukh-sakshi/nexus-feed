import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { reportsApi } from '@/lib/api-client'
import { useAuthStore } from '@/stores/authStore'
import { getErrorMessage } from '@/types/errors'
import type { ReportRequest } from '@/types'

type ReportableType = 'POST' | 'COMMENT'

export const useReport = (reportableId: string, reportableType: ReportableType = 'POST') => {
  const queryClient = useQueryClient()
  const { isAuthenticated } = useAuthStore()

  const reportStatusQuery = useQuery({
    queryKey: ['reportStatus', reportableType, reportableId],
    queryFn: () => reportableType === 'POST' 
      ? reportsApi.getPostReportStatus(reportableId)
      : reportsApi.getCommentReportStatus(reportableId),
    enabled: !!reportableId && isAuthenticated,
    retry: false,
  })

  const submitReportMutation = useMutation({
    mutationFn: (data: ReportRequest) => reportableType === 'POST'
      ? reportsApi.submitPostReport(reportableId, data)
      : reportsApi.submitCommentReport(reportableId, data),
    onSuccess: () => {
      queryClient.setQueryData(['reportStatus', reportableType, reportableId], { hasReported: true })
      queryClient.invalidateQueries({ queryKey: ['reportStatus', reportableType, reportableId] })
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

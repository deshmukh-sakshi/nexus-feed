import { AxiosError } from 'axios'

export interface ApiErrorResponse {
  message?: string
  error?: string
  status?: number
}

export type ApiError = AxiosError<ApiErrorResponse>

export function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    return error.response?.data?.message || error.response?.data?.error || error.message || 'An error occurred'
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'An unknown error occurred'
}

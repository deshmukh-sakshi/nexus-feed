import { useState } from 'react'
import { CheckCircle } from 'lucide-react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { useReport } from '@/hooks/useReport'
import { REPORT_REASONS, type ReportReason } from '@/types'
import { cn } from '@/lib/utils'

interface ReportModalProps {
  isOpen: boolean
  onClose: () => void
  postId: string
}

const MAX_DETAILS_LENGTH = 500

export const ReportModal = ({ isOpen, onClose, postId }: ReportModalProps) => {
  const [selectedReason, setSelectedReason] = useState<ReportReason | null>(null)
  const [additionalDetails, setAdditionalDetails] = useState('')
  const { submitReport, isSubmitting, isSuccess, reset } = useReport(postId)

  const handleClose = () => {
    setSelectedReason(null)
    setAdditionalDetails('')
    reset()
    onClose()
  }

  const handleSubmit = () => {
    if (!selectedReason) return
    submitReport({
      reason: selectedReason,
      additionalDetails: selectedReason === 'OTHER' ? additionalDetails : undefined,
    })
  }

  const remainingChars = MAX_DETAILS_LENGTH - additionalDetails.length

  // Success screen
  if (isSuccess) {
    return (
      <Dialog open={isOpen} onOpenChange={handleClose}>
        <DialogContent className="sm:max-w-md border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] rounded-none bg-green-50 dark:bg-green-950">
          <div className="flex flex-col items-center justify-center py-8 gap-4">
            <div className="w-16 h-16 rounded-full bg-green-500 flex items-center justify-center">
              <CheckCircle className="w-10 h-10 text-white" />
            </div>
            <h2 className="text-xl font-bold text-center">Thanks for reporting</h2>
            <p className="text-center text-muted-foreground">
              We'll review this post and take action if it violates our community guidelines.
            </p>
          </div>
          <DialogFooter className="sm:justify-center">
            <Button
              onClick={handleClose}
              className="w-full sm:w-auto bg-green-500 text-white hover:bg-green-600 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
            >
              Done
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    )
  }

  // Report form
  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md border-2 border-black shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] dark:shadow-[8px_8px_0px_0px_rgba(255,255,255,1)] rounded-none bg-yellow-50 dark:bg-yellow-950">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Report this post</DialogTitle>
        </DialogHeader>
        
        <div className="flex flex-col gap-2 max-h-[50vh] overflow-y-auto">
          {REPORT_REASONS.map((reason) => (
            <button
              key={reason.value}
              type="button"
              onClick={() => setSelectedReason(reason.value)}
              className={cn(
                'flex items-center gap-3 p-3 text-left border-2 border-black transition-all',
                'hover:bg-yellow-100 dark:hover:bg-yellow-900',
                selectedReason === reason.value
                  ? 'bg-yellow-200 dark:bg-yellow-800 shadow-[2px_2px_0px_0px_rgba(0,0,0,1)] dark:shadow-[2px_2px_0px_0px_rgba(255,255,255,1)]'
                  : 'bg-white dark:bg-gray-800'
              )}
            >
              <span className="text-xl" role="img" aria-label={reason.label}>
                {reason.icon}
              </span>
              <span className="font-medium">{reason.label}</span>
            </button>
          ))}
        </div>

        {selectedReason === 'OTHER' && (
          <div className="flex flex-col gap-2">
            <Textarea
              placeholder="Tell us more (optional)..."
              value={additionalDetails}
              onChange={(e) => setAdditionalDetails(e.target.value.slice(0, MAX_DETAILS_LENGTH))}
              className="min-h-24 bg-white dark:bg-gray-800"
              maxLength={MAX_DETAILS_LENGTH}
            />
            <div className="text-right text-sm text-muted-foreground">
              {remainingChars}/{MAX_DETAILS_LENGTH}
            </div>
          </div>
        )}

        <DialogFooter className="flex-col sm:flex-row gap-2 sm:gap-2">
          <Button
            onClick={handleClose}
            className="w-full sm:w-auto bg-gray-300 text-black hover:bg-gray-400 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold"
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={!selectedReason || isSubmitting}
            className="w-full sm:w-auto bg-red-400 text-black hover:bg-red-500 border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] dark:shadow-[4px_4px_0px_0px_rgba(255,255,255,1)] rounded-none font-bold disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? 'Submitting...' : 'Submit'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

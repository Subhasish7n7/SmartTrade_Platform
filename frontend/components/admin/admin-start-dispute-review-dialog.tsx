
"use client"

import * as React from "react"
import { useRouter } from "next/navigation"
import { Loader2, ShieldAlert } from "lucide-react"
import { toast } from "sonner"

import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Textarea } from "@/components/ui/textarea"
import { startAdminDisputeReview } from "@/lib/api/admin/client"

interface Props {
  disputeId: number
}

export function AdminStartDisputeReviewDialog({
  disputeId,
}: Props) {
  const router = useRouter()

  const [open, setOpen] = React.useState(false)
  const [reason, setReason] = React.useState("")
  const [saving, setSaving] = React.useState(false)

  async function submit() {
    const trimmedReason = reason.trim()

    if (!trimmedReason) {
      toast.error("A reason is required")
      return
    }

    try {
      setSaving(true)

      await startAdminDisputeReview(disputeId, {
        reason: trimmedReason,
      })

      toast.success("Dispute review started")

      setOpen(false)
      setReason("")
      router.refresh()
    } catch (error) {
      console.error("Failed to start dispute review:", error)

      toast.error(
        error instanceof Error
          ? error.message
          : "Could not start dispute review.",
      )
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(nextOpen) => {
        if (!saving) {
          setOpen(nextOpen)
        }
      }}
    >
      <DialogTrigger asChild>
        <Button
          variant="surface"
          className="rounded-xl"
        >
          <ShieldAlert className="mr-2 h-4 w-4" />
          Start review
        </Button>
      </DialogTrigger>

      <DialogContent className="glass-strong border-border/60 sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>
            Start review for dispute #{disputeId}?
          </DialogTitle>

          <DialogDescription>
            Move the dispute into the under-review state
            and record why the investigation is being started.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2 py-2">
          <label
            htmlFor="dispute-review-reason"
            className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground"
          >
            Reason
          </label>

          <Textarea
            id="dispute-review-reason"
            value={reason}
            maxLength={2000}
            onChange={(event) => setReason(event.target.value)}
            placeholder="Explain why this dispute is being reviewed..."
            className="min-h-32 resize-none bg-background/40"
            disabled={saving}
          />

          <div className="text-right text-[10px] text-muted-foreground">
            {reason.length}/2000
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="ghost"
            onClick={() => setOpen(false)}
            disabled={saving}
            className="rounded-xl"
          >
            Cancel
          </Button>

          <Button
            onClick={() => void submit()}
            disabled={saving}
            className="rounded-xl"
          >
            {saving && (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            )}

            {saving ? "Processing..." : "Confirm"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

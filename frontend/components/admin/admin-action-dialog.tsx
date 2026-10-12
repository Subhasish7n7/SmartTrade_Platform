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

import {
  hideAdminItem,
  restoreAdminItem,
  restoreAdminUser,
  suspendAdminUser,
} from "@/lib/api/admin/client"

type AdminAction =
  | "SUSPEND_USER"
  | "RESTORE_USER"
  | "HIDE_ITEM"
  | "RESTORE_ITEM"

interface Props {
  action: AdminAction
  targetId: number
  title: string
  description: string
  buttonLabel: string
  reasonLabel?: string
  destructive?: boolean
}

export function AdminActionDialog({
  action,
  targetId,
  title,
  description,
  buttonLabel,
  reasonLabel = "Reason",
  destructive = true,
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

      switch (action) {
        case "SUSPEND_USER":
          await suspendAdminUser(targetId, {
            reason: trimmedReason,
          })
          break

        case "RESTORE_USER":
          await restoreAdminUser(targetId, {
            reason: trimmedReason,
          })
          break

        case "HIDE_ITEM":
          await hideAdminItem(targetId, {
            reason: trimmedReason,
          })
          break

        case "RESTORE_ITEM":
          await restoreAdminItem(targetId, {
            reason: trimmedReason,
          })
          break

        default:
          throw new Error("Unsupported admin action")
      }

      toast.success("Admin action completed")

      setOpen(false)
      setReason("")

      router.refresh()
    } catch (error) {
      console.error("Admin action failed:", error)

      toast.error(
        error instanceof Error
          ? error.message
          : "The admin action could not be completed.",
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
          variant={destructive ? "destructive" : "surface"}
          className="rounded-xl"
        >
          <ShieldAlert className="mr-2 h-4 w-4" />
          {buttonLabel}
        </Button>
      </DialogTrigger>

      <DialogContent className="glass-strong border-border/60 sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>

          <DialogDescription>
            {description}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2 py-2">
          <label className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
            {reasonLabel}
          </label>

          <Textarea
            value={reason}
            maxLength={2000}
            onChange={(event) => setReason(event.target.value)}
            placeholder="Explain why this action is being taken..."
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
            variant={destructive ? "destructive" : "default"}
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
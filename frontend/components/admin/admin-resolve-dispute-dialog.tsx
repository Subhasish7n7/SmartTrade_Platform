"use client"

import * as React from "react"
import { Loader2, Scale } from "lucide-react"
import { useRouter } from "next/navigation"
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
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import type { ResolveDisputeRequest } from "@/lib/types/api/admin"
import { resolveAdminDispute } from "@/lib/api/admin/client"

interface Props {
  disputeId: number
  userOptions: Array<{ id: number; name: string }>
}

export function AdminResolveDisputeDialog({ disputeId, userOptions }: Props) {
  const router = useRouter()
  const [open, setOpen] = React.useState(false)
  const [resolution, setResolution] = React.useState("")
  const [faultUserId, setFaultUserId] = React.useState("")
  const [notes, setNotes] = React.useState("")
  const [saving, setSaving] = React.useState(false)

  async function submit() {
    if (!resolution.trim()) {
      toast.error("Resolution is required")
      return
    }

    const request: ResolveDisputeRequest = {
      resolution: resolution.trim(),
      faultUserId: faultUserId ? Number(faultUserId) : null,
      notes: notes.trim() || undefined,
    }

    try {
      setSaving(true)
      await resolveAdminDispute(disputeId, request)
      toast.success("Dispute resolved")
      setOpen(false)
      router.refresh()
    } catch (error) {
      console.error(error)
      toast.error(error instanceof Error ? error.message : "Could not resolve dispute")
    } finally {
      setSaving(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button className="cta-primary rounded-xl">
          <Scale className="mr-2 h-4 w-4" />
          Resolve dispute
        </Button>
      </DialogTrigger>
      <DialogContent className="glass-strong border-border/60 sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>Resolve dispute #{disputeId}</DialogTitle>
          <DialogDescription>
            Record the final resolution. The resolution and notes become part of the dispute history.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              Resolution
            </label>
            <Input
              value={resolution}
              onChange={(event) => setResolution(event.target.value)}
              placeholder="Enter a valid backend DisputeResolution value"
              className="bg-background/40"
            />
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              Fault user ID (optional)
            </label>
            <Input
              value={faultUserId}
              onChange={(event) => setFaultUserId(event.target.value.replace(/[^0-9]/g, ""))}
              placeholder="Leave blank when no fault is assigned"
              inputMode="numeric"
              className="bg-background/40"
            />
            {userOptions.length > 0 && (
              <div className="flex flex-wrap gap-1.5 pt-1">
                {userOptions.map((user) => (
                  <Button
                    key={user.id}
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setFaultUserId(String(user.id))}
                    className="rounded-lg text-xs"
                  >
                    {user.name} · #{user.id}
                  </Button>
                ))}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <label className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
              Notes
            </label>
            <Textarea
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              maxLength={5000}
              placeholder="Explain the reasoning behind the resolution..."
              className="min-h-32 resize-none bg-background/40"
            />
          </div>
        </div>

        <DialogFooter>
          <Button variant="ghost" onClick={() => setOpen(false)} disabled={saving} className="rounded-xl">
            Cancel
          </Button>
          <Button onClick={() => void submit()} disabled={saving} className="cta-primary rounded-xl">
            {saving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
            Resolve
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
"use client"

import Link from "next/link"
import * as React from "react"
import { Check, RefreshCw, ShieldAlert, X } from "lucide-react"

import { Button } from "@/components/ui/button"
import {
  acceptTrade,
  cancelTrade,
  confirmTradeCompletion,
  openTradeDispute,
} from "@/lib/api/trades/client"
import type { DisputeReason, TradeStatus } from "@/lib/types/api/trade"

interface Props {
  tradeId: number
  otherUserId: number
  currentUserId: number
  status: TradeStatus
  isLatestOffer: boolean
  isCurrentUserOffer: boolean
  canRespondToOffer: boolean
  youConfirmed: boolean
  theyConfirmed: boolean
  completionRequestedAt: string | null
}

const disputeReasons: Array<{
  value: DisputeReason
  label: string
}> = [
  { value: "COMPLETION_DISAGREEMENT", label: "Completion disagreement" },
  { value: "ITEM_NOT_AS_DESCRIBED", label: "Item not as described" },
  { value: "NO_SHOW", label: "No-show" },
  { value: "OTHER", label: "Other" },
]

export function TradeDetailClient({
  tradeId,
  otherUserId,
  status,
  isLatestOffer,
  isCurrentUserOffer,
  canRespondToOffer,
  youConfirmed,
  theyConfirmed,
  completionRequestedAt,
}: Props) {
  const [busy, setBusy] = React.useState(false)
  const [showDispute, setShowDispute] = React.useState(false)
  const [reason, setReason] = React.useState<DisputeReason>(
    "COMPLETION_DISAGREEMENT",
  )
  const [description, setDescription] = React.useState("")

  async function run(action: () => Promise<unknown>) {
    try {
      setBusy(true)
      await action()
      window.location.reload()
    } catch (error) {
      console.error(error)
      window.alert(
        error instanceof Error ? error.message : "Something went wrong.",
      )
    } finally {
      setBusy(false)
    }
  }

  async function handleDispute() {
    if (description.trim().length === 0) {
      window.alert("Please describe the issue before opening a dispute.")
      return
    }

    try {
      setBusy(true)
      await openTradeDispute(tradeId, {
        reason,
        description: description.trim(),
      })
      window.location.reload()
    } catch (error) {
      console.error(error)
      window.alert(
        error instanceof Error
          ? error.message
          : "Could not open the dispute.",
      )
    } finally {
      setBusy(false)
    }
  }

  if (status === "DISPUTED") {
    return (
      <section className="content-surface !p-5 sm:!p-6">
        <div className="flex items-start gap-3">
          <span className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-amber-300/20 bg-amber-300/[0.06] text-amber-300">
            <ShieldAlert className="h-4 w-4" />
          </span>
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              Dispute
            </p>
            <h2 className="mt-1 text-lg font-semibold">Trade under dispute</h2>
            <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
              This trade is being handled through the dispute process. Normal
              participant actions are paused until the dispute is resolved.
            </p>
          </div>
        </div>
      </section>
    )
  }

  if (status === "ACCEPTED") {
    return (
      <section className="content-surface !p-5 sm:!p-6">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="min-w-0">
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              Completion
            </p>
            <h2 className="mt-1 text-lg font-semibold">Confirm the exchange</h2>
            <p className="mt-1 max-w-2xl text-sm leading-6 text-muted-foreground">
              The trade is accepted. Both participants need to confirm that the
              exchange has actually been completed.
            </p>

            <div className="mt-4 grid gap-2 sm:grid-cols-2">
              <ConfirmationTile label="You" confirmed={youConfirmed} />
              <ConfirmationTile label="Other trader" confirmed={theyConfirmed} />
            </div>

            {completionRequestedAt && (
              <p className="mt-3 text-[11px] text-muted-foreground">
                Completion confirmation started {formatDate(completionRequestedAt)}.
              </p>
            )}
          </div>

          <div className="w-full shrink-0 lg:w-64">
            <Button
              className="cta-primary h-11 w-full rounded-xl font-semibold"
              disabled={busy || youConfirmed}
              onClick={() => run(() => confirmTradeCompletion(tradeId))}
            >
              <Check className="mr-2 h-4 w-4" />
              {youConfirmed ? "You confirmed" : "Confirm completion"}
            </Button>

            <button
              type="button"
              onClick={() => setShowDispute((value) => !value)}
              className="mt-3 inline-flex w-full items-center justify-center gap-2 text-xs font-semibold text-muted-foreground transition hover:text-foreground"
            >
              <ShieldAlert className="h-3.5 w-3.5" />
              Report an issue
            </button>
          </div>
        </div>

        {showDispute && (
          <div className="mt-5 border-t border-white/[0.07] pt-5">
            <div className="grid gap-4 lg:grid-cols-[14rem_1fr_auto] lg:items-end">
              <label className="space-y-1.5">
                <span className="text-xs font-semibold text-foreground/80">Reason</span>
                <select
                  value={reason}
                  onChange={(event) => setReason(event.target.value as DisputeReason)}
                  className="h-10 w-full rounded-xl border border-white/[0.10] bg-background/55 px-3 text-sm outline-none focus:border-[var(--trust-blue)]/45 focus:ring-1 focus:ring-[var(--trust-blue)]/15"
                >
                  {disputeReasons.map((item) => (
                    <option key={item.value} value={item.value}>
                      {item.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="space-y-1.5">
                <span className="text-xs font-semibold text-foreground/80">Description</span>
                <textarea
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  maxLength={5000}
                  rows={3}
                  placeholder="Explain what happened and what needs to be reviewed."
                  className="w-full resize-y rounded-xl border border-white/[0.10] bg-background/55 px-3 py-2.5 text-sm leading-6 outline-none placeholder:text-muted-foreground/60 focus:border-[var(--trust-blue)]/45 focus:ring-1 focus:ring-[var(--trust-blue)]/15"
                />
              </label>

              <Button
                variant="destructive"
                className="h-10 rounded-xl font-semibold"
                disabled={busy || description.trim().length === 0}
                onClick={handleDispute}
              >
                Open dispute
              </Button>
            </div>
          </div>
        )}
      </section>
    )
  }

  if (status === "COMPLETED") {
    return (
      <section className="content-surface !p-5 sm:!p-6">
        <div className="flex items-start gap-3">
          <span className="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-emerald-400/20 bg-emerald-400/[0.06] text-emerald-300">
            <Check className="h-4 w-4" />
          </span>
          <div>
            <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
              Completed
            </p>
            <h2 className="mt-1 text-lg font-semibold">Trade completed successfully</h2>
            <p className="mt-1 text-sm leading-6 text-muted-foreground">
              Both participants confirmed completion and the platform finalized the exchange.
            </p>
          </div>
        </div>
      </section>
    )
  }

  if (status === "CANCELLED" || status === "EXPIRED" || !isLatestOffer) {
    return null
  }

  return (
    <section className="content-surface !p-5 sm:!p-6">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
            Offer response
          </p>
          <h2 className="mt-1 text-lg font-semibold">
            {isCurrentUserOffer
              ? "Waiting for the other trader"
              : "Review and respond to this offer"}
          </h2>
          <p className="mt-1 text-sm leading-6 text-muted-foreground">
            {isCurrentUserOffer
              ? "Your offer is the current proposal. You do not need to accept or counter your own offer."
              : "This is the latest proposal in the negotiation."}
          </p>
        </div>

        <div className="flex w-full flex-col gap-2 sm:flex-row lg:w-auto">
          {canRespondToOffer && (
            <>
              <Button
                className="cta-primary h-10 rounded-xl font-semibold sm:min-w-36"
                disabled={busy}
                onClick={() => run(() => acceptTrade(tradeId))}
              >
                <Check className="mr-2 h-4 w-4" />
                Accept trade
              </Button>

              <Link href={`/trades/create/${otherUserId}?tradeId=${tradeId}`} className="sm:min-w-36">
                <Button variant="surface" className="h-10 w-full rounded-xl font-semibold">
                  <RefreshCw className="mr-2 h-4 w-4" />
                  Counter offer
                </Button>
              </Link>

              <Button
                variant="destructive"
                className="h-10 rounded-xl font-semibold sm:min-w-32"
                disabled={busy}
                onClick={() => run(() => cancelTrade(tradeId))}
              >
                <X className="mr-2 h-4 w-4" />
                Cancel trade
              </Button>
            </>
          )}

          {isCurrentUserOffer && (
            <Button
              variant="destructive"
              className="h-10 rounded-xl font-semibold sm:min-w-32"
              disabled={busy}
              onClick={() => run(() => cancelTrade(tradeId))}
            >
              <X className="mr-2 h-4 w-4" />
              Cancel offer
            </Button>
          )}
        </div>
      </div>
    </section>
  )
}

function ConfirmationTile({
  label,
  confirmed,
}: {
  label: string
  confirmed: boolean
}) {
  return (
    <div className="flex items-center justify-between rounded-xl border border-white/[0.06] bg-white/[0.018] px-3 py-2.5">
      <span className="text-xs font-medium text-muted-foreground">{label}</span>
      <span
        className={`inline-flex items-center gap-1.5 text-xs font-semibold ${
          confirmed ? "text-emerald-300" : "text-muted-foreground"
        }`}
      >
        {confirmed ? <Check className="h-3.5 w-3.5" /> : null}
        {confirmed ? "Confirmed" : "Waiting"}
      </span>
    </div>
  )
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("en-IN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value))
}
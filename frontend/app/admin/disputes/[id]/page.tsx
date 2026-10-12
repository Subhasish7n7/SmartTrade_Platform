import { ArrowLeft, ArrowLeftRight, FileText, Scale, UserRound } from "lucide-react"
import Link from "next/link"
import { notFound } from "next/navigation"

import { AdminResolveDisputeDialog } from "@/components/admin/admin-resolve-dispute-dialog"
import { AdminStartDisputeReviewDialog } from "@/components/admin/admin-start-dispute-review-dialog"
import { AdminDate, AdminPanel, AdminStatusBadge } from "@/components/admin/admin-ui"
import { getAdminDispute } from "@/lib/api/admin/server"

interface Props {
  params: Promise<{ id: string }>
}

export default async function AdminDisputeDetailPage({ params }: Props) {
  const { id } = await params
  const disputeId = Number(id)
  if (!Number.isInteger(disputeId)) notFound()

  const data = await getAdminDispute(disputeId).catch(() => null)
  if (!data) notFound()

  const { dispute, description, resolutionNotes, trade } = data
  const status = dispute.status.toUpperCase()
  const resolved = status === "RESOLVED"
  const underReview = status === "UNDER_REVIEW"

  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-4">
        <Link
          href="/admin/disputes"
          className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Back to disputes
        </Link>
        <AdminStatusBadge status={dispute.status} />
      </div>

      <section className="rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.76)] p-5 shadow-[0_16px_40px_rgba(0,0,0,.12)] backdrop-blur-xl sm:p-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <span className="rounded-lg border border-amber-400/15 bg-amber-400/[0.04] px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.12em] text-amber-300">
                Dispute #{dispute.disputeId}
              </span>
              <span className="text-xs text-muted-foreground">Trade #{dispute.tradeId}</span>
            </div>
            <h1 className="mt-2 text-2xl font-bold tracking-tight">{dispute.reason.replaceAll("_", " ")}</h1>
            <p className="mt-1 text-xs text-muted-foreground">
              Opened by {dispute.openedByName} · <AdminDate value={dispute.createdAt} />
            </p>
          </div>

          {!resolved && (
            <div className="flex flex-wrap gap-2">
              {!underReview && <AdminStartDisputeReviewDialog disputeId={dispute.disputeId} />}
              {underReview && (
                <AdminResolveDisputeDialog
                  disputeId={dispute.disputeId}
                  userOptions={[
                    { id: trade.initiatorId, name: trade.initiatorName },
                    { id: trade.receiverId, name: trade.receiverName },
                  ]}
                />
              )}
            </div>
          )}
        </div>
      </section>

      <div className="mt-4 grid gap-4 xl:grid-cols-[1.15fr_.85fr]">
        <AdminPanel title="Dispute statement" description="Details submitted with the dispute.">
          <div className="p-5">
            <div className="flex items-center gap-2 text-xs font-semibold">
              <FileText className="h-4 w-4 text-[var(--trust-blue)]" /> Description
            </div>
            <p className="mt-3 whitespace-pre-line text-sm leading-7 text-foreground/80">
              {description?.trim() || "No description provided."}
            </p>
            {resolutionNotes && (
              <div className="mt-6 border-t border-white/[0.06] pt-5">
                <p className="text-xs font-semibold">Resolution notes</p>
                <p className="mt-2 whitespace-pre-line text-sm leading-7 text-muted-foreground">{resolutionNotes}</p>
              </div>
            )}
          </div>
        </AdminPanel>

        <AdminPanel title="Related trade" description="The trade under dispute.">
          <div className="p-4">
            <Link
              href={`/admin/trades/${trade.tradeId}`}
              className="block rounded-xl border border-white/[0.06] bg-white/[0.015] p-4 transition hover:bg-white/[0.025]"
            >
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <ArrowLeftRight className="h-4 w-4" /> Trade #{trade.tradeId}
              </div>
              <p className="mt-2 font-semibold">
                {trade.initiatorName} ↔ {trade.receiverName}
              </p>
              <div className="mt-3 flex flex-wrap gap-1.5">
                <AdminStatusBadge status={trade.status} />
              </div>
            </Link>
          </div>
        </AdminPanel>
      </div>

      <div className="mt-4 grid gap-4 md:grid-cols-2">
        <InfoCard icon={<UserRound className="h-4 w-4" />} label="Opened by" value={`${dispute.openedByName} · #${dispute.openedById}`} />
        <InfoCard
          icon={<Scale className="h-4 w-4" />}
          label="Resolution"
          value={dispute.resolution?.replaceAll("_", " ") || "Not resolved"}
        />
        <InfoCard
          icon={<UserRound className="h-4 w-4" />}
          label="Fault user"
          value={dispute.faultUserId ? `${dispute.faultUserId}` : "Not assigned"}
        />
        <InfoCard
          icon={<FileText className="h-4 w-4" />}
          label="Resolved by"
          value={dispute.resolvedByName ? `${dispute.resolvedByName} · #${dispute.resolvedById}` : "Not resolved"}
        />
      </div>
    </div>
  )
}

function InfoCard({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-4 shadow-[0_12px_32px_rgba(0,0,0,.08)]">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        {icon}
        {label}
      </div>
      <p className="mt-2 text-sm font-semibold">{value}</p>
    </div>
  )
}

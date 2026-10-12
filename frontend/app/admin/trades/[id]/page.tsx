import Link from "next/link"
import { ArrowLeft, ArrowLeftRight, Clock3, Scale, UserRound } from "lucide-react"
import { notFound } from "next/navigation"

import { AdminDate, AdminPanel, AdminStatusBadge } from "@/components/admin/admin-ui"
import { getAdminTrade } from "@/lib/api/admin/server"

interface Props {
  params: Promise<{ id: string }>
}

export default async function AdminTradeDetailPage({ params }: Props) {
  const { id } = await params
  const tradeId = Number(id)
  if (!Number.isInteger(tradeId)) notFound()

  const data = await getAdminTrade(tradeId).catch(() => null)
  if (!data) notFound()

  const { trade, offerHistory, latestDispute } = data

  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-4">
        <Link href="/admin/trades" className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground"><ArrowLeft className="h-4 w-4" /> Back to trades</Link>
        <AdminStatusBadge status={trade.status} />
      </div>

      <section className="rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.76)] p-5 shadow-[0_16px_40px_rgba(0,0,0,.12)] backdrop-blur-xl sm:p-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div><p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">Trade #{trade.tradeId}</p><h1 className="mt-1 text-2xl font-bold tracking-tight">{trade.initiatorName} <span className="text-muted-foreground">↔</span> {trade.receiverName}</h1></div>
          <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] px-3 py-2 text-xs text-muted-foreground">Created <span className="font-semibold text-foreground"><AdminDate value={trade.createdAt} /></span></div>
        </div>

        <div className="mt-5 grid gap-2 sm:grid-cols-2">
          <ParticipantCard name={trade.initiatorName} email={trade.initiatorEmail} role="Initiator" id={trade.initiatorId} />
          <ParticipantCard name={trade.receiverName} email={trade.receiverEmail} role="Receiver" id={trade.receiverId} />
        </div>

        <div className="mt-5 grid gap-2 sm:grid-cols-3">
          <MetaTile label="Accepted" value={<AdminDate value={trade.acceptedAt} />} />
          <MetaTile label="Completion requested" value={<AdminDate value={trade.completionRequestedAt} />} />
          <MetaTile label="Completed" value={<AdminDate value={trade.completedAt} />} />
        </div>
      </section>

      <div className="mt-4 grid gap-4 xl:grid-cols-[1.15fr_.85fr]">
        <AdminPanel title="Offer history" description={`${offerHistory.length} recorded offer${offerHistory.length === 1 ? "" : "s"}`}>
          {offerHistory.length === 0 ? <div className="p-5 text-sm text-muted-foreground">No offer history was returned.</div> : (
            <div className="divide-y divide-white/[0.05]">
              {offerHistory.map((offer, index) => (
                <div key={offer.offerId} className="p-4 sm:p-5">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="flex items-center gap-2"><span className="rounded-lg border border-[var(--negotiation-violet)]/15 bg-[var(--negotiation-violet)]/[0.05] px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.12em] text-[var(--negotiation-violet)]">Offer #{offer.offerId}</span><span className="text-xs text-muted-foreground">Step {index + 1}</span></div>
                    <span className="text-xs text-muted-foreground"><AdminDate value={offer.createdAt} /></span>
                  </div>
                  <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center">
                    <div className="flex-1 rounded-xl border border-white/[0.06] bg-white/[0.015] p-3"><p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">Sender</p><p className="mt-1 font-semibold">{offer.senderName}</p></div>
                    <ArrowLeftRight className="hidden h-4 w-4 shrink-0 text-muted-foreground sm:block" />
                    <div className="flex-1 rounded-xl border border-white/[0.06] bg-white/[0.015] p-3"><p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">Receiver</p><p className="mt-1 font-semibold">{offer.receiverName}</p></div>
                    <div className="rounded-xl border border-[var(--premium-gold)]/10 bg-[var(--premium-gold)]/[0.04] p-3"><p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">Cash</p><p className="mt-1 font-semibold">₹{offer.cashAdjustment.toLocaleString("en-IN")}</p></div>
                  </div>
                  <div className="mt-3 grid gap-3 sm:grid-cols-2">
                    <OfferItems title={`${offer.senderName}'s items`} items={offer.senderItems} />
                    <OfferItems title={`${offer.receiverName}'s items`} items={offer.receiverItems} />
                  </div>
                </div>
              ))}
            </div>
          )}
        </AdminPanel>

        <AdminPanel title="Trade timeline" description="Important lifecycle timestamps.">
          <div className="space-y-2 p-4">
            <Timeline label="Created" value={trade.createdAt} />
            <Timeline label="Accepted" value={trade.acceptedAt} />
            <Timeline label="Completion requested" value={trade.completionRequestedAt} />
            <Timeline label="Completed" value={trade.completedAt} />
            <Timeline label="Cancelled" value={trade.cancelledAt} />
            <Timeline label="Disputed" value={trade.disputedAt} />
          </div>

          {latestDispute && (
            <div className="border-t border-white/[0.06] p-4">
              <div className="rounded-xl border border-amber-400/15 bg-amber-400/[0.04] p-4">
                <div className="flex items-center gap-2 text-xs font-semibold"><Scale className="h-4 w-4 text-amber-300" /> Latest dispute</div>
                <div className="mt-2 flex items-center gap-2"><span className="font-semibold">#{latestDispute.disputeId}</span><AdminStatusBadge status={latestDispute.status} /></div>
                <Link href={`/admin/disputes/${latestDispute.disputeId}`} className="mt-3 inline-flex text-xs font-semibold text-[var(--trust-blue)]">Open dispute</Link>
              </div>
            </div>
          )}
        </AdminPanel>
      </div>
    </div>
  )
}

function ParticipantCard({ name, email, role, id }: { name: string; email: string; role: string; id: number }) {
  return <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] p-3.5"><div className="flex items-center gap-2 text-xs text-muted-foreground"><UserRound className="h-3.5 w-3.5" />{role}</div><p className="mt-2 font-semibold">{name}</p><p className="mt-0.5 text-xs text-muted-foreground">{email} · #{id}</p></div>
}

function MetaTile({ label, value }: { label: string; value: React.ReactNode }) {
  return <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] p-3.5"><p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">{label}</p><p className="mt-1.5 text-sm font-semibold">{value}</p></div>
}

function Timeline({ label, value }: { label: string; value: string | null }) {
  return <div className="flex items-center justify-between gap-4 rounded-xl border border-white/[0.05] bg-white/[0.015] px-3 py-2.5"><span className="inline-flex items-center gap-2 text-xs text-muted-foreground"><Clock3 className="h-3.5 w-3.5" />{label}</span><span className="text-xs font-medium"><AdminDate value={value} /></span></div>
}

function OfferItems({ title, items }: { title: string; items: Array<{ itemId: number; itemName: string; userPrice: number | null; generatedPrice: number | null; condition: string }> }) {
  return <div className="rounded-xl border border-white/[0.05] bg-white/[0.015] p-3"><p className="text-[10px] font-semibold uppercase tracking-[0.12em] text-muted-foreground">{title}</p>{items.length === 0 ? <p className="mt-2 text-xs text-muted-foreground">No items in this side of the offer.</p> : <div className="mt-2 space-y-1.5">{items.map((item) => <div key={item.itemId} className="flex items-center justify-between gap-3 rounded-lg border border-white/[0.05] px-2.5 py-2"><span className="truncate text-xs font-medium">{item.itemName}</span><span className="shrink-0 text-[10px] text-muted-foreground">{item.userPrice == null ? "—" : `₹${item.userPrice.toLocaleString("en-IN")}`}</span></div>)}</div>}</div>
}
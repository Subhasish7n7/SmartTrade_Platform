import Link from "next/link"

import { AdminDate, AdminPageHeader, AdminPagination, AdminStatusBadge, EmptyAdminState } from "@/components/admin/admin-ui"
import { getAdminDisputes } from "@/lib/api/admin/server"

interface Props {
  searchParams: Promise<Record<string, string | string[] | undefined>>
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value
}

export default async function AdminDisputesPage({ searchParams }: Props) {
  const params = await searchParams
  const page = Number(first(params.page) || 0)
  const status = first(params.status) || ""
  const data = await getAdminDisputes({ page, size: 20, status })

  return (
    <div>
      <AdminPageHeader eyebrow="Moderation queue" title="Disputes" description="Review open disputes, follow investigation state, and open the resolution workflow." />

      <form className="mb-4 flex flex-wrap gap-2 rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-3.5 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
        <select name="status" defaultValue={status} className="h-10 min-w-48 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none">
          <option value="">All dispute states</option>
          <option value="OPEN">Open</option>
          <option value="UNDER_REVIEW">Under review</option>
          <option value="RESOLVED">Resolved</option>
        </select>
        <button className="h-10 rounded-xl bg-primary px-4 text-sm font-semibold text-primary-foreground transition hover:brightness-105">Apply</button>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
        <div className="hidden overflow-x-auto md:block">
          <table className="w-full text-left text-sm"><thead className="border-b border-white/[0.06] text-[10px] uppercase tracking-[0.14em] text-muted-foreground"><tr><th className="px-5 py-3.5">Dispute</th><th className="px-4 py-3.5">Trade</th><th className="px-4 py-3.5">Opened by</th><th className="px-4 py-3.5">Reason</th><th className="px-4 py-3.5">Status</th><th className="px-4 py-3.5">Created</th><th className="px-5 py-3.5" /></tr></thead>
            <tbody className="divide-y divide-white/[0.05]">{data.content.map((dispute) => <tr key={dispute.disputeId} className="transition hover:bg-white/[0.018]"><td className="px-5 py-3.5"><Link href={`/admin/disputes/${dispute.disputeId}`} className="font-semibold hover:text-[var(--trust-blue)]">#{dispute.disputeId}</Link></td><td className="px-4 py-3.5 text-xs text-muted-foreground">#{dispute.tradeId}</td><td className="px-4 py-3.5 font-medium">{dispute.openedByName}</td><td className="max-w-56 px-4 py-3.5 text-xs text-muted-foreground">{dispute.reason.replaceAll("_", " ")}</td><td className="px-4 py-3.5"><AdminStatusBadge status={dispute.status} /></td><td className="px-4 py-3.5 text-xs text-muted-foreground"><AdminDate value={dispute.createdAt} /></td><td className="px-5 py-3.5 text-right"><Link href={`/admin/disputes/${dispute.disputeId}`} className="text-xs font-semibold text-[var(--trust-blue)]">Review</Link></td></tr>)}</tbody>
          </table>
        </div>

        <div className="divide-y divide-white/[0.05] md:hidden">{data.content.map((dispute) => <Link key={dispute.disputeId} href={`/admin/disputes/${dispute.disputeId}`} className="block px-4 py-4 transition hover:bg-white/[0.02]"><div className="flex items-center justify-between gap-3"><span className="font-semibold">Dispute #{dispute.disputeId}</span><AdminStatusBadge status={dispute.status} /></div><p className="mt-2 text-xs text-muted-foreground">Trade #{dispute.tradeId} · {dispute.openedByName}</p><p className="mt-1 text-xs text-muted-foreground">{dispute.reason.replaceAll("_", " ")} · <AdminDate value={dispute.createdAt} /></p></Link>)}</div>

        {data.content.length === 0 && <EmptyAdminState title="No disputes found" description="There are no disputes matching the selected state." />}
        <AdminPagination page={data.page} totalPages={data.totalPages} searchParams={{ page: String(page), status }} />
      </section>
    </div>
  )
}
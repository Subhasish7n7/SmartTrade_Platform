import Link from "next/link"
import { Search } from "lucide-react"

import { AdminDate, AdminPageHeader, AdminPagination, AdminStatusBadge, EmptyAdminState } from "@/components/admin/admin-ui"
import { getAdminTrades } from "@/lib/api/admin/server"

interface Props {
  searchParams: Promise<Record<string, string | string[] | undefined>>
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value
}

export default async function AdminTradesPage({ searchParams }: Props) {
  const params = await searchParams
  const page = Number(first(params.page) || 0)
  const search = first(params.search) || ""
  const status = first(params.status) || ""

  const data = await getAdminTrades({ page, size: 20, search, status })

  return (
    <div>
      <AdminPageHeader eyebrow="Trade monitoring" title="Trades" description="Inspect trades from a neutral administrative perspective, including status and completion state." />

      <form className="mb-4 rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-3.5 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
        <div className="grid gap-2 md:grid-cols-[1fr_220px_auto]">
          <label className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input name="search" defaultValue={search} placeholder="Search user, email, or trade ID" className="h-10 w-full rounded-xl border border-white/[0.09] bg-background/45 pl-9 pr-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          </label>
          <select name="status" defaultValue={status} className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none">
            <option value="">All statuses</option>
            <option value="OPEN">Open</option>
            <option value="NEGOTIATING">Negotiating</option>
            <option value="ACCEPTED">Accepted</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="EXPIRED">Expired</option>
            <option value="DISPUTED">Disputed</option>
          </select>
          <button className="h-10 rounded-xl bg-primary px-4 text-sm font-semibold text-primary-foreground transition hover:brightness-105">Apply</button>
        </div>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
        <div className="hidden overflow-x-auto md:block">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/[0.06] text-[10px] uppercase tracking-[0.14em] text-muted-foreground"><tr><th className="px-5 py-3.5">Trade</th><th className="px-4 py-3.5">Initiator</th><th className="px-4 py-3.5">Receiver</th><th className="px-4 py-3.5">Status</th><th className="px-4 py-3.5">Created</th><th className="px-4 py-3.5">Completion</th><th className="px-5 py-3.5" /></tr></thead>
            <tbody className="divide-y divide-white/[0.05]">
              {data.content.map((trade) => (
                <tr key={trade.tradeId} className="transition hover:bg-white/[0.018]">
                  <td className="px-5 py-3.5"><Link href={`/admin/trades/${trade.tradeId}`} className="font-semibold hover:text-[var(--trust-blue)]">#{trade.tradeId}</Link></td>
                  <td className="px-4 py-3.5"><p className="font-medium">{trade.initiatorName}</p><p className="text-xs text-muted-foreground">{trade.initiatorEmail}</p></td>
                  <td className="px-4 py-3.5"><p className="font-medium">{trade.receiverName}</p><p className="text-xs text-muted-foreground">{trade.receiverEmail}</p></td>
                  <td className="px-4 py-3.5"><AdminStatusBadge status={trade.status} /></td>
                  <td className="px-4 py-3.5 text-xs text-muted-foreground"><AdminDate value={trade.createdAt} /></td>
                  <td className="px-4 py-3.5 text-xs text-muted-foreground">{trade.completedAt ? <AdminDate value={trade.completedAt} /> : trade.initiatorCompletionConfirmed || trade.receiverCompletionConfirmed ? "Confirmation in progress" : "—"}</td>
                  <td className="px-5 py-3.5 text-right"><Link href={`/admin/trades/${trade.tradeId}`} className="text-xs font-semibold text-[var(--trust-blue)]">Inspect</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="divide-y divide-white/[0.05] md:hidden">
          {data.content.map((trade) => (
            <Link key={trade.tradeId} href={`/admin/trades/${trade.tradeId}`} className="block px-4 py-4 transition hover:bg-white/[0.02]">
              <div className="flex items-center justify-between gap-3"><span className="font-semibold">Trade #{trade.tradeId}</span><AdminStatusBadge status={trade.status} /></div>
              <p className="mt-2 text-xs text-muted-foreground">{trade.initiatorName} ↔ {trade.receiverName}</p>
              <p className="mt-1 text-xs text-muted-foreground">Created <AdminDate value={trade.createdAt} /></p>
            </Link>
          ))}
        </div>

        {data.content.length === 0 && <EmptyAdminState title="No trades found" description="Adjust the search or status filter and try again." />}
        <AdminPagination page={data.page} totalPages={data.totalPages} searchParams={{ page: String(page), search, status }} />
      </section>
    </div>
  )
}
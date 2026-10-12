import { AdminDate, AdminPageHeader, AdminPagination, EmptyAdminState } from "@/components/admin/admin-ui"
import { getAdminAuditLogs } from "@/lib/api/admin/server"

interface Props {
  searchParams: Promise<Record<string, string | string[] | undefined>>
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value
}

export default async function AdminAuditLogsPage({ searchParams }: Props) {
  const params = await searchParams
  const page = Number(first(params.page) || 0)
  const action = first(params.action) || ""
  const targetType = first(params.targetType) || ""

  const data = await getAdminAuditLogs({ page, size: 50, action, targetType })

  return (
    <div>
      <AdminPageHeader eyebrow="System history" title="Audit logs" description="Review recorded admin actions, their targets, reasons, and metadata." />

      <form className="mb-4 rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-3.5 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
        <div className="grid gap-2 md:grid-cols-[1fr_1fr_auto]">
          <input name="action" defaultValue={action} placeholder="Action, e.g. SUSPEND_USER" className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          <input name="targetType" defaultValue={targetType} placeholder="Target type, e.g. USER" className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          <button className="h-10 rounded-xl bg-primary px-4 text-sm font-semibold text-primary-foreground transition hover:brightness-105">Apply</button>
        </div>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[900px] text-left text-sm">
            <thead className="border-b border-white/[0.06] text-[10px] uppercase tracking-[0.14em] text-muted-foreground"><tr><th className="px-5 py-3.5">Time</th><th className="px-4 py-3.5">Admin</th><th className="px-4 py-3.5">Action</th><th className="px-4 py-3.5">Target</th><th className="px-4 py-3.5">Reason</th><th className="px-4 py-3.5">Metadata</th></tr></thead>
            <tbody className="divide-y divide-white/[0.05]">
              {data.content.map((log) => (
                <tr key={log.id} className="transition hover:bg-white/[0.018]">
                  <td className="px-5 py-3.5 text-xs text-muted-foreground"><AdminDate value={log.createdAt} /></td>
                  <td className="px-4 py-3.5 font-medium">{log.adminName}</td>
                  <td className="px-4 py-3.5"><span className="rounded-lg border border-[var(--trust-blue)]/12 bg-[var(--trust-blue)]/[0.04] px-2 py-1 text-[10px] font-semibold uppercase tracking-[0.1em] text-[var(--trust-blue)]">{log.action.replaceAll("_", " ")}</span></td>
                  <td className="px-4 py-3.5 text-xs">{log.targetType} #{log.targetId}</td>
                  <td className="max-w-sm px-4 py-3.5 text-xs text-muted-foreground">{log.reason}</td>
                  <td className="max-w-sm px-4 py-3.5"><details className="text-xs"><summary className="cursor-pointer text-muted-foreground">View metadata</summary><pre className="mt-2 max-w-sm whitespace-pre-wrap break-words text-[10px] text-foreground/65">{log.metadata || "No metadata"}</pre></details></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data.content.length === 0 && <EmptyAdminState title="No audit logs found" description="Try a different action or target type." />}
        <AdminPagination page={data.page} totalPages={data.totalPages} searchParams={{ page: String(page), action, targetType }} />
      </section>
    </div>
  )
}
import { ArrowLeft, Mail, Phone, ShieldCheck, UserRound } from "lucide-react"
import Link from "next/link"
import { notFound } from "next/navigation"

import { AdminActionDialog } from "@/components/admin/admin-action-dialog"
import { AdminDate, AdminPanel, AdminStatusBadge } from "@/components/admin/admin-ui"
import { getAdminUser } from "@/lib/api/admin/server"

interface Props {
  params: Promise<{ id: string }>
}

export default async function AdminUserDetailPage({ params }: Props) {
  const { id } = await params
  const userId = Number(id)
  if (!Number.isInteger(userId)) notFound()

  const data = await getAdminUser(userId).catch(() => null)
  if (!data) notFound()

  const { user, recentAdminActions } = data
  const suspended = user.accountStatus.toUpperCase() === "SUSPENDED"

  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-4">
        <Link
          href="/admin/users"
          className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Back to users
        </Link>
        <span className="text-[10px] uppercase tracking-[0.14em] text-muted-foreground">User #{user.userId}</span>
      </div>

      <section className="rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.76)] p-5 shadow-[0_16px_40px_rgba(0,0,0,.12)] backdrop-blur-xl sm:p-6">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-2xl border border-[var(--trust-blue)]/15 bg-[var(--trust-blue)]/[0.06] text-[var(--trust-blue)]">
              <UserRound className="h-6 w-6" />
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-2xl font-bold tracking-tight">{user.name}</h1>
                <AdminStatusBadge status={user.role} />
                <AdminStatusBadge status={user.accountStatus} />
              </div>
              <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1.5 text-xs text-muted-foreground">
                <span className="inline-flex items-center gap-1.5">
                  <Mail className="h-3.5 w-3.5" />
                  {user.email}
                </span>
                {user.phoneNo && (
                  <span className="inline-flex items-center gap-1.5">
                    <Phone className="h-3.5 w-3.5" />
                    {user.phoneNo}
                  </span>
                )}
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            {suspended ? (
              <AdminActionDialog
                action="RESTORE_USER"
                targetId={user.userId}
                title={`Restore ${user.name}?`}
                description="Restore the user's account and record the reason in the admin audit log."
                buttonLabel="Restore account"
                destructive={false}
              />
            ) : (
              <AdminActionDialog
                action="SUSPEND_USER"
                targetId={user.userId}
                title={`Suspend ${user.name}?`}
                description="This will suspend the account. The reason is stored in the admin audit log."
                buttonLabel="Suspend account"
              />
            )}
          </div>
        </div>
      </section>

      <div className="mt-4 grid gap-4 lg:grid-cols-[1.05fr_.95fr]">
        <AdminPanel title="Account overview" description="Current marketplace activity and account metadata.">
          <div className="grid gap-2 p-4 sm:grid-cols-2">
            <Info label="Trust score" value={String(Math.round(user.trustScore))} accent="blue" />
            <Info label="Successful trades" value={String(user.successfulTrades)} accent="gold" />
            <Info label="Total listings" value={String(user.totalListings)} accent="violet" />
            <Info label="Joined" value={<AdminDate value={user.createdAt} />} accent="neutral" />
          </div>
        </AdminPanel>

        <AdminPanel title="Account status" description="The current state used by marketplace access controls.">
          <div className="p-4">
            <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] p-4">
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <ShieldCheck className="h-4 w-4 text-[var(--trust-blue)]" /> Status
              </div>
              <div className="mt-2">
                <AdminStatusBadge status={user.accountStatus} />
              </div>
              <p className="mt-3 text-xs leading-5 text-muted-foreground">
                Latitude: {user.latitude ?? "—"} · Longitude: {user.longitude ?? "—"}
              </p>
            </div>
          </div>
        </AdminPanel>
      </div>

      <div className="mt-4">
        <AdminPanel title="Recent admin actions" description="Actions recorded against this user.">
          {recentAdminActions.length === 0 ? (
            <div className="p-5 text-sm text-muted-foreground">No admin actions recorded for this user.</div>
          ) : (
            <div className="divide-y divide-white/[0.05]">
              {recentAdminActions.map((log) => (
                <div key={log.id} className="grid gap-1 px-4 py-3.5 sm:grid-cols-[150px_140px_1fr_auto] sm:items-center sm:px-5">
                  <span className="text-xs font-semibold">{log.action.replaceAll("_", " ")}</span>
                  <span className="text-[11px] uppercase tracking-[0.12em] text-muted-foreground">
                    {log.targetType} #{log.targetId}
                  </span>
                  <span className="truncate text-xs text-muted-foreground">{log.reason}</span>
                  <span className="text-[11px] text-muted-foreground">
                    <AdminDate value={log.createdAt} />
                  </span>
                </div>
              ))}
            </div>
          )}
        </AdminPanel>
      </div>
    </div>
  )
}

function Info({ label, value, accent }: { label: string; value: React.ReactNode; accent: "blue" | "violet" | "gold" | "neutral" }) {
  const color =
    accent === "blue"
      ? "text-[var(--trust-blue)]"
      : accent === "violet"
        ? "text-[var(--negotiation-violet)]"
        : accent === "gold"
          ? "text-[var(--premium-gold)]"
          : "text-muted-foreground"
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] p-3.5">
      <p className="text-[10px] font-semibold uppercase tracking-[0.13em] text-muted-foreground">{label}</p>
      <p className={`mt-2 text-lg font-bold ${color}`}>{value}</p>
    </div>
  )
}

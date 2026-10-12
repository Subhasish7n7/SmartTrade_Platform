import {
  AlertTriangle,
  ArrowLeftRight,
  CheckCircle2,
  Package,
  Scale,
  ShieldCheck,
  Users,
} from "lucide-react"
import Link from "next/link"

import {
  ActionLink,
  AdminDate,
  AdminPageHeader,
  AdminPanel,
  AdminStatCard,
  AdminStatusBadge,
  EmptyAdminState,
} from "@/components/admin/admin-ui"
import { getAdminDashboard } from "@/lib/api/admin/server"

export default async function AdminDashboardPage() {
  const data = await getAdminDashboard()

  return (
    <div>
      <AdminPageHeader
        eyebrow="Admin overview"
        title="Marketplace health"
        description="Monitor users, listings, trades, trust, and moderation activity from one operational view."
      />

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <AdminStatCard
          label="Users"
          value={data.users.total.toLocaleString("en-IN")}
          detail={`${data.users.active.toLocaleString("en-IN")} active · ${data.users.suspended} suspended`}
          icon={<Users className="h-4 w-4" />}
          tone="blue"
        />
        <AdminStatCard
          label="Items"
          value={data.items.total.toLocaleString("en-IN")}
          detail={`${data.items.available.toLocaleString("en-IN")} available · ${data.items.hidden} hidden`}
          icon={<Package className="h-4 w-4" />}
          tone="violet"
        />
        <AdminStatCard
          label="Active trades"
          value={(data.trades.open + data.trades.negotiating + data.trades.accepted).toLocaleString("en-IN")}
          detail={`${data.trades.disputed} disputed · ${data.trades.completed} completed`}
          icon={<ArrowLeftRight className="h-4 w-4" />}
          tone="gold"
        />
        <AdminStatCard
          label="Disputes"
          value={data.disputes.open + data.disputes.underReview}
          detail={`${data.disputes.open} open · ${data.disputes.underReview} under review`}
          icon={<Scale className="h-4 w-4" />}
          tone="warning"
        />
      </div>

      <div className="mt-4 grid gap-4 xl:grid-cols-[1.05fr_.95fr]">
        <AdminPanel title="Marketplace health" description="Current account, listing, trade and trust signals.">
          <div className="grid gap-3 p-4 sm:grid-cols-2">
            <HealthTile label="Average trust score" value={Math.round(data.averageTrustScore).toLocaleString("en-IN")} helper="Across all users" icon={<ShieldCheck className="h-4 w-4" />} />
            <HealthTile label="Low-trust users" value={data.lowTrustUserCount.toLocaleString("en-IN")} helper="Users below the configured trust threshold" icon={<AlertTriangle className="h-4 w-4" />} warning />
            <HealthTile label="Admin accounts" value={data.users.admins.toLocaleString("en-IN")} helper="Accounts with ADMIN role" icon={<Users className="h-4 w-4" />} />
            <HealthTile label="Completed trades" value={data.trades.completed.toLocaleString("en-IN")} helper={`${data.trades.cancelled} cancelled · ${data.trades.expired} expired`} icon={<CheckCircle2 className="h-4 w-4" />} success />
          </div>
        </AdminPanel>

        <AdminPanel title="Trade pipeline" description="Current counts by trade state.">
          <div className="grid gap-2 p-4 sm:grid-cols-2">
            <PipelineRow label="Open" value={data.trades.open} tone="blue" />
            <PipelineRow label="Negotiating" value={data.trades.negotiating} tone="violet" />
            <PipelineRow label="Accepted" value={data.trades.accepted} tone="gold" />
            <PipelineRow label="Disputed" value={data.trades.disputed} tone="warning" />
            <PipelineRow label="Completed" value={data.trades.completed} tone="success" />
            <PipelineRow label="Cancelled / expired" value={data.trades.cancelled + data.trades.expired} tone="neutral" />
          </div>
        </AdminPanel>
      </div>

      <div className="mt-4 grid gap-4 xl:grid-cols-[1fr_1fr]">
        <AdminPanel
          title="Pending disputes"
          description={`${data.pendingDisputes.length} surfaced by the dashboard endpoint`}
          action={<ActionLink href="/admin/disputes">Open queue</ActionLink>}
        >
          {data.pendingDisputes.length === 0 ? (
            <EmptyAdminState title="No pending disputes" description="There are no disputes currently surfaced on the dashboard." />
          ) : (
            <div className="divide-y divide-white/[0.05]">
              {data.pendingDisputes.slice(0, 6).map((dispute) => (
                <Link
                  key={dispute.disputeId}
                  href={`/admin/disputes/${dispute.disputeId}`}
                  className="flex items-center justify-between gap-4 px-4 py-3.5 transition hover:bg-white/[0.02] sm:px-5"
                >
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-semibold">Dispute #{dispute.disputeId}</span>
                      <AdminStatusBadge status={dispute.status} />
                    </div>
                    <p className="mt-1 truncate text-xs text-muted-foreground">
                      Trade #{dispute.tradeId} · opened by {dispute.openedByName} · {dispute.reason.replaceAll("_", " ")}
                    </p>
                  </div>
                  <span className="shrink-0 text-xs text-muted-foreground">{new Intl.DateTimeFormat("en-IN", { day: "2-digit", month: "short" }).format(new Date(dispute.createdAt))}</span>
                </Link>
              ))}
            </div>
          )}
        </AdminPanel>

        <AdminPanel title="Recent admin actions" description="Latest moderation and administrative events." action={<ActionLink href="/admin/audit-logs">View logs</ActionLink>}>
          {data.recentAdminActions.length === 0 ? (
            <EmptyAdminState title="No admin actions yet" description="Audit activity will appear here once administrators take actions." />
          ) : (
            <div className="divide-y divide-white/[0.05]">
              {data.recentAdminActions.slice(0, 6).map((log) => (
                <div key={log.id} className="flex gap-3 px-4 py-3.5 sm:px-5">
                  <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-white/[0.06] bg-white/[0.02] text-muted-foreground">
                    <ShieldCheck className="h-4 w-4" />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-sm font-medium">{log.action.replaceAll("_", " ")}</span>
                      <span className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">{log.targetType} #{log.targetId}</span>
                    </div>
                    <p className="mt-1 line-clamp-1 text-xs text-muted-foreground">{log.reason}</p>
                    <p className="mt-1 text-[10px] text-muted-foreground">{log.adminName} · <AdminDate value={log.createdAt} /></p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </AdminPanel>
      </div>
    </div>
  )
}

function HealthTile({ label, value, helper, icon, warning, success }: { label: string; value: string; helper: string; icon: React.ReactNode; warning?: boolean; success?: boolean }) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.018] p-3.5">
      <div className="flex items-center gap-2 text-xs text-muted-foreground">
        <span className={warning ? "text-amber-300" : success ? "text-emerald-300" : "text-[var(--trust-blue)]"}>{icon}</span>
        {label}
      </div>
      <p className="mt-2 text-xl font-bold tracking-tight">{value}</p>
      <p className="mt-1 text-[11px] leading-4 text-muted-foreground">{helper}</p>
    </div>
  )
}

function PipelineRow({ label, value, tone }: { label: string; value: number; tone: "blue" | "violet" | "gold" | "warning" | "success" | "neutral" }) {
  const toneClass = {
    blue: "bg-[var(--trust-blue)]",
    violet: "bg-[var(--negotiation-violet)]",
    gold: "bg-[var(--premium-gold)]",
    warning: "bg-amber-300",
    success: "bg-emerald-300",
    neutral: "bg-muted-foreground/60",
  }[tone]

  return (
    <div className="flex items-center gap-3 rounded-xl border border-white/[0.05] bg-white/[0.015] px-3.5 py-3">
      <span className={`h-2 w-2 rounded-full ${toneClass}`} />
      <span className="flex-1 text-sm font-medium">{label}</span>
      <span className="text-sm font-bold">{value.toLocaleString("en-IN")}</span>
    </div>
  )
}
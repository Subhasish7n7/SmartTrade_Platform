import { ArrowLeft, Mail, MapPin, PackageCheck, Tags } from "lucide-react"
import Link from "next/link"
import { notFound } from "next/navigation"

import { AdminActionDialog } from "@/components/admin/admin-action-dialog"
import { AdminDate, AdminPanel, AdminStatusBadge } from "@/components/admin/admin-ui"
import { getAdminItem } from "@/lib/api/admin/server"

interface Props {
  params: Promise<{ id: string }>
}

export default async function AdminItemDetailPage({ params }: Props) {
  const { id } = await params
  const itemId = Number(id)
  if (!Number.isInteger(itemId)) notFound()

  const data = await getAdminItem(itemId).catch(() => null)
  if (!data) notFound()

  const { item, description, labels, recentAdminActions } = data
  const hidden = item.moderationStatus.toUpperCase() === "HIDDEN"
  const location = [item.locality, item.city, item.state].filter(Boolean).join(", ")

  return (
    <div>
      <div className="mb-4 flex items-center justify-between gap-4">
        <Link
          href="/admin/items"
          className="inline-flex items-center gap-2 text-xs font-medium text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Back to items
        </Link>
        <span className="text-[10px] uppercase tracking-[0.14em] text-muted-foreground">Item #{item.itemId}</span>
      </div>

      <section className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
        <div className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.76)] shadow-[0_16px_40px_rgba(0,0,0,.12)] backdrop-blur-xl">
          <div className="grid gap-2 p-4 sm:grid-cols-2 sm:p-5 lg:grid-cols-3">
            {(item.imageUrls.length ? item.imageUrls : [""]).slice(0, 6).map((url, index) => (
              <div key={`${url}-${index}`} className="aspect-[4/3] overflow-hidden rounded-xl border border-white/[0.07] bg-white/[0.018]">
                {url ? (
                  <img src={url} alt={item.itemName} className="h-full w-full object-cover" />
                ) : (
                  <div className="flex h-full items-center justify-center text-muted-foreground">
                    <PackageCheck className="h-8 w-8" />
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        <div className="space-y-4">
          <section className="rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.76)] p-5 shadow-[0_16px_40px_rgba(0,0,0,.12)] backdrop-blur-xl">
            <div className="flex flex-wrap gap-1.5">
              <AdminStatusBadge status={item.moderationStatus} />
              <AdminStatusBadge status={item.available ? "AVAILABLE" : "UNAVAILABLE"} />
            </div>
            <h1 className="mt-3 text-2xl font-bold tracking-tight">{item.itemName}</h1>
            <p className="mt-1 text-xs text-muted-foreground">
              {item.category.replaceAll("_", " ")} · {item.condition.replaceAll("_", " ")}
            </p>
            <div className="mt-5 grid grid-cols-2 gap-2">
              <Metric label="Listed price" value={item.userPrice == null ? "—" : `₹${item.userPrice.toLocaleString("en-IN")}`} />
              <Metric label="Market price" value={item.newPrice == null ? "—" : `₹${item.newPrice.toLocaleString("en-IN")}`} />
              <Metric label="Used estimate" value={item.generatedPrice == null ? "—" : `₹${item.generatedPrice.toLocaleString("en-IN")}`} />
              <Metric label="Pricing" value={item.pricingStatus.replaceAll("_", " ")} />
            </div>

            <div className="mt-5 flex flex-wrap gap-2">
              {hidden ? (
                <AdminActionDialog
                  action="RESTORE_ITEM"
                  targetId={item.itemId}
                  title={`Restore ${item.itemName}?`}
                  description="Restore this listing and record the reason in the admin audit log."
                  buttonLabel="Restore listing"
                  destructive={false}
                />
              ) : (
                <AdminActionDialog
                  action="HIDE_ITEM"
                  targetId={item.itemId}
                  title={`Hide ${item.itemName}?`}
                  description="Hide this listing from the marketplace and record the reason in the admin audit log."
                  buttonLabel="Hide listing"
                />
              )}
            </div>
          </section>

          <AdminPanel title="Seller" description="Account connected to this listing.">
            <div className="p-4">
              <Link
                href={`/admin/users/${item.sellerId}`}
                className="block rounded-xl border border-white/[0.06] bg-white/[0.015] p-3.5 transition hover:bg-white/[0.025]"
              >
                <p className="font-semibold">{item.sellerName}</p>
                <p className="mt-1 inline-flex items-center gap-1.5 text-xs text-muted-foreground">
                  <Mail className="h-3.5 w-3.5" />
                  {item.sellerEmail}
                </p>
              </Link>
            </div>
          </AdminPanel>
        </div>
      </section>

      <div className="mt-4 grid gap-4 lg:grid-cols-[1.2fr_.8fr]">
        <AdminPanel title="Description" description="Exact listing copy provided by the seller.">
          <div className="p-5">
            <p className="whitespace-pre-line text-sm leading-7 text-foreground/80">
              {description?.trim() || "No description was provided."}
            </p>
            {labels.length > 0 && (
              <div className="mt-6 border-t border-white/[0.06] pt-4">
                <div className="flex items-center gap-2 text-xs font-semibold">
                  <Tags className="h-4 w-4 text-[var(--trust-blue)]" /> Labels
                </div>
                <div className="mt-3 flex flex-wrap gap-1.5">
                  {labels.map((label) => (
                    <span
                      key={label}
                      className="rounded-full border border-white/[0.07] bg-white/[0.02] px-2.5 py-1 text-xs text-muted-foreground"
                    >
                      #{label}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </AdminPanel>

        <AdminPanel title="Listing context" description="Moderation-relevant listing metadata.">
          <div className="grid gap-2 p-4">
            <Row label="Created" value={<AdminDate value={item.createdAt} />} />
            <Row
              label="Location"
              value={
                <span className="inline-flex items-center gap-1.5">
                  <MapPin className="h-3.5 w-3.5" />
                  {location || "Not provided"}
                </span>
              }
            />
            <Row
              label="Listing type"
              value={item.forSale && item.forTrade ? "Trade / Sale" : item.forTrade ? "Trade" : item.forSale ? "Sale" : "None"}
            />
            <Row label="Sold / traded" value={item.sold ? "Sold" : item.traded ? "Traded" : "No"} />
          </div>
        </AdminPanel>
      </div>

      <div className="mt-4">
        <AdminPanel title="Recent admin actions" description="Moderation actions recorded for this listing.">
          {recentAdminActions.length === 0 ? (
            <div className="p-5 text-sm text-muted-foreground">No admin actions recorded for this item.</div>
          ) : (
            <div className="divide-y divide-white/[0.05]">
              {recentAdminActions.map((log) => (
                <div key={log.id} className="grid gap-1 px-4 py-3.5 sm:grid-cols-[150px_140px_1fr_auto] sm:items-center sm:px-5">
                  <span className="text-xs font-semibold">{log.action.replaceAll("_", " ")}</span>
                  <span className="text-[11px] uppercase tracking-[0.12em] text-muted-foreground">{log.adminName}</span>
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

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-white/[0.06] bg-white/[0.015] p-3">
      <p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">{label}</p>
      <p className="mt-1.5 truncate text-sm font-bold">{value}</p>
    </div>
  )
}

function Row({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-xl border border-white/[0.05] bg-white/[0.015] px-3.5 py-3 text-xs">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right font-medium">{value}</span>
    </div>
  )
}

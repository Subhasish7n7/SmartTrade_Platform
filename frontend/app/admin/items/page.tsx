import Link from "next/link"
import { Search } from "lucide-react"

import { AdminDate, AdminPageHeader, AdminPagination, AdminStatusBadge, EmptyAdminState } from "@/components/admin/admin-ui"
import { getAdminItems } from "@/lib/api/admin/server"

interface Props {
  searchParams: Promise<Record<string, string | string[] | undefined>>
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value
}

export default async function AdminItemsPage({ searchParams }: Props) {
  const params = await searchParams
  const page = Number(first(params.page) || 0)
  const search = first(params.search) || ""
  const moderationStatus = first(params.moderationStatus) || ""
  const available = first(params.available) || ""

  const data = await getAdminItems({
    page,
    size: 20,
    search,
    moderationStatus,
    available: available === "" ? undefined : available === "true",
  })

  return (
    <div>
      <AdminPageHeader
        eyebrow="Content moderation"
        title="Items"
        description="Review marketplace listings, pricing context, availability, and moderation state."
      />

      <form className="mb-4 rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-3.5 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
        <div className="grid gap-2 md:grid-cols-[1fr_190px_160px_auto]">
          <label className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input name="search" defaultValue={search} placeholder="Search item or seller" className="h-10 w-full rounded-xl border border-white/[0.09] bg-background/45 pl-9 pr-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          </label>
          <input name="moderationStatus" defaultValue={moderationStatus} placeholder="Moderation status" className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          <select name="available" defaultValue={available} className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none">
            <option value="">Any availability</option>
            <option value="true">Available</option>
            <option value="false">Unavailable</option>
          </select>
          <button className="h-10 rounded-xl bg-primary px-4 text-sm font-semibold text-primary-foreground transition hover:brightness-105">Apply</button>
        </div>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
        <div className="hidden overflow-x-auto lg:block">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/[0.06] text-[10px] uppercase tracking-[0.14em] text-muted-foreground">
              <tr>
                <th className="px-5 py-3.5 font-semibold">Item</th>
                <th className="px-4 py-3.5 font-semibold">Seller</th>
                <th className="px-4 py-3.5 font-semibold">Price</th>
                <th className="px-4 py-3.5 font-semibold">Type</th>
                <th className="px-4 py-3.5 font-semibold">Moderation</th>
                <th className="px-4 py-3.5 font-semibold">Availability</th>
                <th className="px-4 py-3.5 font-semibold">Listed</th>
                <th className="px-5 py-3.5" />
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.05]">
              {data.content.map((item) => (
                <tr key={item.itemId} className="transition hover:bg-white/[0.018]">
                  <td className="px-5 py-3.5">
                    <Link href={`/admin/items/${item.itemId}`} className="group flex items-center gap-3">
                      <div className="h-11 w-11 overflow-hidden rounded-xl border border-white/[0.07] bg-white/[0.02]">
                        {item.imageUrls[0] ? <img src={item.imageUrls[0]} alt="" className="h-full w-full object-cover" /> : null}
                      </div>
                      <div className="min-w-0">
                        <p className="truncate font-semibold group-hover:text-[var(--trust-blue)]">{item.itemName}</p>
                        <p className="text-xs text-muted-foreground">#{item.itemId} · {item.category.replaceAll("_", " ")}</p>
                      </div>
                    </Link>
                  </td>
                  <td className="px-4 py-3.5"><p className="font-medium">{item.sellerName}</p><p className="text-xs text-muted-foreground">{item.sellerEmail}</p></td>
                  <td className="px-4 py-3.5 font-semibold">{item.userPrice == null ? "—" : `₹${item.userPrice.toLocaleString("en-IN")}`}</td>
                  <td className="px-4 py-3.5 text-xs text-muted-foreground">{item.forTrade && item.forSale ? "Trade / Sale" : item.forTrade ? "Trade" : item.forSale ? "Sale" : "—"}</td>
                  <td className="px-4 py-3.5"><AdminStatusBadge status={item.moderationStatus} /></td>
                  <td className="px-4 py-3.5"><AdminStatusBadge status={item.available ? "AVAILABLE" : "UNAVAILABLE"} /></td>
                  <td className="px-4 py-3.5 text-xs text-muted-foreground"><AdminDate value={item.createdAt} /></td>
                  <td className="px-5 py-3.5 text-right"><Link href={`/admin/items/${item.itemId}`} className="text-xs font-semibold text-[var(--trust-blue)]">Inspect</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="divide-y divide-white/[0.05] lg:hidden">
          {data.content.map((item) => (
            <Link key={item.itemId} href={`/admin/items/${item.itemId}`} className="block px-4 py-4 transition hover:bg-white/[0.02]">
              <div className="flex gap-3">
                <div className="h-14 w-14 shrink-0 overflow-hidden rounded-xl border border-white/[0.07] bg-white/[0.02]">
                  {item.imageUrls[0] ? <img src={item.imageUrls[0]} alt="" className="h-full w-full object-cover" /> : null}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate font-semibold">{item.itemName}</p>
                  <p className="mt-0.5 text-xs text-muted-foreground">{item.sellerName} · {item.category.replaceAll("_", " ")}</p>
                  <div className="mt-2 flex flex-wrap gap-1.5"><AdminStatusBadge status={item.moderationStatus} /><AdminStatusBadge status={item.available ? "AVAILABLE" : "UNAVAILABLE"} /></div>
                  <p className="mt-2 text-sm font-semibold">{item.userPrice == null ? "Price not set" : `₹${item.userPrice.toLocaleString("en-IN")}`}</p>
                </div>
              </div>
            </Link>
          ))}
        </div>

        {data.content.length === 0 && <EmptyAdminState title="No items found" description="Adjust the search or moderation filters and try again." />}
        <AdminPagination page={data.page} totalPages={data.totalPages} searchParams={{ page: String(page), search, moderationStatus, available }} />
      </section>
    </div>
  )
}
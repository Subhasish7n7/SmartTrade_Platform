import Link from "next/link"
import { Search, Users } from "lucide-react"

import {
  AdminDate,
  AdminPageHeader,
  AdminPagination,
  AdminStatusBadge,
  EmptyAdminState,
} from "@/components/admin/admin-ui"
import { getAdminUsers } from "@/lib/api/admin/server"

interface Props {
  searchParams: Promise<Record<string, string | string[] | undefined>>
}

function first(value: string | string[] | undefined) {
  return Array.isArray(value) ? value[0] : value
}

export default async function AdminUsersPage({ searchParams }: Props) {
  const params = await searchParams
  const page = Number(first(params.page) || 0)
  const search = first(params.search) || ""
  const role = first(params.role) || ""
  const status = first(params.status) || ""

  const data = await getAdminUsers({ page, size: 20, search, role, status })
  const currentParams = { page: String(page), search, role, status }

  return (
    <div>
      <AdminPageHeader
        eyebrow="User management"
        title="Users"
        description="Search accounts, inspect trust activity, and manage account status."
      />

      <form className="mb-4 rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] p-3.5 shadow-[0_12px_32px_rgba(0,0,0,.1)] backdrop-blur-xl">
        <div className="grid gap-2 md:grid-cols-[1fr_160px_160px_auto]">
          <label className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <input name="search" defaultValue={search} placeholder="Search name, email, or phone" className="h-10 w-full rounded-xl border border-white/[0.09] bg-background/45 pl-9 pr-3 text-sm outline-none focus:border-[var(--trust-blue)]/45" />
          </label>
          <select name="role" defaultValue={role} className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none">
            <option value="">All roles</option>
            <option value="USER">User</option>
            <option value="ADMIN">Admin</option>
          </select>
          <select name="status" defaultValue={status} className="h-10 rounded-xl border border-white/[0.09] bg-background/45 px-3 text-sm outline-none">
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="SUSPENDED">Suspended</option>
          </select>
          <button className="h-10 rounded-xl bg-primary px-4 text-sm font-semibold text-primary-foreground transition hover:brightness-105">Apply</button>
        </div>
      </form>

      <section className="overflow-hidden rounded-2xl border border-white/[0.07] bg-[rgba(9,15,24,.72)] shadow-[0_16px_40px_rgba(0,0,0,.11)] backdrop-blur-xl">
        <div className="hidden overflow-x-auto md:block">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-white/[0.06] text-[10px] uppercase tracking-[0.14em] text-muted-foreground">
              <tr>
                <th className="px-5 py-3.5 font-semibold">User</th>
                <th className="px-4 py-3.5 font-semibold">Role</th>
                <th className="px-4 py-3.5 font-semibold">Status</th>
                <th className="px-4 py-3.5 font-semibold">Trust</th>
                <th className="px-4 py-3.5 font-semibold">Trades</th>
                <th className="px-4 py-3.5 font-semibold">Joined</th>
                <th className="px-5 py-3.5 text-right font-semibold"> </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.05]">
              {data.content.map((user) => (
                <tr key={user.userId} className="transition hover:bg-white/[0.018]">
                  <td className="px-5 py-3.5">
                    <Link href={`/admin/users/${user.userId}`} className="group">
                      <p className="font-semibold group-hover:text-[var(--trust-blue)]">{user.name}</p>
                      <p className="mt-0.5 text-xs text-muted-foreground">{user.email}</p>
                    </Link>
                  </td>
                  <td className="px-4 py-3.5"><AdminStatusBadge status={user.role} /></td>
                  <td className="px-4 py-3.5"><AdminStatusBadge status={user.accountStatus} /></td>
                  <td className="px-4 py-3.5"><span className="font-semibold">{Math.round(user.trustScore)}</span></td>
                  <td className="px-4 py-3.5 text-muted-foreground">{user.successfulTrades}</td>
                  <td className="px-4 py-3.5 text-xs text-muted-foreground"><AdminDate value={user.createdAt} /></td>
                  <td className="px-5 py-3.5 text-right"><Link href={`/admin/users/${user.userId}`} className="text-xs font-semibold text-[var(--trust-blue)]">Inspect</Link></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="divide-y divide-white/[0.05] md:hidden">
          {data.content.map((user) => (
            <Link key={user.userId} href={`/admin/users/${user.userId}`} className="block px-4 py-4 transition hover:bg-white/[0.02]">
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold">{user.name}</p>
                  <p className="mt-0.5 truncate text-xs text-muted-foreground">{user.email}</p>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    <AdminStatusBadge status={user.accountStatus} />
                    <AdminStatusBadge status={user.role} />
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-lg font-bold">{Math.round(user.trustScore)}</p>
                  <p className="text-[10px] uppercase tracking-[0.12em] text-muted-foreground">Trust</p>
                </div>
              </div>
              <div className="mt-3 flex items-center justify-between text-xs text-muted-foreground">
                <span>{user.successfulTrades} successful trades</span>
                <span>{user.totalListings} listings</span>
              </div>
            </Link>
          ))}
        </div>

        {data.content.length === 0 && <EmptyAdminState title="No users found" description="Adjust the search or filters and try again." />}

        <AdminPagination page={data.page} totalPages={data.totalPages} searchParams={currentParams} />
      </section>
    </div>
  )
}
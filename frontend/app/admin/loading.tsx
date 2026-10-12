export default function AdminLoading() {
  return (
    <div className="space-y-4">
      <div className="h-20 animate-pulse rounded-2xl border border-white/[0.06] bg-white/[0.025]" />
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        {Array.from({ length: 4 }).map((_, index) => (
          <div key={index} className="h-28 animate-pulse rounded-2xl border border-white/[0.06] bg-white/[0.025]" />
        ))}
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        <div className="h-72 animate-pulse rounded-2xl border border-white/[0.06] bg-white/[0.025]" />
        <div className="h-72 animate-pulse rounded-2xl border border-white/[0.06] bg-white/[0.025]" />
      </div>
    </div>
  )
}
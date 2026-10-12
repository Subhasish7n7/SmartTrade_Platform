"use client"

import { Button } from "@/components/ui/button"

export default function AdminError({ reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="flex min-h-[60vh] items-center justify-center">
      <div className="max-w-md rounded-2xl border border-rose-400/15 bg-rose-400/[0.045] p-6 text-center">
        <h2 className="text-lg font-semibold">Admin data could not be loaded</h2>
        <p className="mt-2 text-sm text-muted-foreground">
          Try refreshing the workspace. If the problem continues, check the backend admin endpoints.
        </p>
        <Button onClick={() => reset()} className="mt-5 rounded-xl">Try again</Button>
      </div>
    </div>
  )
}
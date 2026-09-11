"use client"

import { useState } from "react"
import { AlertCircle, Crosshair, MapPin } from "lucide-react"

import { Button } from "@/components/ui/button"

interface Props {
  value: { latitude?: number; longitude?: number; city?: string; state?: string; locality?: string }
  onChange: (location: { latitude?: number; longitude?: number; city?: string; state?: string; locality?: string }) => void
}

export function LocationPicker({ value, onChange }: Props) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function detectLocation() {
    setError(null)
    if (!navigator.geolocation) {
      setError("Location detection is not supported by this browser.")
      return
    }

    try {
      setLoading(true)
      const position = await new Promise<GeolocationPosition>((resolve, reject) => navigator.geolocation.getCurrentPosition(resolve, reject, { enableHighAccuracy: true, timeout: 10000 }))
      const { latitude, longitude } = position.coords

      const response = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json`, { headers: { Accept: "application/json" } })
      if (!response.ok) throw new Error("Could not resolve your location.")
      const data = await response.json()

      onChange({
        latitude,
        longitude,
        locality: data.address?.suburb || data.address?.village || data.address?.town || "",
        city: data.address?.city || data.address?.municipality || data.address?.county || "",
        state: data.address?.state || "",
      })
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to determine your location.")
    } finally {
      setLoading(false)
    }
  }

  const hasLocation = Boolean(value.latitude !== undefined && value.longitude !== undefined)
  const label = [value.locality, value.city].filter(Boolean).join(", ")

  return (
    <section className="content-surface overflow-hidden p-0">
      <div className="flex flex-col gap-4 border-b border-border/40 px-5 py-4 sm:flex-row sm:items-center sm:justify-between sm:px-6">
        <div className="flex items-start gap-3">
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl border border-[var(--chat-cyan)]/15 bg-[var(--chat-cyan)]/5 text-[var(--chat-cyan)]"><MapPin className="h-4 w-4" /></div>
          <div><h2 className="font-semibold">Item location</h2><p className="mt-1 text-xs leading-5 text-muted-foreground">Use your current location to power nearby marketplace discovery.</p></div>
        </div>
        <Button type="button" variant="surface" className="h-10 rounded-xl" onClick={detectLocation} disabled={loading}><Crosshair className="mr-2 h-4 w-4" />{loading ? "Locating…" : "Use current location"}</Button>
      </div>

      <div className="p-5 sm:p-6">
        {hasLocation ? (
          <div className="rounded-2xl border border-[var(--chat-cyan)]/15 bg-[var(--chat-cyan)]/4 p-4">
            <div className="flex items-start gap-3">
              <div className="mt-0.5 rounded-full bg-black/20 p-2"><MapPin className="h-4 w-4 text-[var(--chat-cyan)]" /></div>
              <div className="min-w-0"><p className="font-medium">{label || "Location detected"}</p><p className="mt-1 text-sm text-muted-foreground">{value.state || "State unavailable"}</p><p className="mt-2 text-[11px] text-muted-foreground">Coordinates captured for nearby search.</p></div>
            </div>
          </div>
        ) : (
          <div className="rounded-2xl border border-border/40 bg-white/[0.015] p-4 text-sm text-muted-foreground">Location is optional. Leaving it blank keeps the listing discoverable without a geographic filter.</div>
        )}

        {error && <div className="mt-3 flex items-start gap-2 rounded-xl border border-destructive/15 bg-destructive/5 p-3 text-xs leading-5 text-destructive"><AlertCircle className="mt-0.5 h-3.5 w-3.5 shrink-0" />{error}</div>}
      </div>
    </section>
  )
}

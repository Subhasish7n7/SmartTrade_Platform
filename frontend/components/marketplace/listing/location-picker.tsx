"use client"

import { useState } from "react"
import {
  AlertCircle,
  CheckCircle2,
  Crosshair,
  MapPin,
  RotateCcw,
} from "lucide-react"

import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

interface LocationValue {
  latitude?: number
  longitude?: number
  city?: string
  state?: string
  locality?: string
  clearCoordinates?: boolean
}

interface Props {
  value: LocationValue
  onChange: (location: LocationValue) => void
}

export function LocationPicker({ value, onChange }: Props) {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const hasCoordinates =
    value.latitude !== undefined &&
    value.longitude !== undefined

  function updateManualField(
    field: "city" | "state" | "locality",
    fieldValue: string,
  ) {
    setError(null)

    // Locality is a descriptive refinement inside the same area, so it
    // must not invalidate an existing coordinate pair. City/state changes
    // may point at a different geographic area, so those clear coordinates.
    if (field === "locality") {
      onChange({
        ...value,
        locality: fieldValue,
      })
      return
    }

    onChange({
      ...value,
      [field]: fieldValue,
      latitude: undefined,
      longitude: undefined,
      clearCoordinates: hasCoordinates || value.clearCoordinates,
    })
  }

  async function detectLocation() {
    setError(null)

    if (!navigator.geolocation) {
      setError("Location detection is not supported by this browser.")
      return
    }

    try {
      setLoading(true)

      const position = await new Promise<GeolocationPosition>(
        (resolve, reject) =>
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 10000,
          }),
      )

      const { latitude, longitude } = position.coords

      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json&zoom=12`,
        {
          headers: {
            Accept: "application/json",
          },
        },
      )

      if (!response.ok) {
        throw new Error("Could not resolve your location.")
      }

      const data = await response.json()

      onChange({
        latitude,
        longitude,
        city:
          data.address?.city ||
          data.address?.municipality ||
          data.address?.county ||
          "",
        state: data.address?.state || "",
        locality:
          data.address?.suburb ||
          data.address?.village ||
          data.address?.town ||
          "",
        clearCoordinates: false,
      })
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to determine your location.",
      )
    } finally {
      setLoading(false)
    }
  }

  function clearCoordinates() {
    setError(null)

    onChange({
      ...value,
      latitude: undefined,
      longitude: undefined,
      clearCoordinates: true,
    })
  }

  return (
    <section className="content-surface overflow-hidden p-0 shadow-[0_18px_55px_rgba(0,0,0,.06)]">
      <div className="border-b border-white/[0.055] px-5 py-4 sm:px-6">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div className="marketplace-section-heading">
            <div className="marketplace-section-icon">
              <MapPin className="h-4 w-4" />
            </div>

            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h2 className="marketplace-section-title !mt-0">
                  Item location
                </h2>

                <Badge
                  variant="outline"
                  className="border-[var(--chat-cyan)]/20 bg-[var(--chat-cyan)]/[0.045] text-[10px] text-[var(--chat-cyan)]"
                >
                  Nearby discovery
                </Badge>
              </div>

              <p className="marketplace-section-copy">
                Choose the general area where this item is located. An exact address is not required.
              </p>
            </div>
          </div>

          <div className="shrink-0">
            {hasCoordinates ? (
              <Button
                type="button"
                variant="surface"
                className="marketplace-action-secondary h-10 rounded-xl text-xs font-semibold"
                onClick={clearCoordinates}
              >
                <RotateCcw className="mr-2 h-3.5 w-3.5" />
                Remove map location
              </Button>
            ) : (
              <Button
                type="button"
                variant="surface"
                className="marketplace-action-secondary h-10 rounded-xl text-xs font-semibold"
                onClick={detectLocation}
                disabled={loading}
              >
                <Crosshair className="mr-2 h-4 w-4" />
                {loading ? "Locating…" : "Use current location"}
              </Button>
            )}
          </div>
        </div>
      </div>

      <div className="space-y-5 p-5 sm:p-6">
        {hasCoordinates && (
          <div className="rounded-xl border border-emerald-400/18 bg-emerald-400/[0.045] p-3.5">
            <div className="flex items-start gap-2.5">
              <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-emerald-300" />

              <div>
                <p className="text-sm font-semibold">Map location enabled</p>
                <p className="mt-1 text-xs leading-5 text-muted-foreground">
                  Your coordinates help with nearby discovery. Changing the city or state removes them; refining the locality does not.
                </p>
              </div>
            </div>
          </div>
        )}

        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label className="text-sm font-medium">City</label>
            <Input
              value={value.city ?? ""}
              onChange={(event) => updateManualField("city", event.target.value)}
              placeholder="e.g. Bhubaneswar"
              className="mt-2 h-11 rounded-xl"
            />
          </div>

          <div>
            <label className="text-sm font-medium">State</label>
            <Input
              value={value.state ?? ""}
              onChange={(event) => updateManualField("state", event.target.value)}
              placeholder="e.g. Odisha"
              className="mt-2 h-11 rounded-xl"
            />
          </div>

          <div>
            <label className="text-sm font-medium">Locality / Area</label>
            <Input
              value={value.locality ?? ""}
              onChange={(event) => updateManualField("locality", event.target.value)}
              placeholder="e.g. Patia"
              className="mt-2 h-11 rounded-xl"
            />
          </div>
        </div>

        {!hasCoordinates && (
          <p className="text-xs leading-5 text-muted-foreground">
            You can enter a city, state, and locality manually without sharing device coordinates.
          </p>
        )}

        {error && (
          <div className="flex items-start gap-2 rounded-xl border border-destructive/15 bg-destructive/5 p-3 text-xs leading-5 text-destructive">
            <AlertCircle className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            {error}
          </div>
        )}
      </div>
    </section>
  )
}

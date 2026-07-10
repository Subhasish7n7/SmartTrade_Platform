"use client"

import { useState } from "react"

import { Button } from "@/components/ui/button"

interface Props {
  value: {
    latitude?: number
    longitude?: number

    city?: string
    state?: string
    locality?: string
  }

  onChange: (
    location: {
      latitude?: number
      longitude?: number

      city?: string
      state?: string
      locality?: string
    }
  ) => void
}

export function LocationPicker({
  value,
  onChange,
}: Props) {

  const [loading, setLoading] =
    useState(false)

  async function detectLocation() {

    try {

      setLoading(true)

      const position =
        await new Promise<GeolocationPosition>(
          (resolve, reject) =>
            navigator.geolocation.getCurrentPosition(
              resolve,
              reject
            )
        )

      const latitude =
        position.coords.latitude

      const longitude =
        position.coords.longitude

      const response =
        await fetch(
          `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json`
        )

      const data =
        await response.json()

      onChange({
        latitude,
        longitude,

        locality:
          data.address.suburb ||
          data.address.village ||
          data.address.town ||
          "",

        city:
          data.address.city ||
          data.address.county ||
          "",

        state:
          data.address.state ||
          "",
      })

    } finally {

      setLoading(false)

    }
  }

  return (
    <div className="content-surface p-5">

      <div className="flex items-center justify-between mb-4">

        <div>

          <h2 className="font-semibold">
            Item Location
          </h2>

          <p className="text-sm text-muted-foreground mt-1">
            Choose where the item is available.
          </p>

        </div>

        <Button
          variant="secondary"
          onClick={detectLocation}
          disabled={loading}
        >
          {loading
            ? "Locating..."
            : "Use Current Location"}
        </Button>

      </div>

      {(value.city ||
        value.state ||
        value.locality) && (

        <div className="trade-stat">

          <p className="font-medium">
            📍 {[
              value.locality,
              value.city,
            ]
              .filter(Boolean)
              .join(", ")}
          </p>

          <p className="text-sm text-muted-foreground">
            {value.state}
          </p>

        </div>

      )}

    </div>
  )
}
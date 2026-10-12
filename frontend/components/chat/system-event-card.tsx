"use client"

import Link from "next/link"

import { Button } from "@/components/ui/button"

interface Props {
  tradeId: number

  text: string
}

export function SystemEventCard({
  tradeId,
  text,
}: Props) {
  return (
    <div className="flex justify-center">
      <div
        className="
          glass
          rounded-2xl
          p-4
          max-w-md
          text-center
        "
      >
        <p className="text-sm">
          {text}
        </p>

        <Link
          href={`/trades/${tradeId}`}
        >
          <Button
            size="sm"
            className="mt-3"
          >
            View Trade
          </Button>
        </Link>
      </div>
    </div>
  )
}
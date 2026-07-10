"use client"

import Link from "next/link"

import {
  ShieldCheck,
  ArrowRight,
} from "lucide-react"

import { Button } from "@/components/ui/button"

interface Props {
  text: string
  tradeId: number
}

export function SystemMessageCard({
  text,
  tradeId,
}: Props) {
  return (
    <div className="flex justify-center">

      <div
        className="
          max-w-md
          rounded-2xl
          border
          border-[var(--trust-blue)]/20
          bg-[var(--trust-blue)]/5
          backdrop-blur-xl
          px-4
          py-3
        "
      >
        <div className="flex items-start gap-3">

          <ShieldCheck
            className="
              h-5
              w-5
              mt-0.5
              text-[var(--trust-blue)]
            "
          />

          <div>

            <p className="text-sm">
              {text}
            </p>

            <Link
              href={`/trades/${tradeId}`}
            >
              <Button
                size="sm"
                variant="ghost"
                className="mt-2"
              >
                View Trade

                <ArrowRight className="ml-2 h-4 w-4" />
              </Button>
            </Link>

          </div>

        </div>
      </div>

    </div>
  )
}
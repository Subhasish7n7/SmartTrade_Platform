"use client"

import { useEffect, useRef, useState, type ReactNode } from "react"
import { ChevronDown, ChevronUp } from "lucide-react"

import { Button } from "@/components/ui/button"
import { cn } from "@/lib/utils"

interface Props {
  title: string
  children: ReactNode
  className?: string
}

const COLLAPSED_HEIGHT = 144

/** A flat, text-first section that only shows Read more when content overflows. */
export function ExpandableContentSection({ title, children, className }: Props) {
  const contentRef = useRef<HTMLDivElement>(null)
  const [expanded, setExpanded] = useState(false)
  const [canExpand, setCanExpand] = useState(false)

  useEffect(() => {
    const element = contentRef.current
    if (!element) return

    const measure = () => {
      const overflowing = element.scrollHeight > COLLAPSED_HEIGHT + 4
      setCanExpand(overflowing)
      if (!overflowing) setExpanded(false)
    }

    measure()
    window.addEventListener("resize", measure)
    const observer = typeof ResizeObserver !== "undefined" ? new ResizeObserver(measure) : null
    observer?.observe(element)

    return () => {
      window.removeEventListener("resize", measure)
      observer?.disconnect()
    }
  }, [children])

  return (
    <section className={cn("mt-7", className)}>
      <h2 className="mb-2 font-semibold text-foreground">{title}</h2>
      <div
        ref={contentRef}
        className="text-sm leading-7 text-muted-foreground"
        style={{
          maxHeight: expanded ? "none" : `${COLLAPSED_HEIGHT}px`,
          overflow: expanded ? "visible" : "hidden",
        }}
      >
        {children}
      </div>
      {canExpand && (
        <Button
          type="button"
          variant="ghost"
          onClick={() => setExpanded((value) => !value)}
          aria-expanded={expanded}
          className={cn(
            "mt-2 h-8 rounded-lg px-2 text-xs font-semibold text-[var(--negotiation-violet)]",
            "hover:!bg-[var(--negotiation-violet)]/[0.09] hover:!text-[var(--negotiation-violet)]",
          )}
        >
          {expanded ? "Read less" : "Read more"}
          {expanded ? <ChevronUp className="ml-1.5 h-4 w-4" /> : <ChevronDown className="ml-1.5 h-4 w-4" />}
        </Button>
      )}
    </section>
  )
}

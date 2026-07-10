import { Badge } from "@/components/ui/badge"

import { cn } from "@/lib/utils"

interface Props {
  status:
    | "OPEN"
    | "NEGOTIATING"
    | "ACCEPTED"
    | "COMPLETED"
    | "CANCELLED"
    | "EXPIRED"
}

export function TradeStatusBadge({
  status,
}: Props) {
  return (
    <Badge
      className={cn(
        "border-none",

        status === "OPEN" &&
          "bg-info/20 text-info",

        status ===
          "NEGOTIATING" &&
          "bg-warning/20 text-warning",

        status ===
          "ACCEPTED" &&
          "bg-primary/20 text-primary",

        status ===
          "COMPLETED" &&
          "bg-emerald-500/20 text-emerald-400",

        status ===
          "CANCELLED" &&
          "bg-destructive/20 text-destructive",

        status ===
          "EXPIRED" &&
          "bg-muted text-muted-foreground"
      )}
    >
      {status}
    </Badge>
  )
}
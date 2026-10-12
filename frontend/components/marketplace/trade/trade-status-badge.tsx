import { Badge } from "@/components/ui/badge"
import { cn } from "@/lib/utils"

interface Props {
  status:
    | "OPEN"
    | "NEGOTIATING"
    | "ACCEPTED"
    | "DISPUTED"
    | "COMPLETED"
    | "CANCELLED"
    | "EXPIRED"
}

export function TradeStatusBadge({ status }: Props) {
  return (
    <Badge
      className={cn(
        "border-none",
        status === "OPEN" && "bg-info/20 text-info",
        status === "NEGOTIATING" && "bg-[var(--negotiation-violet)]/15 text-[var(--negotiation-violet)]",
        status === "ACCEPTED" && "bg-[var(--trust-blue)]/15 text-[var(--trust-blue)]",
        status === "DISPUTED" && "bg-[var(--premium-gold)]/15 text-[var(--premium-gold)]",
        status === "COMPLETED" && "bg-emerald-500/15 text-emerald-300",
        status === "CANCELLED" && "bg-destructive/15 text-destructive",
        status === "EXPIRED" && "bg-muted text-muted-foreground",
      )}
    >
      {status}
    </Badge>
  )
}
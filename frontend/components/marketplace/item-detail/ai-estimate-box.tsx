import { RefreshCw, Sparkles } from "lucide-react"

interface Props {
  aiEstimate: number
}

export function AiEstimateBox({
  aiEstimate,
}: Props) {
  return (
    <div className="p-4 rounded-xl bg-muted/30 border border-border/50">
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-primary" />

          <span className="text-sm font-medium">
            AI Estimated Value
          </span>
        </div>

        <RefreshCw className="h-4 w-4 text-muted-foreground" />
      </div>

      <p className="text-2xl font-bold">
        ${aiEstimate.toLocaleString()}
      </p>

      <p className="text-xs text-muted-foreground mt-1">
        Based on recent market data
      </p>
    </div>
  )
}
"use client"

import { AnimatePresence, motion } from "framer-motion"
import { ArrowDown, ArrowLeftRight, Boxes, Compass, PackageCheck, ShoppingBag, Tags, TrendingUp } from "lucide-react"
import { useRouter } from "next/navigation"
import * as React from "react"

import { Navbar } from "@/components/marketplace/navbar"
import { getRecentMarketplaceItems } from "@/lib/api/items/client"
import { getMarketplaceHomeCache, setMarketplaceHomeCache } from "@/lib/cache/marketplace-home-cache"
import type { ItemResponse } from "@/lib/types/api/item"

import "./marketplace-entry.module.css"

const ENTRY_RECENT_PAGE = 0
const ENTRY_RECENT_SIZE = 25

const ENTRY_FEATURED_ITEM_IDS: number[] = [10, 6]

const ENTRY_SCROLL_THRESHOLD_DESKTOP = 145
const ENTRY_SCROLL_THRESHOLD_TOUCH = 105

export function MarketplaceEntry() {
  const router = useRouter()

  const [items, setItems] = React.useState<ItemResponse[]>([])

  const [loading, setLoading] = React.useState(true)

  const [loadError, setLoadError] = React.useState<string | null>(null)

  const [showWakeHint, setShowWakeHint] = React.useState(false)

  const [transitioning, setTransitioning] = React.useState(false)

  const [scrollProgress, setScrollProgress] = React.useState(0)

  const [atBottom, setAtBottom] = React.useState(false)

  const transitionLockRef = React.useRef(false)

  const scrollProgressRef = React.useRef(0)

  const touchYRef = React.useRef<number | null>(null)

  React.useEffect(() => {
    if (!loading) {
      setShowWakeHint(false)
      return
    }

    const timer = window.setTimeout(() => {
      setShowWakeHint(true)
    }, 8000)

    return () => window.clearTimeout(timer)
  }, [loading])

  const updateProgress = React.useCallback((value: number) => {
    const next = Math.max(0, Math.min(1, value))

    scrollProgressRef.current = next

    setScrollProgress(next)

    return next
  }, [])

  const enterMarketplace = React.useCallback(() => {
    if (transitionLockRef.current) {
      return
    }

    transitionLockRef.current = true

    updateProgress(1)
    setTransitioning(true)

    window.setTimeout(() => {
      router.push("/marketplace")
    }, 560)
  }, [router, updateProgress])

  const refreshRecentItems = React.useCallback(async (background = false) => {
    if (!background) {
      setLoading(true)
      setLoadError(null)
    }

    try {
      const nextItems = await getRecentMarketplaceItems(ENTRY_RECENT_PAGE, ENTRY_RECENT_SIZE)

      setMarketplaceHomeCache(nextItems)

      setItems(nextItems)
      setLoadError(null)
    } catch (error) {
      console.error("Failed to load recent marketplace entry data:", error)

      if (!background) {
        setLoadError("Couldn't connect to the marketplace right now.")
      }
    } finally {
      if (!background) {
        setLoading(false)
      }
    }
  }, [])

  React.useEffect(() => {
    const cached = getMarketplaceHomeCache()

    if (cached) {
      setItems(cached.items)
      setLoadError(null)
      setLoading(false)

      if (!cached.isFresh) {
        void refreshRecentItems(true)
      }

      return
    }

    void refreshRecentItems(false)
  }, [refreshRecentItems])

  React.useEffect(() => {
    const isAtBottom = () => {
      const scrollLimit = Math.max(0, document.documentElement.scrollHeight - window.innerHeight)

      return window.scrollY >= scrollLimit - 2
    }

    const syncBottomState = () => {
      const bottom = isAtBottom()

      setAtBottom(bottom)

      if (!bottom && scrollProgressRef.current > 0) {
        updateProgress(0)
      }
    }

    const consumeWheel = (event: WheelEvent) => {
      if (transitionLockRef.current || !isAtBottom()) {
        return
      }

      if (event.deltaY <= 0) {
        if (scrollProgressRef.current > 0) {
          updateProgress(0)
        }

        return
      }

      /*
       * Only consume the scroll once the tiny
       * entry runway is exhausted.
       * The document stays visually fixed
       * while the ring fills.
       */
      event.preventDefault()

      const delta = event.deltaMode === WheelEvent.DOM_DELTA_LINE ? event.deltaY * 16 : event.deltaY

      const progressDelta = Math.min(Math.max(delta, 1), 48) / ENTRY_SCROLL_THRESHOLD_DESKTOP

      const next = updateProgress(scrollProgressRef.current + progressDelta)

      if (next >= 1) {
        enterMarketplace()
      }
    }

    const handleTouchStart = (event: TouchEvent) => {
      touchYRef.current = event.touches[0]?.clientY ?? null
    }

    const handleTouchMove = (event: TouchEvent) => {
      if (transitionLockRef.current) {
        return
      }

      const currentY = event.touches[0]?.clientY

      const previousY = touchYRef.current

      if (currentY == null || previousY == null) {
        return
      }

      const upwardFingerTravel = previousY - currentY

      touchYRef.current = currentY

      if (!isAtBottom()) {
        return
      }

      if (upwardFingerTravel <= 0) {
        updateProgress(0)
        return
      }

      event.preventDefault()

      const next = updateProgress(scrollProgressRef.current + Math.min(upwardFingerTravel, 42) / ENTRY_SCROLL_THRESHOLD_TOUCH)

      if (next >= 1) {
        enterMarketplace()
      }
    }

    const handleTouchEnd = () => {
      touchYRef.current = null
    }

    window.addEventListener("scroll", syncBottomState, {
      passive: true,
    })

    window.addEventListener("wheel", consumeWheel, {
      passive: false,
    })

    window.addEventListener("touchstart", handleTouchStart, {
      passive: true,
    })

    window.addEventListener("touchmove", handleTouchMove, {
      passive: false,
    })

    window.addEventListener("touchend", handleTouchEnd, {
      passive: true,
    })

    syncBottomState()

    return () => {
      window.removeEventListener("scroll", syncBottomState)

      window.removeEventListener("wheel", consumeWheel)

      window.removeEventListener("touchstart", handleTouchStart)

      window.removeEventListener("touchmove", handleTouchMove)

      window.removeEventListener("touchend", handleTouchEnd)
    }
  }, [enterMarketplace, updateProgress])

  /*
   * Backend gives us the newest 25.
   * Curated IDs stay frontend-only and get
   * priority before the newest image-backed items.
   */
  const recentItems = React.useMemo(() => {
    const available = items.filter((item) => item.available)

    const curated = ENTRY_FEATURED_ITEM_IDS.map((itemId) => available.find((item) => item.itemId === itemId)).filter(
      (item): item is ItemResponse => Boolean(item),
    )

    const newestWithImages = available
      .filter((item) => (item.images?.length ?? 0) > 0)
      .slice()
      .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())

    const seen = new Set(curated.map((item) => item.itemId))

    return [...curated, ...newestWithImages.filter((item) => !seen.has(item.itemId))].slice(0, 3)
  }, [items])

  /*
   * These metrics intentionally use the
   * recent 25-item candidate pool.
   */
  const liveItems = React.useMemo(() => items.filter((item) => item.available), [items])

  const saleItems = React.useMemo(() => liveItems.filter((item) => item.forSale), [liveItems])

  const tradeItems = React.useMemo(() => liveItems.filter((item) => item.forTrade), [liveItems])

  const categoryCount = React.useMemo(() => new Set(liveItems.map((item) => item.category)).size, [liveItems])

  const featuredItem = recentItems[0]

  const supportingItems = recentItems.slice(1, 5)

  return (
    <main className="marketplace-entry min-h-[100svh] overflow-x-hidden">
      <div className="marketplace-entry-background">
        <div className="marketplace-entry-grid" />

        <motion.div
          className="marketplace-entry-orb marketplace-entry-orb-a"
          animate={{
            x: [0, 45, -20, 0],
            y: [0, -30, 24, 0],
            scale: [1, 1.08, 0.94, 1],
          }}
          transition={{
            duration: 18,
            repeat: Infinity,
            ease: "easeInOut",
          }}
        />

        <motion.div
          className="marketplace-entry-orb marketplace-entry-orb-b"
          animate={{
            x: [0, -38, 28, 0],
            y: [0, 24, -26, 0],
            scale: [1, 0.92, 1.07, 1],
          }}
          transition={{
            duration: 21,
            repeat: Infinity,
            ease: "easeInOut",
          }}
        />

        <div className="marketplace-entry-noise" />
      </div>

      <div className="relative z-20">
        <Navbar />

        <section className="mx-auto grid h-[100svh] max-w-[1600px] gap-8 px-4 pb-8 pt-24 sm:px-6 lg:grid-cols-[1.22fr_0.78fr] lg:items-center lg:gap-10 lg:px-10 lg:pb-10 lg:pt-[5.75rem]">
          <div className="relative z-20 flex h-full w-full max-w-[58rem] flex-col justify-between">
            <div className="pt-1 lg:pt-4">
              <motion.h1
                initial={{
                  opacity: 0,
                  y: 24,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
                transition={{
                  duration: 0.7,
                  delay: 0.08,
                }}
                className="marketplace-entry-title mt-5"
              >
                Buy what you
                <span> need.</span>
                <br />
                Trade what you
                <span> have.</span>
              </motion.h1>

              <motion.p
                initial={{
                  opacity: 0,
                  y: 18,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
                transition={{
                  duration: 0.6,
                  delay: 0.16,
                }}
                className="marketplace-entry-copy mt-8 max-w-[41rem]"
              >
                Browse real listings, compare real prices, and choose whether the next move is a purchase, a trade, or both.
              </motion.p>

              <motion.div
                initial={{
                  opacity: 0,
                  y: 18,
                }}
                animate={{
                  opacity: 1,
                  y: 0,
                }}
                transition={{
                  duration: 0.6,
                  delay: 0.24,
                }}
                className="mt-9 flex flex-wrap items-center gap-3"
              >
                <button type="button" onClick={enterMarketplace} className="marketplace-entry-launch" title="Enter marketplace">
                  <span className="flex flex-col text-left">
                    <span className="text-sm font-semibold">Enter marketplace</span>

                    <span className="mt-0.5 text-[11px] font-medium text-foreground/55">Open live listings</span>
                  </span>

                  <span className="marketplace-entry-launch-icon">
                    <ArrowDown className="h-5 w-5" />
                  </span>
                </button>

                <div className="marketplace-entry-scroll-hint">
                  <span className="marketplace-entry-scroll-line" />
                  Scroll to enter
                </div>
              </motion.div>
            </div>

            <div className="pb-1 lg:pb-5">
              <AnimatePresence>
                {!loading && !loadError && (
                  <motion.div
                    initial={{
                      opacity: 0,
                      y: 12,
                    }}
                    animate={{
                      opacity: 1,
                      y: 0,
                    }}
                    transition={{
                      duration: 0.5,
                      delay: 0.35,
                    }}
                    className="grid max-w-[56rem] grid-cols-2 gap-2 sm:grid-cols-4 lg:gap-2.5"
                  >
                    <EntryMetric icon={<Boxes className="h-4 w-4" />} label="Recent live" value={liveItems.length} tone="blue" />

                    <EntryMetric icon={<ShoppingBag className="h-4 w-4" />} label="For sale" value={saleItems.length} tone="cyan" />

                    <EntryMetric
                      icon={<ArrowLeftRight className="h-4 w-4" />}
                      label="Open to trade"
                      value={tradeItems.length}
                      tone="violet"
                    />

                    <EntryMetric icon={<Tags className="h-4 w-4" />} label="Categories" value={categoryCount} tone="gold" />
                  </motion.div>
                )}
              </AnimatePresence>

              <div className="mt-3 flex items-center gap-2 text-[11px] text-muted-foreground">
                <TrendingUp className="h-3.5 w-3.5 text-[var(--trust-blue)]" />
                Live values from recent marketplace listings.
              </div>
            </div>
          </div>

          <div className="relative hidden h-full min-h-0 lg:flex lg:items-center lg:justify-center lg:-translate-x-6">
            <div className="marketplace-entry-stage">
              <div className="marketplace-entry-stage-glow" />

              <div className="marketplace-entry-stage-title">
                <Compass className="h-4 w-4" />
                Featured listing
              </div>

              {loading ? (
                <div className="marketplace-entry-card marketplace-entry-card-featured marketplace-entry-card-loading relative flex items-end justify-center p-6 text-center">
                  <div className="relative z-10">
                    <p className="text-sm font-semibold">Loading marketplace...</p>

                    {showWakeHint && (
                      <p className="mt-1 max-w-[18rem] text-xs text-muted-foreground">
                        The marketplace server may be waking up. The first request can take a little longer.
                      </p>
                    )}
                  </div>
                </div>
              ) : loadError ? (
                <div className="absolute inset-0 flex items-center justify-center px-8 text-center">
                  <div>
                    <div className="marketplace-entry-empty-icon">
                      <PackageCheck className="h-6 w-6" />
                    </div>

                    <p className="mt-4 text-sm font-semibold">Couldn't load marketplace</p>

                    <p className="mt-1 text-xs text-muted-foreground">{loadError}</p>

                    <button
                      type="button"
                      onClick={() => void refreshRecentItems(false)}
                      className="mt-4 rounded-lg border border-white/10 bg-white/[0.04] px-3 py-2 text-xs font-medium transition hover:bg-white/[0.08]"
                    >
                      Try again
                    </button>
                  </div>
                </div>
              ) : featuredItem ? (
                <>
                  {supportingItems.map((item, index) => (
                    <EntryShowcaseCard
                      key={item.itemId}
                      item={item}
                      variant={
                        index === 0
                          ? "supporting-one"
                          : index === 1
                            ? "supporting-two"
                            : index === 2
                              ? "supporting-three"
                              : "supporting-four"
                      }
                    />
                  ))}

                  <EntryShowcaseCard item={featuredItem} variant="featured" />
                </>
              ) : (
                <div className="absolute inset-0 flex items-center justify-center px-10 text-center">
                  <div>
                    <div className="marketplace-entry-empty-icon">
                      <PackageCheck className="h-6 w-6" />
                    </div>

                    <p className="mt-4 text-sm font-semibold">Marketplace is ready.</p>

                    <p className="mt-1 text-xs text-muted-foreground">Your first live listings will appear here.</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </section>

        <div className="marketplace-entry-scroll-sentinel" aria-hidden="true" />

        <AnimatePresence>
          {(atBottom || scrollProgress > 0) && !transitioning && (
            <motion.div
              initial={{
                opacity: 0,
                y: 12,
              }}
              animate={{
                opacity: 1,
                y: 0,
              }}
              exit={{
                opacity: 0,
                y: 12,
              }}
              className="marketplace-entry-scroll-gate"
              aria-live="polite"
            >
              <div
                className="marketplace-entry-progress-ring"
                style={
                  {
                    "--progress": scrollProgress,
                  } as React.CSSProperties
                }
              >
                <svg viewBox="0 0 44 44" aria-hidden="true">
                  <defs>
                    <linearGradient id="entryProgressGradient" x1="0" y1="0" x2="1" y2="1">
                      <stop offset="0%" stopColor="var(--trust-blue)" />

                      <stop offset="58%" stopColor="var(--negotiation-violet)" />

                      <stop offset="100%" stopColor="var(--premium-gold)" />
                    </linearGradient>
                  </defs>

                  <circle className="marketplace-entry-progress-track" cx="22" cy="22" r="18" pathLength="100" />

                  <circle className="marketplace-entry-progress-value" cx="22" cy="22" r="18" pathLength="100" />
                </svg>

                <ArrowDown className="h-4 w-4" />
              </div>

              <span>{scrollProgress > 0 ? "Keep scrolling" : "Scroll to enter"}</span>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      <AnimatePresence>
        {transitioning && (
          <motion.div
            initial={{
              y: "100%",
            }}
            animate={{
              y: "0%",
            }}
            transition={{
              duration: 0.56,
              ease: [0.76, 0, 0.24, 1],
            }}
            className="marketplace-entry-curtain"
          >
            <div className="marketplace-entry-curtain-mark">
              <ArrowLeftRight className="h-6 w-6" />
              <span>SmartTrade</span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </main>
  )
}

function EntryMetric({
  icon,
  label,
  value,
  tone,
}: {
  icon: React.ReactNode
  label: string
  value: number
  tone: "blue" | "cyan" | "violet" | "gold"
}) {
  return (
    <div className={`marketplace-entry-metric marketplace-entry-metric-${tone}`}>
      <span className="marketplace-entry-metric-icon">{icon}</span>

      <div className="min-w-0">
        <p className="marketplace-entry-metric-value">{value.toLocaleString("en-IN")}</p>

        <p className="marketplace-entry-metric-label">{label}</p>
      </div>
    </div>
  )
}

function EntryShowcaseCard({
  item,
  variant,
}: {
  item: ItemResponse
  variant: "featured" | "supporting-one" | "supporting-two" | "supporting-three" | "supporting-four"
}) {
  const image = item.images?.[0]?.secureUrl

  const listingLabel = item.forSale && item.forTrade ? "Sale + Trade" : item.forTrade ? "Trade" : item.forSale ? "Sale" : "Listed"

  return (
    <motion.article
      initial={{
        opacity: 0,
        scale: 0.96,
        y: 18,
      }}
      animate={{
        opacity: 1,
        scale: 1,
        y: [0, -6, 0],
      }}
      transition={{
        opacity: {
          duration: 0.45,
        },
        scale: {
          duration: 0.45,
        },
        y: {
          duration: variant === "featured" ? 4.6 : 5.4,
          repeat: Infinity,
          ease: "easeInOut",
        },
      }}
      className={`marketplace-entry-card marketplace-entry-card-${variant}`}
    >
      <div className="marketplace-entry-card-image">
        {image ? (
          <img src={image} alt={item.itemName} loading={variant === "featured" ? "eager" : "lazy"} />
        ) : (
          <div className="marketplace-entry-card-image-fallback">
            <PackageCheck className="h-6 w-6" />
          </div>
        )}

        <span className="marketplace-entry-card-badge">{listingLabel}</span>
      </div>

      <div className="marketplace-entry-card-body">
        <div className="min-w-0">
          <p className="marketplace-entry-card-category">{formatEnum(item.category)}</p>

          <h3 className="mt-1 truncate text-sm font-semibold">{item.itemName}</h3>
        </div>

        <div className="shrink-0 text-right">
          <p className="marketplace-entry-card-price">₹{item.userPrice.toLocaleString("en-IN")}</p>

          <p className="text-[9px] text-muted-foreground">seller price</p>
        </div>
      </div>
    </motion.article>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

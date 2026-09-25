"use client"

import { Button } from "@/components/ui/button"
import { Slider } from "@/components/ui/slider"
import { cn } from "@/lib/utils"
import { AnimatePresence, motion } from "framer-motion"
import {
  Bike,
  BookOpen,
  Camera,
  ChevronDown,
  Filter,
  Gamepad2,
  Headphones,
  Home,
  Laptop,
  Monitor,
  Package,
  Shirt,
  SlidersHorizontal,
  Smartphone,
  Sparkles,
  Tag,
  ToyBrick,
  Wrench,
  X,
} from "lucide-react"
import * as React from "react"

import {
  ITEM_CATEGORIES,
  ITEM_CONDITIONS,
  type ItemCategory,
  type ItemCondition,
  type ItemSearchFilters,
  type ItemSort,
  type ListingType,
} from "@/lib/types/api/item"

const PRICE_MAX = 2_500_000
const PRICE_STEP = 5_000

const categoryIcons: Partial<Record<ItemCategory, React.ElementType>> = {
  PHONES: Smartphone,
  COMPUTERS: Laptop,
  GAMING: Gamepad2,
  CAMERAS: Camera,
  AUDIO: Headphones,
  MONITORS: Monitor,
  BIKES: Bike,
  BOOKS: BookOpen,
  HOME_APPLIANCES: Home,
  HOME_DECOR: Home,
  TOYS: ToyBrick,
  TOOLS: Wrench,
  CLOTHING: Shirt,
  SHOES: Tag,
  BAGS: Package,
  COLLECTIBLES: Sparkles,
}

const listingTypes: ReadonlyArray<{ label: string; value: ListingType | null }> = [
  { label: "All Listings", value: null },
  { label: "For Sale", value: "SALE" },
  { label: "For Trade", value: "TRADE" },
]

const sorts: ReadonlyArray<{ label: string; value: ItemSort }> = [
  { label: "Relevance", value: "RELEVANCE" },
  { label: "Newest First", value: "NEWEST" },
  { label: "Price: Low to High", value: "PRICE_LOW_TO_HIGH" },
  { label: "Price: High to Low", value: "PRICE_HIGH_TO_LOW" },
  { label: "Distance", value: "DISTANCE" },
]

const distances = [
  { label: "5 km", value: 5 },
  { label: "10 km", value: 10 },
  { label: "25 km", value: 25 },
  { label: "50 km", value: 50 },
  { label: "100 km", value: 100 },
  { label: "Anywhere", value: null },
] as const

const pricePresets = [
  { label: "₹5K", value: 5_000 },
  { label: "₹10K", value: 10_000 },
  { label: "₹25K", value: 25_000 },
  { label: "₹50K", value: 50_000 },
  { label: "₹1L", value: 100_000 },
  { label: "₹2.5L", value: 250_000 },
  { label: "₹5L", value: 500_000 },
  { label: "₹10L", value: 1_000_000 },
  { label: "₹25L", value: 2_500_000 },
] as const

const categoryItems = ITEM_CATEGORIES.map((value) => ({
  value,
  icon: categoryIcons[value] ?? Package,
  label: formatEnum(value),
}))

interface FilterPanelProps {
  isOpen: boolean
  onClose: () => void
  filters: ItemSearchFilters
  onApply: (filters: ItemSearchFilters) => void
}

export function FilterPanel({ isOpen, onClose, onApply, filters }: FilterPanelProps) {
  const [selectedCategory, setSelectedCategory] = React.useState<ItemCategory | null>(filters.category ?? null)

  const [selectedConditions, setSelectedConditions] = React.useState<ItemCondition[]>(filters.conditions ?? [])

  const [selectedListingType, setSelectedListingType] = React.useState<ListingType | null>(filters.listingType ?? null)

  const [selectedSort, setSelectedSort] = React.useState<ItemSort>(filters.sort ?? "RELEVANCE")

  const [selectedDistance, setSelectedDistance] = React.useState<number | null>(filters.radiusKm ?? null)

  const [priceRange, setPriceRange] = React.useState([filters.minPrice ?? 0, filters.maxPrice ?? PRICE_MAX])

  const [expandedSections, setExpandedSections] = React.useState({
    sort: true,
    category: false,
    condition: false,
    listingType: false,
    distance: false,
    price: false,
  })

  React.useEffect(() => {
    setSelectedCategory(filters.category ?? null)
    setSelectedConditions(filters.conditions ?? [])
    setSelectedListingType(filters.listingType ?? null)
    setSelectedSort(filters.sort ?? "RELEVANCE")
    setSelectedDistance(filters.radiusKm ?? null)
    setPriceRange([filters.minPrice ?? 0, filters.maxPrice ?? PRICE_MAX])
  }, [filters])

  const toggleSection = (section: keyof typeof expandedSections) => {
    setExpandedSections((previous) => {
      const nextOpen = !previous[section]

      return {
        sort: false,
        category: false,
        condition: false,
        listingType: false,
        distance: false,
        price: false,
        [section]: nextOpen,
      }
    })
  }

  const toggleCondition = (value: ItemCondition) => {
    setSelectedConditions((previous) =>
      previous.includes(value) ? previous.filter((condition) => condition !== value) : [...previous, value],
    )
  }

  const clearFilters = () => {
    setSelectedCategory(null)
    setSelectedConditions([])
    setSelectedListingType(null)
    setSelectedSort("RELEVANCE")
    setSelectedDistance(null)
    setPriceRange([0, PRICE_MAX])
  }

  const activeFiltersCount =
    (selectedCategory ? 1 : 0) +
    (selectedConditions.length ? 1 : 0) +
    (selectedListingType ? 1 : 0) +
    (selectedDistance !== null ? 1 : 0) +
    (priceRange[0] !== 0 || priceRange[1] !== PRICE_MAX ? 1 : 0) +
    (selectedSort !== "RELEVANCE" ? 1 : 0)

  const handleApply = () => {
    onApply({
      ...filters,
      category: selectedCategory ?? undefined,
      conditions: selectedConditions.length ? selectedConditions : undefined,
      listingType: selectedListingType ?? undefined,
      sort: selectedSort,
      radiusKm: selectedDistance === null ? undefined : selectedDistance,
      minPrice: priceRange[0] > 0 ? priceRange[0] : undefined,
      maxPrice: priceRange[1] < PRICE_MAX ? priceRange[1] : undefined,
      page: 0,
      size: 24,
    })

    onClose()
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 z-50 bg-background/75 backdrop-blur-md"
            onClick={onClose}
          />

          <motion.aside
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{
              type: "spring",
              damping: 30,
              stiffness: 300,
            }}
            className="marketplace-filter-drawer"
            aria-label="Marketplace filters"
          >
            <div className="marketplace-filter-header">
              <div className="flex items-center gap-3">
                <div className="marketplace-section-icon h-10 w-10 rounded-xl">
                  <Filter className="h-4 w-4" />
                </div>

                <div>
                  <h2 className="font-semibold text-foreground">Filters</h2>

                  {activeFiltersCount > 0 && <p className="text-xs text-muted-foreground">{activeFiltersCount} active</p>}
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="ghost"
                  size="sm"
                  onClick={clearFilters}
                  disabled={activeFiltersCount === 0}
                  className="marketplace-filter-clear"
                >
                  Clear filters
                </Button>

                <Button variant="ghost" size="icon" onClick={onClose} className="marketplace-filter-close">
                  <X className="h-5 w-5" />
                </Button>
              </div>
            </div>

            <div className="marketplace-filter-body scrollbar-thin">
              <FilterSection title="Sort" expanded={expandedSections.sort} onToggle={() => toggleSection("sort")}>
                <div className="space-y-2 pt-1">
                  {sorts.map((sort) => (
                    <button
                      key={sort.value}
                      type="button"
                      onClick={() => setSelectedSort(sort.value)}
                      className={cn("marketplace-filter-option", selectedSort === sort.value && "marketplace-filter-option-active")}
                    >
                      <span>{sort.label}</span>

                      {selectedSort === sort.value && <span className="marketplace-filter-check">✓</span>}
                    </button>
                  ))}
                </div>
              </FilterSection>

              <FilterSection title="Category" expanded={expandedSections.category} onToggle={() => toggleSection("category")}>
                <div className="marketplace-filter-category-grid grid grid-cols-2 gap-2 pt-1">
                  {categoryItems.map((category) => {
                    const Icon = category.icon
                    const active = selectedCategory === category.value

                    return (
                      <button
                        key={category.value}
                        type="button"
                        onClick={() => setSelectedCategory(active ? null : category.value)}
                        className={cn("marketplace-filter-category", active && "marketplace-filter-category-active")}
                      >
                        <Icon className="h-4 w-4 shrink-0" />
                        <span className="truncate">{category.label}</span>
                      </button>
                    )
                  })}
                </div>
              </FilterSection>

              <FilterSection title="Condition" expanded={expandedSections.condition} onToggle={() => toggleSection("condition")}>
                <div className="space-y-2 pt-1">
                  {ITEM_CONDITIONS.map((condition) => {
                    const active = selectedConditions.includes(condition)

                    return (
                      <button
                        key={condition}
                        type="button"
                        onClick={() => toggleCondition(condition)}
                        className={cn("marketplace-filter-option", active && "marketplace-filter-option-active")}
                      >
                        <span>{formatEnum(condition)}</span>

                        <span className={cn("marketplace-filter-checkbox", active && "marketplace-filter-checkbox-active")}>
                          {active ? "✓" : ""}
                        </span>
                      </button>
                    )
                  })}
                </div>
              </FilterSection>

              <FilterSection title="Listing type" expanded={expandedSections.listingType} onToggle={() => toggleSection("listingType")}>
                <div className="grid grid-cols-3 gap-2 pt-1">
                  {listingTypes.map((type) => (
                    <button
                      key={type.label}
                      type="button"
                      onClick={() => setSelectedListingType(type.value)}
                      className={cn("marketplace-filter-pill", selectedListingType === type.value && "marketplace-filter-pill-active")}
                    >
                      {type.value === null ? "All" : type.value === "SALE" ? "Sale" : "Trade"}
                    </button>
                  ))}
                </div>

                <p className="mt-2 text-[11px] leading-5 text-muted-foreground">
                  Sale includes listings that support both sale and trade. Trade works the same way.
                </p>
              </FilterSection>

              <FilterSection title="Distance" expanded={expandedSections.distance} onToggle={() => toggleSection("distance")}>
                <div className="flex flex-wrap gap-2 pt-1">
                  {distances.map((distance) => (
                    <button
                      key={distance.label}
                      type="button"
                      onClick={() => setSelectedDistance(distance.value)}
                      className={cn("marketplace-filter-pill", selectedDistance === distance.value && "marketplace-filter-pill-active")}
                    >
                      {distance.label}
                    </button>
                  ))}
                </div>
              </FilterSection>

              <FilterSection title="Price range" expanded={expandedSections.price} onToggle={() => toggleSection("price")}>
                <div className="space-y-4 pt-2">
                  <div className="flex flex-wrap gap-1.5">
                    {pricePresets.map((preset) => {
                      const active = priceRange[1] === preset.value && priceRange[0] === 0

                      return (
                        <button
                          key={preset.label}
                          type="button"
                          onClick={() => setPriceRange([0, preset.value])}
                          className={cn("marketplace-filter-price-preset", active && "marketplace-filter-price-preset-active")}
                        >
                          {preset.label}
                        </button>
                      )
                    })}
                  </div>

                  <Slider value={priceRange} onValueChange={setPriceRange} min={0} max={PRICE_MAX} step={PRICE_STEP} className="w-full" />

                  <div className="flex items-center justify-between gap-3 text-sm">
                    <span className="marketplace-filter-value">₹{formatIndianNumber(priceRange[0])}</span>

                    <span className="text-muted-foreground">to</span>

                    <span className="marketplace-filter-value">₹{formatIndianNumber(priceRange[1])}</span>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-muted-foreground">
                    <span>₹0</span>
                    <span>₹25,00,000+</span>
                  </div>
                </div>
              </FilterSection>
            </div>

            <div className="marketplace-filter-footer">
              <Button className="cta-primary h-12 w-full rounded-xl font-semibold" onClick={handleApply}>
                <SlidersHorizontal className="mr-2 h-4 w-4" />
                Show results
              </Button>
            </div>
          </motion.aside>
        </>
      )}
    </AnimatePresence>
  )
}

function FilterSection({
  title,
  expanded,
  onToggle,
  children,
}: {
  title: string
  expanded: boolean
  onToggle: () => void
  children: React.ReactNode
}) {
  return (
    <section className="marketplace-filter-section">
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center justify-between text-left text-sm font-semibold text-foreground"
      >
        {title}

        <ChevronDown className={cn("h-4 w-4 text-muted-foreground transition-transform", expanded && "rotate-180")} />
      </button>

      <AnimatePresence initial={false}>
        {expanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.18 }}
            className="overflow-hidden"
          >
            {children}
          </motion.div>
        )}
      </AnimatePresence>
    </section>
  )
}

export function FloatingFilterButton({ onClick, activeCount }: { onClick: () => void; activeCount: number }) {
  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="fixed bottom-6 right-6 z-40">
      <Button
        onClick={onClick}
        size="lg"
        className="cta-primary h-14 rounded-2xl px-6 font-semibold shadow-[0_16px_40px_var(--marketplace-filter-shadow)]"
      >
        <SlidersHorizontal className="mr-2 h-5 w-5" />
        Filters
        {activeCount > 0 && (
          <span className="ml-2 flex h-6 min-w-6 items-center justify-center rounded-full border border-foreground/15 bg-background/20 px-1.5 text-xs font-bold text-primary-foreground">
            {activeCount}
          </span>
        )}
      </Button>
    </motion.div>
  )
}

function formatEnum(value: string) {
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function formatIndianNumber(value: number) {
  return value.toLocaleString("en-IN")
}

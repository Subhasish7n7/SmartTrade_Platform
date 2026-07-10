"use client"

import * as React from "react"
import { motion, AnimatePresence } from "framer-motion"
import { 
  Filter, 
  X, 
  ChevronDown,
  Laptop,
  Gamepad2,
  Camera,
  Smartphone,
  Headphones,
  Bike,
  Monitor,
  Watch,
  SlidersHorizontal
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Slider } from "@/components/ui/slider"
import { cn } from "@/lib/utils"

const categories = [
  { icon: Laptop, label: "Gaming Laptops", value: "laptops" },
  { icon: Gamepad2, label: "Collectibles", value: "collectibles" },
  { icon: Camera, label: "Cameras", value: "cameras" },
  { icon: Smartphone, label: "Phones", value: "phones" },
  { icon: Headphones, label: "Audio", value: "audio" },
  { icon: Bike, label: "Bikes", value: "bikes" },
  { icon: Monitor, label: "Monitors", value: "monitors" },
  { icon: Watch, label: "Watches", value: "watches" },
]

const conditions = [
  { label: "Factory New", value: "factory-new", color: "text-emerald-400" },
  { label: "Minimal Wear", value: "minimal-wear", color: "text-teal-400" },
  { label: "Field Tested", value: "field-tested", color: "text-amber-400" },
  { label: "Well Worn", value: "well-worn", color: "text-orange-400" },
  { label: "Battle Scarred", value: "battle-scarred", color: "text-red-400" },
]

const listingTypes = [
  { label: "All Listings", value: "all" },
  { label: "For Sale", value: "sale" },
  { label: "For Trade", value: "trade" },
  { label: "Trade or Sale", value: "both" },
]

const distances = [
  { label: "5 miles", value: 5 },
  { label: "10 miles", value: 10 },
  { label: "25 miles", value: 25 },
  { label: "50 miles", value: 50 },
  { label: "100 miles", value: 100 },
  { label: "Anywhere", value: -1 },
]

interface FilterPanelProps {
  isOpen: boolean
  onClose: () => void
  onApply?: () => void
}

export function FilterPanel({ isOpen, onClose, onApply }: FilterPanelProps) {
  const [selectedCategory, setSelectedCategory] = React.useState<string | null>(null)
  const [selectedConditions, setSelectedConditions] = React.useState<string[]>([])
  const [selectedListingType, setSelectedListingType] = React.useState("all")
  const [selectedDistance, setSelectedDistance] = React.useState(25)
  const [priceRange, setPriceRange] = React.useState([0, 5000])
  const [expandedSections, setExpandedSections] = React.useState({
    category: true,
    condition: true,
    listingType: true,
    distance: true,
    price: true,
  })

  const toggleSection = (section: keyof typeof expandedSections) => {
    setExpandedSections(prev => ({ ...prev, [section]: !prev[section] }))
  }

  const toggleCondition = (value: string) => {
    setSelectedConditions(prev => 
      prev.includes(value) 
        ? prev.filter(c => c !== value)
        : [...prev, value]
    )
  }

  const clearFilters = () => {
    setSelectedCategory(null)
    setSelectedConditions([])
    setSelectedListingType("all")
    setSelectedDistance(25)
    setPriceRange([0, 5000])
  }

  const activeFiltersCount = 
    (selectedCategory ? 1 : 0) + 
    selectedConditions.length + 
    (selectedListingType !== "all" ? 1 : 0) + 
    (selectedDistance !== 25 ? 1 : 0) +
    (priceRange[0] !== 0 || priceRange[1] !== 5000 ? 1 : 0)

  const handleApply = () => {
    onApply?.()
    onClose()
  }

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50"
            onClick={onClose}
          />
          
          {/* Panel */}
          <motion.div
            initial={{ x: "100%" }}
            animate={{ x: 0 }}
            exit={{ x: "100%" }}
            transition={{ type: "spring", damping: 30, stiffness: 300 }}
            className="fixed right-0 top-0 bottom-0 w-full sm:w-96 max-w-full z-50 flex flex-col bg-background border-l border-border"
          >
            {/* Header */}
            <div className="flex items-center justify-between p-4 border-b border-border">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-xl bg-primary/10 flex items-center justify-center">
                  <Filter className="h-5 w-5 text-primary" />
                </div>
                <div>
                  <h2 className="font-semibold text-foreground">Filters</h2>
                  {activeFiltersCount > 0 && (
                    <p className="text-xs text-muted-foreground">{activeFiltersCount} active</p>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2">
                {activeFiltersCount > 0 && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearFilters}
                    className="text-muted-foreground hover:text-foreground"
                  >
                    Clear all
                  </Button>
                )}
                <Button variant="ghost" size="icon" onClick={onClose}>
                  <X className="h-5 w-5" />
                </Button>
              </div>
            </div>

            {/* Scrollable Content */}
            <div className="flex-1 overflow-y-auto p-4 space-y-6 scrollbar-thin">
              {/* Category */}
              <div className="space-y-3">
                <button
                  onClick={() => toggleSection("category")}
                  className="flex items-center justify-between w-full text-sm font-medium text-foreground"
                >
                  Category
                  <ChevronDown className={cn(
                    "h-4 w-4 text-muted-foreground transition-transform duration-200",
                    expandedSections.category && "rotate-180"
                  )} />
                </button>
                <AnimatePresence>
                  {expandedSections.category && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="grid grid-cols-2 gap-2 pt-1">
                        {categories.map((cat) => (
                          <button
                            key={cat.value}
                            onClick={() => setSelectedCategory(selectedCategory === cat.value ? null : cat.value)}
                            className={cn(
                              "flex items-center gap-2 px-3 py-2.5 rounded-xl text-xs font-medium transition-all",
                              selectedCategory === cat.value
                                ? "bg-primary/20 text-primary border border-primary/30"
                                : "bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground border border-transparent"
                            )}
                          >
                            <cat.icon className="h-4 w-4" />
                            <span className="truncate">{cat.label}</span>
                          </button>
                        ))}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* Condition */}
              <div className="space-y-3">
                <button
                  onClick={() => toggleSection("condition")}
                  className="flex items-center justify-between w-full text-sm font-medium text-foreground"
                >
                  Condition
                  <ChevronDown className={cn(
                    "h-4 w-4 text-muted-foreground transition-transform duration-200",
                    expandedSections.condition && "rotate-180"
                  )} />
                </button>
                <AnimatePresence>
                  {expandedSections.condition && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="space-y-2 pt-1">
                        {conditions.map((condition) => (
                          <button
                            key={condition.value}
                            onClick={() => toggleCondition(condition.value)}
                            className={cn(
                              "flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-sm transition-all",
                              selectedConditions.includes(condition.value)
                                ? "bg-muted/80 border border-border/80"
                                : "hover:bg-muted/50 border border-transparent"
                            )}
                          >
                            <div className={cn(
                              "h-5 w-5 rounded border-2 flex items-center justify-center transition-colors",
                              selectedConditions.includes(condition.value)
                                ? "border-primary bg-primary"
                                : "border-muted-foreground"
                            )}>
                              {selectedConditions.includes(condition.value) && (
                                <svg className="h-3 w-3 text-primary-foreground" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                                </svg>
                              )}
                            </div>
                            <span className={cn("font-medium", condition.color)}>{condition.label}</span>
                          </button>
                        ))}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* Listing Type */}
              <div className="space-y-3">
                <button
                  onClick={() => toggleSection("listingType")}
                  className="flex items-center justify-between w-full text-sm font-medium text-foreground"
                >
                  Listing Type
                  <ChevronDown className={cn(
                    "h-4 w-4 text-muted-foreground transition-transform duration-200",
                    expandedSections.listingType && "rotate-180"
                  )} />
                </button>
                <AnimatePresence>
                  {expandedSections.listingType && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="flex flex-wrap gap-2 pt-1">
                        {listingTypes.map((type) => (
                          <button
                            key={type.value}
                            onClick={() => setSelectedListingType(type.value)}
                            className={cn(
                              "px-4 py-2 rounded-full text-sm font-medium transition-all",
                              selectedListingType === type.value
                                ? "bg-primary text-primary-foreground"
                                : "bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground"
                            )}
                          >
                            {type.label}
                          </button>
                        ))}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* Distance */}
              <div className="space-y-3">
                <button
                  onClick={() => toggleSection("distance")}
                  className="flex items-center justify-between w-full text-sm font-medium text-foreground"
                >
                  Distance
                  <ChevronDown className={cn(
                    "h-4 w-4 text-muted-foreground transition-transform duration-200",
                    expandedSections.distance && "rotate-180"
                  )} />
                </button>
                <AnimatePresence>
                  {expandedSections.distance && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="flex flex-wrap gap-2 pt-1">
                        {distances.map((dist) => (
                          <button
                            key={dist.value}
                            onClick={() => setSelectedDistance(dist.value)}
                            className={cn(
                              "px-4 py-2 rounded-full text-sm font-medium transition-all",
                              selectedDistance === dist.value
                                ? "bg-primary text-primary-foreground"
                                : "bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground"
                            )}
                          >
                            {dist.label}
                          </button>
                        ))}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* Price Range */}
              <div className="space-y-3">
                <button
                  onClick={() => toggleSection("price")}
                  className="flex items-center justify-between w-full text-sm font-medium text-foreground"
                >
                  Price Range
                  <ChevronDown className={cn(
                    "h-4 w-4 text-muted-foreground transition-transform duration-200",
                    expandedSections.price && "rotate-180"
                  )} />
                </button>
                <AnimatePresence>
                  {expandedSections.price && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2 }}
                      className="overflow-hidden"
                    >
                      <div className="space-y-4 pt-2">
                        <Slider
                          value={priceRange}
                          onValueChange={setPriceRange}
                          max={5000}
                          step={50}
                          className="w-full"
                        />
                        <div className="flex items-center justify-between text-sm">
                          <span className="px-3 py-1.5 rounded-lg bg-muted/50 text-muted-foreground font-medium">
                            ${priceRange[0].toLocaleString()}
                          </span>
                          <span className="text-muted-foreground">to</span>
                          <span className="px-3 py-1.5 rounded-lg bg-muted/50 text-muted-foreground font-medium">
                            ${priceRange[1].toLocaleString()}
                          </span>
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>
            </div>

            {/* Footer */}
            <div className="p-4 border-t border-border bg-background">
              <Button 
                className="w-full h-12 bg-primary hover:bg-primary/90 text-primary-foreground font-semibold rounded-xl"
                onClick={handleApply}
              >
                Show Results
              </Button>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  )
}

// Floating Filter Button - used across all screen sizes
export function FloatingFilterButton({ onClick, activeCount }: { onClick: () => void; activeCount: number }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      className="fixed bottom-6 right-6 z-40"
    >
      <Button
        onClick={onClick}
        size="lg"
        className="cta-primary h-14 px-6 rounded-2xl font-medium"
      >
        <SlidersHorizontal className="h-5 w-5 mr-2" />
        Filters
        {activeCount > 0 && (
          <span className="ml-2 h-6 w-6 rounded-full bg-primary text-primary-foreground text-xs flex items-center justify-center font-bold">
            {activeCount}
          </span>
        )}
      </Button>
    </motion.div>
  )
}

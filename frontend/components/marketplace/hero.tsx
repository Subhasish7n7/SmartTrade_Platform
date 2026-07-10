"use client"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { cn } from "@/lib/utils"
import { motion } from "framer-motion"
import { Bike, Camera, Gamepad2, Headphones, Laptop, MapPin, Search, Smartphone, TrendingUp } from "lucide-react"

const trendingCategories = [
  { icon: Laptop, label: "Gaming Laptops", count: "2.4k" },
  { icon: Gamepad2, label: "Collectibles", count: "1.8k" },
  { icon: Camera, label: "Cameras", count: "956" },
  { icon: Smartphone, label: "Phones", count: "3.2k" },
  { icon: Headphones, label: "Audio", count: "1.1k" },
  { icon: Bike, label: "Bikes", count: "780" },
]

export function Hero() {
  return (
    <section className="relative pt-24 pb-12 px-4 overflow-hidden">
      {/* Background Effects */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-20 left-1/4 w-96 h-96 bg-primary/10 rounded-full blur-3xl" />
        <div className="absolute bottom-0 right-1/4 w-80 h-80 bg-info/10 rounded-full blur-3xl" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-gradient-radial from-primary/5 to-transparent rounded-full" />
      </div>

      <div className="relative mx-auto max-w-7xl">
        {/* Main Hero Content */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="text-center mb-10"
        >
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold mb-4 text-balance">
            Trade Smarter. <span className="text-gradient">Deal Better.</span>
          </h1>
          <p className="text-lg text-muted-foreground max-w-2xl mx-auto mb-8 text-pretty">
            Join thousands of traders in the most trusted marketplace for gaming gear, electronics, and collectibles. AI-powered pricing
            helps you get fair deals.
          </p>

          {/* Search Bar */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="max-w-2xl mx-auto mb-6"
          >
            <div className="relative glass-strong rounded-2xl p-2">
              <div className="flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                  <Input
                    type="search"
                    placeholder="Search for items, brands, or traders..."
                    className="w-full h-12 pl-12 pr-4 bg-muted/50 border-none rounded-xl text-base placeholder:text-muted-foreground/70"
                  />
                </div>
                <Button className="h-12 px-6 rounded-xl bg-primary hover:bg-primary/90 text-primary-foreground font-semibold">
                  Search
                </Button>
              </div>
            </div>
          </motion.div>

          {/* Location Badge */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full glass-subtle text-sm text-muted-foreground"
          >
            <MapPin className="h-4 w-4 text-primary" />
            <span>Showing items near</span>
            <span className="font-medium text-foreground">San Francisco, CA</span>
            <button className="text-primary hover:underline">Change</button>
          </motion.div>
        </motion.div>

        {/* Trending Categories */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5, delay: 0.3 }}>
          <div className="flex items-center justify-center gap-2 mb-4">
            <TrendingUp className="h-4 w-4 text-primary" />
            <span className="text-sm font-medium text-muted-foreground">Trending Categories</span>
          </div>
          <div className="flex flex-wrap justify-center gap-3">
            {trendingCategories.map((category, index) => (
              <motion.button
                key={category.label}
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3, delay: 0.4 + index * 0.05 }}
                className={cn(
                  "flex items-center gap-2 px-4 py-2.5 rounded-xl",
                  "glass hover:glass-strong hover:border-primary/30",
                  "transition-all duration-300 group",
                )}
              >
                <category.icon className="h-4 w-4 text-muted-foreground group-hover:text-primary transition-colors" />
                <span className="text-sm font-medium text-foreground">{category.label}</span>
                <span className="text-xs text-muted-foreground bg-muted/50 px-2 py-0.5 rounded-full">{category.count}</span>
              </motion.button>
            ))}
          </div>
        </motion.div>

        {/* Stats */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
          className="mt-12 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-3xl mx-auto"
        >
          {[
            { value: "50K+", label: "Active Traders" },
            { value: "120K+", label: "Items Listed" },
            { value: "98%", label: "Satisfaction Rate" },
            { value: "$2M+", label: "Traded Monthly" },
          ].map((stat, index) => (
            <div key={stat.label} className="text-center p-4 rounded-xl glass-subtle">
              <div className="text-2xl sm:text-3xl font-bold text-gradient">{stat.value}</div>
              <div className="text-xs sm:text-sm text-muted-foreground">{stat.label}</div>
            </div>
          ))}
        </motion.div>
      </div>
    </section>
  )
}

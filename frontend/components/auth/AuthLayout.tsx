"use client"

import { ArrowLeftRight, ShieldCheck, Sparkles } from "lucide-react"

import { motion } from "framer-motion"
import { AuthFeature } from "./AuthFeature"

interface Props {
  title: string
  subtitle: string
  children: React.ReactNode
}

export function AuthLayout({ title, subtitle, children }: Props) {
  return (
    <main className="relative min-h-screen overflow-hidden flex items-center py-20">
      <div className="absolute inset-0 -z-10">
        <div
          className="
      absolute
      left-[-10%]
      top-[10%]
      h-96
      w-96
      rounded-full
      blur-[140px]
      bg-[var(--trust-blue)]/15
    "
        />

        <div
          className="
      absolute
      right-[-10%]
      bottom-[10%]
      h-96
      w-96
      rounded-full
      blur-[140px]
      bg-[var(--negotiation-violet)]/15
    "
        />
      </div>

      <div className="mx-auto max-w-7xl w-full px-6">
        <div className="grid lg:grid-cols-2 gap-14 items-center">
          <div className="hidden lg:block">
            <h1 className="text-6xl font-bold bg-gradient-to-r from-[var(--trust-blue)] via-[var(--negotiation-violet)] to-[var(--premium-gold)] bg-clip-text text-transparent">
              SmartTrade
            </h1>

            <p className="mt-6 text-lg text-muted-foreground max-w-lg">
              Trade confidently with AI-assisted pricing, secure negotiations and a premium marketplace experience.
            </p>

            <div className="space-y-4 mt-10">
              <AuthFeature icon={ShieldCheck} title="Secure Authentication" description="JWT protected accounts and secure trading." />

              <AuthFeature icon={ArrowLeftRight} title="Safe Trading" description="Negotiate and exchange items with confidence." />

              <AuthFeature icon={Sparkles} title="AI Fair Pricing" description="Know whether every trade is balanced." />
            </div>
          </div>

          <div>
            <motion.div
              initial={{ opacity: 0, y: 25 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.45 }}
              className="content-surface rounded-3xl border border-border/60 backdrop-blur-xl shadow-2xl p-8"
            >
              <h2 className="text-3xl font-bold">{title}</h2>

              <p className="text-muted-foreground mt-2">{subtitle}</p>

              <div className="mt-8">{children}</div>
            </motion.div>
          </div>
        </div>
      </div>
    </main>
  )
}

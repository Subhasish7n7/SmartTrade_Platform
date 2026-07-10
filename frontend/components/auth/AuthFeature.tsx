"use client"

import { motion } from "framer-motion"
import { LucideIcon } from "lucide-react"

interface Props {
  icon: LucideIcon
  title: string
  description: string
}

export function AuthFeature({
  icon: Icon,
  title,
  description,
}: Props) {

  return (

    <motion.div
      whileHover={{ y: -4 }}
      className="content-surface p-5"
    >

      <div className="flex gap-4">

        <div className="trade-icon">

          <Icon
            className="h-5 w-5 text-primary"
          />

        </div>

        <div>

          <h3 className="font-semibold">

            {title}

          </h3>

          <p className="text-sm text-muted-foreground mt-1">

            {description}

          </p>

        </div>

      </div>

    </motion.div>

  )

}
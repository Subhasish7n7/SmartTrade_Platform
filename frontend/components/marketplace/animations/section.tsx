// components/marketplace/animations/section.tsx

"use client";

import { motion } from "framer-motion";
import { fadeUp, pageContainer } from "./variants";

export function AnimatedPage({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <motion.div
      variants={pageContainer}
      initial="hidden"
      animate="visible"
    >
      {children}
    </motion.div>
  );
}

export function Section({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <motion.div
      variants={fadeUp}
      className={className}
    >
      {children}
    </motion.div>
  );
}
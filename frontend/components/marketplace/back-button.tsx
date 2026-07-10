"use client";

import { ArrowLeft } from "lucide-react";
import { useRouter } from "next/navigation";

export function BackButton() {
  const router = useRouter();

  return (
    <button
      onClick={() => router.back()}
      className="inline-flex items-center gap-2 rounded-xl mb-2 px-3 py-2 text-sm text-muted-foreground cursor-pointer transition-colors hover:text-foreground hover:bg-muted/50"
    >
      <ArrowLeft className="h-4 w-4" />
      Back
    </button>
  );
}
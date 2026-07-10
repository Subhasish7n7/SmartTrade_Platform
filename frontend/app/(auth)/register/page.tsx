"use client";

import { useEffect } from "react";

import Link from "next/link";

import { useRouter } from "next/navigation";

import { ArrowLeft } from "lucide-react";

import {
  AnimatedPage,
  Section,
} from "@/components/marketplace/animations/section";

import { RegisterForm } from "@/components/auth/RegisterForm";

import { AuthLayout } from "@/components/auth/AuthLayout";

import { useAuth } from "@/lib/auth/useAuth";

export default function RegisterPage() {
  const router = useRouter();

  const { authenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading && authenticated) {
      router.replace("/");
    }
  }, [authenticated, loading, router]);

  if (loading) return null;

  return (
    <AnimatedPage>
      <Section>
        <div className="absolute left-6 top-6 z-50">
          <Link
            href="/"
            className="inline-flex items-center gap-2 text-sm text-muted-foreground hover:text-foreground transition-colors"
          >
            <ArrowLeft className="h-4 w-4" />
            Browse Marketplace
          </Link>
        </div>
      </Section>

      <Section>
        <AuthLayout
          title="Create Account"
          subtitle="Join SmartTrade and start trading securely."
        >
          <RegisterForm />
        </AuthLayout>
      </Section>
    </AnimatedPage>
  );
}

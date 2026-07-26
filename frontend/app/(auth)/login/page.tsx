import { redirectIfAuthenticated } from "@/lib/auth/redirect-if-authenticated";

import { AuthLayout } from "@/components/auth/AuthLayout";
import { LoginForm } from "@/components/auth/LoginForm";

interface Props {
  searchParams: Promise<{
    redirect?: string;
  }>;
}

export default async function LoginPage({ searchParams }: Props) {
  await redirectIfAuthenticated();

  const { redirect = "/" } = await searchParams;

  return (
    <AuthLayout
      title="Welcome Back"
      subtitle="Sign in"
    >
      <LoginForm redirect={redirect} />
    </AuthLayout>
  );
}
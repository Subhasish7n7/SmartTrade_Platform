import { redirectIfAuthenticated } from "@/lib/auth/redirect-if-authenticated";

import { AuthLayout } from "@/components/auth/AuthLayout";
import { LoginForm } from "@/components/auth/LoginForm";

export default async function LoginPage() {
    await redirectIfAuthenticated();

    return (
        <AuthLayout
            title="Welcome Back"
            subtitle="Sign in"
        >
            <LoginForm />
        </AuthLayout>
    );
}
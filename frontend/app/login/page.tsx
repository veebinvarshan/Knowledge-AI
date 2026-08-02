"use client";

import AuthLayout from "../../features/authentication/components/AuthLayout";
import LoginForm from "../../features/authentication/components/LoginForm";
import { Suspense } from "react";

export default function LoginPage() {
  return (
    <AuthLayout>
      <Suspense fallback={<div className="text-slate-400 text-xs">Loading Secure Access...</div>}>
        <LoginForm />
      </Suspense>
    </AuthLayout>
  );
}

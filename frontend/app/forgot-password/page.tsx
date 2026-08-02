"use client";

import AuthLayout from "../../features/authentication/components/AuthLayout";
import RecoveryRequestForm from "../../features/authentication/components/RecoveryRequestForm";

export default function ForgotPasswordPage() {
  return (
    <AuthLayout>
      <RecoveryRequestForm />
    </AuthLayout>
  );
}

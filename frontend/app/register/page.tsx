"use client";

import AuthLayout from "../../features/authentication/components/AuthLayout";
import RegisterForm from "../../features/authentication/components/RegisterForm";

export default function RegisterPage() {
  return (
    <AuthLayout>
      <RegisterForm />
    </AuthLayout>
  );
}

"use client";

import { useState } from "react";
import { useAuthActions } from "../hooks/useAuthActions";
import PasswordField from "./PasswordField";
import AlertFeedback from "../../../components/feedback/AlertFeedback";
import { Mail, Globe, ArrowRight } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";

export default function LoginForm() {
  const { login, isLoading, error } = useAuthActions();
  const [tenant, setTenant] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  
  const router = useRouter();
  const searchParams = useSearchParams();
  const callbackUrl = searchParams.get("callbackUrl") || "/dashboard";

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSuccessMsg("");

    if (!tenant || !email || !password) {
      setFormError("All fields are required");
      return;
    }

    try {
      await login(email, password, tenant);
      setSuccessMsg("Authorization granted. Redirecting...");
      setTimeout(() => {
        router.push(callbackUrl);
      }, 1000);
    } catch (err: any) {
      setFormError(err.message || "Failed to authenticate session");
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h2 className="text-2xl font-bold tracking-tight text-slate-100">Enterprise Access</h2>
        <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
          Secure Identity Verification
        </p>
      </div>

      {(formError || error) && (
        <AlertFeedback type="error" message={formError || error || ""} onClose={() => setFormError(null)} />
      )}

      {successMsg && (
        <AlertFeedback type="success" message={successMsg} />
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Tenant/Domain input */}
        <div className="space-y-2">
          <label htmlFor="tenant" className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Organization Domain
          </label>
          <div className="relative">
            <div className="absolute left-3 top-3.5 text-slate-500">
              <Globe className="w-4 h-4" />
            </div>
            <input
              id="tenant"
              type="text"
              placeholder="acme-corp"
              value={tenant}
              onChange={(e) => setTenant(e.target.value)}
              className="w-full pl-10 pr-28 py-3 bg-slate-900/50 border border-slate-800 focus:border-sky-500/50 focus:ring-1 focus:ring-sky-500/50 rounded-lg text-sm transition-all outline-none"
              disabled={isLoading}
              required
            />
            <div className="absolute right-3 top-3.5 text-xs font-semibold text-slate-600">
              .theplatform.com
            </div>
          </div>
        </div>

        {/* Email input */}
        <div className="space-y-2">
          <label htmlFor="email" className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Corporate Email
          </label>
          <div className="relative">
            <div className="absolute left-3 top-3.5 text-slate-500">
              <Mail className="w-4 h-4" />
            </div>
            <input
              id="email"
              type="email"
              placeholder="user@acme-corp.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full pl-10 py-3 bg-slate-900/50 border border-slate-800 focus:border-sky-500/50 focus:ring-1 focus:ring-sky-500/50 rounded-lg text-sm transition-all outline-none"
              disabled={isLoading}
              required
            />
          </div>
        </div>

        {/* Password input */}
        <PasswordField
          label="Credentials Secret"
          id="password"
          placeholder="••••••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={isLoading}
          required
        />

        <div className="flex items-center justify-end text-xs">
          <Link
            href="/forgot-password"
            className="text-slate-500 hover:text-sky-400 transition-colors"
          >
            Forgot your password?
          </Link>
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-bold rounded-lg text-sm hover:opacity-95 shadow-lg shadow-sky-500/10 disabled:opacity-50 transition-all duration-200"
        >
          {isLoading ? (
            <span>Securing Connection...</span>
          ) : (
            <>
              <span>Log In</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      <div className="pt-4 border-t border-slate-900 text-center text-xs text-slate-500">
        Need to scaffold a new domain?{" "}
        <Link href="/register" className="text-sky-400 hover:underline">
          Register Organization
        </Link>
      </div>
    </div>
  );
}

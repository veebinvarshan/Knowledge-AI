"use client";

import { useState, useEffect, Suspense } from "react";
import { useAuthActions } from "../../features/authentication/hooks/useAuthActions";
import PasswordField from "../../features/authentication/components/PasswordField";
import AlertFeedback from "../../components/feedback/AlertFeedback";
import AuthLayout from "../../features/authentication/components/AuthLayout";
import { ArrowRight, Check, X, KeyRound } from "lucide-react";
import { useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";

function ResetPasswordForm() {
  const { resetPassword, isLoading, error } = useAuthActions();
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const token = searchParams.get("token") || "";
  const [newPassword, setNewPassword] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  // Password rules validation states
  const [rules, setRules] = useState({
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false,
  });

  useEffect(() => {
    setRules({
      length: newPassword.length >= 12 && newPassword.length <= 128,
      uppercase: /[A-Z]/.test(newPassword),
      lowercase: /[a-z]/.test(newPassword),
      number: /\d/.test(newPassword),
      special: /[@$!%*?&#]/.test(newPassword),
    });
  }, [newPassword]);

  const isPasswordValid = Object.values(rules).every(Boolean);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSuccessMsg("");

    if (!token) {
      setFormError("Reset token is missing from URL query parameter");
      return;
    }

    if (!newPassword) {
      setFormError("New password is required");
      return;
    }

    if (!isPasswordValid) {
      setFormError("Password does not meet complexity requirements");
      return;
    }

    try {
      await resetPassword(token, newPassword);
      setSuccessMsg("Master credential rotated successfully. Redirecting to access portal...");
      setTimeout(() => {
        router.push("/login");
      }, 2500);
    } catch (err: any) {
      setFormError(err.message || "Failed to reset password credentials");
    }
  };

  const RuleIndicator = ({ met, text }: { met: boolean; text: string }) => (
    <div className="flex items-center gap-1.5 text-[10px] transition-colors">
      {met ? (
        <Check className="w-3 h-3 text-emerald-500 shrink-0" />
      ) : (
        <X className="w-3 h-3 text-slate-600 shrink-0" />
      )}
      <span className={met ? "text-slate-400" : "text-slate-500"}>{text}</span>
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h2 className="text-2xl font-bold tracking-tight text-slate-100">Rotate Credentials</h2>
        <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
          Reset Identity Key
        </p>
      </div>

      {!token && (
        <AlertFeedback type="error" message="A security validation token was not found in the URL. Please verify your recovery link." />
      )}

      {(formError || error) && (
        <AlertFeedback type="error" message={formError || error || ""} onClose={() => setFormError(null)} />
      )}

      {successMsg && (
        <AlertFeedback type="success" message={successMsg} />
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Token display (readonly visual confirmation) */}
        <div className="space-y-2">
          <label className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Validation Token Hash
          </label>
          <div className="relative">
            <div className="absolute left-3 top-3.5 text-slate-500">
              <KeyRound className="w-4 h-4" />
            </div>
            <input
              type="text"
              value={token ? `${token.substring(0, 16)}...` : "No validation token loaded"}
              className="w-full pl-10 py-3 bg-slate-900/30 border border-slate-900 text-slate-500 rounded-lg text-sm outline-none cursor-not-allowed select-none"
              readOnly
            />
          </div>
        </div>

        {/* Password input */}
        <PasswordField
          label="New Master Credentials"
          id="newPassword"
          placeholder="••••••••••••"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          disabled={isLoading || !token}
          required
        />

        {/* Password Complexity Indicator */}
        <div className="p-3 bg-slate-900/20 border border-slate-900 rounded-lg space-y-2">
          <p className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
            Password Complexity Parameters
          </p>
          <div className="grid grid-cols-2 gap-2">
            <RuleIndicator met={rules.length} text="12-128 characters" />
            <RuleIndicator met={rules.uppercase} text="Uppercase letter" />
            <RuleIndicator met={rules.lowercase} text="Lowercase letter" />
            <RuleIndicator met={rules.number} text="Numeric digit" />
            <RuleIndicator met={rules.special} text="Special character (@$!%*?&#)" />
          </div>
        </div>

        <button
          type="submit"
          disabled={isLoading || !token || !isPasswordValid}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-bold rounded-lg text-sm hover:opacity-95 shadow-lg shadow-sky-500/10 disabled:opacity-50 transition-all duration-200"
        >
          {isLoading ? (
            <span>Rotating Secret...</span>
          ) : (
            <>
              <span>Rotate Credentials Master</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      <div className="pt-4 border-t border-slate-900 text-center text-xs text-slate-500">
        Back to Access Portal?{" "}
        <Link href="/login" className="text-sky-400 hover:underline">
          Log In
        </Link>
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <AuthLayout>
      <Suspense fallback={<div className="text-slate-400 text-xs">Loading Security Context...</div>}>
        <ResetPasswordForm />
      </Suspense>
    </AuthLayout>
  );
}

"use client";

import { useState } from "react";
import { useAuthActions } from "../hooks/useAuthActions";
import AlertFeedback from "../../../components/feedback/AlertFeedback";
import { Mail, ArrowRight, ArrowLeft } from "lucide-react";
import Link from "next/link";

export default function RecoveryRequestForm() {
  const { forgotPassword, isLoading, error } = useAuthActions();
  const [email, setEmail] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [formError, setFormError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSuccessMsg("");

    if (!email) {
      setFormError("Corporate email is required");
      return;
    }

    try {
      await forgotPassword(email);
      setSuccessMsg("If this account exists, a recovery token link has been dispatched to your mailbox.");
    } catch (err: any) {
      setFormError(err.message || "Failed to process recovery request");
    }
  };

  return (
    <div className="space-y-6">
      <div className="space-y-2">
        <h2 className="text-2xl font-bold tracking-tight text-slate-100">Recover Credentials</h2>
        <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
          Credential Recovery Portal
        </p>
      </div>

      {(formError || error) && (
        <AlertFeedback type="error" message={formError || error || ""} onClose={() => setFormError(null)} />
      )}

      {successMsg && (
        <AlertFeedback type="success" message={successMsg} />
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Email input */}
        <div className="space-y-2">
          <label htmlFor="email" className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            Registered Corporate Email
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

        <button
          type="submit"
          disabled={isLoading}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-bold rounded-lg text-sm hover:opacity-95 shadow-lg shadow-sky-500/10 disabled:opacity-50 transition-all duration-200"
        >
          {isLoading ? (
            <span>Processing Dispatch...</span>
          ) : (
            <>
              <span>Dispatch Recovery Token</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      <div className="pt-4 border-t border-slate-900 flex items-center justify-between text-xs text-slate-500">
        <Link href="/login" className="flex items-center gap-1.5 hover:text-slate-300 transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to Login
        </Link>
      </div>
    </div>
  );
}

"use client";

import { useState, useEffect } from "react";
import { useAuthActions } from "../hooks/useAuthActions";
import PasswordField from "./PasswordField";
import AlertFeedback from "../../../components/feedback/AlertFeedback";
import { Mail, Globe, ArrowRight, Check, X } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function RegisterForm() {
  const { register: registerAction, isLoading, error } = useAuthActions();
  const [tenant, setTenant] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  
  const router = useRouter();

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
      length: password.length >= 12 && password.length <= 128,
      uppercase: /[A-Z]/.test(password),
      lowercase: /[a-z]/.test(password),
      number: /\d/.test(password),
      special: /[@$!%*?&#]/.test(password),
    });
  }, [password]);

  const isPasswordValid = Object.values(rules).every(Boolean);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);
    setSuccessMsg("");

    if (!tenant || !email || !password) {
      setFormError("All fields are required");
      return;
    }

    if (!isPasswordValid) {
      setFormError("Password does not meet the complexity requirements");
      return;
    }

    try {
      await registerAction(email, password, tenant);
      setSuccessMsg("Scaffolding complete. Redirecting to login portal...");
      setTimeout(() => {
        router.push("/login");
      }, 2500);
    } catch (err: any) {
      setFormError(err.message || "Failed to register organization");
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
        <h2 className="text-2xl font-bold tracking-tight text-slate-100">Scaffold Domain</h2>
        <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
          Create Organization Identity Node
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
            Requested Domain Prefix
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
            Owner Corporate Email
          </label>
          <div className="relative">
            <div className="absolute left-3 top-3.5 text-slate-500">
              <Mail className="w-4 h-4" />
            </div>
            <input
              id="email"
              type="email"
              placeholder="admin@acme-corp.com"
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
          label="Identity Master Credentials"
          id="password"
          placeholder="••••••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={isLoading}
          required
        />

        {/* Password Complexity Visual Indicator */}
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
          disabled={isLoading || !isPasswordValid}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-bold rounded-lg text-sm hover:opacity-95 shadow-lg shadow-sky-500/10 disabled:opacity-50 transition-all duration-200"
        >
          {isLoading ? (
            <span>Provisioning System...</span>
          ) : (
            <>
              <span>Initialize Scaffolding</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </form>

      <div className="pt-4 border-t border-slate-900 text-center text-xs text-slate-500">
        Already have a tenant domain?{" "}
        <Link href="/login" className="text-sky-400 hover:underline">
          Access Portal
        </Link>
      </div>
    </div>
  );
}

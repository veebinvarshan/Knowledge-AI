"use client";

import { useEffect, useState, Suspense } from "react";
import { useAuthActions } from "../../features/authentication/hooks/useAuthActions";
import AlertFeedback from "../../components/feedback/AlertFeedback";
import AuthLayout from "../../features/authentication/components/AuthLayout";
import { ArrowLeft, CheckCircle, ShieldAlert, ShieldCheck } from "lucide-react";
import { useSearchParams, useRouter } from "next/navigation";
import Link from "next/link";
import { motion } from "framer-motion";

function VerifyEmailForm() {
  const { verifyEmail, error } = useAuthActions();
  const searchParams = useSearchParams();
  const router = useRouter();
  
  const token = searchParams.get("token") || "";
  const [status, setStatus] = useState<"verifying" | "success" | "error">("verifying");
  const [errorMsg, setErrorMsg] = useState("");

  useEffect(() => {
    if (!token) {
      setStatus("error");
      setErrorMsg("A security verification token was not provided in the URL.");
      return;
    }

    let active = true;

    async function executeVerification() {
      try {
        await verifyEmail(token);
        if (active) {
          setStatus("success");
          setTimeout(() => {
            router.push("/login");
          }, 3000);
        }
      } catch (err: any) {
        if (active) {
          setStatus("error");
          setErrorMsg(err.message || "Email verification validation failed.");
        }
      }
    }

    executeVerification();

    return () => {
      active = false;
    };
  }, [token, verifyEmail, router]);

  return (
    <div className="space-y-6 text-center">
      <div className="space-y-2">
        <h2 className="text-2xl font-bold tracking-tight text-slate-100">Identity Verification</h2>
        <p className="text-xs text-slate-500 uppercase tracking-widest font-semibold">
          Tenant Validation Pipeline
        </p>
      </div>

      {status === "verifying" && (
        <motion.div 
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          className="flex flex-col items-center py-6 space-y-4"
        >
          <div className="relative w-12 h-12">
            <div className="absolute inset-0 rounded-full border-4 border-slate-800" />
            <div className="absolute inset-0 rounded-full border-4 border-t-sky-400 border-r-transparent border-b-transparent border-l-transparent animate-spin" />
          </div>
          <p className="text-xs text-slate-400">Verifying signature key with database registries...</p>
        </motion.div>
      )}

      {status === "success" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="flex flex-col items-center py-6 space-y-4"
        >
          <div className="w-16 h-16 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 flex items-center justify-center animate-bounce">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <AlertFeedback type="success" message="Verification successful! Redirecting to credentials portal..." />
        </motion.div>
      )}

      {status === "error" && (
        <motion.div 
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="flex flex-col items-center py-6 space-y-4"
        >
          <div className="w-16 h-16 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-500 flex items-center justify-center">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <AlertFeedback type="error" message={errorMsg || error || "Verification failed."} />
          
          <div className="pt-4 w-full">
            <Link
              href="/login"
              className="inline-flex w-full items-center justify-center gap-2 py-3 bg-slate-900 border border-slate-800 text-slate-300 font-semibold rounded-lg text-xs hover:bg-slate-800 transition-all duration-200"
            >
              <ArrowLeft className="w-4 h-4" /> Back to Login
            </Link>
          </div>
        </motion.div>
      )}
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <AuthLayout>
      <Suspense fallback={<div className="text-slate-400 text-xs">Loading Security Context...</div>}>
        <VerifyEmailForm />
      </Suspense>
    </AuthLayout>
  );
}

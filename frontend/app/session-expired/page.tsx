"use client";

import Link from "next/link";
import { ShieldAlert, KeyRound, LogIn } from "lucide-react";
import { motion } from "framer-motion";

export default function SessionExpiredPage() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-950 text-slate-100 items-center justify-center px-6 selection:bg-sky-500 selection:text-slate-900">
      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md p-8 rounded-xl border border-slate-900 bg-slate-950/40 shadow-2xl backdrop-blur-md text-center space-y-6"
      >
        <div className="flex flex-col items-center">
          <div className="w-16 h-16 rounded-full bg-red-500/10 flex items-center justify-center border border-red-500/20 text-red-500 mb-4 animate-pulse">
            <ShieldAlert className="w-8 h-8" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Session Expired</h2>
          <p className="text-xs text-slate-500 mt-1 uppercase tracking-widest font-semibold">
            Security Isolation Active
          </p>
        </div>

        <p className="text-xs text-slate-400 leading-relaxed">
          Your active session lease has expired or the token signature has been revoked due to inactivity. For your protection, credentials must be re-validated.
        </p>

        <div className="pt-2">
          <Link
            href="/login"
            className="inline-flex w-full items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-bold rounded-lg text-sm hover:opacity-95 shadow-lg shadow-sky-500/10 transition-all duration-200"
          >
            <LogIn className="w-4 h-4" /> Re-authenticate Session
          </Link>
        </div>

        <div className="pt-4 border-t border-slate-900 flex items-center gap-2 justify-center text-[10px] text-slate-600">
          <KeyRound className="w-3.5 h-3.5" /> Enforced security compliance via RSA token rotation.
        </div>
      </motion.div>
    </div>
  );
}

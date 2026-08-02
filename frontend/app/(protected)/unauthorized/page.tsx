"use client";

import Link from "next/link";
import { ShieldAlert, ArrowLeft, Lock } from "lucide-react";
import { motion } from "framer-motion";

export default function UnauthorizedPage() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-950 text-slate-100 items-center justify-center px-6 selection:bg-sky-500 selection:text-slate-900">
      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.3 }}
        className="w-full max-w-md p-8 rounded-xl border border-slate-900 bg-slate-950/40 shadow-2xl backdrop-blur-md text-center space-y-6"
      >
        <div className="flex flex-col items-center">
          <div className="w-16 h-16 rounded-full bg-rose-500/10 flex items-center justify-center border border-rose-500/20 text-rose-500 mb-4 animate-pulse">
            <Lock className="w-8 h-8" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight">Access Denied</h2>
          <p className="text-xs text-rose-500 mt-1 uppercase tracking-widest font-semibold">
            Status Code 403: Forbidden
          </p>
        </div>

        <p className="text-xs text-slate-400 leading-relaxed">
          You do not have the required permissions or role clearances to view the requested resource. If you believe this is an error, please contact your security administrator.
        </p>

        <div className="pt-2 flex flex-col gap-2">
          <Link
            href="/dashboard"
            className="inline-flex w-full items-center justify-center gap-2 py-3 bg-slate-900 border border-slate-800 text-slate-300 font-semibold rounded-lg text-xs hover:bg-slate-800 transition-all duration-200"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Dashboard
          </Link>
        </div>

        <div className="pt-4 border-t border-slate-900 flex items-center gap-2 justify-center text-[10px] text-slate-600">
          <ShieldAlert className="w-3.5 h-3.5" /> Request flagged by platform RBAC monitoring.
        </div>
      </motion.div>
    </div>
  );
}

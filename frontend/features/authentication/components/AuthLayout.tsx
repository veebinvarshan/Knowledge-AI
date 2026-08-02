"use client";

import React from "react";
import { Shield } from "lucide-react";
import { motion } from "framer-motion";

export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100 selection:bg-sky-500 selection:text-slate-900">
      {/* Graphic Left Panel (Visible on Desktop) */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-slate-900 border-r border-slate-800 flex-col justify-between p-12 overflow-hidden">
        {/* Subtle decorative mesh background */}
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top_right,rgba(14,165,233,0.1),transparent_50%),radial-gradient(ellipse_at_bottom_left,rgba(99,102,241,0.08),transparent_50%)]" />
        
        {/* Animated accent ring */}
        <div className="absolute -top-40 -left-40 w-96 h-96 rounded-full border border-sky-500/10 blur-xl animate-pulse" />
        
        <div className="relative z-10 flex items-center gap-3">
          <div className="w-10 h-10 rounded-lg bg-gradient-to-tr from-sky-400 to-indigo-600 flex items-center justify-center font-black text-slate-950 text-lg">
            K
          </div>
          <span className="font-bold tracking-tight text-slate-200">KNOWLEDGE PLATFORM</span>
        </div>

        <div className="relative z-10 space-y-6">
          <motion.h1 
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="text-4xl font-extrabold tracking-tight leading-tight text-slate-100"
          >
            Securing Enterprise <br />
            <span className="bg-gradient-to-r from-sky-400 to-indigo-400 bg-clip-text text-transparent">
              Intelligence Nodes
            </span>
          </motion.h1>
          <motion.p 
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="text-sm text-slate-400 leading-relaxed max-w-md"
          >
            Access our isolated semantic search indexes and orchestrated retrieval augmentations with multi-tenant zero-trust validation.
          </motion.p>
        </div>

        <div className="relative z-10 flex items-center gap-2 text-xs text-slate-500">
          <Shield className="w-4 h-4 text-sky-500" />
          <span>FIPS 140-2 Compliant JWT Isolation Layers</span>
        </div>
      </div>

      {/* Interactive Right Panel */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-6 sm:p-12 relative">
        {/* Mesh on mobile */}
        <div className="lg:hidden absolute inset-0 bg-[radial-gradient(ellipse_at_top,rgba(14,165,233,0.08),transparent_50%)]" />
        
        <div className="w-full max-w-md relative z-10">
          {children}
        </div>
      </div>
    </div>
  );
}

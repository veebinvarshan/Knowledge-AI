"use client";

import { motion, AnimatePresence } from "framer-motion";

interface LoadingOverlayProps {
  isVisible: boolean;
  message?: string;
}

export default function LoadingOverlay({ isVisible, message = "Processing authentication secure channel..." }: LoadingOverlayProps) {
  return (
    <AnimatePresence>
      {isVisible && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-slate-950/80 backdrop-blur-md"
        >
          <div className="relative flex flex-col items-center space-y-4">
            {/* Outer gradient rotating ring */}
            <div className="relative w-16 h-16">
              <div className="absolute inset-0 rounded-full border-4 border-slate-800" />
              <motion.div
                animate={{ rotate: 360 }}
                transition={{ repeat: Infinity, duration: 1.2, ease: "linear" }}
                className="absolute inset-0 rounded-full border-4 border-t-sky-400 border-r-indigo-500 border-b-transparent border-l-transparent"
              />
            </div>
            {/* Loading text message */}
            <motion.p
              initial={{ y: 5, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="text-sm font-medium tracking-wide text-slate-300"
            >
              {message}
            </motion.p>
            <span className="text-[10px] uppercase tracking-widest text-slate-500 font-semibold animate-pulse">
              System Securing
            </span>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

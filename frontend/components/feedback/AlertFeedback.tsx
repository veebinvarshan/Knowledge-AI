"use client";

import { motion, AnimatePresence } from "framer-motion";
import { AlertCircle, CheckCircle, Info, X } from "lucide-react";

interface AlertFeedbackProps {
  type: "error" | "success" | "info";
  message: string;
  onClose?: () => void;
}

export default function AlertFeedback({ type, message, onClose }: AlertFeedbackProps) {
  if (!message) return null;

  const config = {
    error: {
      bg: "bg-red-500/10 border-red-500/20 text-red-400",
      icon: <AlertCircle className="w-5 h-5 text-red-400 shrink-0" />,
      label: "Security Error",
    },
    success: {
      bg: "bg-emerald-500/10 border-emerald-500/20 text-emerald-400",
      icon: <CheckCircle className="w-5 h-5 text-emerald-400 shrink-0" />,
      label: "Success",
    },
    info: {
      bg: "bg-sky-500/10 border-sky-500/20 text-sky-400",
      icon: <Info className="w-5 h-5 text-sky-400 shrink-0" />,
      label: "Notice",
    },
  };

  const current = config[type];

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -10 }}
        className={`flex items-start gap-3 p-4 rounded-lg border backdrop-blur-md ${current.bg} w-full`}
      >
        {current.icon}
        <div className="flex-1 space-y-1">
          <h4 className="text-xs font-semibold uppercase tracking-wider opacity-80">
            {current.label}
          </h4>
          <p className="text-xs leading-relaxed opacity-90">{message}</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            type="button"
            className="text-slate-400 hover:text-slate-200 transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        )}
      </motion.div>
    </AnimatePresence>
  );
}

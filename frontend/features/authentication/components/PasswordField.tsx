"use client";

import { useState } from "react";
import { Eye, EyeOff, Lock } from "lucide-react";

interface PasswordFieldProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
}

export default function PasswordField({ label, id, ...props }: PasswordFieldProps) {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="space-y-2">
      <label htmlFor={id} className="text-xs font-semibold uppercase tracking-wider text-slate-400">
        {label}
      </label>
      <div className="relative">
        <div className="absolute left-3 top-3.5 text-slate-500">
          <Lock className="w-4 h-4" />
        </div>
        <input
          id={id}
          type={showPassword ? "text" : "password"}
          className="w-full pl-10 pr-10 py-3 bg-slate-900/50 border border-slate-800 focus:border-sky-500/50 focus:ring-1 focus:ring-sky-500/50 rounded-lg text-sm transition-all outline-none"
          {...props}
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-3.5 text-slate-500 hover:text-slate-300 transition-colors"
        >
          {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
        </button>
      </div>
    </div>
  );
}

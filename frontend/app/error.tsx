"use client";

import { useEffect } from "react";
import Link from "next/link";
import { AlertCircle } from "lucide-react";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-slate-950 text-slate-100 px-6 text-center">
      <div className="w-16 h-16 rounded-full bg-destructive/10 flex items-center justify-center text-destructive mb-6">
        <AlertCircle className="w-8 h-8" />
      </div>
      <h2 className="text-2xl font-bold mb-3">Something went wrong</h2>
      <p className="text-slate-400 text-sm max-w-md mb-8">
        An unexpected application error occurred. Internal configurations remain stable.
      </p>
      <div className="flex gap-4">
        <button
          onClick={() => reset()}
          className="px-5 py-2.5 bg-slate-800 hover:bg-slate-700 font-semibold rounded-lg text-sm transition-all"
        >
          Try Again
        </button>
        <Link
          href="/"
          className="px-5 py-2.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-semibold rounded-lg text-sm hover:opacity-90 transition-all"
        >
          Return Home
        </Link>
      </div>
    </div>
  );
}

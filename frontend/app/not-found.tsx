import Link from "next/link";
import { HelpCircle } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-slate-950 text-slate-100 px-6 text-center">
      <div className="w-16 h-16 rounded-full bg-sky-500/10 flex items-center justify-center text-sky-400 mb-6 animate-bounce">
        <HelpCircle className="w-8 h-8" />
      </div>
      <h2 className="text-4xl font-extrabold tracking-tight mb-3">404</h2>
      <h3 className="text-xl font-semibold text-slate-300 mb-2">Resource Not Found</h3>
      <p className="text-slate-500 text-sm max-w-sm mb-8 leading-relaxed">
        The requested documentation index or console view does not exist inside current workspace namespaces.
      </p>
      <Link
        href="/"
        className="px-6 py-3 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-semibold rounded-lg text-sm hover:opacity-90 transition-all duration-200"
      >
        Go Back Home
      </Link>
    </div>
  );
}

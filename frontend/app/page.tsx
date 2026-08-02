import Link from "next/link";
import { ArrowRight, Shield, Search, Database, MessageSquare } from "lucide-react";

export default function LandingPage() {
  return (
    <div className="flex flex-col min-h-screen bg-slate-950 text-slate-50 selection:bg-sky-500 selection:text-slate-900">
      {/* Header */}
      <header className="px-6 lg:px-12 h-20 flex items-center justify-between border-b border-slate-900">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-sky-400 to-indigo-600 flex items-center justify-center font-bold text-slate-950">
            K
          </div>
          <span className="font-semibold text-lg tracking-wider">THE PLATFORM</span>
        </div>
        <nav className="flex items-center gap-6">
          <Link href="/login" className="text-sm font-medium hover:text-sky-400 transition-colors">
            Sign In
          </Link>
          <Link href="/login" className="px-4 py-2 text-sm font-medium bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 rounded-md hover:opacity-90 transition-all duration-200 shadow-lg shadow-sky-500/10">
            Get Started
          </Link>
        </nav>
      </header>

      {/* Hero Section */}
      <main className="flex-1 flex flex-col items-center justify-center px-6 text-center max-w-5xl mx-auto py-24">
        <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-sky-500/20 bg-sky-500/5 text-xs text-sky-400 mb-8 animate-pulse">
          <Shield className="w-3.5 h-3.5" /> Secure Enterprise Knowledge Base
        </div>
        
        <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight mb-8 leading-tight">
          Unify Your Corporate Intelligence with{" "}
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-sky-400 to-indigo-400">
            Secure RAG Chat
          </span>
        </h1>
        
        <p className="text-lg md:text-xl text-slate-400 max-w-3xl mb-12 leading-relaxed">
          Aggregating fragmented documentation across your organization. Synthesize answers, locate specification documents, and query files safely with real-time access control policies.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 mb-24">
          <Link href="/login" className="flex items-center gap-2 px-6 py-3.5 bg-gradient-to-r from-sky-400 to-indigo-500 text-slate-950 font-semibold rounded-lg hover:opacity-95 transition-all duration-200 shadow-xl shadow-sky-500/15">
            Launch Portal <ArrowRight className="w-4 h-4" />
          </Link>
          <Link href="/login" className="px-6 py-3.5 border border-slate-800 hover:border-slate-700 bg-slate-900/50 hover:bg-slate-900 rounded-lg font-semibold transition-all duration-200">
            Request Demo
          </Link>
        </div>

        {/* Feature Grid */}
        <div className="grid md:grid-cols-3 gap-8 w-full">
          <div className="p-8 rounded-xl border border-slate-900 bg-slate-950/50 hover:border-slate-800/80 transition-all duration-300 text-left group">
            <div className="w-12 h-12 rounded-lg bg-sky-500/10 flex items-center justify-center text-sky-400 mb-6 group-hover:bg-sky-500/20 transition-all">
              <Search className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold mb-3">Hybrid Semantic Search</h3>
            <p className="text-slate-400 text-sm leading-relaxed">
              Combines dense vector retrieval matching with sparse keyword indices for precise searches.
            </p>
          </div>

          <div className="p-8 rounded-xl border border-slate-900 bg-slate-950/50 hover:border-slate-800/80 transition-all duration-300 text-left group">
            <div className="w-12 h-12 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 mb-6 group-hover:bg-indigo-500/20 transition-all">
              <MessageSquare className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold mb-3">Contextual Chat AI</h3>
            <p className="text-slate-400 text-sm leading-relaxed">
              Interact with your files using Gemini 2.5 Flash, returning grounded answers with citations.
            </p>
          </div>

          <div className="p-8 rounded-xl border border-slate-900 bg-slate-950/50 hover:border-slate-800/80 transition-all duration-300 text-left group">
            <div className="w-12 h-12 rounded-lg bg-purple-500/10 flex items-center justify-center text-purple-400 mb-6 group-hover:bg-purple-500/20 transition-all">
              <Database className="w-5 h-5" />
            </div>
            <h3 className="text-lg font-semibold mb-3">Granular ACL Security</h3>
            <p className="text-slate-400 text-sm leading-relaxed">
              Pre-filters vectors at query execution time, ensuring users see only what they have authorization to access.
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-8 px-6 border-t border-slate-900 text-center text-xs text-slate-500">
        &copy; {new Date().getFullYear()} The Application. All rights reserved. Built to enterprise standards.
      </footer>
    </div>
  );
}

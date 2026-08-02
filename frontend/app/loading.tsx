export default function Loading() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-slate-950 text-slate-100">
      <div className="w-12 h-12 rounded-full border-4 border-sky-500/20 border-t-sky-400 animate-spin mb-4"></div>
      <p className="text-sm font-medium tracking-widest text-slate-400">LOADING PLATFORM...</p>
    </div>
  );
}

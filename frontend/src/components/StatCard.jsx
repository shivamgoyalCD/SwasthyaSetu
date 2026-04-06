export default function StatCard({ label, value, hint }) {
  return (
    <div className="rounded-[24px] border border-slate-200/80 bg-white/80 p-5 shadow-sm">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-3 text-3xl font-semibold tracking-tight text-ink">{value}</p>
      <p className="mt-2 text-sm text-slate-600">{hint}</p>
    </div>
  );
}

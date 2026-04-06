export default function SectionCard({ title, eyebrow, children, className = '' }) {
  return (
    <section className={`app-panel p-6 ${className}`}>
      <div className="mb-4 space-y-1">
        {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
        <h2 className="text-xl font-semibold tracking-tight text-ink">{title}</h2>
      </div>
      {children}
    </section>
  );
}

export default function PageIntro({ eyebrow, title, description, actions }) {
  return (
    <div className="app-panel overflow-hidden p-6 md:p-8">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
        <div className="max-w-3xl space-y-3">
          <p className="eyebrow">{eyebrow}</p>
          <div className="space-y-2">
            <h1 className="text-3xl font-semibold tracking-tight text-ink md:text-4xl">
              {title}
            </h1>
            <p className="max-w-2xl text-sm leading-7 text-slate-600 md:text-base">
              {description}
            </p>
          </div>
        </div>
        {actions ? <div className="flex flex-wrap gap-3">{actions}</div> : null}
      </div>
    </div>
  );
}

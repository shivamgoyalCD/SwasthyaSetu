import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { demoUsers } from '../store/session';
import { appNav } from '../utils/navigation';

const linkClassName = ({ isActive }) =>
  [
    'rounded-2xl border px-4 py-3 transition',
    isActive
      ? 'border-spruce bg-spruce text-white shadow-lg shadow-spruce/20'
      : 'border-slate-200 bg-white/70 text-slate-700 hover:border-spruce/30 hover:bg-white'
  ].join(' ');

function resolveCurrentSection(pathname) {
  return (
    appNav.find((item) => pathname === item.path || pathname.startsWith(`${item.path}/`)) ??
    appNav[0]
  );
}

export default function AppShell() {
  const location = useLocation();
  const currentSection = resolveCurrentSection(location.pathname);
  const profile = demoUsers.doctor;
  const today = new Date().toLocaleDateString('en-IN', {
    weekday: 'long',
    day: 'numeric',
    month: 'long'
  });

  return (
    <div className="min-h-screen bg-transparent text-ink">
      <div className="mx-auto flex min-h-screen max-w-7xl flex-col gap-5 p-4 lg:flex-row lg:p-6">
        <aside className="app-panel lg:sticky lg:top-6 lg:h-[calc(100vh-3rem)] lg:w-[300px] lg:flex-none">
          <div className="flex h-full flex-col p-5">
            <div className="space-y-4 border-b border-slate-200/80 pb-5">
              <div className="flex items-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-spruce text-lg font-semibold text-white">
                  SS
                </div>
                <div>
                  <p className="text-lg font-semibold tracking-tight text-ink">SwasthyaSetu</p>
                  <p className="text-sm text-slate-500">Telehealth command surface</p>
                </div>
              </div>
              <div className="rounded-[24px] bg-slate-950 p-4 text-white">
                <p className="text-xs uppercase tracking-[0.28em] text-white/60">Signed in as</p>
                <p className="mt-3 text-xl font-semibold">{profile.name}</p>
                <div className="mt-3 flex items-center justify-between text-sm text-white/75">
                  <span>{profile.role}</span>
                  <span>{profile.status}</span>
                </div>
              </div>
            </div>

            <nav className="mt-5 hidden flex-1 gap-3 overflow-y-auto pr-1 lg:flex lg:flex-col">
              {appNav.map((item) => (
                <NavLink key={item.path} className={linkClassName} to={item.path}>
                  <p className="font-semibold">{item.label}</p>
                  <p className="mt-1 text-sm opacity-80">{item.caption}</p>
                </NavLink>
              ))}
            </nav>

            <div className="mt-5 rounded-[24px] border border-dashed border-slate-300/80 bg-slate-50/80 p-4 text-sm text-slate-600">
              <p className="font-semibold text-ink">Demo environment</p>
              <p className="mt-2">
                Router, layout, and page shells are wired. Replace the mock data with live API
                calls as backend endpoints are connected.
              </p>
            </div>
          </div>
        </aside>

        <div className="flex min-h-0 flex-1 flex-col gap-5">
          <header className="app-panel p-5">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
              <div>
                <p className="eyebrow">Current section</p>
                <h1 className="mt-2 text-2xl font-semibold tracking-tight text-ink">
                  {currentSection.label}
                </h1>
                <p className="mt-2 text-sm text-slate-600">{currentSection.caption}</p>
              </div>
              <div className="grid gap-3 sm:grid-cols-3">
                <div className="rounded-2xl border border-slate-200/80 bg-white/80 px-4 py-3">
                  <p className="text-xs uppercase tracking-[0.28em] text-slate-400">Date</p>
                  <p className="mt-2 font-semibold text-ink">{today}</p>
                </div>
                <div className="rounded-2xl border border-slate-200/80 bg-white/80 px-4 py-3">
                  <p className="text-xs uppercase tracking-[0.28em] text-slate-400">Language</p>
                  <p className="mt-2 font-semibold text-ink">{profile.language}</p>
                </div>
                <div className="rounded-2xl border border-slate-200/80 bg-white/80 px-4 py-3">
                  <p className="text-xs uppercase tracking-[0.28em] text-slate-400">Mode</p>
                  <p className="mt-2 font-semibold text-ink">Web console</p>
                </div>
              </div>
            </div>
          </header>

          <nav className="flex gap-3 overflow-x-auto pb-1 lg:hidden">
            {appNav.map((item) => (
              <NavLink key={item.path} className={linkClassName} to={item.path}>
                <p className="whitespace-nowrap font-semibold">{item.label}</p>
              </NavLink>
            ))}
          </nav>

          <main className="pb-6">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}

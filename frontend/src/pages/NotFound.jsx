import { Link } from 'react-router-dom';
import usePageMeta from '../hooks/usePageMeta';

export default function NotFound() {
  usePageMeta('Page Not Found', 'Fallback route for unknown frontend paths.');

  return (
    <div className="app-panel p-8 text-center">
      <p className="eyebrow">404</p>
      <h1 className="mt-3 text-3xl font-semibold tracking-tight text-ink">Page not found</h1>
      <p className="mt-3 text-slate-600">
        The requested route does not exist in the current frontend scaffold.
      </p>
      <Link
        className="mt-6 inline-flex rounded-2xl bg-spruce px-4 py-3 font-semibold text-white"
        to="/patient"
      >
        Go to Patient Home
      </Link>
    </div>
  );
}

import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { requestOtp, verifyOtp } from '../api/auth';
import usePageMeta from '../hooks/usePageMeta';
import { isAuthenticated, setAccessToken } from '../utils/auth';

const entryPoints = [
  { title: 'Patient access', copy: 'Join your appointment, view prescriptions, and continue chat.' },
  { title: 'Doctor console', copy: 'Check queue, run consultations, and issue prescriptions.' },
  { title: 'Admin operations', copy: 'Review onboarding, service status, and platform activity.' }
];

export default function Login() {
  usePageMeta('Login', 'Access the SwasthyaSetu patient, doctor, and admin experience.');

  const location = useLocation();
  const navigate = useNavigate();
  const redirectTo = location.state?.from?.pathname ?? '/patient';
  const [phone, setPhone] = useState('');
  const [otp, setOtp] = useState('');
  const [sessionId, setSessionId] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isRequestingOtp, setIsRequestingOtp] = useState(false);
  const [isVerifyingOtp, setIsVerifyingOtp] = useState(false);

  if (isAuthenticated()) {
    return <Navigate replace to={redirectTo} />;
  }

  async function handleRequestOtp(event) {
    event.preventDefault();

    const trimmedPhone = phone.trim();

    if (!trimmedPhone) {
      setErrorMessage('Enter a phone number to request an OTP.');
      setSuccessMessage('');
      return;
    }

    try {
      setIsRequestingOtp(true);
      setErrorMessage('');

      const response = await requestOtp(trimmedPhone);
      setSessionId(response.sessionId);
      setOtp('');
      setSuccessMessage(`OTP requested for ${trimmedPhone}. Use the code you received to continue.`);
    } catch (error) {
      setSuccessMessage('');
      setErrorMessage(error.message);
    } finally {
      setIsRequestingOtp(false);
    }
  }

  async function handleVerifyOtp(event) {
    event.preventDefault();

    const trimmedOtp = otp.trim();

    if (!sessionId) {
      setErrorMessage('Request an OTP before verifying.');
      setSuccessMessage('');
      return;
    }

    if (!trimmedOtp) {
      setErrorMessage('Enter the OTP to finish signing in.');
      setSuccessMessage('');
      return;
    }

    try {
      setIsVerifyingOtp(true);
      setErrorMessage('');

      const response = await verifyOtp(sessionId, trimmedOtp);
      setAccessToken(response.accessToken);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setSuccessMessage('');
      setErrorMessage(error.message);
    } finally {
      setIsVerifyingOtp(false);
    }
  }

  return (
    <div className="min-h-screen bg-transparent px-4 py-8 text-ink sm:px-6 lg:px-8">
      <div className="mx-auto grid min-h-[calc(100vh-4rem)] max-w-6xl gap-6 lg:grid-cols-[1.15fr_0.85fr]">
        <section className="app-panel flex flex-col justify-between overflow-hidden p-8 md:p-10">
          <div className="space-y-5">
            <p className="eyebrow">Unified healthcare access</p>
            <div className="space-y-4">
              <h1 className="max-w-3xl text-4xl font-semibold tracking-tight text-ink md:text-6xl">
                Care journeys for patients, doctors, and admins in one interface.
              </h1>
              <p className="max-w-2xl text-base leading-8 text-slate-600 md:text-lg">
                This Vite + React frontend is scaffolded for teleconsultation workflows with
                appointments, waiting room management, live chat, calls, and prescriptions.
              </p>
            </div>
          </div>

          <div className="mt-10 grid gap-4 md:grid-cols-3">
            {entryPoints.map((item) => (
              <div
                key={item.title}
                className="rounded-[24px] border border-white/80 bg-gradient-to-br from-white to-slate-50 p-5"
              >
                <p className="text-lg font-semibold tracking-tight text-ink">{item.title}</p>
                <p className="mt-3 text-sm leading-7 text-slate-600">{item.copy}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="app-panel p-8 md:p-10">
          <div className="space-y-6">
            <div>
              <p className="eyebrow">Login</p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink">
                Sign in with OTP verification
              </h2>
              <p className="mt-3 text-sm leading-7 text-slate-600">
                Request an OTP using your phone number, then verify it to store the access token
                locally and unlock the protected application routes.
              </p>
            </div>

            <form className="space-y-4" onSubmit={handleRequestOtp}>
              <label className="block space-y-2">
                <span className="text-sm font-medium text-slate-700">Mobile number</span>
                <input
                  className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-base outline-none transition focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  type="tel"
                  placeholder="+91 98765 43210"
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                />
              </label>
              <button
                className="w-full rounded-2xl bg-spruce px-4 py-3 text-base font-semibold text-white transition hover:bg-teal-700"
                disabled={isRequestingOtp}
                type="submit"
              >
                {isRequestingOtp ? 'Requesting OTP...' : sessionId ? 'Resend OTP' : 'Request OTP'}
              </button>
            </form>

            {sessionId ? (
              <form className="space-y-4 rounded-[24px] border border-slate-200 bg-slate-50/80 p-5" onSubmit={handleVerifyOtp}>
                <div>
                  <p className="text-sm font-semibold text-ink">OTP verification</p>
                  <p className="mt-2 text-sm text-slate-600">
                    Session created successfully. Enter the OTP associated with the requested phone
                    number to finish login.
                  </p>
                </div>
                <label className="block space-y-2">
                  <span className="text-sm font-medium text-slate-700">One-time password</span>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-base outline-none transition focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    type="password"
                    placeholder="Enter OTP"
                    value={otp}
                    onChange={(event) => setOtp(event.target.value)}
                  />
                </label>
                <button
                  className="w-full rounded-2xl bg-slate-950 px-4 py-3 text-base font-semibold text-white transition hover:bg-slate-800"
                  disabled={isVerifyingOtp}
                  type="submit"
                >
                  {isVerifyingOtp ? 'Verifying OTP...' : 'Verify OTP'}
                </button>
              </form>
            ) : null}

            {errorMessage ? (
              <div className="rounded-[24px] border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
                {errorMessage}
              </div>
            ) : null}

            {successMessage ? (
              <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
                {successMessage}
              </div>
            ) : null}

            <div className="rounded-[24px] border border-dashed border-slate-300 bg-slate-50/80 p-4 text-sm text-slate-600">
              <p className="font-semibold text-ink">Protected access</p>
              <p className="mt-2">
                Pages inside the app shell now require a stored access token. Successful OTP
                verification redirects you to the page that originally requested authentication.
              </p>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}

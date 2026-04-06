import { useEffect, useState } from 'react';
import { getPendingDoctors, rejectDoctor, verifyDoctor } from '../api/users';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { getAccessTokenPayload } from '../utils/auth';

function formatTimestamp(value) {
  if (!value) {
    return 'Unknown';
  }

  return new Date(value).toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit'
  });
}

function formatStatusLabel(value) {
  if (!value) {
    return 'Unknown';
  }

  return value
    .toLowerCase()
    .split('_')
    .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
    .join(' ');
}

function getStatusBadgeClass(status) {
  switch (status) {
    case 'VERIFIED':
      return 'bg-emerald-100 text-emerald-800';
    case 'REJECTED':
      return 'bg-rose-100 text-rose-700';
    default:
      return 'bg-amber-100 text-amber-800';
  }
}

function sortPendingDoctors(doctors) {
  return [...doctors].sort((first, second) => {
    const firstTs = first.createdAt ? new Date(first.createdAt).getTime() : 0;
    const secondTs = second.createdAt ? new Date(second.createdAt).getTime() : 0;

    return firstTs - secondTs;
  });
}

export default function Admin() {
  usePageMeta('Admin', 'Admin console for doctor verification and moderation updates.');

  const accessTokenPayload = getAccessTokenPayload();
  const currentUserRole = String(accessTokenPayload?.role ?? '').toUpperCase();
  const adminRoleError =
    currentUserRole !== 'ADMIN' ? 'Admin role required to view this page.' : '';

  const [pendingDoctors, setPendingDoctors] = useState([]);
  const [isLoadingPending, setIsLoadingPending] = useState(false);
  const [isRefreshingPending, setIsRefreshingPending] = useState(false);
  const [pendingError, setPendingError] = useState('');

  const [actionDoctorId, setActionDoctorId] = useState('');
  const [actionType, setActionType] = useState('');
  const [statusMessage, setStatusMessage] = useState(null);
  const [recentUpdates, setRecentUpdates] = useState([]);
  const [sessionCounts, setSessionCounts] = useState({
    verified: 0,
    rejected: 0
  });

  useEffect(() => {
    if (adminRoleError) {
      setPendingDoctors([]);
      setPendingError('');
      setStatusMessage(null);
      setIsLoadingPending(false);
      return undefined;
    }

    let isActive = true;

    async function loadPendingDoctors() {
      try {
        setIsLoadingPending(true);
        setPendingError('');

        const response = await getPendingDoctors();

        if (!isActive) {
          return;
        }

        setPendingDoctors(sortPendingDoctors(response ?? []));
      } catch (error) {
        if (!isActive) {
          return;
        }

        setPendingDoctors([]);
        setPendingError(error.message);
      } finally {
        if (isActive) {
          setIsLoadingPending(false);
        }
      }
    }

    void loadPendingDoctors();

    return () => {
      isActive = false;
    };
  }, [adminRoleError]);

  async function handleRefreshPendingDoctors() {
    if (adminRoleError) {
      setPendingError(adminRoleError);
      return;
    }

    try {
      setIsRefreshingPending(true);
      setPendingError('');

      const response = await getPendingDoctors();
      setPendingDoctors(sortPendingDoctors(response ?? []));
    } catch (error) {
      setPendingError(error.message);
    } finally {
      setIsRefreshingPending(false);
    }
  }

  async function handleDoctorDecision(doctor, decision) {
    try {
      setActionDoctorId(doctor.id);
      setActionType(decision);
      setPendingError('');
      setStatusMessage(null);

      const updatedDoctor =
        decision === 'verify' ? await verifyDoctor(doctor.id) : await rejectDoctor(doctor.id);

      setPendingDoctors((previousDoctors) =>
        previousDoctors.filter((item) => item.id !== doctor.id)
      );
      setSessionCounts((previousCounts) => ({
        ...previousCounts,
        verified:
          decision === 'verify' ? previousCounts.verified + 1 : previousCounts.verified,
        rejected:
          decision === 'reject' ? previousCounts.rejected + 1 : previousCounts.rejected
      }));
      setRecentUpdates((previousUpdates) =>
        [
          {
            doctorId: updatedDoctor.id,
            specialization: updatedDoctor.specialization,
            status: updatedDoctor.status,
            updatedAt: new Date().toISOString()
          },
          ...previousUpdates
        ].slice(0, 6)
      );
      setStatusMessage({
        tone: 'success',
        text: `Doctor ${updatedDoctor.id} marked as ${formatStatusLabel(updatedDoctor.status)}.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setActionDoctorId('');
      setActionType('');
    }
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Admin control"
        title="Review pending doctor registrations and apply verification decisions."
        description="This page loads `GET /users/admin/doctors/pending` and applies doctor approval decisions through the admin verification endpoints, while keeping a session update log visible."
        actions={
          <button
            className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
            disabled={Boolean(adminRoleError) || isLoadingPending || isRefreshingPending}
            onClick={handleRefreshPendingDoctors}
            type="button"
          >
            {isRefreshingPending ? 'Refreshing...' : 'Refresh pending list'}
          </button>
        }
      />

      <div className="page-grid">
        <StatCard
          label="Pending verifications"
          value={String(pendingDoctors.length)}
          hint="Records returned by the pending-doctor admin endpoint."
        />
        <StatCard
          label="Verified this session"
          value={String(sessionCounts.verified)}
          hint="Successful admin approvals from this browser session."
        />
        <StatCard
          label="Rejected this session"
          value={String(sessionCounts.rejected)}
          hint="Successful admin rejections from this browser session."
        />
      </div>

      {adminRoleError ? (
        <div className="rounded-[28px] border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-900">
          {adminRoleError}
        </div>
      ) : null}

      {statusMessage ? (
        <div
          className={`rounded-[28px] px-5 py-4 text-sm ${
            statusMessage.tone === 'success'
              ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
              : 'border border-rose-200 bg-rose-50 text-rose-700'
          }`}
        >
          {statusMessage.text}
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[1.08fr_0.92fr]">
        <SectionCard title="Pending doctors" eyebrow="Moderation queue">
          <div className="space-y-4">
            {isLoadingPending ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                Loading pending doctor profiles...
              </div>
            ) : null}

            {pendingError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {pendingError}
              </div>
            ) : null}

            {!isLoadingPending && pendingDoctors.length === 0 && !pendingError && !adminRoleError ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No doctor profiles are pending verification right now.
              </div>
            ) : null}

            <div className="space-y-3">
              {pendingDoctors.map((doctor) => {
                const isUpdatingThisDoctor = actionDoctorId === doctor.id;

                return (
                  <div
                    key={doctor.id}
                    className="rounded-2xl border border-slate-200/80 bg-white/80 p-4"
                  >
                    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                      <div className="space-y-3">
                        <div className="flex flex-wrap items-center gap-2">
                          <span className="rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-white">
                            Pending review
                          </span>
                          <span
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] ${getStatusBadgeClass(
                              doctor.status
                            )}`}
                          >
                            {formatStatusLabel(doctor.status)}
                          </span>
                        </div>

                        <div className="space-y-2 text-sm text-slate-600">
                          <p>
                            <span className="font-semibold text-ink">Doctor ID:</span>{' '}
                            <span className="break-all">{doctor.id}</span>
                          </p>
                          <p>
                            <span className="font-semibold text-ink">User ID:</span>{' '}
                            <span className="break-all">{doctor.userId}</span>
                          </p>
                          <p>
                            <span className="font-semibold text-ink">Specialization:</span>{' '}
                            {doctor.specialization || 'Not provided'}
                          </p>
                          <p>
                            <span className="font-semibold text-ink">License number:</span>{' '}
                            {doctor.licenseNo || 'Not provided'}
                          </p>
                          <p>
                            <span className="font-semibold text-ink">Experience:</span>{' '}
                            {doctor.experienceYears != null
                              ? `${doctor.experienceYears} year(s)`
                              : 'Not provided'}
                          </p>
                          <p>
                            <span className="font-semibold text-ink">Submitted:</span>{' '}
                            {formatTimestamp(doctor.createdAt)}
                          </p>
                        </div>
                      </div>

                      <div className="flex flex-col gap-2 sm:flex-row">
                        <button
                          className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-semibold text-rose-700"
                          disabled={Boolean(actionDoctorId)}
                          onClick={() => handleDoctorDecision(doctor, 'reject')}
                          type="button"
                        >
                          {isUpdatingThisDoctor && actionType === 'reject'
                            ? 'Rejecting...'
                            : 'Reject'}
                        </button>
                        <button
                          className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
                          disabled={Boolean(actionDoctorId)}
                          onClick={() => handleDoctorDecision(doctor, 'verify')}
                          type="button"
                        >
                          {isUpdatingThisDoctor && actionType === 'verify'
                            ? 'Verifying...'
                            : 'Verify'}
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Status updates" eyebrow="Session activity">
          <div className="space-y-4">
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
              Each successful decision is removed from the pending queue immediately and logged here
              so the operator can confirm the latest moderation actions.
            </div>

            {recentUpdates.length === 0 ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No verification decisions have been applied in this session yet.
              </div>
            ) : null}

            <div className="space-y-3">
              {recentUpdates.map((update, index) => (
                <div
                  key={`${update.doctorId}-${update.updatedAt}-${index}`}
                  className="rounded-2xl border border-slate-200/80 bg-white/80 p-4"
                >
                  <div className="flex flex-wrap items-center gap-2">
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] ${getStatusBadgeClass(
                        update.status
                      )}`}
                    >
                      {formatStatusLabel(update.status)}
                    </span>
                    <span className="text-xs uppercase tracking-[0.16em] text-slate-400">
                      {formatTimestamp(update.updatedAt)}
                    </span>
                  </div>
                  <p className="mt-3 text-sm font-semibold text-ink">
                    {update.specialization || 'Doctor profile updated'}
                  </p>
                  <p className="mt-2 break-all text-sm text-slate-600">{update.doctorId}</p>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

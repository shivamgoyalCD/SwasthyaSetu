import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getDoctorAvailability,
  getDoctorQueue,
  replaceDoctorAvailability
} from '../api/appointments';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { getAccessTokenPayload } from '../utils/auth';

const weekdayOptions = [
  { value: 0, label: 'Sunday' },
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' }
];

const defaultAvailabilityForm = {
  dayOfWeek: '1',
  startTime: '09:00',
  endTime: '13:00',
  slotMinutes: '20',
  bufferMinutes: '5'
};

function normalizeTimeValue(value) {
  return typeof value === 'string' ? value.slice(0, 5) : '';
}

function toEditableAvailabilitySlot(slot) {
  return {
    dayOfWeek: Number(slot.dayOfWeek),
    startTime: normalizeTimeValue(slot.startTime),
    endTime: normalizeTimeValue(slot.endTime),
    slotMinutes: Number(slot.slotMinutes),
    bufferMinutes: Number(slot.bufferMinutes)
  };
}

function timeStringToMinutes(value) {
  const [hours = '0', minutes = '0'] = String(value).split(':');
  return Number(hours) * 60 + Number(minutes);
}

function sortAvailabilitySlots(slots) {
  return [...slots].sort((first, second) => {
    const dayDifference = first.dayOfWeek - second.dayOfWeek;

    if (dayDifference !== 0) {
      return dayDifference;
    }

    const startDifference =
      timeStringToMinutes(first.startTime) - timeStringToMinutes(second.startTime);

    if (startDifference !== 0) {
      return startDifference;
    }

    return timeStringToMinutes(first.endTime) - timeStringToMinutes(second.endTime);
  });
}

function getWeekdayLabel(dayOfWeek) {
  return weekdayOptions.find((option) => option.value === dayOfWeek)?.label ?? 'Unknown day';
}

function formatTimeLabel(value) {
  if (!value) {
    return 'Unknown time';
  }

  const [hours = '0', minutes = '0'] = value.split(':');
  const date = new Date();
  date.setHours(Number(hours), Number(minutes), 0, 0);

  return date.toLocaleTimeString('en-IN', {
    hour: 'numeric',
    minute: '2-digit'
  });
}

function getWindowMinutes(slot) {
  return Math.max(timeStringToMinutes(slot.endTime) - timeStringToMinutes(slot.startTime), 0);
}

export default function DoctorDashboard() {
  usePageMeta('Doctor Dashboard', 'Doctor operations for queue, availability, and consultations.');

  const navigate = useNavigate();
  const accessTokenPayload = getAccessTokenPayload();
  const doctorId = accessTokenPayload?.sub ?? '';
  const currentUserRole = String(accessTokenPayload?.role ?? '').toUpperCase();
  const roleErrorMessage =
    currentUserRole !== 'DOCTOR'
      ? 'Doctor role required to view this dashboard.'
      : !doctorId
        ? 'Doctor identity is missing from the access token.'
        : '';

  const [availabilityForm, setAvailabilityForm] = useState(defaultAvailabilityForm);
  const [availabilitySlots, setAvailabilitySlots] = useState([]);
  const [availabilityError, setAvailabilityError] = useState('');
  const [availabilitySuccess, setAvailabilitySuccess] = useState('');
  const [isLoadingAvailability, setIsLoadingAvailability] = useState(false);
  const [isSavingAvailability, setIsSavingAvailability] = useState(false);

  const [queueItems, setQueueItems] = useState([]);
  const [queueError, setQueueError] = useState('');
  const [isLoadingQueue, setIsLoadingQueue] = useState(false);
  const [isRefreshingQueue, setIsRefreshingQueue] = useState(false);

  const waitingCount = queueItems.filter((item) => item.presence).length;
  const totalWindowMinutes = availabilitySlots.reduce(
    (total, slot) => total + getWindowMinutes(slot),
    0
  );
  const formDisabled = Boolean(roleErrorMessage) || isLoadingAvailability || isSavingAvailability;

  useEffect(() => {
    if (roleErrorMessage) {
      setAvailabilitySlots([]);
      setAvailabilityError('');
      setAvailabilitySuccess('');
      setIsLoadingAvailability(false);
      return undefined;
    }

    let isActive = true;

    async function loadAvailability() {
      try {
        setIsLoadingAvailability(true);
        setAvailabilityError('');

        const response = await getDoctorAvailability();

        if (!isActive) {
          return;
        }

        setAvailabilitySlots(sortAvailabilitySlots((response ?? []).map(toEditableAvailabilitySlot)));
      } catch (error) {
        if (!isActive) {
          return;
        }

        setAvailabilitySlots([]);
        setAvailabilityError(error.message);
      } finally {
        if (isActive) {
          setIsLoadingAvailability(false);
        }
      }
    }

    void loadAvailability();

    return () => {
      isActive = false;
    };
  }, [roleErrorMessage]);

  useEffect(() => {
    if (roleErrorMessage || !doctorId) {
      setQueueItems([]);
      setQueueError('');
      setIsLoadingQueue(false);
      return undefined;
    }

    let isActive = true;

    async function loadQueue(showLoader) {
      try {
        if (showLoader) {
          setIsLoadingQueue(true);
        }

        setQueueError('');
        const response = await getDoctorQueue(doctorId);

        if (!isActive) {
          return;
        }

        setQueueItems(response ?? []);
      } catch (error) {
        if (!isActive) {
          return;
        }

        if (showLoader) {
          setQueueItems([]);
        }

        setQueueError(error.message);
      } finally {
        if (isActive && showLoader) {
          setIsLoadingQueue(false);
        }
      }
    }

    void loadQueue(true);
    const intervalId = window.setInterval(() => {
      void loadQueue(false);
    }, 10000);

    return () => {
      isActive = false;
      window.clearInterval(intervalId);
    };
  }, [doctorId, roleErrorMessage]);

  function handleAvailabilityFieldChange(field, value) {
    setAvailabilityForm((previousForm) => ({
      ...previousForm,
      [field]: value
    }));
    setAvailabilityError('');
    setAvailabilitySuccess('');
  }

  function handleAddAvailabilitySlot(event) {
    event.preventDefault();

    const nextSlot = {
      dayOfWeek: Number(availabilityForm.dayOfWeek),
      startTime: availabilityForm.startTime,
      endTime: availabilityForm.endTime,
      slotMinutes: Number(availabilityForm.slotMinutes),
      bufferMinutes: Number(availabilityForm.bufferMinutes)
    };

    if (!nextSlot.startTime || !nextSlot.endTime) {
      setAvailabilityError('Start time and end time are required.');
      return;
    }

    if (timeStringToMinutes(nextSlot.startTime) >= timeStringToMinutes(nextSlot.endTime)) {
      setAvailabilityError('Start time must be before end time.');
      return;
    }

    if (nextSlot.slotMinutes <= 0) {
      setAvailabilityError('Slot minutes must be greater than 0.');
      return;
    }

    if (nextSlot.bufferMinutes < 0) {
      setAvailabilityError('Buffer minutes must be 0 or greater.');
      return;
    }

    setAvailabilitySlots((previousSlots) =>
      sortAvailabilitySlots([...previousSlots, nextSlot])
    );
    setAvailabilityError('');
    setAvailabilitySuccess('');
  }

  function handleRemoveAvailabilitySlot(slotIndex) {
    setAvailabilitySlots((previousSlots) =>
      previousSlots.filter((_, currentIndex) => currentIndex !== slotIndex)
    );
    setAvailabilityError('');
    setAvailabilitySuccess('');
  }

  async function handleSaveAvailability() {
    if (roleErrorMessage) {
      setAvailabilityError(roleErrorMessage);
      return;
    }

    try {
      setIsSavingAvailability(true);
      setAvailabilityError('');
      setAvailabilitySuccess('');

      const savedAvailability = await replaceDoctorAvailability(availabilitySlots);
      setAvailabilitySlots(
        sortAvailabilitySlots((savedAvailability ?? []).map(toEditableAvailabilitySlot))
      );
      setAvailabilitySuccess(
        availabilitySlots.length === 0
          ? 'Availability cleared for this doctor.'
          : 'Availability updated successfully.'
      );
    } catch (error) {
      setAvailabilityError(error.message);
    } finally {
      setIsSavingAvailability(false);
    }
  }

  async function handleRefreshQueue() {
    if (roleErrorMessage || !doctorId) {
      setQueueError(roleErrorMessage || 'Doctor identity is missing from the access token.');
      return;
    }

    try {
      setIsRefreshingQueue(true);
      setQueueError('');

      const response = await getDoctorQueue(doctorId);
      setQueueItems(response ?? []);
    } catch (error) {
      setQueueError(error.message);
    } finally {
      setIsRefreshingQueue(false);
    }
  }

  function openChat(appointmentId) {
    navigate(`/chat?appointmentId=${appointmentId}&doctorId=${doctorId}`);
  }

  function openCall(appointmentId) {
    navigate(`/call?appointmentId=${appointmentId}&doctorId=${doctorId}`);
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Doctor operations"
        title="Manage weekly availability and run today's queue."
        description="This dashboard loads the logged-in doctor's saved weekly schedule, replaces it through the appointment-service availability endpoint, and refreshes the doctor queue every 10 seconds."
        actions={
          <>
            <button
              className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
              disabled={Boolean(roleErrorMessage) || isLoadingQueue || isRefreshingQueue}
              onClick={handleRefreshQueue}
              type="button"
            >
              {isRefreshingQueue ? 'Refreshing queue...' : 'Refresh queue'}
            </button>
            <button
              className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
              disabled={formDisabled}
              onClick={handleSaveAvailability}
              type="button"
            >
              {isSavingAvailability ? 'Saving availability...' : 'Save availability'}
            </button>
          </>
        }
      />

      <div className="page-grid">
        <StatCard
          label="Weekly windows"
          value={String(availabilitySlots.length)}
          hint="Availability is persisted as a full slots array, not an incremental add."
        />
        <StatCard
          label="Configured time"
          value={`${totalWindowMinutes} min`}
          hint="Total consultation window minutes across the current weekly schedule."
        />
        <StatCard
          label="Waiting patients"
          value={String(waitingCount)}
          hint={`${queueItems.length} appointment(s) currently in the doctor queue.`}
        />
      </div>

      {roleErrorMessage ? (
        <div className="rounded-[28px] border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-900">
          {roleErrorMessage}
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[1.08fr_0.92fr]">
        <SectionCard title="Availability editor" eyebrow="Weekly schedule">
          <div className="space-y-5">
            <form className="space-y-4" onSubmit={handleAddAvailabilitySlot}>
              <fieldset className="grid gap-4 md:grid-cols-2" disabled={formDisabled}>
                <label className="space-y-2">
                  <span className="block text-sm font-medium text-slate-700">Weekday</span>
                  <select
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    value={availabilityForm.dayOfWeek}
                    onChange={(event) =>
                      handleAvailabilityFieldChange('dayOfWeek', event.target.value)
                    }
                  >
                    {weekdayOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="space-y-2">
                  <span className="block text-sm font-medium text-slate-700">Slot minutes</span>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    min="1"
                    type="number"
                    value={availabilityForm.slotMinutes}
                    onChange={(event) =>
                      handleAvailabilityFieldChange('slotMinutes', event.target.value)
                    }
                  />
                </label>

                <label className="space-y-2">
                  <span className="block text-sm font-medium text-slate-700">Start time</span>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    type="time"
                    value={availabilityForm.startTime}
                    onChange={(event) =>
                      handleAvailabilityFieldChange('startTime', event.target.value)
                    }
                  />
                </label>

                <label className="space-y-2">
                  <span className="block text-sm font-medium text-slate-700">End time</span>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    type="time"
                    value={availabilityForm.endTime}
                    onChange={(event) =>
                      handleAvailabilityFieldChange('endTime', event.target.value)
                    }
                  />
                </label>

                <label className="space-y-2 md:col-span-2">
                  <span className="block text-sm font-medium text-slate-700">Buffer minutes</span>
                  <input
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    min="0"
                    type="number"
                    value={availabilityForm.bufferMinutes}
                    onChange={(event) =>
                      handleAvailabilityFieldChange('bufferMinutes', event.target.value)
                    }
                  />
                </label>
              </fieldset>

              <button
                className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                disabled={formDisabled}
                type="submit"
              >
                Add slot to schedule
              </button>
            </form>

            <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4 text-sm text-slate-600">
              Build the full weekly list here first. `POST /appointments/doctor/availability`
              replaces all saved slots for the doctor in one request.
            </div>

            {isLoadingAvailability ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                Loading saved availability...
              </div>
            ) : null}

            {availabilityError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {availabilityError}
              </div>
            ) : null}

            {availabilitySuccess ? (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
                {availabilitySuccess}
              </div>
            ) : null}

            {!isLoadingAvailability && availabilitySlots.length === 0 && !availabilityError && !roleErrorMessage ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No weekly slots are configured yet. Add one or more windows, then save.
              </div>
            ) : null}

            <div className="space-y-3">
              {availabilitySlots.map((slot, index) => (
                <div
                  key={`${slot.dayOfWeek}-${slot.startTime}-${slot.endTime}-${index}`}
                  className="grid gap-4 rounded-2xl border border-slate-200/80 bg-white/80 p-4 md:grid-cols-[1fr_auto]"
                >
                  <div className="space-y-2">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-spruce/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-spruce">
                        {getWeekdayLabel(slot.dayOfWeek)}
                      </span>
                      <span className="text-sm font-medium text-ink">
                        {formatTimeLabel(slot.startTime)} to {formatTimeLabel(slot.endTime)}
                      </span>
                    </div>
                    <p className="text-sm text-slate-600">
                      Slot length {slot.slotMinutes} min with {slot.bufferMinutes} min buffer.
                    </p>
                  </div>
                  <button
                    className="rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-semibold text-rose-700"
                    disabled={Boolean(roleErrorMessage) || isSavingAvailability}
                    onClick={() => handleRemoveAvailabilitySlot(index)}
                    type="button"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Doctor queue" eyebrow="Today">
          <div className="space-y-4">
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
              <p className="font-semibold text-ink">Logged-in doctor ID</p>
              <p className="mt-2 break-all">{doctorId || 'Not available'}</p>
              <p className="mt-3 text-xs uppercase tracking-[0.18em] text-slate-400">
                Queue refresh interval: 10 seconds
              </p>
            </div>

            {isLoadingQueue ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                Loading today's queue...
              </div>
            ) : null}

            {queueError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {queueError}
              </div>
            ) : null}

            {!isLoadingQueue && queueItems.length === 0 && !queueError && !roleErrorMessage ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No appointments are currently waiting in the queue for this doctor.
              </div>
            ) : null}

            <div className="space-y-3">
              {queueItems.map((item, index) => (
                <div
                  key={item.appointmentId}
                  className="rounded-2xl border border-slate-200/80 bg-white/80 p-4"
                >
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div className="space-y-3">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-white">
                          Queue #{index + 1}
                        </span>
                        <span
                          className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] ${
                            item.presence
                              ? 'bg-emerald-100 text-emerald-700'
                              : 'bg-amber-100 text-amber-800'
                          }`}
                        >
                          {item.presence ? 'Patient waiting' : 'Queued only'}
                        </span>
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-ink">Appointment ID</p>
                        <p className="mt-2 break-all text-sm text-slate-600">{item.appointmentId}</p>
                      </div>
                    </div>

                    <div className="flex flex-col gap-2 sm:flex-row">
                      <button
                        className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                        onClick={() => openChat(item.appointmentId)}
                        type="button"
                      >
                        Open Chat
                      </button>
                      <button
                        className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
                        onClick={() => openCall(item.appointmentId)}
                        type="button"
                      >
                        Open Call
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

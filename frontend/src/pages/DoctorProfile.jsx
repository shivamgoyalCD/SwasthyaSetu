import { useEffect, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { bookAppointment, getAppointmentSlots } from '../api/appointments';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { defaultDoctorId, getDoctorById } from '../store/mockDoctors';

function formatDateInput(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function formatSlotTime(slotTs) {
  return new Date(slotTs).toLocaleTimeString('en-IN', {
    hour: 'numeric',
    minute: '2-digit'
  });
}

export default function DoctorProfile() {
  const { doctorId } = useParams();
  const navigate = useNavigate();
  const doctor = getDoctorById(doctorId);
  const [selectedDate, setSelectedDate] = useState(() => formatDateInput(new Date()));
  const [slots, setSlots] = useState([]);
  const [slotsError, setSlotsError] = useState('');
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [bookingSlotStartTs, setBookingSlotStartTs] = useState('');
  const [bookingError, setBookingError] = useState('');

  usePageMeta(
    doctor ? `${doctor.name}` : 'Doctor Profile',
    'Doctor profile, consultation fees, and upcoming slots.'
  );

  useEffect(() => {
    if (!doctor) {
      return undefined;
    }

    let isActive = true;

    async function loadSlots() {
      try {
        setIsLoadingSlots(true);
        setSlotsError('');

        const response = await getAppointmentSlots(doctor.id, selectedDate);

        if (!isActive) {
          return;
        }

        setSlots(response);
      } catch (error) {
        if (!isActive) {
          return;
        }

        setSlots([]);
        setSlotsError(error.message);
      } finally {
        if (isActive) {
          setIsLoadingSlots(false);
        }
      }
    }

    loadSlots();

    return () => {
      isActive = false;
    };
  }, [doctor, selectedDate]);

  if (!doctor) {
    return <Navigate replace to={`/doctor/profile/${defaultDoctorId}`} />;
  }

  async function handleBookSlot(startTs) {
    try {
      setBookingSlotStartTs(startTs);
      setBookingError('');

      const appointment = await bookAppointment(doctor.id, startTs);
      navigate(
        `/waiting-room?appointmentId=${appointment.id}&doctorId=${appointment.doctorId}`,
        { replace: false }
      );
    } catch (error) {
      setBookingError(error.message);
    } finally {
      setBookingSlotStartTs('');
    }
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Doctor profile"
        title={`${doctor.name} · ${doctor.specialty}`}
        description="This profile is populated from a shared mock doctor list and selected by route parameter, not by a user-service call."
      />

      <div className="page-grid">
        <StatCard
          label="Consultation fee"
          value={`Rs. ${doctor.fee}`}
          hint="Video consultation with digital prescription"
        />
        <StatCard label="Next slot" value={doctor.nextAvailable} hint={doctor.experience} />
        <StatCard
          label="Patient rating"
          value={`${doctor.rating}/5`}
          hint={`Based on ${doctor.reviews} teleconsultations`}
        />
      </div>

      <div className="grid gap-5 lg:grid-cols-[0.95fr_1.05fr]">
        <SectionCard title="Professional summary" eyebrow="About the doctor">
          <div className="space-y-3 text-sm leading-7 text-slate-600">
            <p className="text-base font-semibold text-ink">{doctor.name}</p>
            <p>{doctor.about}</p>
            <div className="flex flex-wrap gap-2">
              {doctor.languages.map((language) => (
                <span
                  key={language}
                  className="rounded-full bg-spruce/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.16em] text-spruce"
                >
                  {language}
                </span>
              ))}
            </div>
            {doctor.credentials.map((item) => (
              <div key={item} className="rounded-2xl bg-slate-50/80 p-4">
                {item}
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="Book a slot" eyebrow="Live availability">
          <div className="space-y-5">
            <label className="block space-y-2">
              <span className="text-sm font-medium text-slate-700">Select date</span>
              <input
                className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-base outline-none transition focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                min={formatDateInput(new Date())}
                type="date"
                value={selectedDate}
                onChange={(event) => setSelectedDate(event.target.value)}
              />
            </label>

            <div className="rounded-2xl bg-slate-50/80 p-4 text-sm text-slate-600">
              <p className="font-semibold text-ink">Selected date</p>
              <p className="mt-2">
                Fetching slot data from `GET /appointments/slots?doctorId&date` for{' '}
                {selectedDate}.
              </p>
            </div>

            {isLoadingSlots ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                Loading available slots...
              </div>
            ) : null}

            {slotsError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {slotsError}
              </div>
            ) : null}

            {bookingError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {bookingError}
              </div>
            ) : null}

            {!isLoadingSlots && !slotsError && slots.length === 0 ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No available slots were returned for this doctor on the selected date.
              </div>
            ) : null}

            {!isLoadingSlots && slots.length > 0 ? (
              <div className="grid gap-3 sm:grid-cols-2">
                {slots.map((slot) => (
                  <div
                    key={`${slot.startTs}-${slot.endTs}`}
                    className="rounded-2xl border border-slate-200/80 bg-white/80 p-4"
                  >
                    <p className="font-semibold text-ink">
                      {formatSlotTime(slot.startTs)} to {formatSlotTime(slot.endTs)}
                    </p>
                    <p className="mt-2 text-sm text-slate-500">Consultation slot available</p>
                    <button
                      className="mt-4 rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
                      disabled={Boolean(bookingSlotStartTs)}
                      onClick={() => handleBookSlot(slot.startTs)}
                      type="button"
                    >
                      {bookingSlotStartTs === slot.startTs ? 'Booking...' : 'Book'}
                    </button>
                  </div>
                ))}
              </div>
            ) : null}

            <div className="grid gap-3 sm:grid-cols-2">
              {doctor.availability.map((slot) => (
                <div
                  key={slot}
                  className="rounded-2xl border border-dashed border-slate-200/80 bg-slate-50/80 p-4 text-sm text-slate-600"
                >
                  <p className="font-semibold text-ink">{slot}</p>
                  <p className="mt-2">Static mock schedule summary</p>
                </div>
              ))}
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

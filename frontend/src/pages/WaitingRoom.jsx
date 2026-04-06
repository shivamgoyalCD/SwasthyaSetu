import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { joinWaitingRoom } from '../api/appointments';
import { createRtcRoom, getRtcRoomStatus } from '../api/rtc';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';

const checks = [
  ['Camera', 'Detected and ready'],
  ['Microphone', 'Input level healthy'],
  ['Internet', 'Stable on broadband'],
  ['Translation', 'English to Hindi enabled']
];

function getRtcCreateKey(appointmentId) {
  return `rtcRoomCreated:${appointmentId}`;
}

export default function WaitingRoom() {
  usePageMeta('Waiting Room', 'Queue view and readiness checks before the consultation starts.');

  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const appointmentId = searchParams.get('appointmentId');
  const doctorId = searchParams.get('doctorId');

  const [joinError, setJoinError] = useState('');
  const [statusError, setStatusError] = useState('');
  const [isPreparingRoom, setIsPreparingRoom] = useState(false);
  const [isRtcReady, setIsRtcReady] = useState(false);
  const [rtcRoomId, setRtcRoomId] = useState('');
  const [participants, setParticipants] = useState([]);

  const hasBookingContext = Boolean(appointmentId);
  const hasRtcParticipants = participants.length > 0;
  const doctorJoined = doctorId ? participants.includes(doctorId) : participants.length > 0;

  useEffect(() => {
    if (!appointmentId) {
      return undefined;
    }

    let isActive = true;

    async function prepareWaitingRoom() {
      try {
        setIsPreparingRoom(true);
        setIsRtcReady(false);
        setJoinError('');

        await joinWaitingRoom(appointmentId);

        if (!isActive) {
          return;
        }

        const rtcCreateKey = getRtcCreateKey(appointmentId);
        const roomAlreadyCreated = window.sessionStorage.getItem(rtcCreateKey) === 'true';

        if (roomAlreadyCreated) {
          setRtcRoomId(appointmentId);
          setIsRtcReady(true);
          return;
        }

        const createdRoom = await createRtcRoom(appointmentId);

        if (!isActive) {
          return;
        }

        setRtcRoomId(createdRoom.roomId);
        setIsRtcReady(true);
        window.sessionStorage.setItem(rtcCreateKey, 'true');
      } catch (error) {
        if (!isActive) {
          return;
        }

        setIsRtcReady(false);
        setJoinError(error.message);
      } finally {
        if (isActive) {
          setIsPreparingRoom(false);
        }
      }
    }

    prepareWaitingRoom();

    return () => {
      isActive = false;
    };
  }, [appointmentId]);

  useEffect(() => {
    if (!appointmentId || joinError || !isRtcReady) {
      return undefined;
    }

    let isActive = true;

    async function fetchRtcStatus() {
      try {
        setStatusError('');

        const roomStatus = await getRtcRoomStatus(appointmentId);

        if (!isActive) {
          return;
        }

        setParticipants(roomStatus.participants ?? []);
      } catch (error) {
        if (!isActive) {
          return;
        }

        setStatusError(error.message);
      }
    }

    fetchRtcStatus();
    const intervalId = window.setInterval(fetchRtcStatus, 5000);

    return () => {
      isActive = false;
      window.clearInterval(intervalId);
    };
  }, [appointmentId, isRtcReady, joinError]);

  function openChat() {
    navigate(`/chat?appointmentId=${appointmentId}&doctorId=${doctorId ?? ''}`);
  }

  function openCall() {
    navigate(`/call?appointmentId=${appointmentId}&doctorId=${doctorId ?? ''}`);
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Waiting room"
        title={doctorJoined ? 'Doctor joined the consultation room.' : 'Waiting for the doctor to join.'}
        description="This page joins the appointment waiting room, creates the RTC room once for the appointment, and polls RTC room status every 5 seconds."
        actions={
          <>
            <button
              className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
              disabled={!hasBookingContext}
              onClick={openChat}
              type="button"
            >
              Open Chat
            </button>
            <button
              className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
              disabled={!hasBookingContext}
              onClick={openCall}
              type="button"
            >
              Open Call
            </button>
          </>
        }
      />

      <div className="page-grid">
        <StatCard
          label="Appointment state"
          value={!appointmentId ? 'Missing' : joinError ? 'Error' : isPreparingRoom ? 'Joining' : 'Joined'}
          hint={appointmentId ? 'Patient waiting room participation is being managed automatically' : 'Open this page from a booked appointment'}
        />
        <StatCard
          label="RTC room status"
          value={doctorJoined ? 'Doctor joined' : hasRtcParticipants ? 'Participants active' : 'Waiting'}
          hint="Status is refreshed every 5 seconds from the RTC service"
        />
        <StatCard
          label="Participants"
          value={String(participants.length)}
          hint={doctorId ? 'Doctor joined when the doctor UUID appears in the room participant list' : 'RTC participant count'}
        />
      </div>

      {!hasBookingContext ? (
        <div className="rounded-[28px] border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-900">
          appointmentId is required to join the waiting room and create the RTC room.
        </div>
      ) : null}

      {joinError ? (
        <div className="rounded-[28px] border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700">
          {joinError}
        </div>
      ) : null}

      {statusError ? (
        <div className="rounded-[28px] border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700">
          {statusError}
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
        <SectionCard title="Pre-call checks" eyebrow="Device readiness">
          <div className="space-y-3">
            {checks.map(([label, state]) => (
              <div
                key={label}
                className="flex items-center justify-between rounded-2xl border border-slate-200/80 bg-white/80 p-4"
              >
                <span className="font-semibold text-ink">{label}</span>
                <span className="text-sm text-slate-600">{state}</span>
              </div>
            ))}
          </div>
        </SectionCard>

        <SectionCard title="Room activity" eyebrow="Live context">
          <div className="space-y-4 text-sm leading-7 text-slate-600">
            <div className="rounded-2xl bg-white/80 p-4">
              <p className="font-semibold text-ink">Appointment ID</p>
              <p className="mt-2 break-all">{appointmentId ?? 'Not provided'}</p>
            </div>
            <div className="rounded-2xl bg-white/80 p-4">
              <p className="font-semibold text-ink">Doctor ID</p>
              <p className="mt-2 break-all">{doctorId ?? 'Not provided'}</p>
            </div>
            <div className="rounded-2xl bg-white/80 p-4">
              <p className="font-semibold text-ink">RTC room ID</p>
              <p className="mt-2 break-all">{rtcRoomId || appointmentId || 'Not created yet'}</p>
            </div>
            <div className={`rounded-2xl p-4 ${doctorJoined ? 'bg-emerald-50 text-emerald-900' : 'bg-slate-50/80'}`}>
              <p className="font-semibold text-ink">Doctor presence</p>
              <p className="mt-2">
                {doctorJoined
                  ? 'Doctor joined'
                  : 'Doctor has not joined the RTC room yet. You can still open Chat or Call while waiting.'}
              </p>
            </div>
            <div className="rounded-2xl bg-slate-50/80 p-4">
              <p className="font-semibold text-ink">Participants</p>
              <p className="mt-2 break-all">
                {participants.length > 0 ? participants.join(', ') : 'No RTC participants reported yet'}
              </p>
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

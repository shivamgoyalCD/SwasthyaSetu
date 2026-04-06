import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { demoUsers } from '../store/session';
import { getDoctorById } from '../store/mockDoctors';
import { getAccessTokenPayload } from '../utils/auth';

const rtcConfiguration = {
  iceServers: [
    {
      urls: import.meta.env.VITE_STUN_SERVER ?? 'stun:stun.l.google.com:19302'
    }
  ]
};

function buildRtcWebSocketUrl() {
  const wsBaseUrl = import.meta.env.VITE_WS_URL ?? 'http://localhost:8080/ws';
  return `${wsBaseUrl.replace(/^http/, 'ws').replace(/\/$/, '')}/rtc`;
}

function getPeerTarget(participants, currentUserId, doctorId) {
  const participantTarget = participants.find((participantId) => participantId !== currentUserId);

  if (participantTarget) {
    return participantTarget;
  }

  if (doctorId && doctorId !== currentUserId) {
    return doctorId;
  }

  return '';
}

export default function Call() {
  usePageMeta('Call', 'Video consultation layout for doctor and patient communication.');

  const [searchParams] = useSearchParams();
  const appointmentId = searchParams.get('appointmentId');
  const doctorId = searchParams.get('doctorId');
  const doctor = doctorId ? getDoctorById(doctorId) : null;
  const accessTokenPayload = getAccessTokenPayload();
  const currentUserId = accessTokenPayload?.sub ?? '';
  const currentUserRole = accessTokenPayload?.role ?? 'PATIENT';
  const speakerLabel =
    currentUserRole === 'DOCTOR' ? doctor?.name ?? 'Doctor' : demoUsers.patient.name;

  const localVideoRef = useRef(null);
  const remoteVideoRef = useRef(null);
  const websocketRef = useRef(null);
  const peerConnectionRef = useRef(null);
  const localStreamRef = useRef(null);
  const remoteStreamRef = useRef(null);
  const currentPeerUserIdRef = useRef('');
  const pendingIceCandidatesRef = useRef([]);

  const [socketStatus, setSocketStatus] = useState('Disconnected');
  const [callStatus, setCallStatus] = useState('Idle');
  const [participants, setParticipants] = useState([]);
  const [remoteUserId, setRemoteUserId] = useState('');
  const [callError, setCallError] = useState('');
  const [isStartingCall, setIsStartingCall] = useState(false);
  const [captionDraft, setCaptionDraft] = useState('');
  const [captionLang, setCaptionLang] = useState('en');
  const [captionTargetLang, setCaptionTargetLang] = useState('hi');
  const [captions, setCaptions] = useState([]);

  function joinRtcRoom() {
    if (!appointmentId || !currentUserId) {
      throw new Error('appointmentId and current user are required to join the RTC room.');
    }

    setSocketStatus('Joining room');
    sendRtcMessage({
      action: 'joinRoom',
      appointmentId,
      userId: currentUserId
    });
  }

  async function ensureLocalMedia() {
    if (localStreamRef.current) {
      return localStreamRef.current;
    }

    const stream = await navigator.mediaDevices.getUserMedia({
      audio: true,
      video: true
    });

    localStreamRef.current = stream;

    if (localVideoRef.current) {
      localVideoRef.current.srcObject = stream;
    }

    return stream;
  }

  function sendRtcMessage(payload) {
    const socket = websocketRef.current;

    if (!socket || socket.readyState !== WebSocket.OPEN) {
      throw new Error('RTC signaling socket is not connected.');
    }

    socket.send(JSON.stringify(payload));
  }

  function cleanupPeerConnection() {
    if (peerConnectionRef.current) {
      peerConnectionRef.current.onicecandidate = null;
      peerConnectionRef.current.ontrack = null;
      peerConnectionRef.current.onconnectionstatechange = null;
      peerConnectionRef.current.close();
      peerConnectionRef.current = null;
    }

    currentPeerUserIdRef.current = '';
    pendingIceCandidatesRef.current = [];

    if (remoteStreamRef.current) {
      remoteStreamRef.current.getTracks().forEach((track) => track.stop());
    }

    remoteStreamRef.current = new MediaStream();

    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = remoteStreamRef.current;
    }
  }

  async function flushPendingIceCandidates() {
    const peerConnection = peerConnectionRef.current;

    if (!peerConnection?.remoteDescription) {
      return;
    }

    while (pendingIceCandidatesRef.current.length > 0) {
      const candidate = pendingIceCandidatesRef.current.shift();
      await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
    }
  }

  function createPeerConnection(targetUserId) {
    if (peerConnectionRef.current && currentPeerUserIdRef.current === targetUserId) {
      return peerConnectionRef.current;
    }

    cleanupPeerConnection();

    const peerConnection = new RTCPeerConnection(rtcConfiguration);
    currentPeerUserIdRef.current = targetUserId;
    peerConnectionRef.current = peerConnection;
    remoteStreamRef.current = new MediaStream();

    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = remoteStreamRef.current;
    }

    if (localStreamRef.current) {
      localStreamRef.current.getTracks().forEach((track) => {
        peerConnection.addTrack(track, localStreamRef.current);
      });
    }

    peerConnection.onicecandidate = (event) => {
      if (!event.candidate || !appointmentId || !targetUserId) {
        return;
      }

      try {
        sendRtcMessage({
          action: 'iceCandidate',
          appointmentId,
          toUserId: targetUserId,
          candidate: event.candidate.toJSON()
        });
      } catch (error) {
        setCallError(error.message);
      }
    };

    peerConnection.ontrack = (event) => {
      if (!remoteStreamRef.current) {
        remoteStreamRef.current = new MediaStream();
      }

      event.streams[0].getTracks().forEach((track) => {
        remoteStreamRef.current.addTrack(track);
      });

      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = remoteStreamRef.current;
      }
    };

    peerConnection.onconnectionstatechange = () => {
      switch (peerConnection.connectionState) {
        case 'connected':
          setCallStatus('Connected');
          break;
        case 'connecting':
          setCallStatus('Connecting');
          break;
        case 'disconnected':
        case 'failed':
          setCallStatus('Disconnected');
          break;
        case 'closed':
          setCallStatus('Ended');
          break;
        default:
          break;
      }
    };

    return peerConnection;
  }

  async function handleIncomingOffer(fromUserId, sdp) {
    await ensureLocalMedia();
    setRemoteUserId(fromUserId);
    setCallStatus('Answering');

    const peerConnection = createPeerConnection(fromUserId);
    await peerConnection.setRemoteDescription({
      type: 'offer',
      sdp
    });
    await flushPendingIceCandidates();

    const answer = await peerConnection.createAnswer();
    await peerConnection.setLocalDescription(answer);

    sendRtcMessage({
      action: 'answer',
      appointmentId,
      toUserId: fromUserId,
      sdp: answer.sdp
    });
  }

  async function handleIncomingAnswer(fromUserId, sdp) {
    setRemoteUserId(fromUserId);

    const peerConnection = createPeerConnection(fromUserId);
    await peerConnection.setRemoteDescription({
      type: 'answer',
      sdp
    });
    await flushPendingIceCandidates();
  }

  async function handleIncomingIceCandidate(fromUserId, candidate) {
    setRemoteUserId(fromUserId);

    const peerConnection = createPeerConnection(fromUserId);

    if (!peerConnection.remoteDescription) {
      pendingIceCandidatesRef.current.push(candidate);
      return;
    }

    await peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
  }

  async function startCall(targetUserId) {
    if (!appointmentId) {
      setCallError('appointmentId is required to start the call.');
      return;
    }

    if (!targetUserId) {
      setCallError('No remote participant is available for signaling yet.');
      return;
    }

    try {
      setIsStartingCall(true);
      setCallError('');
      setCallStatus('Starting');

      await ensureLocalMedia();
      const peerConnection = createPeerConnection(targetUserId);
      const offer = await peerConnection.createOffer();

      await peerConnection.setLocalDescription(offer);

      sendRtcMessage({
        action: 'offer',
        appointmentId,
        toUserId: targetUserId,
        sdp: offer.sdp
      });

      setRemoteUserId(targetUserId);
      setCallStatus('Calling');
    } catch (error) {
      setCallStatus('Error');
      setCallError(error.message);
    } finally {
      setIsStartingCall(false);
    }
  }

  function handleStartCall() {
    if (socketStatus !== 'Joined room') {
      try {
        joinRtcRoom();
      } catch (error) {
        setCallError(error.message);
      }
      return;
    }

    const targetUserId = getPeerTarget(participants, currentUserId, doctorId);
    void startCall(targetUserId);
  }

  function handleEndCall() {
    try {
      if (appointmentId && currentUserId && websocketRef.current?.readyState === WebSocket.OPEN) {
        sendRtcMessage({
          action: 'leaveRoom',
          appointmentId,
          userId: currentUserId
        });
      }
    } catch (error) {
      setCallError(error.message);
    }

    cleanupPeerConnection();

    if (localStreamRef.current) {
      localStreamRef.current.getTracks().forEach((track) => track.stop());
      localStreamRef.current = null;
    }

    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null;
    }

    setRemoteUserId('');
    setParticipants([]);
    setCallStatus('Ended');
    setSocketStatus('Left room');
  }

  function handleSendCaption(event) {
    event.preventDefault();

    const trimmedCaption = captionDraft.trim();

    if (!appointmentId) {
      setCallError('appointmentId is required to send captions.');
      return;
    }

    if (!trimmedCaption) {
      setCallError('Enter caption text before sending.');
      return;
    }

    try {
      setCallError('');
      sendRtcMessage({
        action: 'caption',
        appointmentId,
        speaker: speakerLabel,
        text: trimmedCaption,
        lang: captionLang,
        targetLang: captionTargetLang
      });
      setCaptionDraft('');
    } catch (error) {
      setCallError(error.message);
    }
  }

  useEffect(() => {
    if (!appointmentId || !currentUserId) {
      setSocketStatus('Missing context');
      return undefined;
    }

    let isActive = true;
    const socket = new WebSocket(buildRtcWebSocketUrl());
    websocketRef.current = socket;
    setSocketStatus('Connecting');
    setCallError('');

    socket.onopen = () => {
      if (!isActive) {
        return;
      }

      setSocketStatus('Connected');

      try {
        joinRtcRoom();
      } catch (error) {
        setCallError(error.message);
      }
    };

    socket.onmessage = (event) => {
      if (!isActive) {
        return;
      }

      try {
        const message = JSON.parse(event.data);

        switch (message.action) {
          case 'joinedRoom': {
            const nextParticipants = message.participants ?? [];
            setParticipants(nextParticipants);
            setRemoteUserId(getPeerTarget(nextParticipants, currentUserId, doctorId));
            setSocketStatus('Joined room');
            break;
          }
          case 'offer':
            void handleIncomingOffer(message.fromUserId, message.sdp);
            break;
          case 'answer':
            void handleIncomingAnswer(message.fromUserId, message.sdp);
            break;
          case 'iceCandidate':
            void handleIncomingIceCandidate(message.fromUserId, message.candidate);
            break;
          case 'caption':
            setCaptions((previousCaptions) =>
              [
                ...previousCaptions,
                {
                  speaker: message.speaker,
                  originalText: message.originalText,
                  translatedText: message.translatedText,
                  ts: message.ts
                }
              ].slice(-3)
            );
            break;
          case 'error':
            setCallStatus('Error');
            setCallError(message.message ?? 'RTC signaling failed.');
            break;
          default:
            break;
        }
      } catch (error) {
        setCallStatus('Error');
        setCallError(error.message);
      }
    };

    socket.onerror = () => {
      if (!isActive) {
        return;
      }

      setSocketStatus('Error');
      setCallError('RTC signaling socket failed.');
    };

    socket.onclose = () => {
      if (!isActive) {
        return;
      }

      setSocketStatus('Closed');
    };

    return () => {
      isActive = false;

      if (socket.readyState === WebSocket.OPEN && appointmentId && currentUserId) {
        socket.send(
          JSON.stringify({
            action: 'leaveRoom',
            appointmentId,
            userId: currentUserId
          })
        );
      }

      socket.close();
      websocketRef.current = null;
      cleanupPeerConnection();

      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach((track) => track.stop());
        localStreamRef.current = null;
      }
    };
  }, [appointmentId, currentUserId, doctorId]);

  useEffect(() => {
    if (remoteVideoRef.current && !remoteVideoRef.current.srcObject) {
      remoteStreamRef.current = new MediaStream();
      remoteVideoRef.current.srcObject = remoteStreamRef.current;
    }
  }, []);

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Call room"
        title="Minimal WebRTC consultation room with live signaling."
        description="This page connects to `/ws/rtc`, joins the RTC room, negotiates offer/answer/ICE with a single peer, and renders local and remote video elements."
        actions={
          <>
            <button
              className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
              disabled={!appointmentId || !currentUserId || isStartingCall || socketStatus === 'Connecting'}
              onClick={handleStartCall}
              type="button"
            >
              {isStartingCall ? 'Starting...' : 'Start Call'}
            </button>
            <button
              className="rounded-2xl bg-rose-600 px-4 py-3 text-sm font-semibold text-white"
              onClick={handleEndCall}
              type="button"
            >
              End Call
            </button>
          </>
        }
      />

      <div className="page-grid">
        <StatCard
          label="Socket"
          value={socketStatus}
          hint="RTC signaling connection to /ws/rtc"
        />
        <StatCard
          label="Call state"
          value={callStatus}
          hint={remoteUserId ? `Peer target: ${remoteUserId}` : 'No peer target selected yet'}
        />
        <StatCard
          label="Participants"
          value={String(participants.length)}
          hint="Joined room participant list returned by signaling service"
        />
      </div>

      {callError ? (
        <div className="rounded-[28px] border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700">
          {callError}
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[1.2fr_0.8fr]">
        <SectionCard title="Live consultation" eyebrow="Video stage">
          <div className="space-y-4">
            <div className="relative">
              <div className="grid gap-4 lg:grid-cols-2">
                <div className="relative overflow-hidden rounded-[28px] bg-slate-950">
                  <video
                    ref={localVideoRef}
                    autoPlay
                    className="h-[320px] w-full object-cover"
                    muted
                    playsInline
                  />
                  <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-5 text-white">
                    <p className="text-xs uppercase tracking-[0.28em] text-white/60">Local feed</p>
                    <p className="mt-2 text-xl font-semibold">You</p>
                  </div>
                </div>
                <div className="relative overflow-hidden rounded-[28px] bg-slate-900">
                  <video
                    ref={remoteVideoRef}
                    autoPlay
                    className="h-[320px] w-full object-cover"
                    playsInline
                  />
                  <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/80 to-transparent p-5 text-white">
                    <p className="text-xs uppercase tracking-[0.28em] text-white/60">Remote feed</p>
                    <p className="mt-2 text-xl font-semibold">{doctor?.name ?? 'Participant'}</p>
                  </div>
                </div>
              </div>

              <div className="pointer-events-none absolute inset-x-3 bottom-3 z-10">
                {captions.length === 0 ? (
                  <div className="mx-auto max-w-3xl rounded-2xl bg-slate-950/72 px-4 py-3 text-center text-sm text-white/70 backdrop-blur">
                    No captions received yet.
                  </div>
                ) : (
                  <div className="mx-auto max-w-3xl space-y-2">
                    {captions.map((caption, index) => (
                      <div
                        key={`${caption.ts}-${index}`}
                        className="rounded-2xl bg-slate-950/82 px-4 py-3 text-white shadow-lg backdrop-blur"
                      >
                        <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-white/60">
                          {caption.speaker}
                        </p>
                        <p className="mt-2 text-sm text-white/90">{caption.originalText}</p>
                        <p className="mt-1 text-sm font-medium text-tide">
                          {caption.translatedText}
                        </p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <form
              className="rounded-[28px] border border-slate-200 bg-white/90 p-4"
              onSubmit={handleSendCaption}
            >
              <div className="grid gap-3 md:grid-cols-[1fr_140px_140px_auto]">
                <input
                  className="w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Type a caption..."
                  value={captionDraft}
                  onChange={(event) => setCaptionDraft(event.target.value)}
                />
                <input
                  className="rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Lang"
                  value={captionLang}
                  onChange={(event) => setCaptionLang(event.target.value)}
                />
                <input
                  className="rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Target"
                  value={captionTargetLang}
                  onChange={(event) => setCaptionTargetLang(event.target.value)}
                />
                <button
                  className="rounded-2xl bg-slate-950 px-4 py-3 font-semibold text-white"
                  type="submit"
                >
                  Send Caption
                </button>
              </div>
            </form>
          </div>
        </SectionCard>

        <SectionCard title="Call context" eyebrow="Signaling details">
          <div className="space-y-3 text-sm leading-7 text-slate-600">
            <div className="rounded-2xl bg-slate-50/80 p-4">
              <p className="font-semibold text-ink">Appointment ID</p>
              <p className="mt-2 break-all">{appointmentId ?? 'Not provided'}</p>
            </div>
            <div className="rounded-2xl bg-slate-50/80 p-4">
              <p className="font-semibold text-ink">Current user ID</p>
              <p className="mt-2 break-all">{currentUserId || 'Not available'}</p>
            </div>
            <div className="rounded-2xl bg-slate-50/80 p-4">
              <p className="font-semibold text-ink">Remote user ID</p>
              <p className="mt-2 break-all">{remoteUserId || 'Waiting for peer selection'}</p>
            </div>
            <div className="rounded-2xl bg-slate-50/80 p-4">
              Start Call captures local media, creates an `RTCPeerConnection`, and sends offer or
              answer signaling through `/ws/rtc`. No bandwidth adaptation or advanced reconnection
              logic is included.
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

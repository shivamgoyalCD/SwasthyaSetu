import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  createConversationMessage,
  getConversationMessages,
  startConversation
} from '../api/chat';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import usePageMeta from '../hooks/usePageMeta';
import { getDoctorById } from '../store/mockDoctors';
import { getAccessToken, getAccessTokenPayload } from '../utils/auth';

const languageOptions = [
  { value: 'en', label: 'English' },
  { value: 'hi', label: 'Hindi' },
  { value: 'mr', label: 'Marathi' },
  { value: 'pa', label: 'Punjabi' },
  { value: 'ml', label: 'Malayalam' }
];

function formatTimestamp(value) {
  if (!value) {
    return '';
  }

  return new Date(value).toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    hour: 'numeric',
    minute: '2-digit'
  });
}

function mergeMessagePages(previousMessages, incomingMessages) {
  const messageMap = new Map(previousMessages.map((message) => [message.id, message]));

  incomingMessages.forEach((message) => {
    messageMap.set(message.id, message);
  });

  return [...messageMap.values()].sort(
    (first, second) => new Date(first.createdAt) - new Date(second.createdAt)
  );
}

function buildChatWebSocketUrl() {
  const wsBaseUrl = import.meta.env.VITE_WS_URL ?? 'http://localhost:8080/ws';
  const normalizedBaseUrl = wsBaseUrl.replace(/^http/, 'ws').replace(/\/$/, '');
  const accessToken = getAccessToken();
  const url = new URL(`${normalizedBaseUrl}/chat`);

  if (accessToken) {
    url.searchParams.set('accessToken', accessToken);
  }

  return url.toString();
}

export default function Chat() {
  usePageMeta('Chat', 'Secure consultation chat with room for multilingual assistance.');

  const [searchParams] = useSearchParams();
  const appointmentId = searchParams.get('appointmentId');
  const doctorId = searchParams.get('doctorId');
  const doctor = doctorId ? getDoctorById(doctorId) : null;
  const accessTokenPayload = getAccessTokenPayload();
  const currentUserId = accessTokenPayload?.sub ?? '';

  const [conversationId, setConversationId] = useState('');
  const [messages, setMessages] = useState([]);
  const [nextCursor, setNextCursor] = useState(null);
  const [draftMessage, setDraftMessage] = useState('');
  const [originalLang, setOriginalLang] = useState('en');
  const [targetLang, setTargetLang] = useState('hi');
  const [showOriginalByMessageId, setShowOriginalByMessageId] = useState({});
  const [isBootstrapping, setIsBootstrapping] = useState(false);
  const [isLoadingOlder, setIsLoadingOlder] = useState(false);
  const [isSendingMessage, setIsSendingMessage] = useState(false);
  const [chatError, setChatError] = useState('');
  const [liveStatus, setLiveStatus] = useState('Disconnected');
  const [liveError, setLiveError] = useState('');

  useEffect(() => {
    if (!appointmentId) {
      setConversationId('');
      setMessages([]);
      setNextCursor(null);
      return undefined;
    }

    let isActive = true;

    async function bootstrapConversation() {
      try {
        setIsBootstrapping(true);
        setChatError('');
        setMessages([]);
        setNextCursor(null);

        const conversation = await startConversation(appointmentId);

        if (!isActive) {
          return;
        }

        setConversationId(conversation.conversationId);

        const historyPage = await getConversationMessages(conversation.conversationId);

        if (!isActive) {
          return;
        }

        setMessages(historyPage.items ?? []);
        setNextCursor(historyPage.nextCursor ?? null);
      } catch (error) {
        if (!isActive) {
          return;
        }

        setConversationId('');
        setMessages([]);
        setNextCursor(null);
        setChatError(error.message);
      } finally {
        if (isActive) {
          setIsBootstrapping(false);
        }
      }
    }

    bootstrapConversation();

    return () => {
      isActive = false;
    };
  }, [appointmentId]);

  useEffect(() => {
    if (!conversationId) {
      setLiveStatus('Disconnected');
      setLiveError('');
      return undefined;
    }

    let isActive = true;
    let socket;

    try {
      setLiveStatus('Connecting');
      setLiveError('');
      socket = new WebSocket(buildChatWebSocketUrl());
    } catch (error) {
      setLiveStatus('Unavailable');
      setLiveError(error.message);
      return undefined;
    }

    socket.onopen = () => {
      if (!isActive) {
        return;
      }

      setLiveStatus('Joining');
      socket.send(
        JSON.stringify({
          action: 'join',
          conversationId
        })
      );
    };

    socket.onmessage = (event) => {
      if (!isActive) {
        return;
      }

      try {
        const message = JSON.parse(event.data);

        if (message.action === 'joined') {
          setLiveStatus('Live');
          return;
        }

        if (message.action === 'message' && message.payload) {
          setMessages((previousMessages) => mergeMessagePages(previousMessages, [message.payload]));
          return;
        }

        if (message.action === 'error') {
          setLiveStatus('Error');
          setLiveError(message.message ?? 'WebSocket error');
        }
      } catch (error) {
        setLiveStatus('Error');
        setLiveError(error.message);
      }
    };

    socket.onerror = () => {
      if (!isActive) {
        return;
      }

      setLiveStatus('Error');
      setLiveError('Live chat connection failed.');
    };

    socket.onclose = () => {
      if (!isActive) {
        return;
      }

      setLiveStatus('Closed');
    };

    return () => {
      isActive = false;
      socket.close();
    };
  }, [conversationId]);

  async function handleLoadOlderMessages() {
    if (!conversationId || !nextCursor) {
      return;
    }

    try {
      setIsLoadingOlder(true);
      setChatError('');

      const historyPage = await getConversationMessages(conversationId, nextCursor);
      setMessages((previousMessages) => mergeMessagePages(previousMessages, historyPage.items ?? []));
      setNextCursor(historyPage.nextCursor ?? null);
    } catch (error) {
      setChatError(error.message);
    } finally {
      setIsLoadingOlder(false);
    }
  }

  async function handleSendMessage(event) {
    event.preventDefault();

    const trimmedMessage = draftMessage.trim();

    if (!conversationId) {
      setChatError('Conversation is not ready yet.');
      return;
    }

    if (!trimmedMessage) {
      setChatError('Enter a message before sending.');
      return;
    }

    try {
      setIsSendingMessage(true);
      setChatError('');

      const createdMessage = await createConversationMessage(conversationId, {
        content: trimmedMessage,
        originalLang,
        targetLang
      });

      setMessages((previousMessages) => mergeMessagePages(previousMessages, [createdMessage]));
      setDraftMessage('');
    } catch (error) {
      setChatError(error.message);
    } finally {
      setIsSendingMessage(false);
    }
  }

  function toggleOriginal(messageId) {
    setShowOriginalByMessageId((previousState) => ({
      ...previousState,
      [messageId]: !previousState[messageId]
    }));
  }

  function getSenderLabel(message) {
    if (message.senderId === currentUserId) {
      return 'You';
    }

    if (doctorId && message.senderId === doctorId) {
      return doctor?.name ?? 'Doctor';
    }

    return 'Participant';
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Consultation chat"
        title="A chat surface for medical coordination, message history, and translation."
        description="The page starts or reuses the appointment conversation, loads message history, and sends translated text messages using the chat-service endpoints."
      />

      <div className="grid gap-5 xl:grid-cols-[0.72fr_1.28fr]">
        <SectionCard title="Conversation" eyebrow="Session context">
          <div className="space-y-4 text-sm text-slate-600">
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Appointment ID</p>
              <p className="mt-2 break-all">{appointmentId ?? 'Not provided'}</p>
            </div>
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Conversation ID</p>
              <p className="mt-2 break-all">{conversationId || 'Starting conversation...'}</p>
            </div>
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Live socket</p>
              <p className="mt-2">{liveStatus}</p>
            </div>
            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Translation pair</p>
              <div className="mt-3 grid gap-3">
                <label className="space-y-2">
                  <span className="block text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">
                    Original language
                  </span>
                  <select
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    value={originalLang}
                    onChange={(event) => setOriginalLang(event.target.value)}
                  >
                    {languageOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="space-y-2">
                  <span className="block text-xs font-semibold uppercase tracking-[0.2em] text-slate-400">
                    Target language
                  </span>
                  <select
                    className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                    value={targetLang}
                    onChange={(event) => setTargetLang(event.target.value)}
                  >
                    {languageOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            </div>
            <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4">
              Messages render translated text by default when `translatedContent` exists. Use the
              per-message toggle to switch back to the original content.
            </div>
          </div>
        </SectionCard>

        <SectionCard title="Message stream" eyebrow="History and live messages">
          <div className="space-y-4">
            {!appointmentId ? (
              <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
                appointmentId is required to start a conversation. Open Chat from the Waiting Room
                to carry the appointment context into this page.
              </div>
            ) : null}

            {chatError ? (
              <div className="rounded-2xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
                {chatError}
              </div>
            ) : null}

            {liveError ? (
              <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
                {liveError}
              </div>
            ) : null}

            {nextCursor ? (
              <button
                className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                disabled={isLoadingOlder}
                onClick={handleLoadOlderMessages}
                type="button"
              >
                {isLoadingOlder ? 'Loading older messages...' : 'Load older messages'}
              </button>
            ) : null}

            {isBootstrapping ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                Starting conversation and loading message history...
              </div>
            ) : null}

            {!isBootstrapping && messages.length === 0 && appointmentId && !chatError ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                No messages yet. Send the first message to start the consultation thread.
              </div>
            ) : null}

            {messages.map((message) => {
              const isCurrentUser = message.senderId === currentUserId;
              const hasTranslation =
                Boolean(message.translatedContent) &&
                message.translatedContent !== message.content;
              const showOriginal = Boolean(showOriginalByMessageId[message.id]);
              const renderedContent =
                hasTranslation && !showOriginal ? message.translatedContent : message.content;

              return (
                <div
                  key={message.id}
                  className={`max-w-3xl rounded-[24px] p-4 ${
                    isCurrentUser
                      ? 'ml-auto bg-spruce text-white'
                      : 'bg-slate-100 text-slate-700'
                  }`}
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <p className="text-xs font-semibold uppercase tracking-[0.2em] opacity-70">
                      {getSenderLabel(message)}
                    </p>
                    <p className="text-xs opacity-70">{formatTimestamp(message.createdAt)}</p>
                  </div>
                  <p className="mt-2 text-sm leading-7">{renderedContent}</p>
                  <div className="mt-3 flex flex-wrap items-center gap-3">
                    <span className="text-xs opacity-70">
                      Original: {message.originalLang?.toUpperCase() ?? 'N/A'}
                    </span>
                    {hasTranslation ? (
                      <button
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${
                          isCurrentUser
                            ? 'bg-white/15 text-white'
                            : 'bg-white text-slate-700'
                        }`}
                        onClick={() => toggleOriginal(message.id)}
                        type="button"
                      >
                        {showOriginal ? 'Show translated' : 'Show original'}
                      </button>
                    ) : null}
                  </div>
                </div>
              );
            })}

            <form
              className="rounded-[28px] border border-slate-200 bg-white/90 p-4"
              onSubmit={handleSendMessage}
            >
              <div className="flex flex-col gap-3">
                <textarea
                  className="min-h-28 w-full rounded-2xl border border-slate-200 px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Type a message for the consultation..."
                  value={draftMessage}
                  onChange={(event) => setDraftMessage(event.target.value)}
                />
                <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                  <p className="text-sm text-slate-500">
                    Sending as TEXT with `{originalLang}` to `{targetLang}` translation.
                  </p>
                  <button
                    className="rounded-2xl bg-spruce px-5 py-3 font-semibold text-white"
                    disabled={!appointmentId || !conversationId || isSendingMessage}
                    type="submit"
                  >
                    {isSendingMessage ? 'Sending...' : 'Send'}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

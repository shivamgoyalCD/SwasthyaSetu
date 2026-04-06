import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  endPrescriptionConsultation,
  generatePrescriptionPdf,
  getPrescriptionDownloadUrl,
  startPrescriptionConsultation,
  updatePrescriptionConsultationSummary
} from '../api/prescriptions';
import PageIntro from '../components/PageIntro';
import SectionCard from '../components/SectionCard';
import StatCard from '../components/StatCard';
import usePageMeta from '../hooks/usePageMeta';
import { getAccessTokenPayload } from '../utils/auth';

function formatTimestamp(value) {
  if (!value) {
    return 'Not available';
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
    return 'Not started';
  }

  return value
    .toLowerCase()
    .split('_')
    .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
    .join(' ');
}

function normalizeSummaryJson(value) {
  if (value == null) {
    return '';
  }

  if (typeof value === 'string') {
    if (!value.trim()) {
      return '';
    }

    try {
      return JSON.stringify(JSON.parse(value), null, 2);
    } catch {
      return value;
    }
  }

  return JSON.stringify(value, null, 2);
}

function getSummaryPreview(summaryJson) {
  const trimmedSummary = summaryJson.trim();

  if (!trimmedSummary) {
    return {
      complaint: '',
      advice: '',
      notes: '',
      isValidJson: true
    };
  }

  try {
    const parsedSummary = JSON.parse(trimmedSummary);

    if (!parsedSummary || typeof parsedSummary !== 'object' || Array.isArray(parsedSummary)) {
      return {
        complaint: '',
        advice: '',
        notes: trimmedSummary,
        isValidJson: false
      };
    }

    return {
      complaint:
        typeof parsedSummary.complaint === 'string' ? parsedSummary.complaint : '',
      advice: typeof parsedSummary.advice === 'string' ? parsedSummary.advice : '',
      notes: typeof parsedSummary.notes === 'string' ? parsedSummary.notes : '',
      isValidJson: true
    };
  } catch {
    return {
      complaint: '',
      advice: '',
      notes: trimmedSummary,
      isValidJson: false
    };
  }
}

function buildNextSearchParams(searchParams, values) {
  const nextSearchParams = new URLSearchParams(searchParams);

  Object.entries(values).forEach(([key, value]) => {
    if (value) {
      nextSearchParams.set(key, value);
      return;
    }

    nextSearchParams.delete(key);
  });

  return nextSearchParams;
}

export default function Prescription() {
  usePageMeta('Prescription', 'Consultation summary, PDF generation, and prescription download.');

  const [searchParams, setSearchParams] = useSearchParams();
  const accessTokenPayload = getAccessTokenPayload();
  const currentUserRole = String(accessTokenPayload?.role ?? '').toUpperCase();
  const canManageConsultation = currentUserRole === 'DOCTOR' || currentUserRole === 'ADMIN';
  const canDownloadPrescription =
    currentUserRole === 'PATIENT' || canManageConsultation;

  const [appointmentId, setAppointmentId] = useState(
    () => searchParams.get('appointmentId') ?? ''
  );
  const [consultationId, setConsultationId] = useState(
    () => searchParams.get('consultationId') ?? ''
  );
  const [prescriptionId, setPrescriptionId] = useState(
    () => searchParams.get('prescriptionId') ?? ''
  );

  const [consultationStatus, setConsultationStatus] = useState('');
  const [startedAt, setStartedAt] = useState('');
  const [endedAt, setEndedAt] = useState('');
  const [summaryJson, setSummaryJson] = useState('');
  const [downloadUrl, setDownloadUrl] = useState('');
  const [statusMessage, setStatusMessage] = useState(null);

  const [isStartingConsultation, setIsStartingConsultation] = useState(false);
  const [isEndingConsultation, setIsEndingConsultation] = useState(false);
  const [isSavingSummary, setIsSavingSummary] = useState(false);
  const [isGeneratingPrescription, setIsGeneratingPrescription] = useState(false);
  const [isFetchingDownloadUrl, setIsFetchingDownloadUrl] = useState(false);

  const summaryPreview = getSummaryPreview(summaryJson);
  const hasSummary = Boolean(summaryJson.trim());
  const prescriptionReady = Boolean(prescriptionId);

  useEffect(() => {
    const nextAppointmentId = searchParams.get('appointmentId') ?? '';
    const nextConsultationId = searchParams.get('consultationId') ?? '';
    const nextPrescriptionId = searchParams.get('prescriptionId') ?? '';

    setAppointmentId((currentValue) =>
      currentValue === nextAppointmentId ? currentValue : nextAppointmentId
    );
    setConsultationId((currentValue) =>
      currentValue === nextConsultationId ? currentValue : nextConsultationId
    );
    setPrescriptionId((currentValue) =>
      currentValue === nextPrescriptionId ? currentValue : nextPrescriptionId
    );
  }, [searchParams]);

  function updateRouteContext(values) {
    setSearchParams(buildNextSearchParams(searchParams, values), { replace: true });
  }

  function resetPrescriptionOutputs() {
    setPrescriptionId('');
    setDownloadUrl('');
  }

  function handleAppointmentIdChange(value) {
    setAppointmentId(value);
    setConsultationId('');
    setConsultationStatus('');
    setStartedAt('');
    setEndedAt('');
    setSummaryJson('');
    resetPrescriptionOutputs();
    setStatusMessage(null);
  }

  function handleConsultationIdChange(value) {
    setConsultationId(value);
    setSummaryJson('');
    setConsultationStatus('');
    setStartedAt('');
    setEndedAt('');
    resetPrescriptionOutputs();
    setStatusMessage(null);
  }

  function handlePrescriptionIdChange(value) {
    setPrescriptionId(value);
    setDownloadUrl('');
    setStatusMessage(null);
  }

  async function handleStartConsultation() {
    const trimmedAppointmentId = appointmentId.trim();

    if (!trimmedAppointmentId) {
      setStatusMessage({
        tone: 'error',
        text: 'appointmentId is required to start a consultation.'
      });
      return;
    }

    try {
      setIsStartingConsultation(true);
      setStatusMessage(null);
      setSummaryJson('');
      resetPrescriptionOutputs();

      const consultation = await startPrescriptionConsultation(trimmedAppointmentId);

      setConsultationId(consultation.id ?? '');
      setConsultationStatus(consultation.status ?? '');
      setStartedAt(consultation.startedAt ?? '');
      setEndedAt(consultation.endedAt ?? '');
      updateRouteContext({
        appointmentId: trimmedAppointmentId,
        consultationId: consultation.id ?? '',
        prescriptionId: ''
      });
      setStatusMessage({
        tone: 'success',
        text: `Consultation ${consultation.id} is ready for appointment ${trimmedAppointmentId}.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setIsStartingConsultation(false);
    }
  }

  async function handleEndConsultation() {
    const trimmedConsultationId = consultationId.trim();

    if (!trimmedConsultationId) {
      setStatusMessage({
        tone: 'error',
        text: 'consultationId is required to end the consultation.'
      });
      return;
    }

    try {
      setIsEndingConsultation(true);
      setStatusMessage(null);
      resetPrescriptionOutputs();

      const summary = await endPrescriptionConsultation(trimmedConsultationId);

      setConsultationStatus('ENDED');
      setEndedAt(new Date().toISOString());
      setSummaryJson(normalizeSummaryJson(summary));
      updateRouteContext({
        appointmentId: appointmentId.trim(),
        consultationId: trimmedConsultationId,
        prescriptionId: ''
      });
      setStatusMessage({
        tone: 'success',
        text: `Summary generated for consultation ${trimmedConsultationId}. Review and edit the JSON if needed.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setIsEndingConsultation(false);
    }
  }

  async function handlePatchSummary() {
    const trimmedConsultationId = consultationId.trim();
    const trimmedSummary = summaryJson.trim();

    if (!trimmedConsultationId) {
      setStatusMessage({
        tone: 'error',
        text: 'consultationId is required to patch the summary.'
      });
      return;
    }

    if (!trimmedSummary) {
      setStatusMessage({
        tone: 'error',
        text: 'json_summary is required before patching.'
      });
      return;
    }

    try {
      setIsSavingSummary(true);
      setStatusMessage(null);

      const updatedSummary = await updatePrescriptionConsultationSummary(
        trimmedConsultationId,
        trimmedSummary
      );

      setSummaryJson(normalizeSummaryJson(updatedSummary.json_summary));
      setStatusMessage({
        tone: 'success',
        text: `Summary patched for consultation ${trimmedConsultationId}.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setIsSavingSummary(false);
    }
  }

  async function handleGeneratePrescription() {
    const trimmedConsultationId = consultationId.trim();

    if (!trimmedConsultationId) {
      setStatusMessage({
        tone: 'error',
        text: 'consultationId is required to generate the prescription PDF.'
      });
      return;
    }

    try {
      setIsGeneratingPrescription(true);
      setStatusMessage(null);
      setDownloadUrl('');

      const generatedPrescription = await generatePrescriptionPdf(trimmedConsultationId);

      setPrescriptionId(generatedPrescription.prescriptionId ?? '');
      updateRouteContext({
        appointmentId: appointmentId.trim(),
        consultationId: trimmedConsultationId,
        prescriptionId: generatedPrescription.prescriptionId ?? ''
      });
      setStatusMessage({
        tone: 'success',
        text: `Prescription ${generatedPrescription.prescriptionId} generated successfully.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setIsGeneratingPrescription(false);
    }
  }

  async function handleFetchDownloadUrl() {
    const trimmedPrescriptionId = prescriptionId.trim();

    if (!trimmedPrescriptionId) {
      setStatusMessage({
        tone: 'error',
        text: 'prescriptionId is required to fetch the download URL.'
      });
      return;
    }

    try {
      setIsFetchingDownloadUrl(true);
      setStatusMessage(null);

      const response = await getPrescriptionDownloadUrl(trimmedPrescriptionId);

      setDownloadUrl(response.downloadUrl ?? '');
      updateRouteContext({
        appointmentId: appointmentId.trim(),
        consultationId: consultationId.trim(),
        prescriptionId: trimmedPrescriptionId
      });
      setStatusMessage({
        tone: 'success',
        text: `Download URL ready for prescription ${trimmedPrescriptionId}.`
      });
    } catch (error) {
      setStatusMessage({
        tone: 'error',
        text: error.message
      });
    } finally {
      setIsFetchingDownloadUrl(false);
    }
  }

  return (
    <div className="space-y-5">
      <PageIntro
        eyebrow="Prescription flow"
        title="Run consultation close-out and deliver the prescription PDF."
        description="Doctors can start and end the prescription consultation workflow, edit the stored raw json summary, generate the prescription PDF, and fetch the download URL. Patients can open the prescription download URL using a prescription ID."
      />

      <div className="page-grid">
        <StatCard
          label="Active role"
          value={currentUserRole || 'Unknown'}
          hint="Doctor and admin can manage consultation close-out. Patients can open download links."
        />
        <StatCard
          label="Consultation state"
          value={formatStatusLabel(consultationStatus)}
          hint={consultationId ? `Consultation ID: ${consultationId}` : 'No consultation selected'}
        />
        <StatCard
          label="Prescription state"
          value={prescriptionReady ? 'Generated' : 'Not generated'}
          hint={prescriptionId ? `Prescription ID: ${prescriptionId}` : 'Generate a PDF or paste a prescription ID'}
        />
      </div>

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

      {canManageConsultation ? (
        <div className="grid gap-5 xl:grid-cols-[0.94fr_1.06fr]">
          <SectionCard title="Consultation controls" eyebrow="Doctor workflow">
            <div className="space-y-5">
              <label className="space-y-2">
                <span className="block text-sm font-medium text-slate-700">Appointment ID</span>
                <input
                  className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Paste appointment UUID"
                  value={appointmentId}
                  onChange={(event) => handleAppointmentIdChange(event.target.value)}
                />
              </label>

              <button
                className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
                disabled={isStartingConsultation || isEndingConsultation}
                onClick={handleStartConsultation}
                type="button"
              >
                {isStartingConsultation ? 'Starting consultation...' : 'Start consultation'}
              </button>

              <label className="space-y-2">
                <span className="block text-sm font-medium text-slate-700">Consultation ID</span>
                <input
                  className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                  placeholder="Paste consultation UUID"
                  value={consultationId}
                  onChange={(event) => handleConsultationIdChange(event.target.value)}
                />
              </label>

              <button
                className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                disabled={!consultationId.trim() || isEndingConsultation || isStartingConsultation}
                onClick={handleEndConsultation}
                type="button"
              >
                {isEndingConsultation ? 'Ending consultation...' : 'End consultation'}
              </button>

              <div className="space-y-3 text-sm text-slate-600">
                <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
                  <p className="font-semibold text-ink">Started</p>
                  <p className="mt-2">{formatTimestamp(startedAt)}</p>
                </div>
                <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
                  <p className="font-semibold text-ink">Ended</p>
                  <p className="mt-2">{formatTimestamp(endedAt)}</p>
                </div>
                <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4">
                  `POST /prescriptions/consultations/end` stores a raw `json_summary` behind the
                  scenes. The textarea on the right edits that stored string directly.
                </div>
              </div>
            </div>
          </SectionCard>

          <SectionCard title="Summary editor" eyebrow="Raw json_summary">
            <div className="space-y-4">
              <textarea
                className="min-h-[320px] w-full rounded-[28px] border border-slate-200 bg-white px-4 py-4 font-mono text-sm outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                placeholder={`{\n  "complaint": "",\n  "advice": "",\n  "notes": ""\n}`}
                value={summaryJson}
                onChange={(event) => setSummaryJson(event.target.value)}
              />

              <div className="flex flex-col gap-3 md:flex-row">
                <button
                  className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-700"
                  disabled={!consultationId.trim() || !hasSummary || isSavingSummary}
                  onClick={handlePatchSummary}
                  type="button"
                >
                  {isSavingSummary ? 'Patching summary...' : 'Patch summary'}
                </button>
                <button
                  className="rounded-2xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white"
                  disabled={!consultationId.trim() || !hasSummary || isGeneratingPrescription}
                  onClick={handleGeneratePrescription}
                  type="button"
                >
                  {isGeneratingPrescription ? 'Generating PDF...' : 'Generate PDF'}
                </button>
              </div>

              <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4 text-sm text-slate-600">
                Valid JSON with `complaint`, `advice`, and `notes` produces a cleaner PDF. If the
                content is not valid JSON, the backend falls back to placing the raw text into the
                PDF notes section.
              </div>
            </div>
          </SectionCard>
        </div>
      ) : null}

      <div className="grid gap-5 xl:grid-cols-[1.04fr_0.96fr]">
        <SectionCard title="Summary preview" eyebrow="Rendered fields">
          <div className="space-y-4 text-sm text-slate-600">
            {!hasSummary ? (
              <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
                No summary is loaded yet. End a consultation to generate one, or paste a raw
                `json_summary` string into the editor if you already have it.
              </div>
            ) : null}

            {hasSummary && !summaryPreview.isValidJson ? (
              <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-amber-900">
                The current summary is not valid JSON. The PDF generator will treat the full text
                as notes.
              </div>
            ) : null}

            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Complaint</p>
              <p className="mt-2 whitespace-pre-wrap">
                {summaryPreview.complaint || 'No complaint recorded'}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Advice</p>
              <p className="mt-2 whitespace-pre-wrap">
                {summaryPreview.advice || 'No advice recorded'}
              </p>
            </div>

            <div className="rounded-2xl border border-slate-200/80 bg-white/80 p-4">
              <p className="font-semibold text-ink">Notes</p>
              <p className="mt-2 whitespace-pre-wrap break-words">
                {summaryPreview.notes || 'No notes recorded'}
              </p>
            </div>
          </div>
        </SectionCard>

        <SectionCard
          title="Prescription access"
          eyebrow={currentUserRole === 'PATIENT' ? 'Patient download' : 'Download and share'}
        >
          <div className="space-y-4">
            {!canDownloadPrescription ? (
              <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
                This role cannot access prescription downloads.
              </div>
            ) : null}

            <label className="space-y-2">
              <span className="block text-sm font-medium text-slate-700">Prescription ID</span>
              <input
                className="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 outline-none focus:border-spruce focus:ring-4 focus:ring-spruce/10"
                placeholder="Paste prescription UUID"
                value={prescriptionId}
                onChange={(event) => handlePrescriptionIdChange(event.target.value)}
              />
            </label>

            <button
              className="rounded-2xl bg-spruce px-4 py-3 text-sm font-semibold text-white"
              disabled={!canDownloadPrescription || !prescriptionId.trim() || isFetchingDownloadUrl}
              onClick={handleFetchDownloadUrl}
              type="button"
            >
              {isFetchingDownloadUrl ? 'Fetching download URL...' : 'Get download URL'}
            </button>

            {downloadUrl ? (
              <div className="space-y-3 rounded-2xl border border-slate-200/80 bg-white/80 p-4 text-sm text-slate-600">
                <p className="font-semibold text-ink">Download URL</p>
                <p className="break-all">{downloadUrl}</p>
                <a
                  className="inline-flex rounded-2xl border border-slate-200 bg-white px-4 py-3 font-semibold text-slate-700"
                  href={downloadUrl}
                  rel="noreferrer"
                  target="_blank"
                >
                  Open prescription PDF
                </a>
              </div>
            ) : null}

            <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50/80 p-4 text-sm text-slate-600">
              Doctors can generate the prescription ID here and share it through the consultation
              flow. Patients can open the returned download URL from the same page once they have
              the prescription ID.
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
}

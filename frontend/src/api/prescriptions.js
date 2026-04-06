import apiClient, { getApiErrorMessage, unwrapApiResponse } from './client';

export async function startPrescriptionConsultation(appointmentId) {
  try {
    const response = await apiClient.post('/prescriptions/consultations/start', null, {
      params: {
        appointmentId
      }
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function endPrescriptionConsultation(consultationId) {
  try {
    const response = await apiClient.post('/prescriptions/consultations/end', null, {
      params: {
        consultationId
      }
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function updatePrescriptionConsultationSummary(consultationId, jsonSummary) {
  try {
    const response = await apiClient.patch(`/prescriptions/consultations/${consultationId}/summary`, {
      json_summary: jsonSummary
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function generatePrescriptionPdf(consultationId) {
  try {
    const response = await apiClient.post(
      `/prescriptions/consultations/${consultationId}/prescription/generate`
    );

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function getPrescriptionDownloadUrl(prescriptionId) {
  try {
    const response = await apiClient.get(`/prescriptions/${prescriptionId}/download`);
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

import apiClient, { getApiErrorMessage, unwrapApiResponse } from './client';

export async function requestOtp(phone) {
  try {
    const response = await apiClient.post('/auth/request-otp', { phone });
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function verifyOtp(sessionId, otp) {
  try {
    const response = await apiClient.post('/auth/verify-otp', { sessionId, otp });
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

import apiClient, { getApiErrorMessage, unwrapApiResponse } from './client';

export async function getPendingDoctors() {
  try {
    const response = await apiClient.get('/users/admin/doctors/pending');
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function verifyDoctor(doctorId) {
  try {
    const response = await apiClient.post(`/users/admin/doctor/${doctorId}/verify`);
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function rejectDoctor(doctorId) {
  try {
    const response = await apiClient.post(`/users/admin/doctor/${doctorId}/reject`);
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

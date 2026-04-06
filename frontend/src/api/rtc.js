import apiClient, { getApiErrorMessage } from './client';

export async function createRtcRoom(appointmentId) {
  try {
    const response = await apiClient.post('/rtc/room/create', null, {
      params: {
        appointmentId
      }
    });

    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function getRtcRoomStatus(appointmentId) {
  try {
    const response = await apiClient.get(`/rtc/room/${appointmentId}/status`);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

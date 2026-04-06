import apiClient, { getApiErrorMessage, unwrapApiResponse } from './client';

export async function getAppointmentSlots(doctorId, date) {
  try {
    const response = await apiClient.get('/appointments/slots', {
      params: {
        doctorId,
        date
      }
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function bookAppointment(doctorId, startTs) {
  try {
    const response = await apiClient.post('/appointments/book', {
      doctorId,
      startTs
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function joinWaitingRoom(appointmentId) {
  try {
    const response = await apiClient.post(`/appointments/${appointmentId}/join-waiting-room`);
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function getDoctorAvailability() {
  try {
    const response = await apiClient.get('/appointments/doctor/availability');
    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function replaceDoctorAvailability(slots) {
  try {
    const response = await apiClient.post('/appointments/doctor/availability', {
      slots
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function getDoctorQueue(doctorId) {
  try {
    const response = await apiClient.get('/appointments/doctor/queue', {
      params: {
        doctorId
      }
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

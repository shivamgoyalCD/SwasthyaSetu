import axios from 'axios';
import { getAccessToken } from '../utils/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use((config) => {
  const accessToken = getAccessToken();

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

export function unwrapApiResponse(response) {
  const payload = response?.data;

  if (!payload?.success) {
    throw new Error(payload?.error?.message ?? 'Request failed.');
  }

  return payload.data;
}

export function getApiErrorMessage(error) {
  return (
    error?.response?.data?.error?.message ??
    error?.response?.data?.message ??
    error?.message ??
    'Something went wrong.'
  );
}

export { API_BASE_URL };
export default apiClient;

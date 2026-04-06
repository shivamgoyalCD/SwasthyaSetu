import apiClient, { getApiErrorMessage, unwrapApiResponse } from './client';

export async function startConversation(appointmentId) {
  try {
    const response = await apiClient.post('/chat/conversations/start', null, {
      params: {
        appointmentId
      }
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function getConversationMessages(conversationId, cursor) {
  try {
    const response = await apiClient.get(`/chat/conversations/${conversationId}/messages`, {
      params: cursor ? { cursor } : {}
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

export async function createConversationMessage(
  conversationId,
  { content, originalLang, targetLang }
) {
  try {
    const response = await apiClient.post(`/chat/conversations/${conversationId}/messages`, {
      type: 'TEXT',
      content,
      originalLang,
      targetLang
    });

    return unwrapApiResponse(response);
  } catch (error) {
    throw new Error(getApiErrorMessage(error));
  }
}

import client from './client'

export const listMessages = (swapId, afterId = 0) =>
  client.get(`/swaps/${swapId}/messages`, { params: { afterId } }).then((r) => r.data)
export const sendMessage = (swapId, text) =>
  client.post(`/swaps/${swapId}/messages`, { text }).then((r) => r.data)

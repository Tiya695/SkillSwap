import client from './client'

export const createSwap = (data) => client.post('/swaps', data).then((r) => r.data)
export const listSwaps = (params) => client.get('/swaps', { params }).then((r) => r.data)
export const getSwap = (id) => client.get(`/swaps/${id}`).then((r) => r.data)
export const updateSwapStatus = (id, status) =>
  client.patch(`/swaps/${id}/status`, { status }).then((r) => r.data)

export const finishEarly = (id) => client.post(`/swaps/${id}/finish-early`).then((r) => r.data)
export const setMeetingLink = (id, url) =>
  client.patch(`/swaps/${id}/meeting-link`, { url }).then((r) => r.data)

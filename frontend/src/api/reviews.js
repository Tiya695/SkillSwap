import client from './client'

export const addReview = (swapId, data) =>
  client.post(`/swaps/${swapId}/reviews`, data).then((r) => r.data)
export const listUserReviews = (userId) => client.get(`/users/${userId}/reviews`).then((r) => r.data)

import client from './client'

export const getAssessments = (swapId) => client.get(`/swaps/${swapId}/assessments`).then((r) => r.data)

export const createAssessment = (swapId, questions) =>
  client.post(`/swaps/${swapId}/assessments`, { questions }).then((r) => r.data)

export const submitAnswers = (assessmentId, answers) =>
  client.post(`/assessments/${assessmentId}/submit`, { answers }).then((r) => r.data)

export const allowRetake = (assessmentId) =>
  client.post(`/assessments/${assessmentId}/retake`).then((r) => r.data)

export const generateQuestions = (swapId, topic, count) =>
  client.post(`/swaps/${swapId}/assessments/generate`, { topic, count }).then((r) => r.data)

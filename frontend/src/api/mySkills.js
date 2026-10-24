import client from './client'

export const listMySkills = (type) =>
  client.get('/me/skills', { params: type ? { type } : {} }).then((r) => r.data)
export const addMySkill = (data) => client.post('/me/skills', data).then((r) => r.data)
export const updateMySkill = (id, data) => client.put(`/me/skills/${id}`, data).then((r) => r.data)
export const deleteMySkill = (id) => client.delete(`/me/skills/${id}`)

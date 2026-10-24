import client from './client'

export const listSkills = (params) => client.get('/skills', { params }).then((r) => r.data)
export const listCategories = () => client.get('/skills/categories').then((r) => r.data)
export const createSkill = (data) => client.post('/skills', data).then((r) => r.data)
export const updateSkill = (id, data) => client.put(`/skills/${id}`, data).then((r) => r.data)
export const deleteSkill = (id) => client.delete(`/skills/${id}`)

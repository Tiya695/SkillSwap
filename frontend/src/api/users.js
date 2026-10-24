import client from './client'

export const getMe = () => client.get('/users/me').then((r) => r.data)
export const updateMe = (data) => client.put('/users/me', data).then((r) => r.data)
export const getProfile = (id) => client.get(`/users/${id}`).then((r) => r.data)

export const uploadAvatar = (file) => {
  const form = new FormData()
  form.append('file', file)
  return client
    .post('/users/me/avatar', form, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((r) => r.data)
}
export const removeAvatar = () => client.delete('/users/me/avatar').then((r) => r.data)

export const deleteAccount = (password) => client.delete('/users/me', { data: { password } })

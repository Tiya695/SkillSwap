import client from './client'

export const listMatches = () => client.get('/matches').then((r) => r.data)

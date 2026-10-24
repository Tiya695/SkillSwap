
export function getErrorMessage(error) {
  if (error?.response?.data?.message) return error.response.data.message
  if (error?.code === 'ERR_NETWORK') return 'Cannot reach the server. Is the backend running?'
  return 'Something went wrong. Please try again.'
}

export function getFieldErrors(error) {
  return error?.response?.data?.fieldErrors || {}
}

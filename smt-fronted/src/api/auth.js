import { request } from '../utils/request'

export const signIn = (data) => request('/auth/sign-in', {
  method: 'POST',
  body: JSON.stringify(data)
})

export const signOut = () => request('/auth/sign-out', { method: 'POST' })

export const getMe = (options = {}) => request('/auth/me', options)

export const updatePassword = (data) => request('/auth/me/password', {
  method: 'PUT',
  body: JSON.stringify(data)
})

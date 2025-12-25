import { request } from '../utils/request'

export const getUsers = () => request('/admin/users')

export const createUser = (data) => request('/admin/users', {
  method: 'POST',
  body: JSON.stringify(data)
})

export const updateUser = (id, data) => request(`/admin/users/${id}`, {
  method: 'PUT',
  body: JSON.stringify(data)
})

export const deleteUser = (id) => request(`/admin/users/${id}`, {
  method: 'DELETE'
})
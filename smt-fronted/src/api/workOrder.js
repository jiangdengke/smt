import { request } from '../utils/request'

export const getWorkOrders = (query = {}) => {
  const params = new URLSearchParams(
    Object.entries(query).filter(([, value]) => value !== null && value !== '' && value !== undefined)
  ).toString()
  return request(`/repair-work-orders${params ? `?${params}` : ''}`)
}

export const completeWorkOrder = (id, data) =>
  request(`/repair-work-orders/${id}/complete`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })

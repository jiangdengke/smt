import { request } from '../utils/request'

export const getRepairRecords = (query) => {
  const params = new URLSearchParams(
    Object.entries(query).filter(([, value]) => value !== null && value !== '' && value !== undefined)
  ).toString()
  return request(`/repair-records${params ? `?${params}` : ''}`)
}

export const createRepairRecord = (data) => request('/repair-records', {
  method: 'POST',
  body: JSON.stringify(data)
})

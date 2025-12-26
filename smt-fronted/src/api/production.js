import { request } from '../utils/request'

export const getProductionDaily = (prodDate, shift) => {
  const params = new URLSearchParams({ prodDate, shift }).toString()
  return request(`/production-daily?${params}`)
}

export const saveProductionDailyBatch = (data) =>
  request('/production-daily/batch', {
    method: 'POST',
    body: JSON.stringify(data)
  })

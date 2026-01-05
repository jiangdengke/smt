import { request } from '../utils/request'

export const getProductionDaily = (prodDate, shift, factoryName, workshopName, lineName) => {
  const params = new URLSearchParams({
    prodDate,
    shift,
    factoryName,
    workshopName,
    lineName
  }).toString()
  return request(`/production-daily?${params}`)
}

export const saveProductionDailyBatch = (data) => request('/production-daily/batch', {
  method: 'POST',
  body: JSON.stringify(data)
})

export const getProductionDailyRecords = () => request('/production-daily/records')

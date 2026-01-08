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

export const exportProductionDailyRecords = async (ids) => {
  const response = await fetch(`${import.meta.env.VITE_API_BASE || '/api'}/production-daily/records/export`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ids })
  })

  if (!response.ok) {
    throw new Error('导出失败')
  }

  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  let fileName = '每日产能.xlsx'
  const disposition = response.headers.get('content-disposition')
  if (disposition && disposition.includes('filename*=')) {
    const matches = /filename\*=(utf-8'')?(.+)/.exec(disposition)
    if (matches && matches[2]) {
      fileName = decodeURIComponent(matches[2])
    }
  }
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  window.URL.revokeObjectURL(url)
  document.body.removeChild(a)
}

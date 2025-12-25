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

export const updateRepairRecord = (id, data) => request(`/repair-records/${id}`, {
  method: 'PUT',
  body: JSON.stringify(data)
})

export const deleteRepairRecord = (id) => request(`/repair-records/${id}`, {
  method: 'DELETE'
})

export const exportRepairRecords = async (query) => {
  const params = new URLSearchParams(
    Object.entries(query).filter(([, value]) => value !== null && value !== '' && value !== undefined)
  ).toString()
  
  const response = await fetch(`${import.meta.env.VITE_API_BASE || '/api'}/repair-records/export?${params}`, {
    method: 'GET',
    credentials: 'include'
  })

  if (!response.ok) {
    throw new Error('导出失败')
  }

  const blob = await response.blob()
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  // Try to get filename from header
  const disposition = response.headers.get('content-disposition')
  let fileName = '维修记录.xlsx'
  if (disposition && disposition.indexOf('filename*=') !== -1) {
    const matches = /filename\*=(utf-8'')?(.+)/.exec(disposition)
    if (matches != null && matches[2]) { 
      fileName = decodeURIComponent(matches[2])
    }
  }
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  window.URL.revokeObjectURL(url)
  document.body.removeChild(a)
}

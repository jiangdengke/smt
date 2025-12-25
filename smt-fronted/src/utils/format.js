export const formatDate = (value) => {
  if (!value) return ''
  const text = String(value)
  if (text.includes('T')) return text.split('T')[0]
  if (text.includes(' ')) return text.split(' ')[0]
  return text
}

export const formatShift = (value) => {
  if (!value) return ''
  const normalized = String(value).toUpperCase()
  if (normalized === 'DAY') return '白'
  if (normalized === 'NIGHT') return '夜'
  return value
}

export const toDateTime = (dateValue, endOfDay = false) => {
  if (!dateValue) return null
  const dateText = String(dateValue).trim()
  if (!dateText) return null
  return endOfDay ? `${dateText}T23:59:59` : `${dateText}T00:00:00`
}

export const normalizeText = (value) => {
  if (value === null || value === undefined) return null
  const text = String(value).trim()
  return text.length ? text : null
}

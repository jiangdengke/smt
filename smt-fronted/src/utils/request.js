const apiBase = import.meta.env.VITE_API_BASE || '/api'

export const formatErrorMessage = (error, fallback) => {
  const message = error?.message || fallback || '请求失败'
  if (
    message.includes('Failed to fetch') ||
    message.includes('NetworkError') ||
    message.includes('Load failed')
  ) {
    return '网络异常，请检查后端服务是否启动'
  }
  if (message.toLowerCase().includes('system error')) {
    return '系统异常，请稍后再试'
  }
  return message
}

export const request = async (path, options = {}) => {
  const { allowUnauthorized, ...fetchOptions } = options
  const response = await fetch(`${apiBase}${path}`, {
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...(fetchOptions.headers || {}) },
    ...fetchOptions
  })
  
  if (!response.ok) {
    if (response.status === 401 && allowUnauthorized) return null
    
    const contentType = response.headers.get('content-type') || ''
    const text = await response.text()
    let message = response.statusText
    
    if (text) {
      const isJson =
        contentType.includes('application/json') ||
        contentType.includes('application/problem+json') ||
        text.trim().startsWith('{')
      
      if (isJson) {
        try {
          const payload = JSON.parse(text)
          message = payload?.detail || payload?.message || payload?.title || message
        } catch (error) {
          message = text
        }
      } else {
        message = text
      }
    }
    throw new Error(message || response.statusText)
  }
  
  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''
  const text = await response.text()
  if (!text) return null
  
  if (contentType.includes('application/json')) {
    return JSON.parse(text)
  }
  return text
}

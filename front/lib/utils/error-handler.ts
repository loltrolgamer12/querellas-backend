/**
 * Utility functions for error handling
 */

export interface NetworkError {
  status: number
  message: string
}

/**
 * Checks if an error is a network/connection error
 */
export function isNetworkError(error: any): boolean {
  if (!error) return false

  const errorStatus = error?.status || error?.response?.status || 0
  const errorMessage = error?.message || error?.toString() || ''
  const isEmptyObject = error && typeof error === 'object' && Object.keys(error).length === 0

  return (
    errorStatus === 0 ||
    errorMessage.includes('No se pudo conectar') ||
    errorMessage.includes('Failed to fetch') ||
    errorMessage.includes('NetworkError') ||
    errorMessage.includes('Network request failed') ||
    error instanceof TypeError ||
    isEmptyObject
  )
}

/**
 * Safely extracts error information
 */
export function extractErrorInfo(error: any): { status: number; message: string } {
  return {
    status: error?.status || error?.response?.status || 0,
    message: error?.message || error?.toString() || 'Error desconocido',
  }
}




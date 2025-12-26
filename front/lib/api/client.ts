import { API_BASE_URL } from './config'

export interface ApiError {
  message: string
  status: number
  errors?: Record<string, string[]>
}

class ApiClient {
  private baseUrl: string
  private token: string | null = null

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl
    // Load token from localStorage on initialization
    if (typeof window !== 'undefined') {
      this.token = localStorage.getItem('auth_token')
    }
  }

  setToken(token: string | null) {
    this.token = token
    if (token && typeof window !== 'undefined') {
      localStorage.setItem('auth_token', token)
    } else if (typeof window !== 'undefined') {
      localStorage.removeItem('auth_token')
    }
  }

  getToken(): string | null {
    return this.token
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`
    
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    }

    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`
    }

    const config: RequestInit = {
      ...options,
      headers,
    }

    try {
      const response = await fetch(url, config)

      if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`
        let errors: Record<string, string[]> | undefined

        try {
          const errorData = await response.json()
          errorMessage = errorData.message || errorMessage
          errors = errorData.errors
        } catch {
          // If response is not JSON, use status text
          errorMessage = response.statusText || errorMessage
        }

        const error: ApiError = {
          message: errorMessage,
          status: response.status,
          errors,
        }

        // Handle 401 Unauthorized - token might be expired
        if (response.status === 401) {
          this.setToken(null)
          if (typeof window !== 'undefined') {
            window.location.href = '/login'
          }
        }

        throw error
      }

      // Handle empty responses (204 No Content)
      if (response.status === 204) {
        return undefined as T
      }

      return await response.json()
    } catch (error) {
      // Si es un error de red (Failed to fetch), proporcionar un mensaje más claro
      if (error instanceof TypeError) {
        const isNetworkError = 
          error.message.includes('fetch') || 
          error.message.includes('Failed to fetch') ||
          error.message.includes('NetworkError') ||
          error.message.includes('Network request failed')
        
        if (isNetworkError) {
          throw {
            message: `No se pudo conectar con el servidor. Asegúrate de que el backend esté corriendo en ${this.baseUrl}`,
            status: 0,
          } as ApiError
        }
      }
      
      // Si el error ya es un ApiError, re-lanzarlo
      if (error && typeof error === 'object' && 'status' in error) {
        throw error
      }
      
      // Para cualquier otro error, crear un ApiError
      throw {
        message: error instanceof Error ? error.message : (typeof error === 'string' ? error : 'Error desconocido de conexión'),
        status: 0,
      } as ApiError
    }
  }

  async get<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'GET' })
  }

  async post<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async put<T>(endpoint: string, data?: unknown, options?: RequestInit): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async delete<T>(endpoint: string, options?: RequestInit): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: 'DELETE' })
  }

  // For file uploads
  async upload<T>(
    endpoint: string,
    formData: FormData,
    options?: RequestInit
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`
    
    const headers: HeadersInit = {}
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`
    }

    const config: RequestInit = {
      ...options,
      method: 'POST',
      headers,
      body: formData,
    }

    try {
      const response = await fetch(url, config)

      if (!response.ok) {
        let errorMessage = `HTTP error! status: ${response.status}`
        try {
          const errorData = await response.json()
          errorMessage = errorData.message || errorMessage
        } catch {
          errorMessage = response.statusText || errorMessage
        }

        throw {
          message: errorMessage,
          status: response.status,
        } as ApiError
      }

      if (response.status === 204) {
        return undefined as T
      }

      return await response.json()
    } catch (error) {
      // Si es un error de red (Failed to fetch), proporcionar un mensaje más claro
      if (error instanceof TypeError && error.message.includes('fetch')) {
        throw {
          message: `No se pudo conectar con el servidor. Asegúrate de que el backend esté corriendo en ${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081'}`,
          status: 0,
        } as ApiError
      }
      if (error instanceof Error && 'status' in error) {
        throw error
      }
      throw {
        message: error instanceof Error ? error.message : 'Unknown error',
        status: 0,
      } as ApiError
    }
  }
}

export const apiClient = new ApiClient(API_BASE_URL)


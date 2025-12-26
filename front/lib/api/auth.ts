import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { LoginRequest, LoginResponse } from '../types'

export interface RegisterRequest {
  nombre: string
  email: string
  telefono?: string
  rol: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR'
  zona?: 'NEIVA' | 'CORREGIMIENTO'
  password: string
}

export interface RefreshTokenRequest {
  token: string
}

export const authApi = {
  // Login
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.LOGIN, credentials)
    if (response.token) {
      apiClient.setToken(response.token)
    }
    return response
  },

  // Register
  register: async (data: RegisterRequest) => {
    const response = await apiClient.post(API_ENDPOINTS.AUTH.REGISTER, data)
    if (response.token) {
      apiClient.setToken(response.token)
    }
    return response
  },

  // Refresh token
  refresh: async (data: RefreshTokenRequest): Promise<LoginResponse> => {
    const response = await apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.REFRESH, data)
    if (response.token) {
      apiClient.setToken(response.token)
    }
    return response
  },

  // Logout (client-side only)
  logout: () => {
    apiClient.setToken(null)
  },
}






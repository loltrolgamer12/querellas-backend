import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { User, PaginatedResponse } from '../types'

export interface UsuarioListParams {
  role?: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR'
  page?: number
  size?: number
}

export interface UsuarioCreateParams {
  nombre: string
  email: string
  telefono?: string
  rol: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR'
  zona?: 'NEIVA' | 'CORREGIMIENTO'
  password: string
}

export interface UsuarioUpdateParams {
  nombre?: string
  email?: string
  telefono?: string
  rol?: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR'
  zona?: 'NEIVA' | 'CORREGIMIENTO'
  password?: string
}

export interface CambioEstadoUsuarioParams {
  nuevoEstado: 'ACTIVO' | 'BLOQUEADO' | 'NO_DISPONIBLE'
}

export const usuariosApi = {
  // Listar usuarios con paginación
  list: async (params: UsuarioListParams = {}): Promise<PaginatedResponse<User>> => {
    const searchParams = new URLSearchParams()
    
    if (params.role) searchParams.append('role', params.role)
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())

    const queryString = searchParams.toString()
    const endpoint = queryString ? `${API_ENDPOINTS.USUARIOS.BASE}?${queryString}` : API_ENDPOINTS.USUARIOS.BASE
    
    return apiClient.get<PaginatedResponse<User>>(endpoint)
  },

  // Obtener usuario por ID
  getById: async (id: number): Promise<User> => {
    return apiClient.get<User>(API_ENDPOINTS.USUARIOS.BY_ID(id))
  },

  // Crear nuevo usuario
  create: async (data: UsuarioCreateParams): Promise<User> => {
    return apiClient.post<User>(API_ENDPOINTS.USUARIOS.BASE, data)
  },

  // Actualizar usuario
  update: async (id: number, data: UsuarioUpdateParams): Promise<User> => {
    return apiClient.put<User>(API_ENDPOINTS.USUARIOS.BASE.replace('/api/usuarios', `/api/usuarios/${id}`), data)
  },

  // Cambiar estado de usuario
  cambiarEstado: async (id: number, data: CambioEstadoUsuarioParams): Promise<User> => {
    return apiClient.put<User>(API_ENDPOINTS.USUARIOS.CAMBIAR_ESTADO(id), data)
  },

  // Eliminar usuario
  delete: async (id: number): Promise<void> => {
    return apiClient.delete(API_ENDPOINTS.USUARIOS.BY_ID(id))
  },

  // Listar inspectores
  listInspectores: async (zona?: 'NEIVA' | 'CORREGIMIENTO'): Promise<User[]> => {
    const endpoint = zona
      ? `${API_ENDPOINTS.USUARIOS.INSPECTORES}?zona=${zona}`
      : API_ENDPOINTS.USUARIOS.INSPECTORES
    return apiClient.get<User[]>(endpoint)
  },
}






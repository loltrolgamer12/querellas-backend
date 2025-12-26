import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { ItemSimple } from '../types'

export interface CatalogoCreateParams {
  nombre: string
}

export const catalogosApi = {
  // Temas
  listTemas: async (): Promise<ItemSimple[]> => {
    return apiClient.get<ItemSimple[]>(API_ENDPOINTS.CATALOGOS.TEMAS)
  },

  createTema: async (data: CatalogoCreateParams): Promise<ItemSimple> => {
    return apiClient.post<ItemSimple>(API_ENDPOINTS.CATALOGOS.TEMAS, data)
  },

  updateTema: async (id: number, data: CatalogoCreateParams): Promise<ItemSimple> => {
    return apiClient.put<ItemSimple>(`${API_ENDPOINTS.CATALOGOS.TEMAS}/${id}`, data)
  },

  deleteTema: async (id: number): Promise<void> => {
    return apiClient.delete(`${API_ENDPOINTS.CATALOGOS.TEMAS}/${id}`)
  },

  // Comunas
  listComunas: async (): Promise<ItemSimple[]> => {
    return apiClient.get<ItemSimple[]>(API_ENDPOINTS.CATALOGOS.COMUNAS)
  },

  createComuna: async (data: CatalogoCreateParams): Promise<ItemSimple> => {
    return apiClient.post<ItemSimple>(API_ENDPOINTS.CATALOGOS.COMUNAS, data)
  },

  updateComuna: async (id: number, data: CatalogoCreateParams): Promise<ItemSimple> => {
    return apiClient.put<ItemSimple>(`${API_ENDPOINTS.CATALOGOS.COMUNAS}/${id}`, data)
  },

  deleteComuna: async (id: number): Promise<void> => {
    return apiClient.delete(`${API_ENDPOINTS.CATALOGOS.COMUNAS}/${id}`)
  },

  // Estados
  listEstados: async (modulo?: 'QUERELLA' | 'DESPACHO'): Promise<ItemSimple[]> => {
    if (modulo) {
      return apiClient.get<ItemSimple[]>(API_ENDPOINTS.CATALOGOS.ESTADOS_BY_MODULO(modulo))
    }
    return apiClient.get<ItemSimple[]>(API_ENDPOINTS.CATALOGOS.ESTADOS)
  },
}


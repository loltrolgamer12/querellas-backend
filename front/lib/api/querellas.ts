import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { Querella, PaginatedResponse, CambioEstadoRequest, AsignarInspectorRequest } from '../types'

export interface QuerellaListParams {
  qTexto?: string
  estadoNombre?: string
  inspectorId?: number
  temaNombre?: string
  comunaId?: number
  desde?: string
  hasta?: string
  page?: number
  size?: number
  sort?: string
}

export interface QuerellaCreateParams {
  direccion: string
  descripcion: string
  naturaleza: 'OFICIO' | 'PERSONA' | 'ANONIMA'
  temaId?: number
  comunaId?: number
  inspectorAsignadoId?: number
  asignadoPorId?: number
  barrio?: string
  generoQuerellante?: string
  generoQuerellado?: string
  normasAplicables?: string
  observaciones?: string
}

export const querellasApi = {
  // Listar querellas con filtros y paginación
  list: async (params: QuerellaListParams = {}): Promise<PaginatedResponse<Querella>> => {
    const searchParams = new URLSearchParams()
    
    if (params.qTexto) searchParams.append('qTexto', params.qTexto)
    if (params.estadoNombre) searchParams.append('estadoNombre', params.estadoNombre)
    if (params.inspectorId) searchParams.append('inspectorId', params.inspectorId.toString())
    if (params.temaNombre) searchParams.append('temaNombre', params.temaNombre)
    if (params.comunaId) searchParams.append('comunaId', params.comunaId.toString())
    if (params.desde) searchParams.append('desde', params.desde)
    if (params.hasta) searchParams.append('hasta', params.hasta)
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sort) searchParams.append('sort', params.sort)

    const queryString = searchParams.toString()
    const endpoint = queryString ? `${API_ENDPOINTS.QUERELLAS.BASE}?${queryString}` : API_ENDPOINTS.QUERELLAS.BASE
    
    return apiClient.get<PaginatedResponse<Querella>>(endpoint)
  },

  // Obtener querella por ID
  getById: async (id: number): Promise<Querella> => {
    return apiClient.get<Querella>(API_ENDPOINTS.QUERELLAS.BY_ID(id))
  },

  // Crear nueva querella
  create: async (data: QuerellaCreateParams): Promise<Querella> => {
    return apiClient.post<Querella>(API_ENDPOINTS.QUERELLAS.BASE, data)
  },

  // Asignar inspector
  asignarInspector: async (id: number, data: AsignarInspectorRequest, asignadoPorId?: number): Promise<Querella> => {
    const endpoint = asignadoPorId 
      ? `${API_ENDPOINTS.QUERELLAS.ASIGNAR_INSPECTOR(id)}?asignadoPorId=${asignadoPorId}`
      : API_ENDPOINTS.QUERELLAS.ASIGNAR_INSPECTOR(id)
    return apiClient.put<Querella>(endpoint, data)
  },

  // Cambiar estado
  cambiarEstado: async (id: number, data: CambioEstadoRequest): Promise<Querella> => {
    return apiClient.put<Querella>(API_ENDPOINTS.QUERELLAS.CAMBIAR_ESTADO(id), data)
  },

  // Obtener historial de estados
  getHistorial: async (id: number) => {
    return apiClient.get(API_ENDPOINTS.QUERELLAS.HISTORIAL(id))
  },

  // Obtener posibles duplicados
  getPosiblesDuplicados: async (id: number): Promise<Querella[]> => {
    return apiClient.get<Querella[]>(API_ENDPOINTS.QUERELLAS.POSIBLES_DUPLICADOS(id))
  },

  // Actualizar querella
  update: async (id: number, data: Partial<QuerellaCreateParams>): Promise<Querella> => {
    return apiClient.put<Querella>(API_ENDPOINTS.QUERELLAS.BY_ID(id), data)
  },
}


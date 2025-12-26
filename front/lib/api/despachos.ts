import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { DespachoComisorio } from '../types'

export interface DespachoListParams {
  page?: number
  size?: number
  sortBy?: string
  direction?: 'ASC' | 'DESC'
}

export interface DespachoCreateParams {
  fechaRecibido: string
  radicadoProceso?: string
  numeroDespacho: string
  entidadProcedente: string
  asunto: string
  demandanteApoderado?: string
  demandadoApoderado?: string
  inspectorAsignadoId?: number
  asignadoPorId?: number
  fechaDevolucion?: string
  observaciones?: string
}

export interface DespachoUpdateParams extends Partial<DespachoCreateParams> {}

export const despachosApi = {
  // Listar despachos con paginación
  list: async (params: DespachoListParams = {}): Promise<{ content: DespachoComisorio[], totalElements: number, totalPages: number, number: number, size: number }> => {
    const searchParams = new URLSearchParams()
    
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.direction) searchParams.append('direction', params.direction)

    const queryString = searchParams.toString()
    const endpoint = queryString ? `${API_ENDPOINTS.DESPACHOS.BASE}?${queryString}` : API_ENDPOINTS.DESPACHOS.BASE
    
    return apiClient.get(endpoint)
  },

  // Obtener despacho por ID
  getById: async (id: number): Promise<DespachoComisorio> => {
    return apiClient.get<DespachoComisorio>(API_ENDPOINTS.DESPACHOS.BY_ID(id))
  },

  // Crear nuevo despacho
  create: async (data: DespachoCreateParams): Promise<DespachoComisorio> => {
    return apiClient.post<DespachoComisorio>(API_ENDPOINTS.DESPACHOS.BASE, data)
  },

  // Actualizar despacho
  update: async (id: number, data: DespachoUpdateParams): Promise<DespachoComisorio> => {
    return apiClient.put<DespachoComisorio>(API_ENDPOINTS.DESPACHOS.BASE.replace('/api/despachos-comisorios', `/api/despachos-comisorios/${id}`), data)
  },

  // Listar pendientes
  listPendientes: async (): Promise<DespachoComisorio[]> => {
    return apiClient.get<DespachoComisorio[]>(API_ENDPOINTS.DESPACHOS.PENDIENTES)
  },

  // Listar devueltos
  listDevueltos: async (): Promise<DespachoComisorio[]> => {
    return apiClient.get<DespachoComisorio[]>(API_ENDPOINTS.DESPACHOS.DEVUELTOS)
  },

  // Listar por inspector
  listPorInspector: async (inspectorId: number): Promise<DespachoComisorio[]> => {
    return apiClient.get<DespachoComisorio[]>(API_ENDPOINTS.DESPACHOS.POR_INSPECTOR(inspectorId))
  },

  // Asignar inspector
  asignarInspector: async (id: number, inspectorId: number, asignadoPorId?: number): Promise<DespachoComisorio> => {
    const endpoint = asignadoPorId
      ? `${API_ENDPOINTS.DESPACHOS.ASIGNAR_INSPECTOR(id)}?inspectorId=${inspectorId}&asignadoPorId=${asignadoPorId}`
      : `${API_ENDPOINTS.DESPACHOS.ASIGNAR_INSPECTOR(id)}?inspectorId=${inspectorId}`
    return apiClient.put<DespachoComisorio>(endpoint)
  },

  // Marcar como devuelto
  marcarComoDevuelto: async (id: number, fechaDevolucion?: string): Promise<DespachoComisorio> => {
    const endpoint = fechaDevolucion
      ? `${API_ENDPOINTS.DESPACHOS.MARCAR_DEVUELTO(id)}?fechaDevolucion=${fechaDevolucion}`
      : API_ENDPOINTS.DESPACHOS.MARCAR_DEVUELTO(id)
    return apiClient.put<DespachoComisorio>(endpoint)
  },

  // Eliminar despacho
  delete: async (id: number): Promise<void> => {
    return apiClient.delete(API_ENDPOINTS.DESPACHOS.BY_ID(id))
  },

  // Generar reporte
  generarReporte: async (desde: string, hasta: string) => {
    return apiClient.get(`${API_ENDPOINTS.DESPACHOS.REPORTE}?desde=${desde}&hasta=${hasta}`)
  },

  // Generar reporte Excel
  generarReporteExcel: async (desde: string, hasta: string): Promise<Blob> => {
    const url = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081'}${API_ENDPOINTS.DESPACHOS.REPORTE_EXCEL}?desde=${desde}&hasta=${hasta}`
    const token = apiClient.getToken()
    
    const response = await fetch(url, {
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
      },
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.blob()
  },
}






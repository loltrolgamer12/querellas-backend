import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { Communication } from '../types'

export interface ComunicacionCreateParams {
  tipo: 'CARTA' | 'CORREO' | 'AUDIENCIA'
  numeroRadicado?: string
  asunto?: string
  contenido?: string
  fechaEnvio?: string
  destinatario?: string
}

export interface ComunicacionUpdateParams extends Partial<ComunicacionCreateParams> {}

export interface CambioEstadoComunicacionParams {
  nuevoEstado: string
}

export const comunicacionesApi = {
  // Listar comunicaciones de una querella
  listByQuerella: async (querellaId: number): Promise<Communication[]> => {
    return apiClient.get<Communication[]>(API_ENDPOINTS.COMUNICACIONES.BY_QUERELLA(querellaId))
  },

  // Crear comunicación
  create: async (querellaId: number, data: ComunicacionCreateParams): Promise<Communication> => {
    return apiClient.post<Communication>(API_ENDPOINTS.COMUNICACIONES.CREATE(querellaId), data)
  },

  // Actualizar comunicación
  update: async (querellaId: number, comunicacionId: number, data: ComunicacionUpdateParams): Promise<Communication> => {
    return apiClient.put<Communication>(API_ENDPOINTS.COMUNICACIONES.UPDATE(querellaId, comunicacionId), data)
  },

  // Cambiar estado de comunicación
  cambiarEstado: async (querellaId: number, comunicacionId: number, data: CambioEstadoComunicacionParams): Promise<Communication> => {
    return apiClient.put<Communication>(API_ENDPOINTS.COMUNICACIONES.CAMBIAR_ESTADO(querellaId, comunicacionId), data)
  },

  // Eliminar comunicación
  delete: async (querellaId: number, comunicacionId: number): Promise<void> => {
    return apiClient.delete(API_ENDPOINTS.COMUNICACIONES.DELETE(querellaId, comunicacionId))
  },
}






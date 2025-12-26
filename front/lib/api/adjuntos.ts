import { apiClient } from './client'
import { API_ENDPOINTS } from './config'
import type { Attachment } from '../types'

export const adjuntosApi = {
  // Listar adjuntos de una querella
  listByQuerella: async (querellaId: number): Promise<Attachment[]> => {
    return apiClient.get<Attachment[]>(API_ENDPOINTS.ADJUNTOS.BY_QUERELLA(querellaId))
  },

  // Subir adjunto
  subir: async (querellaId: number, archivo: File, descripcion?: string): Promise<Attachment> => {
    const formData = new FormData()
    formData.append('archivo', archivo)
    if (descripcion) {
      formData.append('descripcion', descripcion)
    }

    return apiClient.upload<Attachment>(API_ENDPOINTS.ADJUNTOS.SUBIR(querellaId), formData)
  },

  // Descargar adjunto
  descargar: async (querellaId: number, adjuntoId: number): Promise<Blob> => {
    const url = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081'}${API_ENDPOINTS.ADJUNTOS.DESCARGAR(querellaId, adjuntoId)}`
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

  // Eliminar adjunto
  delete: async (querellaId: number, adjuntoId: number): Promise<void> => {
    return apiClient.delete(API_ENDPOINTS.ADJUNTOS.DELETE(querellaId, adjuntoId))
  },
}






// Enums matching backend
export type RolUsuario = "INSPECTOR" | "DIRECTORA" | "AUXILIAR"
export type EstadoUsuario = "ACTIVO" | "BLOQUEADO" | "NO_DISPONIBLE"
export type ZonaInspector = "NEIVA" | "CORREGIMIENTO"
export type Naturaleza = "OFICIO" | "PERSONA" | "ANONIMA"

// Legacy types for backward compatibility
export type UserRole = "inspector" | "director" | "auxiliar"
export type CaseType = "querella" | "despacho"
export type CaseStatus = string // Dynamic from backend

// User types matching UsuarioResponse
export interface User {
  id: number
  nombre: string
  email: string
  telefono?: string
  rol: RolUsuario
  estado: EstadoUsuario
  zona?: ZonaInspector
  creadoEn: string
  actualizadoEn?: string
}

// Catalog types matching ItemSimpleDTO
export interface ItemSimple {
  id: number | string
  nombre: string
}

export interface Theme extends ItemSimple {
  status?: "activo" | "inactivo"
}

export interface Comuna extends ItemSimple {
}

// Querella types matching QuerellaResponse
export interface Querella {
  id: number
  radicadoInterno: string
  idLocal?: string
  direccion: string
  descripcion: string
  naturaleza?: Naturaleza
  temaId?: number
  temaNombre?: string
  inspectorAsignadoId?: number
  inspectorAsignadoNombre?: string
  inspectorAsignadoZona?: ZonaInspector
  asignadoPorId?: number
  asignadoPorNombre?: string
  comunaId?: number
  comunaNombre?: string
  estadoActual?: string
  creadoEn: string
  // Additional fields
  barrio?: string
  generoQuerellante?: string
  generoQuerellado?: string
  normasAplicables?: string
  observaciones?: string
  tieneFallo?: boolean
  tieneApelacion?: boolean
  archivado?: boolean
  materializacionMedida?: boolean
}

// Despacho types matching DespachoComitorioResponse
export interface DespachoComisorio {
  id: number
  fechaRecibido: string
  radicadoProceso?: string
  numeroDespacho: string
  entidadProcedente: string
  asunto: string
  demandanteApoderado?: string
  demandadoApoderado?: string
  inspectorAsignadoId?: number
  inspectorAsignadoNombre?: string
  inspectorAsignadoZona?: ZonaInspector
  asignadoPorId?: number
  asignadoPorNombre?: string
  fechaDevolucion?: string
  observaciones?: string
  estado?: string // PENDIENTE, DEVUELTO
  creadoEn: string
  actualizadoEn?: string
}

// Unified Case type for UI compatibility
export interface Case {
  id: string | number
  internalId?: string
  radicadoInterno?: string
  alcaldiaId?: string
  localId?: string
  idLocal?: string
  type: CaseType
  status: CaseStatus
  estadoActual?: string
  theme?: string
  temaNombre?: string
  temaId?: number
  address: string
  direccion?: string
  description: string
  descripcion?: string
  querellante?: string
  naturaleza?: Naturaleza | "oficio" | "persona" | "anonima"
  assignedTo?: string | number
  inspectorAsignadoId?: number
  inspectorAsignadoNombre?: string
  comunaId?: string | number
  comunaNombre?: string
  createdAt: string
  creadoEn?: string
  updatedAt?: string
  actualizadoEn?: string
  createdBy?: string
  // Despacho fields
  oficioNumber?: string
  numeroDespacho?: string
  autoridad?: string
  entidadProcedente?: string
  procesoTipo?: string
  asunto?: string
  fechaRecibido?: string
}

// Pagination types
export interface PaginatedResponse<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  sort?: string
}

// Historial types matching HistorialEstadoDTO
export interface CaseHistory {
  id?: number | string
  estadoNombre: string
  motivo: string
  usuarioId: number
  creadoEn: string
}

// Attachment types matching AdjuntoResponse
export interface Attachment {
  id: number
  nombreArchivo: string
  tipoArchivo: string
  tamanoBytes: number
  descripcion?: string
  cargadoPor?: {
    id: number
    nombre: string
  }
  creadoEn: string
  url?: string
}

// Communication types matching ComunicacionResponse
export interface Communication {
  id: number
  tipo: "CARTA" | "CORREO" | "AUDIENCIA"
  numeroRadicado?: string
  asunto?: string
  contenido?: string
  fechaEnvio?: string
  destinatario?: string
  estado?: string
  creadoPor?: {
    id: number
    nombre: string
  }
  creadoEn: string
}

// Notification types
export interface Notification {
  id: string | number
  userId: string | number
  title: string
  message: string
  type: "asignacion" | "cambio_estado" | "comentario"
  caseId?: string | number
  read: boolean
  createdAt: string
}

// API Request/Response types
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  type: string
}

export interface CambioEstadoRequest {
  nuevoEstado: string
  motivo: string
  usuarioId: number
}

export interface AsignarInspectorRequest {
  inspectorId: number
}

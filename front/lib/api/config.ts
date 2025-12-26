// API Configuration
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081'

export const API_ENDPOINTS = {
  // Auth
  AUTH: {
    LOGIN: '/api/auth/login',
    REGISTER: '/api/auth/register',
    REFRESH: '/api/auth/refresh',
  },
  // Querellas
  QUERELLAS: {
    BASE: '/api/querellas',
    BY_ID: (id: number) => `/api/querellas/${id}`,
    HISTORIAL: (id: number) => `/api/querellas/${id}/historial`,
    ASIGNAR_INSPECTOR: (id: number) => `/api/querellas/${id}/inspector`,
    CAMBIAR_ESTADO: (id: number) => `/api/querellas/${id}/estado`,
    POSIBLES_DUPLICADOS: (id: number) => `/api/querellas/${id}/posibles-duplicados`,
    ASIGNAR_AUTOMATICO: '/api/querellas/asignar-automatico',
  },
  // Despachos Comisorios
  DESPACHOS: {
    BASE: '/api/despachos-comisorios',
    BY_ID: (id: number) => `/api/despachos-comisorios/${id}`,
    PENDIENTES: '/api/despachos-comisorios/pendientes',
    DEVUELTOS: '/api/despachos-comisorios/devueltos',
    POR_INSPECTOR: (inspectorId: number) => `/api/despachos-comisorios/inspector/${inspectorId}`,
    ASIGNAR_INSPECTOR: (id: number) => `/api/despachos-comisorios/${id}/asignar-inspector`,
    MARCAR_DEVUELTO: (id: number) => `/api/despachos-comisorios/${id}/marcar-devuelto`,
    REPORTE: '/api/despachos-comisorios/reporte',
    REPORTE_EXCEL: '/api/despachos-comisorios/reporte/excel',
  },
  // Usuarios
  USUARIOS: {
    BASE: '/api/usuarios',
    BY_ID: (id: number) => `/api/usuarios/${id}`,
    CAMBIAR_ESTADO: (id: number) => `/api/usuarios/${id}/estado`,
    INSPECTORES: '/api/usuarios/inspectores',
  },
  // Catálogos
  CATALOGOS: {
    TEMAS: '/api/catalogos/temas',
    COMUNAS: '/api/catalogos/comunas',
    ESTADOS: '/api/catalogos/estados',
    ESTADOS_BY_MODULO: (modulo: string) => `/api/catalogos/estados?modulo=${modulo}`,
  },
  // Adjuntos (para querellas)
  ADJUNTOS: {
    BY_QUERELLA: (querellaId: number) => `/api/querellas/${querellaId}/adjuntos`,
    SUBIR: (querellaId: number) => `/api/querellas/${querellaId}/adjuntos`,
    DESCARGAR: (querellaId: number, adjuntoId: number) => `/api/querellas/${querellaId}/adjuntos/${adjuntoId}/descargar`,
    DELETE: (querellaId: number, adjuntoId: number) => `/api/querellas/${querellaId}/adjuntos/${adjuntoId}`,
  },
  // Comunicaciones (para querellas)
  COMUNICACIONES: {
    BY_QUERELLA: (querellaId: number) => `/api/querellas/${querellaId}/comunicaciones`,
    CREATE: (querellaId: number) => `/api/querellas/${querellaId}/comunicaciones`,
    UPDATE: (querellaId: number, comunicacionId: number) => `/api/querellas/${querellaId}/comunicaciones/${comunicacionId}`,
    CAMBIAR_ESTADO: (querellaId: number, comunicacionId: number) => `/api/querellas/${querellaId}/comunicaciones/${comunicacionId}/estado`,
    DELETE: (querellaId: number, comunicacionId: number) => `/api/querellas/${querellaId}/comunicaciones/${comunicacionId}`,
  },
} as const


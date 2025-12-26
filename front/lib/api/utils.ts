import type { Querella, DespachoComisorio, Case } from '../types'

/**
 * Convierte una Querella del backend a un Case del frontend
 */
export function querellaToCase(querella: Querella): Case {
  return {
    id: querella.id,
    internalId: querella.radicadoInterno,
    radicadoInterno: querella.radicadoInterno,
    localId: querella.idLocal,
    idLocal: querella.idLocal,
    type: 'querella',
    status: querella.estadoActual || '',
    estadoActual: querella.estadoActual,
    theme: querella.temaNombre,
    temaNombre: querella.temaNombre,
    temaId: querella.temaId,
    address: querella.direccion,
    direccion: querella.direccion,
    description: querella.descripcion,
    descripcion: querella.descripcion,
    naturaleza: querella.naturaleza,
    assignedTo: querella.inspectorAsignadoId,
    inspectorAsignadoId: querella.inspectorAsignadoId,
    inspectorAsignadoNombre: querella.inspectorAsignadoNombre,
    comunaId: querella.comunaId,
    comunaNombre: querella.comunaNombre,
    createdAt: querella.creadoEn,
    creadoEn: querella.creadoEn,
    createdBy: querella.asignadoPorId?.toString(),
  }
}

/**
 * Convierte un DespachoComisorio del backend a un Case del frontend
 */
export function despachoToCase(despacho: DespachoComisorio): Case {
  return {
    id: despacho.id,
    type: 'despacho',
    status: despacho.estado || '',
    estadoActual: despacho.estado,
    address: despacho.entidadProcedente, // Usar entidad procedente como dirección
    direccion: despacho.entidadProcedente,
    description: despacho.asunto,
    descripcion: despacho.asunto,
    asunto: despacho.asunto,
    oficioNumber: despacho.numeroDespacho,
    numeroDespacho: despacho.numeroDespacho,
    autoridad: despacho.entidadProcedente,
    entidadProcedente: despacho.entidadProcedente,
    procesoTipo: despacho.asunto,
    fechaRecibido: despacho.fechaRecibido,
    assignedTo: despacho.inspectorAsignadoId,
    inspectorAsignadoId: despacho.inspectorAsignadoId,
    inspectorAsignadoNombre: despacho.inspectorAsignadoNombre,
    createdAt: despacho.creadoEn,
    creadoEn: despacho.creadoEn,
    updatedAt: despacho.actualizadoEn,
    actualizadoEn: despacho.actualizadoEn,
  }
}


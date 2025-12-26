/**
 * Application constants
 */

export const MOCK_QUERELLA_STATES = [
  { id: "RECIBIDA", nombre: "RECIBIDA" },
  { id: "ASIGNADA", nombre: "ASIGNADA" },
  { id: "EN_PROCESO", nombre: "EN_PROCESO" },
  { id: "EN_INVESTIGACION", nombre: "EN_INVESTIGACION" },
  { id: "CITACION_ENVIADA", nombre: "CITACION_ENVIADA" },
  { id: "AUDIENCIA_PROGRAMADA", nombre: "AUDIENCIA_PROGRAMADA" },
  { id: "EN_AUDIENCIA", nombre: "EN_AUDIENCIA" },
  { id: "RESOLUCION_EMITIDA", nombre: "RESOLUCION_EMITIDA" },
  { id: "CERRADA", nombre: "CERRADA" },
  { id: "ARCHIVADA", nombre: "ARCHIVADA" },
  { id: "ANULADA", nombre: "ANULADA" },
] as const

export const MOCK_DESPACHO_STATES = [
  { id: "RECIBIDO", nombre: "RECIBIDO" },
  { id: "ASIGNADO", nombre: "ASIGNADO" },
  { id: "EN_TRAMITE", nombre: "EN_TRAMITE" },
  { id: "DILIGENCIADO", nombre: "DILIGENCIADO" },
  { id: "DEVUELTO", nombre: "DEVUELTO" },
  { id: "PENDIENTE", nombre: "PENDIENTE" },
  { id: "FINALIZADO", nombre: "FINALIZADO" },
] as const

export const MOCK_TEMAS = [
  { id: 1, nombre: "Urbanismo" },
  { id: 2, nombre: "Espacio Público" },
  { id: 3, nombre: "Actividad Económica" },
  { id: 4, nombre: "Policía Urbana" },
  { id: 5, nombre: "Control de Precios" },
] as const

export const MOCK_COMUNAS = Array.from({ length: 10 }, (_, i) => ({
  id: i + 1,
  nombre: `Comuna ${i + 1}`,
})) as const

export const CORREGIMIENTOS = [
  "Aipecito",
  "El Caguán",
  "Chapinero",
  "Fortalecillas",
  "Guacirco",
  "Río de las Ceibas",
  "San Luis",
  "Vegalarga",
] as const

export const GENEROS = ["MASCULINO", "FEMENINO", "OTRO"] as const




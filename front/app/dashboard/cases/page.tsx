"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { useState, useEffect } from "react"
import type { Case } from "@/lib/types"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent } from "@/components/ui/card"
import { Plus, Search, Filter, FileText, Calendar, MapPin, User, Loader2 } from "lucide-react"
import Link from "next/link"
import { useAuth } from "@/lib/auth-context"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { toast } from "sonner"
import { querellasApi, despachosApi } from "@/lib/api"
import { querellaToCase, despachoToCase } from "@/lib/api/utils"
import { catalogosApi } from "@/lib/api/catalogos"
import { usuariosApi } from "@/lib/api/usuarios"
import { mockCases } from "@/lib/mock-data"
import { isNetworkError } from "@/lib/utils/error-handler"
import { MOCK_QUERELLA_STATES, MOCK_DESPACHO_STATES } from "@/lib/utils/constants"

export default function CasesPage() {
  const { user } = useAuth()
  const [searchTerm, setSearchTerm] = useState("")
  const [caseTypeFilter, setCaseTypeFilter] = useState<string>("all")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [selectedCase, setSelectedCase] = useState<Case | null>(null)
  const [showStatusDialog, setShowStatusDialog] = useState(false)
  const [newStatus, setNewStatus] = useState("")
  const [statusReason, setStatusReason] = useState("")
  const [cases, setCases] = useState<Case[]>([])
  const [loading, setLoading] = useState(true)
  const [estados, setEstados] = useState<Array<{ id: string; nombre: string }>>([])
  const [estadosDialog, setEstadosDialog] = useState<Array<{ id: string; nombre: string }>>([])
  const [usuarios, setUsuarios] = useState<Array<{ id: number; nombre: string }>>([])

  // Cargar datos iniciales
  useEffect(() => {
    loadUsuarios()
  }, [])

  // Cargar estados cuando cambia el tipo de caso
  useEffect(() => {
    loadEstados()
  }, [caseTypeFilter])

  const loadCases = async () => {
    try {
      setLoading(true)
      const params: any = {
        page: 0,
        size: 100,
      }

      if (user?.rol === "INSPECTOR" && user.id) {
        params.inspectorId = user.id
      }

      if (searchTerm) {
        params.qTexto = searchTerm
      }

      if (statusFilter !== "all") {
        params.estadoNombre = statusFilter
      }

      // Cargar solo el tipo seleccionado
      const shouldLoadQuerellas = caseTypeFilter === "all" || caseTypeFilter === "querella"
      const shouldLoadDespachos = caseTypeFilter === "all" || caseTypeFilter === "despacho"

      const promises: Promise<any>[] = []

      if (shouldLoadQuerellas) {
        promises.push(
          querellasApi.list(params).catch((err) => {
            if (isNetworkError(err)) {
              console.info("Backend no disponible, usando datos mock para querellas")
              // Convertir mockCases a formato compatible
              const mockCasesConverted = mockCases
                .filter(c => c.type === 'querella')
                .map((c) => ({
                  id: typeof c.id === 'string' ? parseInt(c.id) : c.id,
                  radicadoInterno: c.internalId || `Q-2025-${c.id}`,
                  direccion: c.address,
                  descripcion: c.description,
                  estadoActual: c.status.toUpperCase().replace('_', ''),
                  temaNombre: c.theme,
                  naturaleza: c.naturaleza?.toUpperCase() as any,
                  inspectorAsignadoId: typeof c.assignedTo === 'string' ? parseInt(c.assignedTo) : c.assignedTo,
                  creadoEn: c.createdAt,
                }))
              return { items: mockCasesConverted, page: 0, size: mockCasesConverted.length, totalItems: mockCasesConverted.length, totalPages: 1 }
            }
            return { items: [], page: 0, size: 0, totalItems: 0, totalPages: 0 }
          })
        )
      } else {
        promises.push(Promise.resolve({ items: [], page: 0, size: 0, totalItems: 0, totalPages: 0 }))
      }

      if (shouldLoadDespachos) {
        promises.push(
          despachosApi.list({ page: 0, size: 100 }).catch((err) => {
            if (isNetworkError(err)) {
              const mockDespachos = mockCases
                .filter(c => c.type === 'despacho')
                .map((c) => ({
                  id: typeof c.id === 'string' ? parseInt(c.id) : c.id,
                  numeroDespacho: c.oficioNumber || `OF-2025-${c.id}`,
                  entidadProcedente: c.autoridad || "Juzgado",
                  asunto: c.description,
                  fechaRecibido: c.createdAt,
                  estado: c.status.toUpperCase(),
                  creadoEn: c.createdAt,
                }))
              return { content: mockDespachos, totalElements: mockDespachos.length, totalPages: 1, number: 0, size: mockDespachos.length }
            }
            return { content: [], totalElements: 0, totalPages: 0, number: 0, size: 0 }
          })
        )
      } else {
        promises.push(Promise.resolve({ content: [], totalElements: 0, totalPages: 0, number: 0, size: 0 }))
      }

      const [querellasResponse, despachosResponse] = await Promise.all(promises)

      const querellasCases = querellasResponse.items?.map(querellaToCase) || []
      const despachosCases = despachosResponse.content?.map(despachoToCase) || []

      setCases([...querellasCases, ...despachosCases])
    } catch (error: any) {
      if (isNetworkError(error)) {
        console.info("Usando datos mock debido a error de conexión")
        const mockCasesConverted = mockCases.map(c => querellaToCase({
          id: typeof c.id === 'string' ? parseInt(c.id) : c.id,
          radicadoInterno: c.internalId || `Q-2025-${c.id}`,
          direccion: c.address,
          descripcion: c.description,
          estadoActual: c.status,
          temaNombre: c.theme,
          naturaleza: c.naturaleza?.toUpperCase() as any,
          inspectorAsignadoId: typeof c.assignedTo === 'string' ? parseInt(c.assignedTo) : c.assignedTo,
          creadoEn: c.createdAt,
        }))
        setCases(mockCasesConverted)
      } else {
        console.error("Error loading cases:", error)
        toast.error("Error al cargar los casos: " + (error.message || "Error desconocido"))
      }
    } finally {
      setLoading(false)
    }
  }

  const loadEstados = async () => {
    try {
      let estadosList: Array<{ nombre: string }> = []
      
      if (caseTypeFilter === "querella") {
        estadosList = await catalogosApi.listEstados("QUERELLA")
      } else if (caseTypeFilter === "despacho") {
        estadosList = await catalogosApi.listEstados("DESPACHO")
      } else {
        estadosList = await catalogosApi.listEstados()
      }
      
      setEstados(estadosList.map((e) => ({ id: e.nombre, nombre: e.nombre })))
    } catch (error: any) {
      if (isNetworkError(error)) {
        console.info("Backend no disponible, usando estados mock")
        let estadosMock: Array<{ id: string; nombre: string }> = []
        
        if (caseTypeFilter === "querella") {
          estadosMock = [...MOCK_QUERELLA_STATES]
        } else if (caseTypeFilter === "despacho") {
          estadosMock = [...MOCK_DESPACHO_STATES]
        } else {
          estadosMock = [...MOCK_QUERELLA_STATES, ...MOCK_DESPACHO_STATES]
        }
        setEstados(estadosMock)
      } else {
        console.error("Error loading estados:", error)
        setEstados([])
      }
    }
  }

  const loadUsuarios = async () => {
    try {
      const usuariosList = await usuariosApi.list({ size: 100 })
      setUsuarios(usuariosList.items.map((u) => ({ id: u.id, nombre: u.nombre })))
    } catch (error: any) {
      if (!isNetworkError(error)) {
        console.error("Error loading usuarios:", error)
      }
      setUsuarios([])
    }
  }

  // Recargar cuando cambian los filtros
  useEffect(() => {
    const timeoutId = setTimeout(() => {
      loadCases()
    }, 300) // Debounce de 300ms

    return () => clearTimeout(timeoutId)
  }, [searchTerm, statusFilter, caseTypeFilter])

  // Resetear filtro de estado cuando cambia el tipo de caso
  useEffect(() => {
    setStatusFilter("all")
  }, [caseTypeFilter])

  const filteredCases = cases.filter((caso) => {
    // Filtro por tipo
    const matchesType = caseTypeFilter === "all" || caso.type === caseTypeFilter

    // Filtro por búsqueda
    const matchesSearch =
      (caso.internalId || caso.radicadoInterno || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
      (caso.description || caso.descripcion || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
      (caso.address || caso.direccion || "").toLowerCase().includes(searchTerm.toLowerCase())

    // Filtro por estado
    const casoStatus = caso.status || caso.estadoActual || ""
    const matchesStatus = statusFilter === "all" || 
      casoStatus.toUpperCase() === statusFilter.toUpperCase() || 
      casoStatus.toUpperCase().replace('_', '') === statusFilter.toUpperCase()

    return matchesType && matchesSearch && matchesStatus
  })

  const getStatusBadge = (status: string, caseType?: string) => {
    // Normalizar el estado
    const statusUpper = status?.toUpperCase() || ''
    
    // Variantes para QUERELLA
    const querellaVariants: Record<string, { label: string; className: string }> = {
      RECIBIDA: { label: "Recibida", className: "bg-blue-900/50 text-blue-300 border-blue-800" },
      ASIGNADA: { label: "Asignada", className: "bg-indigo-900/50 text-indigo-300 border-indigo-800" },
      EN_PROCESO: { label: "En Proceso", className: "bg-yellow-900/50 text-yellow-300 border-yellow-800" },
      EN_INVESTIGACION: { label: "En Investigación", className: "bg-orange-900/50 text-orange-300 border-orange-800" },
      CITACION_ENVIADA: { label: "Citación Enviada", className: "bg-cyan-900/50 text-cyan-300 border-cyan-800" },
      AUDIENCIA_PROGRAMADA: { label: "Audiencia Programada", className: "bg-purple-900/50 text-purple-300 border-purple-800" },
      EN_AUDIENCIA: { label: "En Audiencia", className: "bg-pink-900/50 text-pink-300 border-pink-800" },
      RESOLUCION_EMITIDA: { label: "Resolución Emitida", className: "bg-green-900/50 text-green-300 border-green-800" },
      CERRADA: { label: "Cerrada", className: "bg-slate-700 text-slate-300 border-slate-600" },
      ARCHIVADA: { label: "Archivada", className: "bg-slate-600 text-slate-200 border-slate-500" },
      ANULADA: { label: "Anulada", className: "bg-red-900/50 text-red-300 border-red-800" },
    }
    
    // Variantes para DESPACHO
    const despachoVariants: Record<string, { label: string; className: string }> = {
      RECIBIDO: { label: "Recibido", className: "bg-blue-900/50 text-blue-300 border-blue-800" },
      ASIGNADO: { label: "Asignado", className: "bg-indigo-900/50 text-indigo-300 border-indigo-800" },
      EN_TRAMITE: { label: "En Trámite", className: "bg-yellow-900/50 text-yellow-300 border-yellow-800" },
      DILIGENCIADO: { label: "Diligenciado", className: "bg-green-900/50 text-green-300 border-green-800" },
      DEVUELTO: { label: "Devuelto", className: "bg-purple-900/50 text-purple-300 border-purple-800" },
      PENDIENTE: { label: "Pendiente", className: "bg-orange-900/50 text-orange-300 border-orange-800" },
    }
    
    // Seleccionar el conjunto de variantes según el tipo
    const variants = caseType === 'despacho' ? despachoVariants : querellaVariants
    const variant = variants[statusUpper] || { label: status, className: "bg-slate-800 text-slate-300" }
    
    return (
      <Badge variant="outline" className={variant.className}>
        {variant.label}
      </Badge>
    )
  }

  const getPriorityBadge = (priority?: string) => {
    if (!priority) return null
    const variants: Record<string, { label: string; className: string }> = {
      alta: { label: "Alta", className: "bg-red-900/50 text-red-300 border-red-800" },
      media: { label: "Media", className: "bg-yellow-900/50 text-yellow-300 border-yellow-800" },
      baja: { label: "Baja", className: "bg-green-900/50 text-green-300 border-green-800" },
    }
    const variant = variants[priority]
    return (
      <Badge variant="outline" className={variant.className}>
        {variant.label}
      </Badge>
    )
  }

  const getAssignedUserName = (id?: string | number) => {
    if (!id) return "Sin asignar"
    const usuario = usuarios.find((u) => u.id === Number(id))
    return usuario?.nombre || "Desconocido"
  }

  const handleStatusChange = async () => {
    if (!newStatus || !statusReason.trim() || !selectedCase) {
      toast.error("Debe seleccionar un estado y proporcionar un motivo")
      return
    }

    try {
      const caseId = typeof selectedCase.id === 'string' ? parseInt(selectedCase.id) : selectedCase.id
      
      if (selectedCase.type === "querella") {
        await querellasApi.cambiarEstado(caseId, {
          nuevoEstado: newStatus,
          motivo: statusReason,
          usuarioId: user?.id || 0,
        })
      } else {
        // Para despachos, el cambio de estado se maneja diferente
        toast.info("El cambio de estado para despachos se maneja de forma diferente")
      }

      toast.success("Estado actualizado correctamente")
      setShowStatusDialog(false)
      setSelectedCase(null)
      setNewStatus("")
      setStatusReason("")
      loadCases() // Recargar casos
    } catch (error: any) {
      console.error("Error changing status:", error)
      toast.error("Error al cambiar el estado: " + (error.message || "Error desconocido"))
    }
  }

  const openStatusDialog = async (caso: Case) => {
    setSelectedCase(caso)
    setNewStatus(caso.status)
    setShowStatusDialog(true)
    
    // Cargar estados específicos para el tipo de caso
    try {
      const modulo = caso.type === "querella" ? "QUERELLA" : "DESPACHO"
      const estadosList = await catalogosApi.listEstados(modulo)
      setEstadosDialog(estadosList.map((e) => ({ id: e.nombre, nombre: e.nombre })))
    } catch (error: any) {
      if (isNetworkError(error)) {
        const estadosMock = caso.type === "querella" 
          ? [...MOCK_QUERELLA_STATES]
          : [...MOCK_DESPACHO_STATES]
        setEstadosDialog(estadosMock)
      } else {
        setEstadosDialog(estados)
      }
    }
  }

  return (
    <MainLayout>
      <div className="space-y-4 md:space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-2xl md:text-3xl font-bold text-white">Casos</h2>
            <p className="text-slate-400 mt-1 text-sm md:text-base">Gestión de querellas y despachos comisorios</p>
          </div>
          <div className="flex flex-col sm:flex-row gap-2 w-full sm:w-auto">
            {(user?.rol === "INSPECTOR" || user?.rol === "DIRECTORA" || user?.rol === "AUXILIAR") && (
              <Link href="/dashboard/cases/new" className="w-full sm:w-auto">
                <Button className="bg-blue-600 hover:bg-blue-700 text-white w-full sm:w-auto">
                  <Plus className="mr-2 h-4 w-4" />
                  Radicar Caso
                </Button>
              </Link>
            )}
            {user?.rol === "DIRECTORA" && (
              <Button className="bg-green-600 hover:bg-green-700 text-white w-full sm:w-auto">
                Realizar Reparto
              </Button>
            )}
          </div>
        </div>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="pt-4 md:pt-6">
            <div className="flex flex-col gap-3 md:gap-4">
              <div className="flex-1">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-500" />
                  <Input
                    placeholder="Buscar por ID, descripción o dirección..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="pl-10 bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                  />
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 md:gap-4">
                <Select value={caseTypeFilter} onValueChange={setCaseTypeFilter}>
                  <SelectTrigger className="w-full bg-slate-800 border-slate-700 text-white">
                    <Filter className="mr-2 h-4 w-4" />
                    <SelectValue placeholder="Tipo de caso" />
                  </SelectTrigger>
                  <SelectContent className="bg-slate-900 border-slate-800">
                    <SelectItem value="all" className="text-white hover:bg-slate-800">
                      Todos los tipos
                    </SelectItem>
                    <SelectItem value="querella" className="text-white hover:bg-slate-800">
                      Querella
                    </SelectItem>
                    <SelectItem value="despacho" className="text-white hover:bg-slate-800">
                      Despacho Comisorio
                    </SelectItem>
                  </SelectContent>
                </Select>
                <Select value={statusFilter} onValueChange={setStatusFilter}>
                  <SelectTrigger className="w-full bg-slate-800 border-slate-700 text-white">
                    <Filter className="mr-2 h-4 w-4" />
                    <SelectValue placeholder="Estado" />
                  </SelectTrigger>
                  <SelectContent className="bg-slate-900 border-slate-800">
                    <SelectItem value="all" className="text-white hover:bg-slate-800">
                      Todos los estados
                    </SelectItem>
                    {estados.map((estado) => (
                      <SelectItem key={estado.id} value={estado.id} className="text-white hover:bg-slate-800">
                        {estado.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-3">
          {loading ? (
            <Card className="bg-slate-900 border-slate-800">
              <CardContent className="py-12 text-center">
                <Loader2 className="h-12 w-12 text-blue-600 mx-auto mb-4 animate-spin" />
                <p className="text-slate-400">Cargando casos...</p>
              </CardContent>
            </Card>
          ) : filteredCases.length === 0 ? (
            <Card className="bg-slate-900 border-slate-800">
              <CardContent className="py-12 text-center">
                <FileText className="h-12 w-12 text-slate-600 mx-auto mb-4" />
                <p className="text-slate-400">No se encontraron casos</p>
              </CardContent>
            </Card>
          ) : (
            filteredCases.map((caso) => (
              <Card key={caso.id} className="bg-slate-900 border-slate-800 hover:border-slate-700 transition-colors">
                <CardContent className="p-3 md:p-4">
                  <div className="flex flex-col gap-3 md:gap-4">
                    {/* Left: Case Info */}
                    <div className="flex-1 min-w-0 space-y-2">
                      <div className="flex items-center gap-2 flex-wrap">
                        <Link
                          href={`/dashboard/cases/${caso.id}`}
                          className="text-base md:text-lg font-semibold text-white hover:text-blue-400 transition-colors"
                        >
                          {caso.internalId || caso.radicadoInterno || `Caso ${caso.id}`}
                        </Link>
                        {getStatusBadge(caso.status, caso.type)}
                        {getPriorityBadge(caso.priority)}
                        <Badge variant="outline" className="bg-slate-800 text-slate-300 border-slate-700">
                          {caso.type === "querella" ? "Querella" : "Despacho"}
                        </Badge>
                      </div>

                      <p className="text-sm text-slate-300 line-clamp-2">{caso.description}</p>

                      <div className="flex flex-wrap gap-3 md:gap-4 text-xs text-slate-400">
                        <div className="flex items-center gap-1">
                          <MapPin className="h-3 w-3 flex-shrink-0" />
                          <span className="truncate">{caso.address}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Calendar className="h-3 w-3 flex-shrink-0" />
                          {new Date(caso.createdAt || caso.creadoEn || Date.now()).toLocaleDateString("es-CO")}
                        </div>
                        <div className="flex items-center gap-1">
                          <User className="h-3 w-3 flex-shrink-0" />
                          <span className="truncate">
                            {caso.inspectorAsignadoNombre || getAssignedUserName(caso.assignedTo || caso.inspectorAsignadoId)}
                          </span>
                        </div>
                        {(caso.theme || caso.temaNombre) && (
                          <div className="flex items-center gap-1">
                            <FileText className="h-3 w-3 flex-shrink-0" />
                            {caso.theme || caso.temaNombre}
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Right: Actions */}
                    <div className="flex flex-row md:flex-col gap-2">
                      {(user?.rol === "INSPECTOR" || user?.rol === "DIRECTORA") && (
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={(e) => {
                            e.preventDefault()
                            openStatusDialog(caso)
                          }}
                          className="flex-1 md:flex-none bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
                        >
                          Cambiar Estado
                        </Button>
                      )}
                      <Link href={`/dashboard/cases/${caso.id}`} className="flex-1 md:flex-none">
                        <Button
                          size="sm"
                          variant="outline"
                          className="w-full bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
                        >
                          Ver Detalles
                        </Button>
                      </Link>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>

      {/* Status Change Dialog */}
      <Dialog open={showStatusDialog} onOpenChange={setShowStatusDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white max-w-[95vw] md:max-w-lg">
          <DialogHeader>
            <DialogTitle>Cambiar Estado del Caso</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label className="text-slate-300">Caso</Label>
              <p className="text-sm text-slate-400 mt-1">{selectedCase?.internalId || selectedCase?.radicadoInterno || `Caso ${selectedCase?.id}`}</p>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Nuevo Estado *</Label>
              <Select value={newStatus} onValueChange={setNewStatus}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue placeholder="Seleccionar estado" />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  {estadosDialog.length > 0 ? (
                    estadosDialog.map((estado) => (
                      <SelectItem key={estado.id} value={estado.id} className="text-white hover:bg-slate-800">
                        {estado.nombre}
                      </SelectItem>
                    ))
                  ) : (
                    estados.map((estado) => (
                      <SelectItem key={estado.id} value={estado.id} className="text-white hover:bg-slate-800">
                        {estado.nombre}
                      </SelectItem>
                    ))
                  )}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Motivo del Cambio *</Label>
              <Textarea
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
                placeholder="Explique el motivo del cambio de estado..."
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[100px]"
              />
            </div>

            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowStatusDialog(false)
                  setSelectedCase(null)
                  setNewStatus("")
                  setStatusReason("")
                }}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleStatusChange} className="bg-blue-600 hover:bg-blue-700 text-white">
                Guardar Cambio
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </MainLayout>
  )
}


"use client"

import type React from "react"
import { MainLayout } from "@/components/layout/main-layout"
import { useState, useEffect, use } from "react"
import type { Case, CaseHistory, Attachment, Communication } from "@/lib/types"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ArrowLeft, Calendar, MapPin, User, FileText, Upload, X, MessageSquare, Loader2 } from "lucide-react"
import Link from "next/link"
import { useAuth } from "@/lib/auth-context"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Input } from "@/components/ui/input"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { toast } from "sonner"
import { Separator } from "@/components/ui/separator"
import { querellasApi, despachosApi, adjuntosApi, comunicacionesApi, usuariosApi, catalogosApi } from "@/lib/api"
import { querellaToCase, despachoToCase } from "@/lib/api/utils"
import { mockCases, mockCaseHistory, mockAttachments, mockCommunications, mockUsers } from "@/lib/mock-data"
import { isNetworkError } from "@/lib/utils/error-handler"
import { MOCK_QUERELLA_STATES, MOCK_DESPACHO_STATES, MOCK_TEMAS, MOCK_COMUNAS, CORREGIMIENTOS, GENEROS } from "@/lib/utils/constants"

export default function CaseDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const resolvedParams = use(params)
  return <CaseDetailClient caseId={resolvedParams.id} />
}

function CaseDetailClient({ caseId }: { caseId: string }) {
  const { user } = useAuth()
  const [caso, setCaso] = useState<Case | null>(null)
  const [loading, setLoading] = useState(true)
  const [history, setHistory] = useState<CaseHistory[]>([])
  const [attachments, setAttachments] = useState<Attachment[]>([])
  const [communications, setCommunications] = useState<Communication[]>([])
  const [estados, setEstados] = useState<Array<{ id: string; nombre: string }>>([])
  const [inspectores, setInspectores] = useState<Array<{ id: number; nombre: string }>>([])
  const [temas, setTemas] = useState<Array<{ id: number; nombre: string }>>([])
  const [comunas, setComunas] = useState<Array<{ id: number; nombre: string }>>([])

  const [showStatusDialog, setShowStatusDialog] = useState(false)
  const [showReassignDialog, setShowReassignDialog] = useState(false)
  const [showEditDialog, setShowEditDialog] = useState(false)
  const [showAttachmentDialog, setShowAttachmentDialog] = useState(false)
  const [showCommunicationDialog, setShowCommunicationDialog] = useState(false)

  const [newStatus, setNewStatus] = useState("")
  const [statusReason, setStatusReason] = useState("")
  const [newInspector, setNewInspector] = useState("")
  const [reassignReason, setReassignReason] = useState("")
  const [files, setFiles] = useState<File[]>([])

  const [commType, setCommType] = useState<"CARTA" | "CORREO" | "AUDIENCIA">("CARTA")
  const [commDate, setCommDate] = useState("")
  const [commObservations, setCommObservations] = useState("")

  // Estados para edición
  const [editAddress, setEditAddress] = useState("")
  const [editDescription, setEditDescription] = useState("")
  const [editThemeId, setEditThemeId] = useState("")
  const [editNaturaleza, setEditNaturaleza] = useState<"OFICIO" | "PERSONA" | "ANONIMA">("PERSONA")
  const [editBarrio, setEditBarrio] = useState("")
  const [editCorregimiento, setEditCorregimiento] = useState("")
  const [editGeneroQuerellante, setEditGeneroQuerellante] = useState("")
  const [editGeneroQuerellado, setEditGeneroQuerellado] = useState("")
  const [editObservaciones, setEditObservaciones] = useState("")
  // Para despachos
  const [editNumeroDespacho, setEditNumeroDespacho] = useState("")
  const [editEntidadProcedente, setEditEntidadProcedente] = useState("")
  const [editAsunto, setEditAsunto] = useState("")
  const [editRadicadoProceso, setEditRadicadoProceso] = useState("")
  const [editDemandanteApoderado, setEditDemandanteApoderado] = useState("")
  const [editDemandadoApoderado, setEditDemandadoApoderado] = useState("")

  useEffect(() => {
    loadCase()
    loadInspectores()
  }, [caseId])

  useEffect(() => {
    if (caso) {
      loadEstados()
    }
  }, [caso])

  // Cargar temas y comunas cuando se abre el diálogo de edición
  useEffect(() => {
    if (!showEditDialog || caso?.type !== "querella") {
      return
    }

    const loadCatalogos = async () => {
      try {
        const [temasList, comunasList] = await Promise.all([
          catalogosApi.listTemas(),
          catalogosApi.listComunas(),
        ])
        setTemas(temasList.map((t) => ({ id: t.id as number, nombre: t.nombre })))
        setComunas(comunasList.map((c) => ({ id: c.id as number, nombre: c.nombre })))
        } catch (error: any) {
          if (!isNetworkError(error)) {
            console.error("Error loading catalogos for edit:", error)
          }
          setTemas([...MOCK_TEMAS])
          setComunas([...MOCK_COMUNAS])
        }
    }
    loadCatalogos()
  }, [showEditDialog, caso])

  const loadCase = async () => {
    try {
      setLoading(true)
      const caseIdNum = parseInt(caseId)

      // Try to load as querella first
      try {
        const querella = await querellasApi.getById(caseIdNum)
        const caseData = querellaToCase(querella)
        setCaso(caseData)
        
        // Load related data
        const [historialData, adjuntosData, comunicacionesData] = await Promise.all([
          querellasApi.getHistorial(caseIdNum).catch(() => []),
          adjuntosApi.listByQuerella(caseIdNum).catch(() => []),
          comunicacionesApi.listByQuerella(caseIdNum).catch(() => []),
        ])
        
        setHistory(Array.isArray(historialData) ? historialData : [])
        setAttachments(adjuntosData || [])
        setCommunications(comunicacionesData || [])
      } catch (querellaError: any) {
        // If not a querella, try as despacho
        try {
          const despacho = await despachosApi.getById(caseIdNum)
          const caseData = despachoToCase(despacho)
          setCaso(caseData)
          // Despachos might not have historial/adjuntos/comunicaciones in the same way
        } catch (despachoError: any) {
          // Si el backend no está disponible, usar datos mock
          if (querellaError?.status === 0 || despachoError?.status === 0 || 
              querellaError?.message?.includes('No se pudo conectar') || 
              despachoError?.message?.includes('No se pudo conectar')) {
            console.info("Backend no disponible, usando datos mock para el caso")
            
            // Buscar el caso en los datos mock (comparar tanto por ID numérico como string, y por internalId)
            // Normalizar ambos IDs a string para comparación
            const caseIdStr = String(caseId)
            const mockCase = mockCases.find(c => {
              const cIdStr = String(c.id)
              return (
                cIdStr === caseIdStr ||
                cIdStr === String(caseIdNum) ||
                c.internalId === caseId ||
                c.internalId === caseIdStr ||
                (typeof c.id === 'number' && c.id === caseIdNum) ||
                (typeof c.id === 'string' && c.id === caseIdStr)
              )
            })
            
            if (mockCase) {
              // Convertir mockCase a formato Case
              const caseData: Case = {
                id: mockCase.id,
                internalId: mockCase.internalId,
                type: mockCase.type,
                status: mockCase.status,
                estadoActual: mockCase.status,
                theme: mockCase.theme,
                temaNombre: mockCase.theme,
                address: mockCase.address,
                direccion: mockCase.address,
                description: mockCase.description,
                descripcion: mockCase.description,
                naturaleza: mockCase.naturaleza as any,
                assignedTo: mockCase.assignedTo,
                inspectorAsignadoId: typeof mockCase.assignedTo === 'string' ? parseInt(mockCase.assignedTo) : mockCase.assignedTo,
                createdAt: mockCase.createdAt,
                creadoEn: mockCase.createdAt,
                updatedAt: mockCase.updatedAt,
                oficioNumber: mockCase.oficioNumber,
                numeroDespacho: mockCase.oficioNumber,
                autoridad: mockCase.autoridad,
                entidadProcedente: mockCase.autoridad,
                procesoTipo: mockCase.procesoTipo,
              }
              
              setCaso(caseData)
              
              // Cargar datos relacionados mock - usando propiedades del tipo mock
              const mockHistory = (mockCaseHistory as any[]).filter((h: any) => h.caseId === String(mockCase.id))
              const mockAtts = (mockAttachments as any[]).filter((a: any) => a.caseId === String(mockCase.id))
              const mockComms = (mockCommunications as any[]).filter((c: any) => c.caseId === String(mockCase.id))
              
              // Convertir historial mock al formato del backend
              const historyConverted = mockHistory.map((h: any, idx: number) => ({
                id: typeof h.id === 'string' ? parseInt(h.id) : (h.id || idx),
                estadoNombre: h.action || h.estadoNombre || '',
                motivo: h.reason || h.motivo || "",
                usuarioId: typeof h.userId === 'string' ? parseInt(h.userId) : (h.userId || 0),
                creadoEn: h.timestamp || h.creadoEn || new Date().toISOString(),
              }))
              
              // Convertir adjuntos mock al formato del backend
              const attachmentsConverted = mockAtts.map((a: any) => ({
                id: typeof a.id === 'string' ? parseInt(a.id) : a.id,
                nombreArchivo: a.fileName || a.nombreArchivo || '',
                tipoArchivo: a.fileType || a.tipoArchivo || '',
                tamanoBytes: a.fileSize || a.tamanoBytes || 0,
                descripcion: a.descripcion || "",
                creadoEn: a.uploadedAt || a.creadoEn || new Date().toISOString(),
                url: a.url,
              }))
              
              // Convertir comunicaciones mock al formato del backend
              const communicationsConverted = mockComms.map((c: any) => ({
                id: typeof c.id === 'string' ? parseInt(c.id) : c.id,
                tipo: (c.type || c.tipo || 'CARTA').toUpperCase() as any,
                fechaEnvio: c.date || c.fechaEnvio || new Date().toISOString(),
                contenido: c.observations || c.contenido || "",
                creadoEn: c.createdAt || c.creadoEn || new Date().toISOString(),
              }))
              
              setHistory(historyConverted)
              setAttachments(attachmentsConverted)
              setCommunications(communicationsConverted)
            } else {
              setCaso(null)
            }
          } else {
            // Solo loggear si no es un error de conexión
            if (querellaError?.status !== 0 && despachoError?.status !== 0) {
              console.error("Case not found:", querellaError, despachoError)
            }
            setCaso(null)
          }
        }
      }
    } catch (error: any) {
      console.error("Error loading case:", error)
      // Si es error de conexión, intentar con datos mock
      if (error?.status === 0 || error?.message?.includes('No se pudo conectar')) {
        const caseIdNum = parseInt(caseId)
        const mockCase = mockCases.find(c => 
          String(c.id) === String(caseId) || 
          String(c.id) === caseId || 
          c.id === caseIdNum ||
          c.internalId === caseId ||
          c.internalId === String(caseId)
        )
        if (mockCase) {
          const caseData: Case = {
            id: mockCase.id,
            internalId: mockCase.internalId,
            type: mockCase.type,
            status: mockCase.status,
            estadoActual: mockCase.status,
            theme: mockCase.theme,
            address: mockCase.address,
            description: mockCase.description,
            naturaleza: mockCase.naturaleza as any,
            createdAt: mockCase.createdAt,
          }
          setCaso(caseData)
        } else {
          setCaso(null)
        }
      } else {
        toast.error("Error al cargar el caso: " + (error.message || "Error desconocido"))
        setCaso(null)
      }
    } finally {
      setLoading(false)
    }
  }

  const loadEstados = async () => {
    try {
      // Determinar el módulo según el tipo de caso
      const modulo = caso?.type === 'despacho' ? 'DESPACHO' : 'QUERELLA'
      
      const estadosList = await catalogosApi.listEstados(modulo).catch((err) => {
        if (isNetworkError(err)) {
          console.info("Backend no disponible, usando datos mock para estados")
          return modulo === 'DESPACHO' 
            ? MOCK_DESPACHO_STATES.map(e => ({ id: e.id, nombre: e.nombre }))
            : MOCK_QUERELLA_STATES.map(e => ({ id: e.id, nombre: e.nombre }))
        }
        throw err
      })
      setEstados(estadosList.map((e) => ({ id: String(e.nombre || e.id), nombre: e.nombre })))
    } catch (error: any) {
      if (isNetworkError(error)) {
        console.info("Backend no disponible, usando datos mock para estados")
        const modulo = caso?.type === 'despacho' ? 'DESPACHO' : 'QUERELLA'
        const estadosMock = modulo === 'DESPACHO' 
          ? [...MOCK_DESPACHO_STATES]
          : [...MOCK_QUERELLA_STATES]
        setEstados(estadosMock)
      } else {
        console.error("Error loading estados:", error)
        setEstados([])
      }
    }
  }

  const loadInspectores = async () => {
    try {
      const inspectoresList = await usuariosApi.listInspectores().catch((err) => {
        if (err?.status === 0 || err?.message?.includes('No se pudo conectar')) {
          console.info("Backend no disponible, usando datos mock para inspectores")
          return (mockUsers as any[])
            .filter((u: any) => (u.role || u.rol) === 'inspector' || (u.role || u.rol) === 'INSPECTOR')
            .map((u: any) => ({
              id: typeof u.id === 'string' ? parseInt(u.id) : u.id,
              nombre: u.name || u.nombre || '',
              email: u.email || '',
              telefono: u.phone || u.telefono || '',
              rol: (u.role || u.rol || 'INSPECTOR').toUpperCase() as any,
              estado: (u.status || u.estado || 'ACTIVO').toUpperCase() as any,
              zona: 'NEIVA' as any,
              creadoEn: u.createdAt || u.creadoEn || new Date().toISOString(),
              actualizadoEn: u.actualizadoEn || u.createdAt || new Date().toISOString(),
            }))
        }
        throw err
      })
      setInspectores(inspectoresList.map((i) => ({ id: i.id, nombre: i.nombre })))
    } catch (error: any) {
      // Solo loggear si no es un error de conexión
      if (error?.status !== 0 && error?.message && !error.message.includes('No se pudo conectar')) {
        console.error("Error loading inspectores:", error)
      }
      // Si el backend no está disponible, usar lista vacía
      setInspectores([])
    }
  }

  if (loading) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <Loader2 className="h-12 w-12 text-blue-600 mx-auto mb-4 animate-spin" />
          <p className="text-slate-400">Cargando caso...</p>
        </div>
      </MainLayout>
    )
  }

  if (!caso) {
    return (
      <MainLayout>
        <div className="text-center py-12">
          <p className="text-slate-400 mb-4">Caso no encontrado</p>
          <Link href="/dashboard/cases">
            <Button className="bg-blue-600 hover:bg-blue-700 text-white">Volver a Casos</Button>
          </Link>
        </div>
      </MainLayout>
    )
  }

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

  const getAssignedUserName = (id?: string | number) => {
    if (!id) return "Sin asignar"
    const inspector = inspectores.find((i) => i.id === Number(id))
    return inspector?.nombre || caso.inspectorAsignadoNombre || "Desconocido"
  }

  const handleStatusChange = async () => {
    if (!newStatus || !statusReason.trim() || !caso) {
      toast.error("Debe seleccionar un estado y proporcionar un motivo")
      return
    }

    try {
      const caseId = typeof caso.id === 'string' ? parseInt(caso.id) : caso.id
      
      if (caso.type === "querella") {
        await querellasApi.cambiarEstado(caseId, {
          nuevoEstado: newStatus,
          motivo: statusReason,
          usuarioId: user?.id || 0,
        })
        toast.success("Estado actualizado correctamente")
        setShowStatusDialog(false)
        setNewStatus("")
        setStatusReason("")
        loadCase() // Recargar caso
      } else {
        toast.info("El cambio de estado para despachos se maneja de forma diferente")
      }
    } catch (error: any) {
      console.error("Error changing status:", error)
      toast.error("Error al cambiar el estado: " + (error.message || "Error desconocido"))
    }
  }

  const handleReassign = async () => {
    if (!newInspector || !reassignReason.trim() || !caso) {
      toast.error("Debe seleccionar un inspector y proporcionar un motivo")
      return
    }

    try {
      const caseId = typeof caso.id === 'string' ? parseInt(caso.id) : caso.id
      const inspectorId = parseInt(newInspector)
      
      if (caso.type === "querella") {
        await querellasApi.asignarInspector(caseId, { inspectorId }, user?.id)
        toast.success("Caso reasignado correctamente")
        setShowReassignDialog(false)
        setNewInspector("")
        setReassignReason("")
        loadCase() // Recargar caso
      } else {
        await despachosApi.asignarInspector(caseId, inspectorId, user?.id)
        toast.success("Caso reasignado correctamente")
        setShowReassignDialog(false)
        setNewInspector("")
        setReassignReason("")
        loadCase() // Recargar caso
      }
    } catch (error: any) {
      console.error("Error reassigning:", error)
      toast.error("Error al reasignar: " + (error.message || "Error desconocido"))
    }
  }

  const handleEdit = async () => {
    if (!caso) return

    try {
      const caseId = typeof caso.id === 'string' ? parseInt(caso.id) : caso.id

      if (caso.type === "querella") {
        if (!editAddress || !editDescription) {
          toast.error("Dirección y descripción son obligatorios")
          return
        }

        const updateData: any = {
          direccion: editAddress,
          descripcion: editDescription,
          naturaleza: editNaturaleza,
        }

        if (editThemeId) updateData.temaId = parseInt(editThemeId)
        if (editBarrio) updateData.barrio = editBarrio
        if (editCorregimiento && editCorregimiento !== "none") updateData.corregimiento = editCorregimiento
        if (editGeneroQuerellante) updateData.generoQuerellante = editGeneroQuerellante
        if (editGeneroQuerellado) updateData.generoQuerellado = editGeneroQuerellado
        if (editObservaciones) updateData.observaciones = editObservaciones

        await querellasApi.update(caseId, updateData)
        toast.success("Querella actualizada correctamente")
        setShowEditDialog(false)
        loadCase() // Recargar caso
      } else {
        if (!editNumeroDespacho || !editEntidadProcedente || !editAsunto) {
          toast.error("Número de despacho, entidad procedente y asunto son obligatorios")
          return
        }

        const updateData: any = {
          numeroDespacho: editNumeroDespacho,
          entidadProcedente: editEntidadProcedente,
          asunto: editAsunto,
        }

        if (editRadicadoProceso) updateData.radicadoProceso = editRadicadoProceso
        if (editDemandanteApoderado) updateData.demandanteApoderado = editDemandanteApoderado
        if (editDemandadoApoderado) updateData.demandadoApoderado = editDemandadoApoderado
        if (editObservaciones) updateData.observaciones = editObservaciones

        await despachosApi.update(caseId, updateData)
        toast.success("Despacho actualizado correctamente")
        setShowEditDialog(false)
        loadCase() // Recargar caso
      }
    } catch (error: any) {
      console.error("Error updating case:", error)
      toast.error("Error al actualizar el caso: " + (error.message || "Error desconocido"))
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setFiles((prev) => [...prev, ...Array.from(e.target.files!)])
    }
  }

  const removeFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index))
  }

  const handleUploadAttachments = async () => {
    if (files.length === 0 || !caso) {
      toast.error("Debe seleccionar al menos un archivo")
      return
    }

    try {
      const caseId = typeof caso.id === 'string' ? parseInt(caso.id) : caso.id
      
      if (caso.type === "querella") {
        for (const file of files) {
          await adjuntosApi.subir(caseId, file)
        }
        toast.success(`${files.length} archivo(s) cargado(s) correctamente`)
        setShowAttachmentDialog(false)
        setFiles([])
        loadCase() // Recargar caso para mostrar nuevos adjuntos
      } else {
        toast.info("La carga de adjuntos para despachos se maneja de forma diferente")
      }
    } catch (error: any) {
      console.error("Error uploading attachments:", error)
      toast.error("Error al cargar archivos: " + (error.message || "Error desconocido"))
    }
  }

  const handleAddCommunication = async () => {
    if (!commDate || !commObservations.trim() || !caso) {
      toast.error("Debe completar todos los campos")
      return
    }

    try {
      const caseId = typeof caso.id === 'string' ? parseInt(caso.id) : caso.id
      
      if (caso.type === "querella") {
        await comunicacionesApi.create(caseId, {
          tipo: commType,
          fechaEnvio: commDate,
          contenido: commObservations,
        })
        toast.success("Comunicación registrada correctamente")
        setShowCommunicationDialog(false)
        setCommType("CARTA")
        setCommDate("")
        setCommObservations("")
        loadCase() // Recargar caso para mostrar nueva comunicación
      } else {
        toast.info("Las comunicaciones para despachos se manejan de forma diferente")
      }
    } catch (error: any) {
      console.error("Error adding communication:", error)
      toast.error("Error al registrar comunicación: " + (error.message || "Error desconocido"))
    }
  }

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-4">
            <Link href="/dashboard/cases">
              <Button variant="ghost" size="icon" className="text-slate-400 hover:text-white hover:bg-slate-800">
                <ArrowLeft className="h-5 w-5" />
              </Button>
            </Link>
            <div>
              <div className="flex items-center gap-3 mb-2">
                <h2 className="text-3xl font-bold text-white">{caso.internalId || caso.radicadoInterno || `Caso ${caso.id}`}</h2>
                {getStatusBadge(caso.status || caso.estadoActual || "", caso.type)}
                <Badge variant="outline" className="bg-slate-800 text-slate-300 border-slate-700">
                  {caso.type === "querella" ? "Querella" : "Despacho"}
                </Badge>
              </div>
              <p className="text-slate-400">{caso.description || caso.descripcion}</p>
            </div>
          </div>

          {(user?.rol === "INSPECTOR" || user?.rol === "DIRECTORA") && (
            <div className="flex gap-2">
              <Button
                onClick={() => {
                  // Normalizar el estado actual para que coincida con los IDs de los estados
                  const currentStatus = (caso.status || caso.estadoActual || "").toLowerCase().replace(/\s+/g, '_')
                  // Buscar el estado que coincida (por nombre o ID)
                  const matchingEstado = estados.find(e => 
                    e.id.toLowerCase() === currentStatus || 
                    e.nombre.toLowerCase() === (caso.status || caso.estadoActual || "").toLowerCase()
                  )
                  setNewStatus(matchingEstado?.id || caso.status || caso.estadoActual || "")
                  setShowStatusDialog(true)
                }}
                className="bg-blue-600 hover:bg-blue-700 text-white"
              >
                Cambiar Estado
              </Button>
              <Button
                onClick={() => setShowReassignDialog(true)}
                variant="outline"
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Reasignar
              </Button>
              <Button
                onClick={() => {
                  // Inicializar campos de edición con los valores actuales del caso
                  if (caso.type === "querella") {
                    setEditAddress(caso.address || caso.direccion || "")
                    setEditDescription(caso.description || caso.descripcion || "")
                    setEditThemeId((caso as any).themeId?.toString() || "")
                    setEditNaturaleza((caso.naturaleza as "OFICIO" | "PERSONA" | "ANONIMA") || "PERSONA")
                    setEditBarrio((caso as any).barrio || "")
                    setEditCorregimiento((caso as any).corregimiento || "")
                    setEditGeneroQuerellante((caso as any).generoQuerellante || "")
                    setEditGeneroQuerellado((caso as any).generoQuerellado || "")
                    setEditObservaciones((caso as any).observaciones || "")
                  } else {
                    setEditNumeroDespacho(caso.numeroDespacho || "")
                    setEditEntidadProcedente(caso.entidadProcedente || "")
                    setEditAsunto(caso.asunto || "")
                    setEditRadicadoProceso((caso as any).radicadoProceso || "")
                    setEditDemandanteApoderado((caso as any).demandanteApoderado || "")
                    setEditDemandadoApoderado((caso as any).demandadoApoderado || "")
                    setEditObservaciones((caso as any).observaciones || "")
                  }
                  setShowEditDialog(true)
                }}
                variant="outline"
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Editar
              </Button>
            </div>
          )}
        </div>

        {/* Info Cards */}
        <div className="grid md:grid-cols-3 gap-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-blue-900/30 rounded-lg">
                  <MapPin className="h-5 w-5 text-blue-400" />
                </div>
                <div>
                  <p className="text-xs text-slate-500">Dirección</p>
                  <p className="text-sm text-white font-medium">{caso.address || caso.direccion}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-purple-900/30 rounded-lg">
                  <User className="h-5 w-5 text-purple-400" />
                </div>
                <div>
                  <p className="text-xs text-slate-500">Asignado a</p>
                  <p className="text-sm text-white font-medium">{caso.inspectorAsignadoNombre || getAssignedUserName(caso.assignedTo || caso.inspectorAsignadoId)}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardContent className="pt-6">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-green-900/30 rounded-lg">
                  <Calendar className="h-5 w-5 text-green-400" />
                </div>
                <div>
                  <p className="text-xs text-slate-500">Fecha de creación</p>
                  <p className="text-sm text-white font-medium">
                    {new Date(caso.createdAt || caso.creadoEn || Date.now()).toLocaleDateString("es-CO")}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Tabs */}
        <Tabs defaultValue="details" className="space-y-4">
          <TabsList className="bg-slate-900 border border-slate-800">
            <TabsTrigger value="details" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
              Detalles
            </TabsTrigger>
            <TabsTrigger value="history" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
              Historial
            </TabsTrigger>
            <TabsTrigger value="attachments" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
              Adjuntos ({attachments.length})
            </TabsTrigger>
            <TabsTrigger
              value="communications"
              className="data-[state=active]:bg-blue-600 data-[state=active]:text-white"
            >
              Comunicaciones ({communications.length})
            </TabsTrigger>
          </TabsList>

          {/* Details Tab */}
          <TabsContent value="details">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader>
                <CardTitle className="text-white">Información Detallada</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid md:grid-cols-2 gap-4">
                  <div>
                    <Label className="text-slate-400 text-xs">ID Interno</Label>
                    <p className="text-white font-medium">{caso.internalId || caso.radicadoInterno || `Caso ${caso.id}`}</p>
                  </div>
                  {caso.idLocal && (
                    <div>
                      <Label className="text-slate-400 text-xs">ID Local</Label>
                      <p className="text-white font-medium">{caso.idLocal}</p>
                    </div>
                  )}
                  <div>
                    <Label className="text-slate-400 text-xs">Tipo</Label>
                    <p className="text-white font-medium capitalize">{caso.type}</p>
                  </div>
                  {(caso.theme || caso.temaNombre) && (
                    <div>
                      <Label className="text-slate-400 text-xs">Tema</Label>
                      <p className="text-white font-medium">{caso.theme || caso.temaNombre}</p>
                    </div>
                  )}
                  {caso.inspectorAsignadoNombre && (
                    <div>
                      <Label className="text-slate-400 text-xs">Inspector Asignado</Label>
                      <p className="text-white font-medium">{caso.inspectorAsignadoNombre}</p>
                    </div>
                  )}
                  {caso.naturaleza && (
                    <div>
                      <Label className="text-slate-400 text-xs">Naturaleza</Label>
                      <p className="text-white font-medium capitalize">{caso.naturaleza}</p>
                    </div>
                  )}
                  {(caso.oficioNumber || caso.numeroDespacho) && (
                    <div>
                      <Label className="text-slate-400 text-xs">Número de Oficio/Despacho</Label>
                      <p className="text-white font-medium">{caso.oficioNumber || caso.numeroDespacho}</p>
                    </div>
                  )}
                  {(caso.autoridad || caso.entidadProcedente) && (
                    <div>
                      <Label className="text-slate-400 text-xs">Autoridad/Entidad</Label>
                      <p className="text-white font-medium">{caso.autoridad || caso.entidadProcedente}</p>
                    </div>
                  )}
                  {(caso.procesoTipo || caso.asunto) && (
                    <div>
                      <Label className="text-slate-400 text-xs">Tipo de Proceso/Asunto</Label>
                      <p className="text-white font-medium">{caso.procesoTipo || caso.asunto}</p>
                    </div>
                  )}
                </div>

                <Separator className="bg-slate-800" />

                <div>
                  <Label className="text-slate-400 text-xs">Descripción</Label>
                  <p className="text-white mt-2">{caso.description || caso.descripcion}</p>
                </div>

                <div>
                  <Label className="text-slate-400 text-xs">Dirección</Label>
                  <p className="text-white mt-2">{caso.address || caso.direccion}</p>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* History Tab */}
          <TabsContent value="history">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader>
                <CardTitle className="text-white">Historial de Cambios</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {history.map((item, index) => (
                    <div key={item.id || `history-${index}-${item.creadoEn}`} className="flex gap-4">
                      <div className="flex flex-col items-center">
                        <div className="h-3 w-3 rounded-full bg-blue-600" />
                        {index < history.length - 1 && <div className="w-0.5 flex-1 bg-slate-700 mt-2" />}
                      </div>
                      <div className="flex-1 pb-4">
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="text-white font-medium">{item.estadoNombre}</p>
                            {item.motivo && <p className="text-sm text-slate-500 mt-2 italic">Motivo: {item.motivo}</p>}
                          </div>
                          <p className="text-xs text-slate-600">{new Date(item.creadoEn).toLocaleString("es-CO")}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Attachments Tab */}
          <TabsContent value="attachments">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-white">Archivos Adjuntos</CardTitle>
                {(user?.rol === "INSPECTOR" || user?.rol === "DIRECTORA") && (
                  <Button
                    onClick={() => setShowAttachmentDialog(true)}
                    size="sm"
                    className="bg-blue-600 hover:bg-blue-700 text-white"
                  >
                    <Upload className="mr-2 h-4 w-4" />
                    Subir Archivos
                  </Button>
                )}
              </CardHeader>
              <CardContent>
                {attachments.length === 0 ? (
                  <div className="text-center py-8">
                    <FileText className="h-12 w-12 text-slate-600 mx-auto mb-2" />
                    <p className="text-slate-400">No hay archivos adjuntos</p>
                  </div>
                ) : (
                  <div className="grid md:grid-cols-2 gap-4">
                    {attachments.map((attachment) => (
                      <div
                        key={attachment.id}
                        className="p-4 bg-slate-800 rounded-lg border border-slate-700 hover:border-slate-600 transition-colors"
                      >
                        <div className="flex items-start justify-between">
                          <div className="flex-1 min-w-0">
                            <p className="text-white font-medium truncate">{attachment.nombreArchivo}</p>
                            <p className="text-xs text-slate-500 mt-1">
                              {(attachment.tamanoBytes / 1024 / 1024).toFixed(2)} MB
                            </p>
                            <p className="text-xs text-slate-600 mt-1">
                              {new Date(attachment.creadoEn).toLocaleString("es-CO")}
                            </p>
                          </div>
                          <Button
                            size="sm"
                            variant="ghost"
                            className="text-blue-400 hover:text-blue-300 hover:bg-slate-700"
                          >
                            Ver
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Communications Tab */}
          <TabsContent value="communications">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-white">Comunicaciones</CardTitle>
                {(user?.rol === "INSPECTOR" || user?.rol === "DIRECTORA") && (
                  <Button
                    onClick={() => setShowCommunicationDialog(true)}
                    size="sm"
                    className="bg-blue-600 hover:bg-blue-700 text-white"
                  >
                    <MessageSquare className="mr-2 h-4 w-4" />
                    Registrar Comunicación
                  </Button>
                )}
              </CardHeader>
              <CardContent>
                {communications.length === 0 ? (
                  <div className="text-center py-8">
                    <MessageSquare className="h-12 w-12 text-slate-600 mx-auto mb-2" />
                    <p className="text-slate-400">No hay comunicaciones registradas</p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {communications.map((comm) => (
                      <div key={comm.id} className="p-4 bg-slate-800 rounded-lg border border-slate-700">
                        <div className="flex items-start justify-between mb-2">
                          <Badge variant="outline" className="bg-blue-900/50 text-blue-300 border-blue-800 capitalize">
                            {comm.tipo}
                          </Badge>
                          {comm.fechaEnvio && <p className="text-xs text-slate-600">{new Date(comm.fechaEnvio).toLocaleDateString("es-CO")}</p>}
                        </div>
                        {comm.destinatario && <p className="text-sm text-slate-400 mb-2">Destinatario: {comm.destinatario}</p>}
                        {comm.contenido && <p className="text-sm text-white">{comm.contenido}</p>}
                        {comm.asunto && <p className="text-sm text-slate-300 mt-1">Asunto: {comm.asunto}</p>}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      {/* Status Change Dialog */}
      <Dialog open={showStatusDialog} onOpenChange={setShowStatusDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Cambiar Estado del Caso</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nuevo Estado *</Label>
              <Select value={newStatus} onValueChange={setNewStatus}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue placeholder="Seleccionar estado" />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  {estados.length === 0 ? (
                    <SelectItem value="" disabled className="text-slate-500">
                      Cargando estados...
                    </SelectItem>
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

      {/* Reassign Dialog */}
      <Dialog open={showReassignDialog} onOpenChange={setShowReassignDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Reasignar Caso</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nuevo Inspector *</Label>
              <Select value={newInspector} onValueChange={setNewInspector}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue placeholder="Seleccionar inspector" />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  {inspectores
                    .filter((i) => i.id !== caso.inspectorAsignadoId)
                    .map((i) => (
                      <SelectItem key={i.id} value={i.id.toString()} className="text-white hover:bg-slate-800">
                        {i.nombre}
                      </SelectItem>
                    ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Motivo de la Reasignación *</Label>
              <Textarea
                value={reassignReason}
                onChange={(e) => setReassignReason(e.target.value)}
                placeholder="Explique el motivo de la reasignación..."
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[100px]"
              />
            </div>

            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowReassignDialog(false)
                  setNewInspector("")
                  setReassignReason("")
                }}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleReassign} className="bg-blue-600 hover:bg-blue-700 text-white">
                Reasignar
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Dialog */}
      <Dialog open={showEditDialog} onOpenChange={setShowEditDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Editar {caso?.type === "querella" ? "Querella" : "Despacho Comisorio"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            {caso?.type === "querella" ? (
              <>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Dirección / Lugar *</Label>
                    <Input
                      value={editAddress}
                      onChange={(e) => setEditAddress(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label className="text-slate-300">Tema</Label>
                    <Select value={editThemeId} onValueChange={setEditThemeId}>
                      <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                        <SelectValue placeholder="Seleccionar tema" />
                      </SelectTrigger>
                      <SelectContent className="bg-slate-900 border-slate-800">
                        {temas.map((t) => (
                          <SelectItem key={t.id} value={t.id.toString()} className="text-white hover:bg-slate-800">
                            {t.nombre}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label className="text-slate-300">Descripción Breve *</Label>
                  <Textarea
                    value={editDescription}
                    onChange={(e) => setEditDescription(e.target.value)}
                    className="bg-slate-800 border-slate-700 text-white min-h-[100px]"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label className="text-slate-300">Naturaleza</Label>
                  <RadioGroup value={editNaturaleza} onValueChange={(v) => setEditNaturaleza(v as any)}>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="PERSONA" id="edit-persona" className="border-slate-600 text-blue-600" />
                      <Label htmlFor="edit-persona" className="text-slate-300 font-normal cursor-pointer">Persona</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="OFICIO" id="edit-oficio" className="border-slate-600 text-blue-600" />
                      <Label htmlFor="edit-oficio" className="text-slate-300 font-normal cursor-pointer">De Oficio</Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem value="ANONIMA" id="edit-anonima" className="border-slate-600 text-blue-600" />
                      <Label htmlFor="edit-anonima" className="text-slate-300 font-normal cursor-pointer">Anónima</Label>
                    </div>
                  </RadioGroup>
                </div>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Barrio</Label>
                    <Input
                      value={editBarrio}
                      onChange={(e) => setEditBarrio(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label className="text-slate-300">Corregimiento</Label>
                    <Select value={editCorregimiento || "none"} onValueChange={(v) => setEditCorregimiento(v === "none" ? "" : v)}>
                      <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                        <SelectValue placeholder="Seleccionar corregimiento" />
                      </SelectTrigger>
                      <SelectContent className="bg-slate-900 border-slate-800">
                        <SelectItem value="none" className="text-white hover:bg-slate-800">Ninguno</SelectItem>
                        {CORREGIMIENTOS.map((corregimiento) => (
                          <SelectItem key={corregimiento} value={corregimiento} className="text-white hover:bg-slate-800">
                            {corregimiento}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Género del Querellante</Label>
                    <Select value={editGeneroQuerellante} onValueChange={setEditGeneroQuerellante}>
                      <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                        <SelectValue placeholder="Seleccionar género" />
                      </SelectTrigger>
                      <SelectContent className="bg-slate-900 border-slate-800">
                        {GENEROS.map((genero) => (
                          <SelectItem key={genero} value={genero} className="text-white hover:bg-slate-800">
                            {genero}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label className="text-slate-300">Género del Querellado</Label>
                    <Select value={editGeneroQuerellado} onValueChange={setEditGeneroQuerellado}>
                      <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                        <SelectValue placeholder="Seleccionar género" />
                      </SelectTrigger>
                      <SelectContent className="bg-slate-900 border-slate-800">
                        {GENEROS.map((genero) => (
                          <SelectItem key={genero} value={genero} className="text-white hover:bg-slate-800">
                            {genero}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label className="text-slate-300">Observaciones</Label>
                  <Textarea
                    value={editObservaciones}
                    onChange={(e) => setEditObservaciones(e.target.value)}
                    className="bg-slate-800 border-slate-700 text-white min-h-[80px]"
                  />
                </div>
              </>
            ) : (
              <>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Número de Despacho *</Label>
                    <Input
                      value={editNumeroDespacho}
                      onChange={(e) => setEditNumeroDespacho(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                      required
                    />
                  </div>
                  <div className="space-y-2">
                    <Label className="text-slate-300">Entidad Procedente *</Label>
                    <Input
                      value={editEntidadProcedente}
                      onChange={(e) => setEditEntidadProcedente(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                      required
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label className="text-slate-300">Asunto *</Label>
                  <Input
                    value={editAsunto}
                    onChange={(e) => setEditAsunto(e.target.value)}
                    className="bg-slate-800 border-slate-700 text-white"
                    required
                  />
                </div>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Radicado del Proceso</Label>
                    <Input
                      value={editRadicadoProceso}
                      onChange={(e) => setEditRadicadoProceso(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                    />
                  </div>
                </div>
                <div className="grid md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label className="text-slate-300">Demandante/Apoderado</Label>
                    <Input
                      value={editDemandanteApoderado}
                      onChange={(e) => setEditDemandanteApoderado(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label className="text-slate-300">Demandado/Apoderado</Label>
                    <Input
                      value={editDemandadoApoderado}
                      onChange={(e) => setEditDemandadoApoderado(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label className="text-slate-300">Observaciones</Label>
                  <Textarea
                    value={editObservaciones}
                    onChange={(e) => setEditObservaciones(e.target.value)}
                    className="bg-slate-800 border-slate-700 text-white min-h-[100px]"
                  />
                </div>
              </>
            )}
            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setShowEditDialog(false)}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleEdit} className="bg-blue-600 hover:bg-blue-700 text-white">
                Guardar Cambios
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Upload Attachments Dialog */}
      <Dialog open={showAttachmentDialog} onOpenChange={setShowAttachmentDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Subir Archivos Adjuntos</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="border-2 border-dashed border-slate-700 rounded-lg p-6 text-center hover:border-slate-600 transition-colors">
              <input
                type="file"
                id="attachment-upload"
                multiple
                onChange={handleFileChange}
                className="hidden"
                accept="image/*,.pdf,.doc,.docx"
              />
              <label htmlFor="attachment-upload" className="cursor-pointer">
                <Upload className="h-10 w-10 text-slate-500 mx-auto mb-2" />
                <p className="text-sm text-slate-400">Haga clic para cargar archivos o arrástrelos aquí</p>
                <p className="text-xs text-slate-600 mt-1">PDF, imágenes, documentos (máx. 10MB por archivo)</p>
              </label>
            </div>

            {files.length > 0 && (
              <div className="space-y-2">
                {files.map((file, index) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-3 bg-slate-800 rounded-lg border border-slate-700"
                  >
                    <span className="text-sm text-slate-300 truncate flex-1">{file.name}</span>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => removeFile(index)}
                      className="text-slate-400 hover:text-red-400 hover:bg-slate-700"
                    >
                      <X className="h-4 w-4" />
                    </Button>
                  </div>
                ))}
              </div>
            )}

            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowAttachmentDialog(false)
                  setFiles([])
                }}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleUploadAttachments} className="bg-blue-600 hover:bg-blue-700 text-white">
                Subir Archivos
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Add Communication Dialog */}
      <Dialog open={showCommunicationDialog} onOpenChange={setShowCommunicationDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Registrar Comunicación</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Tipo de Comunicación *</Label>
              <Select value={commType} onValueChange={(v) => setCommType(v as any)}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  <SelectItem value="CARTA" className="text-white hover:bg-slate-800">
                    Carta
                  </SelectItem>
                  <SelectItem value="CORREO" className="text-white hover:bg-slate-800">
                    Correo
                  </SelectItem>
                  <SelectItem value="AUDIENCIA" className="text-white hover:bg-slate-800">
                    Audiencia
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Fecha *</Label>
              <Input
                type="date"
                value={commDate}
                onChange={(e) => setCommDate(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Observaciones *</Label>
              <Textarea
                value={commObservations}
                onChange={(e) => setCommObservations(e.target.value)}
                placeholder="Describa la comunicación..."
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[100px]"
              />
            </div>

            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowCommunicationDialog(false)
                  setCommType("CARTA")
                  setCommDate("")
                  setCommObservations("")
                }}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleAddCommunication} className="bg-blue-600 hover:bg-blue-700 text-white">
                Registrar
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </MainLayout>
  )
}

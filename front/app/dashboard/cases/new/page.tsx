"use client"

import type React from "react"

import { MainLayout } from "@/components/layout/main-layout"
import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { ArrowLeft, Upload, X, Loader2 } from "lucide-react"
import Link from "next/link"
import { toast } from "sonner"
import { useAuth } from "@/lib/auth-context"
import { querellasApi, despachosApi, catalogosApi, usuariosApi } from "@/lib/api"
import { isNetworkError } from "@/lib/utils/error-handler"
import { MOCK_TEMAS, MOCK_COMUNAS, CORREGIMIENTOS, GENEROS } from "@/lib/utils/constants"

export default function NewCasePage() {
  const router = useRouter()
  const { user } = useAuth()
  const [caseType, setCaseType] = useState<"querella" | "despacho">("querella")
  const [files, setFiles] = useState<File[]>([])
  const [loading, setLoading] = useState(false)
  const [temas, setTemas] = useState<Array<{ id: number; nombre: string }>>([])
  const [comunas, setComunas] = useState<Array<{ id: number; nombre: string }>>([])
  const [barrios, setBarrios] = useState<Array<{ id: number; nombre: string }>>([])
  const [inspectores, setInspectores] = useState<Array<{ id: number; nombre: string }>>([])

  // Querella fields
  const [address, setAddress] = useState("")
  const [description, setDescription] = useState("")
  const [themeId, setThemeId] = useState("")
  const [naturaleza, setNaturaleza] = useState<"OFICIO" | "PERSONA" | "ANONIMA">("PERSONA")
  const [inspectorId, setInspectorId] = useState("")
  const [comunaId, setComunaId] = useState("")
  const [corregimiento, setCorregimiento] = useState("none")
  const [barrio, setBarrio] = useState("")
  const [generoQuerellante, setGeneroQuerellante] = useState("")
  const [generoQuerellado, setGeneroQuerellado] = useState("")
  const [observaciones, setObservaciones] = useState("")
  const [idInterno, setIdInterno] = useState("")
  const [idAlcaldia, setIdAlcaldia] = useState("")

  // Despacho fields
  const [numeroDespacho, setNumeroDespacho] = useState("")
  const [entidadProcedente, setEntidadProcedente] = useState("")
  const [asunto, setAsunto] = useState("")
  const [fechaRecibido, setFechaRecibido] = useState("")
  const [fechaDevolucion, setFechaDevolucion] = useState("")
  const [radicadoProceso, setRadicadoProceso] = useState("")
  const [demandanteApoderado, setDemandanteApoderado] = useState("")
  const [demandadoApoderado, setDemandadoApoderado] = useState("")
  const [observacionesDespacho, setObservacionesDespacho] = useState("")

  useEffect(() => {
    loadCatalogos()
    loadInspectores()
  }, [])

  // Cargar barrios cuando cambia la comuna
  useEffect(() => {
    if (comunaId) {
      loadBarrios(comunaId)
    } else {
      setBarrios([])
      setBarrio("")
    }
  }, [comunaId])

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
        console.error("Error loading catalogos:", error)
      } else {
        console.info("Backend no disponible, usando datos mock para catálogos")
      }
      
      setTemas([...MOCK_TEMAS])
      setComunas([...MOCK_COMUNAS])
    }
  }

  const loadInspectores = async () => {
    try {
      const inspectoresList = await usuariosApi.listInspectores()
      setInspectores(inspectoresList.map((i) => ({ id: i.id, nombre: i.nombre })))
    } catch (error: any) {
      if (!isNetworkError(error)) {
        console.error("Error loading inspectores:", error)
      } else {
        console.info("Backend no disponible, usando datos mock para inspectores")
      }
      
      const mockInspectores = [
        { id: 1, nombre: "Inspector 1" },
        { id: 2, nombre: "Inspector 2" },
        { id: 3, nombre: "Inspector 3" },
      ]
      setInspectores(mockInspectores)
    }
  }

  const loadBarrios = async (comunaId: string) => {
    if (!comunaId) {
      setBarrios([])
      setBarrio("")
      return
    }

    try {
      // Aquí deberías llamar a una API para obtener barrios por comuna
      // Por ahora usaremos datos mock
      const mockBarriosPorComuna: Record<string, Array<{ id: number; nombre: string }>> = {
        "1": [
          { id: 1, nombre: "Barrio Centro" },
          { id: 2, nombre: "Barrio La Toma" },
          { id: 3, nombre: "Barrio El Centro" },
        ],
        "2": [
          { id: 4, nombre: "Barrio Las Palmas" },
          { id: 5, nombre: "Barrio San Pedro" },
          { id: 6, nombre: "Barrio El Prado" },
        ],
        "3": [
          { id: 7, nombre: "Barrio Los Rosales" },
          { id: 8, nombre: "Barrio La Esperanza" },
          { id: 9, nombre: "Barrio San José" },
        ],
        "4": [
          { id: 10, nombre: "Barrio El Jardín" },
          { id: 11, nombre: "Barrio La Floresta" },
          { id: 12, nombre: "Barrio Los Alpes" },
        ],
        "5": [
          { id: 13, nombre: "Barrio El Paraíso" },
          { id: 14, nombre: "Barrio La Victoria" },
          { id: 15, nombre: "Barrio San Martín" },
        ],
        "6": [
          { id: 16, nombre: "Barrio El Progreso" },
          { id: 17, nombre: "Barrio La Paz" },
          { id: 18, nombre: "Barrio Los Andes" },
        ],
        "7": [
          { id: 19, nombre: "Barrio El Bosque" },
          { id: 20, nombre: "Barrio La Primavera" },
          { id: 21, nombre: "Barrio San Antonio" },
        ],
        "8": [
          { id: 22, nombre: "Barrio El Recreo" },
          { id: 23, nombre: "Barrio La Unión" },
          { id: 24, nombre: "Barrio Los Pinos" },
        ],
        "9": [
          { id: 25, nombre: "Barrio El Sol" },
          { id: 26, nombre: "Barrio La Estrella" },
          { id: 27, nombre: "Barrio San Rafael" },
        ],
        "10": [
          { id: 28, nombre: "Barrio El Mirador" },
          { id: 29, nombre: "Barrio La Colina" },
          { id: 30, nombre: "Barrio Los Olivos" },
        ],
      }

      const barrios = mockBarriosPorComuna[comunaId] || []
      setBarrios(barrios)
      setBarrio("") // Limpiar barrio cuando cambia la comuna
    } catch (error: any) {
      console.error("Error loading barrios:", error)
      setBarrios([])
      setBarrio("")
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (caseType === "querella") {
        if (!address || !description) {
          toast.error("Dirección y descripción son obligatorios")
          setLoading(false)
          return
        }

        const querellaData: any = {
          direccion: address,
          descripcion: description,
          naturaleza: naturaleza,
        }

        if (themeId) querellaData.temaId = parseInt(themeId)
        if (comunaId) querellaData.comunaId = parseInt(comunaId)
        if (inspectorId) querellaData.inspectorAsignadoId = parseInt(inspectorId)
        if (user?.id) querellaData.asignadoPorId = user.id
        if (barrio) querellaData.barrio = barrio
        if (corregimiento && corregimiento !== "none") querellaData.corregimiento = corregimiento
        if (generoQuerellante) querellaData.generoQuerellante = generoQuerellante
        if (generoQuerellado) querellaData.generoQuerellado = generoQuerellado
        if (observaciones) querellaData.observaciones = observaciones
        if (idInterno) querellaData.idInterno = idInterno
        if (idAlcaldia) querellaData.idAlcaldia = idAlcaldia

        const nuevaQuerella = await querellasApi.create(querellaData)
        toast.success(`Querella radicada exitosamente: ${nuevaQuerella.radicadoInterno}`)
        router.push(`/dashboard/cases/${nuevaQuerella.id}`)
      } else {
        if (!numeroDespacho || !entidadProcedente || !asunto || !fechaRecibido) {
          toast.error("Todos los campos obligatorios deben estar completos")
          setLoading(false)
          return
        }

        const despachoData: any = {
          numeroDespacho,
          entidadProcedente,
          asunto,
          fechaRecibido: new Date(fechaRecibido).toISOString(),
        }

        if (radicadoProceso) despachoData.radicadoProceso = radicadoProceso
        if (demandanteApoderado) despachoData.demandanteApoderado = demandanteApoderado
        if (demandadoApoderado) despachoData.demandadoApoderado = demandadoApoderado
        if (inspectorId) despachoData.inspectorAsignadoId = parseInt(inspectorId)
        if (user?.id) despachoData.asignadoPorId = user.id
        if (observacionesDespacho) despachoData.observaciones = observacionesDespacho
        if (fechaDevolucion) despachoData.fechaDevolucion = new Date(fechaDevolucion).toISOString()

        const nuevoDespacho = await despachosApi.create(despachoData)
        toast.success(`Despacho radicado exitosamente: ${nuevoDespacho.numeroDespacho}`)
        router.push(`/dashboard/cases/${nuevoDespacho.id}`)
      }
    } catch (error: any) {
      console.error("Error creating case:", error)
      toast.error("Error al radicar el caso: " + (error.message || "Error desconocido"))
      setLoading(false)
    }
  }

  return (
    <MainLayout>
      <div className="max-w-4xl mx-auto space-y-6">
        {/* Header */}
        <div className="flex items-center gap-4">
          <Link href="/dashboard/cases">
            <Button variant="ghost" size="icon" className="text-slate-400 hover:text-white hover:bg-slate-800">
              <ArrowLeft className="h-5 w-5" />
            </Button>
          </Link>
          <div>
            <h2 className="text-3xl font-bold text-white">Radicar Nuevo Caso</h2>
            <p className="text-slate-400 mt-1">Complete el formulario para radicar un caso</p>
          </div>
        </div>

        <Tabs value={caseType} onValueChange={(v) => setCaseType(v as "querella" | "despacho")}>
          <TabsList className="grid w-full grid-cols-2 bg-slate-900 border border-slate-800">
            <TabsTrigger value="querella" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
              Querella
            </TabsTrigger>
            <TabsTrigger value="despacho" className="data-[state=active]:bg-blue-600 data-[state=active]:text-white">
              Despacho Comisorio
            </TabsTrigger>
          </TabsList>

          <form onSubmit={handleSubmit}>
            {/* Querella Form */}
            <TabsContent value="querella" className="space-y-6">
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white">Información de la Querella</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Dirección / Lugar *</Label>
                      <Input
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                        placeholder="Ej: Calle 10 #5-20, Neiva"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                        required
                      />
                    </div>

                    <div className="space-y-2">
                      <Label className="text-slate-300">Tema</Label>
                      <Select value={themeId} onValueChange={setThemeId} disabled={temas.length === 0}>
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white disabled:opacity-50 disabled:cursor-not-allowed">
                          <SelectValue placeholder={temas.length === 0 ? "No hay temas disponibles" : "Seleccionar tema"} />
                        </SelectTrigger>
                        {temas.length > 0 && (
                          <SelectContent className="bg-slate-900 border-slate-800">
                            {temas.map((t) => (
                              <SelectItem key={t.id} value={t.id.toString()} className="text-white hover:bg-slate-800">
                                {t.nombre}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        )}
                      </Select>
                      {temas.length === 0 && (
                        <p className="text-xs text-slate-500 mt-1">Cargando temas...</p>
                      )}
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Descripción Breve *</Label>
                    <Textarea
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder="Describa brevemente el caso..."
                      className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[100px]"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Naturaleza</Label>
                    <RadioGroup value={naturaleza} onValueChange={(v) => setNaturaleza(v as any)}>
                      <div className="flex items-center space-x-2">
                        <RadioGroupItem value="PERSONA" id="persona" className="border-slate-600 text-blue-600" />
                        <Label htmlFor="persona" className="text-slate-300 font-normal cursor-pointer">
                          Persona
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <RadioGroupItem value="OFICIO" id="oficio" className="border-slate-600 text-blue-600" />
                        <Label htmlFor="oficio" className="text-slate-300 font-normal cursor-pointer">
                          De Oficio
                        </Label>
                      </div>
                      <div className="flex items-center space-x-2">
                        <RadioGroupItem value="ANONIMA" id="anonima" className="border-slate-600 text-blue-600" />
                        <Label htmlFor="anonima" className="text-slate-300 font-normal cursor-pointer">
                          Anónima
                        </Label>
                      </div>
                    </RadioGroup>
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Comuna</Label>
                      <Select 
                        value={comunaId || "none"} 
                        onValueChange={(v) => {
                          if (v === "none") {
                            setComunaId("")
                            setBarrio("") // Limpiar barrio cuando se selecciona "Ninguna"
                          } else {
                            setComunaId(v)
                            setCorregimiento("none") // Limpiar corregimiento si se selecciona comuna
                          }
                        }}
                        disabled={!!corregimiento && corregimiento !== "none"}
                      >
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white disabled:opacity-50 disabled:cursor-not-allowed">
                          <SelectValue placeholder={comunas.length === 0 ? "No hay comunas disponibles" : "Seleccionar comuna"} />
                        </SelectTrigger>
                        {comunas.length > 0 && (
                          <SelectContent className="bg-slate-900 border-slate-800">
                            <SelectItem value="none" className="text-white hover:bg-slate-800">
                              Ninguna
                            </SelectItem>
                            {comunas.map((c) => (
                              <SelectItem key={c.id} value={c.id.toString()} className="text-white hover:bg-slate-800">
                                {c.nombre}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        )}
                      </Select>
                      {corregimiento && corregimiento !== "none" && (
                        <p className="text-xs text-slate-500 mt-1">Seleccione "Ninguna" en comuna para poder seleccionar un corregimiento</p>
                      )}
                      {comunas.length === 0 && !corregimiento && (
                        <p className="text-xs text-slate-500 mt-1">Cargando comunas...</p>
                      )}
                    </div>

                    <div className="space-y-2">
                      <Label className="text-slate-300">Barrio</Label>
                      <Select 
                        value={barrio} 
                        onValueChange={setBarrio}
                        disabled={!comunaId || barrios.length === 0}
                      >
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white disabled:opacity-50 disabled:cursor-not-allowed">
                          <SelectValue placeholder={!comunaId ? "Seleccione una comuna primero" : barrios.length === 0 ? "No hay barrios disponibles" : "Seleccionar barrio (opcional)"} />
                        </SelectTrigger>
                        {barrios.length > 0 && (
                          <SelectContent className="bg-slate-900 border-slate-800">
                            {barrios.map((b) => (
                              <SelectItem key={b.id} value={b.nombre} className="text-white hover:bg-slate-800">
                                {b.nombre}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        )}
                      </Select>
                      {!comunaId && (
                        <p className="text-xs text-slate-500 mt-1">Seleccione una comuna para ver los barrios</p>
                      )}
                    </div>
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Corregimiento</Label>
                      <Select 
                        value={corregimiento || "none"} 
                        onValueChange={(v) => {
                          if (v === "none") {
                            setCorregimiento("")
                          } else {
                            setCorregimiento(v)
                            setComunaId("") // Limpiar comuna si se selecciona corregimiento
                          }
                        }}
                        disabled={!!comunaId}
                      >
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white disabled:opacity-50 disabled:cursor-not-allowed">
                          <SelectValue placeholder="Seleccionar corregimiento (opcional)" />
                        </SelectTrigger>
                        <SelectContent className="bg-slate-900 border-slate-800">
                          <SelectItem value="none" className="text-white hover:bg-slate-800">
                            Ninguno
                          </SelectItem>
                          {CORREGIMIENTOS.map((corregimiento) => (
                            <SelectItem key={corregimiento} value={corregimiento} className="text-white hover:bg-slate-800">
                              {corregimiento}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      {comunaId && (
                        <p className="text-xs text-slate-500 mt-1">Deshabilite la comuna para seleccionar un corregimiento</p>
                      )}
                    </div>
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Género del Querellante</Label>
                      <Select value={generoQuerellante} onValueChange={setGeneroQuerellante}>
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                          <SelectValue placeholder="Seleccionar género (opcional)" />
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
                      <Select value={generoQuerellado} onValueChange={setGeneroQuerellado}>
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                          <SelectValue placeholder="Seleccionar género (opcional)" />
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

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">ID Interno (Opcional)</Label>
                      <Input
                        value={idInterno}
                        onChange={(e) => setIdInterno(e.target.value)}
                        placeholder="Ingrese el ID interno manualmente"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label className="text-slate-300">ID Alcaldía (Opcional)</Label>
                      <Input
                        value={idAlcaldia}
                        onChange={(e) => setIdAlcaldia(e.target.value)}
                        placeholder="Ingrese el ID de alcaldía manualmente"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Observaciones</Label>
                    <Textarea
                      value={observaciones}
                      onChange={(e) => setObservaciones(e.target.value)}
                      placeholder="Observaciones adicionales (opcional)"
                      className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[80px]"
                    />
                  </div>

                  {(user?.rol === "DIRECTORA" || user?.rol === "AUXILIAR") && (
                    <div className="space-y-2">
                      <Label className="text-slate-300">Inspector</Label>
                      <Select value={inspectorId} onValueChange={setInspectorId} disabled={inspectores.length === 0}>
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white disabled:opacity-50 disabled:cursor-not-allowed">
                          <SelectValue placeholder={inspectores.length === 0 ? "No hay inspectores disponibles" : "Asignar inspector"} />
                        </SelectTrigger>
                        {inspectores.length > 0 && (
                          <SelectContent className="bg-slate-900 border-slate-800">
                            {inspectores.map((i) => (
                              <SelectItem key={i.id} value={i.id.toString()} className="text-white hover:bg-slate-800">
                                {i.nombre}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        )}
                      </Select>
                      {inspectores.length === 0 && (
                        <p className="text-xs text-slate-500 mt-1">Cargando inspectores...</p>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Attachments */}
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white">Adjuntos (Opcional)</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="border-2 border-dashed border-slate-700 rounded-lg p-6 text-center hover:border-slate-600 transition-colors">
                    <input
                      type="file"
                      id="file-upload"
                      multiple
                      onChange={handleFileChange}
                      className="hidden"
                      accept="image/*,.pdf,.doc,.docx"
                    />
                    <label htmlFor="file-upload" className="cursor-pointer">
                      <Upload className="h-10 w-10 text-slate-500 mx-auto mb-2" />
                      <p className="text-sm text-slate-400">Haga clic para cargar archivos o arrastrélos aquí</p>
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
                </CardContent>
              </Card>

              <div className="flex gap-2 justify-end">
                <Link href="/dashboard/cases">
                  <Button
                    type="button"
                    variant="outline"
                    className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
                  >
                    Cancelar
                  </Button>
                </Link>
                <Button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white" disabled={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Radicando...
                    </>
                  ) : (
                    "Radicar Querella"
                  )}
                </Button>
              </div>
            </TabsContent>

            {/* Despacho Form */}
            <TabsContent value="despacho" className="space-y-6">
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white">Información del Despacho Comisorio</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Número de Despacho *</Label>
                      <Input
                        value={numeroDespacho}
                        onChange={(e) => setNumeroDespacho(e.target.value)}
                        placeholder="Ej: OF-2025-123"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                        required
                      />
                    </div>

                    <div className="space-y-2">
                      <Label className="text-slate-300">Fecha de Recibido *</Label>
                      <Input
                        type="datetime-local"
                        value={fechaRecibido}
                        onChange={(e) => setFechaRecibido(e.target.value)}
                        className="bg-slate-800 border-slate-700 text-white"
                        required
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Entidad Procedente *</Label>
                    <Input
                      value={entidadProcedente}
                      onChange={(e) => setEntidadProcedente(e.target.value)}
                      placeholder="Ej: Juzgado Civil del Circuito"
                      className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Asunto *</Label>
                    <Input
                      value={asunto}
                      onChange={(e) => setAsunto(e.target.value)}
                      placeholder="Ej: Diligencia de secuestro"
                      className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Fecha de Devolución al Juzgado Comitente (Opcional)</Label>
                    <Input
                      type="datetime-local"
                      value={fechaDevolucion}
                      onChange={(e) => setFechaDevolucion(e.target.value)}
                      className="bg-slate-800 border-slate-700 text-white"
                    />
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Radicado del Proceso</Label>
                      <Input
                        value={radicadoProceso}
                        onChange={(e) => setRadicadoProceso(e.target.value)}
                        placeholder="Radicado del proceso (opcional)"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      />
                    </div>
                  </div>

                  <div className="grid md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="text-slate-300">Demandante/Apoderado</Label>
                      <Input
                        value={demandanteApoderado}
                        onChange={(e) => setDemandanteApoderado(e.target.value)}
                        placeholder="Demandante o apoderado (opcional)"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      />
                    </div>
                    <div className="space-y-2">
                      <Label className="text-slate-300">Demandado/Apoderado</Label>
                      <Input
                        value={demandadoApoderado}
                        onChange={(e) => setDemandadoApoderado(e.target.value)}
                        placeholder="Demandado o apoderado (opcional)"
                        className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label className="text-slate-300">Observaciones</Label>
                    <Textarea
                      value={observacionesDespacho}
                      onChange={(e) => setObservacionesDespacho(e.target.value)}
                      placeholder="Observaciones adicionales..."
                      className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500 min-h-[100px]"
                    />
                  </div>

                  {(user?.rol === "DIRECTORA" || user?.rol === "AUXILIAR") && (
                    <div className="space-y-2">
                      <Label className="text-slate-300">Inspector</Label>
                      <Select value={inspectorId} onValueChange={setInspectorId}>
                        <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                          <SelectValue placeholder="Asignar inspector" />
                        </SelectTrigger>
                        <SelectContent className="bg-slate-900 border-slate-800">
                          {inspectores.map((i) => (
                            <SelectItem key={i.id} value={i.id.toString()} className="text-white hover:bg-slate-800">
                              {i.nombre}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* PDF Upload (Required for Despacho) */}
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white">PDF del Oficio *</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="border-2 border-dashed border-slate-700 rounded-lg p-6 text-center hover:border-slate-600 transition-colors">
                    <input
                      type="file"
                      id="pdf-upload"
                      onChange={handleFileChange}
                      className="hidden"
                      accept=".pdf"
                      required
                    />
                    <label htmlFor="pdf-upload" className="cursor-pointer">
                      <Upload className="h-10 w-10 text-slate-500 mx-auto mb-2" />
                      <p className="text-sm text-slate-400">Haga clic para cargar el PDF del oficio</p>
                      <p className="text-xs text-slate-600 mt-1">Solo archivos PDF (máx. 10MB)</p>
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
                </CardContent>
              </Card>

              <div className="flex gap-2 justify-end">
                <Link href="/dashboard/cases">
                  <Button
                    type="button"
                    variant="outline"
                    className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
                  >
                    Cancelar
                  </Button>
                </Link>
                <Button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white" disabled={loading}>
                  {loading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Radicando...
                    </>
                  ) : (
                    "Radicar Despacho"
                  )}
                </Button>
              </div>
            </TabsContent>
          </form>
        </Tabs>
      </div>
    </MainLayout>
  )
}

"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { useState } from "react"
import { mockCases, mockThemes } from "@/lib/mock-data"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Search, FileText, AlertTriangle } from "lucide-react"
import Link from "next/link"
import { Alert, AlertDescription } from "@/components/ui/alert"

export default function SearchPage() {
  const [internalId, setInternalId] = useState("")
  const [alcaldiaId, setAlcaldiaId] = useState("")
  const [localId, setLocalId] = useState("")
  const [address, setAddress] = useState("")
  const [description, setDescription] = useState("")
  const [caseType, setCaseType] = useState("all")
  const [theme, setTheme] = useState("all")
  const [status, setStatus] = useState("all")
  const [dateFrom, setDateFrom] = useState("")
  const [dateTo, setDateTo] = useState("")

  const [results, setResults] = useState<typeof mockCases>([])
  const [duplicates, setDuplicates] = useState<typeof mockCases>([])
  const [searched, setSearched] = useState(false)

  const handleSearch = () => {
    let filtered = mockCases

    if (caseType && caseType !== "all") filtered = filtered.filter((c) => c.type === caseType)
    if (internalId) filtered = filtered.filter((c) => c.internalId.toLowerCase().includes(internalId.toLowerCase()))
    if (alcaldiaId) filtered = filtered.filter((c) => c.alcaldiaId?.toLowerCase().includes(alcaldiaId.toLowerCase()))
    if (localId) filtered = filtered.filter((c) => c.localId?.toLowerCase().includes(localId.toLowerCase()))
    if (address) filtered = filtered.filter((c) => c.address.toLowerCase().includes(address.toLowerCase()))
    if (description) filtered = filtered.filter((c) => c.description.toLowerCase().includes(description.toLowerCase()))
    if (theme && theme !== "all") filtered = filtered.filter((c) => c.theme === theme)
    if (status && status !== "all") filtered = filtered.filter((c) => c.status === status)
    if (dateFrom) filtered = filtered.filter((c) => new Date(c.createdAt) >= new Date(dateFrom))
    if (dateTo) filtered = filtered.filter((c) => new Date(c.createdAt) <= new Date(dateTo))

    // Detect duplicates (simple similarity check)
    const potentialDuplicates = mockCases.filter((c) =>
      filtered.some(
        (r) =>
          r.id !== c.id &&
          (r.address.toLowerCase() === c.address.toLowerCase() ||
            r.description.toLowerCase().includes(c.description.toLowerCase().substring(0, 20))),
      ),
    )

    setResults(filtered)
    setDuplicates(potentialDuplicates)
    setSearched(true)
  }

  const handleClear = () => {
    setInternalId("")
    setAlcaldiaId("")
    setLocalId("")
    setAddress("")
    setDescription("")
    setCaseType("all")
    setTheme("all")
    setStatus("all")
    setDateFrom("")
    setDateTo("")
    setResults([])
    setDuplicates([])
    setSearched(false)
  }

  // Estados según el tipo de caso
  const getEstadosByType = (type: string) => {
    if (type === "despacho") {
      return [
        { id: "RECIBIDO", nombre: "Recibido" },
        { id: "ASIGNADO", nombre: "Asignado" },
        { id: "EN_TRAMITE", nombre: "En Trámite" },
        { id: "DILIGENCIADO", nombre: "Diligenciado" },
        { id: "DEVUELTO", nombre: "Devuelto" },
        { id: "PENDIENTE", nombre: "Pendiente" },
      ]
    } else {
      // QUERELLA
      return [
        { id: "RECIBIDA", nombre: "Recibida" },
        { id: "ASIGNADA", nombre: "Asignada" },
        { id: "EN_PROCESO", nombre: "En Proceso" },
        { id: "EN_INVESTIGACION", nombre: "En Investigación" },
        { id: "CITACION_ENVIADA", nombre: "Citación Enviada" },
        { id: "AUDIENCIA_PROGRAMADA", nombre: "Audiencia Programada" },
        { id: "EN_AUDIENCIA", nombre: "En Audiencia" },
        { id: "RESOLUCION_EMITIDA", nombre: "Resolución Emitida" },
        { id: "CERRADA", nombre: "Cerrada" },
        { id: "ARCHIVADA", nombre: "Archivada" },
        { id: "ANULADA", nombre: "Anulada" },
      ]
    }
  }

  const getStatusBadge = (status: string, caseType?: string) => {
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
    
    const variants = caseType === 'despacho' ? despachoVariants : querellaVariants
    const variant = variants[statusUpper] || { label: status, className: "bg-slate-800 text-slate-300" }
    
    return (
      <Badge variant="outline" className={variant.className}>
        {variant.label}
      </Badge>
    )
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-bold text-green-900">Búsqueda Avanzada</h2>
          <p className="text-gray-600 mt-1">Busque casos utilizando múltiples criterios</p>
        </div>

        {/* Search Form */}
        <Card className="bg-white border-green-200 shadow-sm">
          <CardHeader className="border-b border-green-100">
            <CardTitle className="text-green-900">Criterios de Búsqueda</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 pt-6">
            <div className="grid md:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label className="text-green-900 font-medium">ID Interno</Label>
                <Input
                  value={internalId}
                  onChange={(e) => setInternalId(e.target.value)}
                  placeholder="Q-2025-000001"
                  className="bg-white border-gray-300 text-gray-900 placeholder:text-gray-400 focus:border-green-600 focus:ring-green-600"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">ID Alcaldía</Label>
                <Input
                  value={alcaldiaId}
                  onChange={(e) => setAlcaldiaId(e.target.value)}
                  placeholder="ALC-2025-001"
                  className="bg-white border-gray-300 text-gray-900 placeholder:text-gray-400 focus:border-green-600 focus:ring-green-600"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">ID Local</Label>
                <Input
                  value={localId}
                  onChange={(e) => setLocalId(e.target.value)}
                  placeholder="5U-00001"
                  className="bg-white border-gray-300 text-gray-900 placeholder:text-gray-400 focus:border-green-600 focus:ring-green-600"
                />
              </div>
            </div>

            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Dirección</Label>
                <Input
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Buscar por dirección..."
                  className="bg-white border-gray-300 text-gray-900 placeholder:text-gray-400 focus:border-green-600 focus:ring-green-600"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Descripción</Label>
                <Input
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Buscar en descripción..."
                  className="bg-white border-gray-300 text-gray-900 placeholder:text-gray-400 focus:border-green-600 focus:ring-green-600"
                />
              </div>
            </div>

            <div className="grid md:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Tipo de Caso</Label>
                <Select value={caseType} onValueChange={(value) => {
                  setCaseType(value)
                  setStatus("all") // Limpiar estado cuando cambia el tipo
                }}>
                  <SelectTrigger className="bg-white border-gray-300 text-gray-900 focus:border-green-600 focus:ring-green-600">
                    <SelectValue placeholder="Todos" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-gray-300">
                    <SelectItem value="all" className="text-gray-900 hover:bg-green-50">
                      Todos
                    </SelectItem>
                    <SelectItem value="querella" className="text-gray-900 hover:bg-green-50">
                      Querella
                    </SelectItem>
                    <SelectItem value="despacho" className="text-gray-900 hover:bg-green-50">
                      Despacho Comisorio
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Tema</Label>
                <Select value={theme} onValueChange={setTheme}>
                  <SelectTrigger className="bg-white border-gray-300 text-gray-900 focus:border-green-600 focus:ring-green-600">
                    <SelectValue placeholder="Todos" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-gray-300">
                    <SelectItem value="all" className="text-gray-900 hover:bg-green-50">
                      Todos
                    </SelectItem>
                    {mockThemes.map((t) => (
                      <SelectItem key={t.id} value={t.name} className="text-gray-900 hover:bg-green-50">
                        {t.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Estado</Label>
                <Select value={status} onValueChange={setStatus}>
                  <SelectTrigger className="bg-white border-gray-300 text-gray-900 focus:border-green-600 focus:ring-green-600">
                    <SelectValue placeholder="Todos" />
                  </SelectTrigger>
                  <SelectContent className="bg-white border-gray-300">
                    <SelectItem value="all" className="text-gray-900 hover:bg-green-50">
                      Todos
                    </SelectItem>
                    {getEstadosByType(caseType || "querella").map((estado) => (
                      <SelectItem key={estado.id} value={estado.id} className="text-gray-900 hover:bg-green-50">
                        {estado.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

            </div>

            <div className="grid md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Fecha Desde</Label>
                <Input
                  type="date"
                  value={dateFrom}
                  onChange={(e) => setDateFrom(e.target.value)}
                  className="bg-white border-gray-300 text-gray-900 focus:border-green-600 focus:ring-green-600"
                />
              </div>

              <div className="space-y-2">
                <Label className="text-green-900 font-medium">Fecha Hasta</Label>
                <Input
                  type="date"
                  value={dateTo}
                  onChange={(e) => setDateTo(e.target.value)}
                  className="bg-white border-gray-300 text-gray-900 focus:border-green-600 focus:ring-green-600"
                />
              </div>
            </div>

            <div className="flex gap-2 justify-end pt-4 border-t border-green-100">
              <Button
                onClick={handleClear}
                variant="outline"
                className="border-gray-300 text-gray-700 hover:bg-gray-50 bg-transparent"
              >
                Limpiar
              </Button>
              <Button onClick={handleSearch} className="bg-green-700 hover:bg-green-800 text-white">
                <Search className="mr-2 h-4 w-4" />
                Buscar
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Duplicates Alert */}
        {searched && duplicates.length > 0 && (
          <Alert className="bg-yellow-900/20 border-yellow-800">
            <AlertTriangle className="h-4 w-4 text-yellow-400" />
            <AlertDescription className="text-yellow-300">
              Se encontraron {duplicates.length} posibles casos duplicados. Revise los resultados cuidadosamente.
            </AlertDescription>
          </Alert>
        )}

        {/* Results */}
        {searched && (
          <Card className="bg-white border-green-200 shadow-sm">
            <CardHeader className="border-b border-green-100">
              <CardTitle className="text-green-900">Resultados de Búsqueda ({results.length})</CardTitle>
            </CardHeader>
            <CardContent className="pt-6">
              {results.length === 0 ? (
                <div className="text-center py-8">
                  <FileText className="h-12 w-12 text-gray-400 mx-auto mb-2" />
                  <p className="text-gray-600">No se encontraron casos con los criterios especificados</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {results.map((caso) => (
                    <Link
                      key={caso.id}
                      href={`/dashboard/cases/${caso.id}`}
                      className="block p-4 bg-gray-50 rounded-lg border border-gray-200 hover:border-green-500 hover:bg-green-50 transition-colors"
                    >
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-2">
                            <p className="text-green-900 font-semibold">{caso.internalId}</p>
                            <Badge variant="outline" className="bg-slate-100 text-slate-700 border-slate-300">
                              {caso.type === 'querella' ? 'Querella' : 'Despacho'}
                            </Badge>
                            {getStatusBadge(caso.status, caso.type)}
                            {duplicates.some((d) => d.id === caso.id) && (
                              <Badge variant="outline" className="bg-yellow-100 text-yellow-800 border-yellow-300">
                                Posible Duplicado
                              </Badge>
                            )}
                          </div>
                          <p className="text-sm text-gray-700 mb-2">{caso.description}</p>
                          <p className="text-xs text-gray-500">{caso.address}</p>
                        </div>
                        <p className="text-xs text-gray-500">{new Date(caso.createdAt).toLocaleDateString("es-CO")}</p>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </MainLayout>
  )
}

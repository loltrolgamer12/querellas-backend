"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { useState } from "react"
import { mockCases, mockThemes } from "@/lib/mock-data"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { FileText, Download, BarChart3 } from "lucide-react"
import { toast } from "sonner"
import { useAuth } from "@/lib/auth-context"

export default function ReportsPage() {
  const { user } = useAuth()
  const [quarter, setQuarter] = useState("Q1")
  const [year, setYear] = useState("2025")
  const [theme, setTheme] = useState("all")

  const handleGenerateReport = () => {
    toast.success("Reporte generado exitosamente")
  }

  const handleExportPDF = () => {
    toast.success("Exportando reporte a PDF...")
  }

  const handleExportExcel = () => {
    toast.success("Exportando reporte a Excel...")
  }

  // Calculate statistics
  const filteredCases = mockCases.filter((c) => {
    if (theme !== "all" && c.theme !== theme) return false
    return true
  })

  const stats = {
    total: filteredCases.length,
    querellas: filteredCases.filter((c) => c.type === "querella").length,
    despachos: filteredCases.filter((c) => c.type === "despacho").length,
    recibidas: filteredCases.filter((c) => c.status === "recibida").length,
    verificacion: filteredCases.filter((c) => c.status === "verificacion").length,
    enTramite: filteredCases.filter((c) => c.status === "en_tramite").length,
    cerradas: filteredCases.filter((c) => c.status === "cerrada").length,
  }


  const byTheme = mockThemes.map((t) => ({
    name: t.name,
    count: filteredCases.filter((c) => c.theme === t.name).length,
  }))

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-bold text-white">Reportes Trimestrales</h2>
          <p className="text-slate-400 mt-1">Genere y exporte reportes estadísticos</p>
        </div>

        {/* Report Configuration */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white">Configuración del Reporte</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid md:grid-cols-4 gap-4">
              <div className="space-y-2">
                <Label className="text-slate-300">Trimestre</Label>
                <Select value={quarter} onValueChange={setQuarter}>
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-slate-900 border-slate-800">
                    <SelectItem value="Q1" className="text-white hover:bg-slate-800">
                      Q1 (Ene-Mar)
                    </SelectItem>
                    <SelectItem value="Q2" className="text-white hover:bg-slate-800">
                      Q2 (Abr-Jun)
                    </SelectItem>
                    <SelectItem value="Q3" className="text-white hover:bg-slate-800">
                      Q3 (Jul-Sep)
                    </SelectItem>
                    <SelectItem value="Q4" className="text-white hover:bg-slate-800">
                      Q4 (Oct-Dic)
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label className="text-slate-300">Año</Label>
                <Select value={year} onValueChange={setYear}>
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-slate-900 border-slate-800">
                    <SelectItem value="2025" className="text-white hover:bg-slate-800">
                      2025
                    </SelectItem>
                    <SelectItem value="2024" className="text-white hover:bg-slate-800">
                      2024
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>


              <div className="space-y-2">
                <Label className="text-slate-300">Tema</Label>
                <Select value={theme} onValueChange={setTheme}>
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent className="bg-slate-900 border-slate-800">
                    <SelectItem value="all" className="text-white hover:bg-slate-800">
                      Todos
                    </SelectItem>
                    {mockThemes.map((t) => (
                      <SelectItem key={t.id} value={t.name} className="text-white hover:bg-slate-800">
                        {t.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="flex gap-2 justify-end">
              <Button onClick={handleGenerateReport} className="bg-blue-600 hover:bg-blue-700 text-white">
                <BarChart3 className="mr-2 h-4 w-4" />
                Generar Reporte
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Statistics Summary */}
        <div className="grid md:grid-cols-4 gap-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-slate-400">Total de Casos</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold text-white">{stats.total}</p>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-slate-400">Querellas</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold text-white">{stats.querellas}</p>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-slate-400">En Trámite</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold text-white">{stats.enTramite}</p>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="pb-2">
              <CardTitle className="text-sm font-medium text-slate-400">Cerradas</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-3xl font-bold text-white">{stats.cerradas}</p>
            </CardContent>
          </Card>
        </div>

        {/* Detailed Statistics */}
        <div className="grid md:grid-cols-2 gap-6">

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Casos por Tema</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {byTheme.map((item) => (
                  <div key={item.name} className="flex items-center justify-between">
                    <span className="text-slate-300">{item.name}</span>
                    <span className="text-white font-semibold">{item.count}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Export Options */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white">Exportar Reporte</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex gap-4">
              <Button
                onClick={handleExportPDF}
                variant="outline"
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                <FileText className="mr-2 h-4 w-4" />
                Exportar a PDF
              </Button>
              <Button
                onClick={handleExportExcel}
                variant="outline"
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                <Download className="mr-2 h-4 w-4" />
                Exportar a Excel
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </MainLayout>
  )
}

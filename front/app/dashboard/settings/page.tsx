"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { useState } from "react"
import { mockThemes, mockComunas } from "@/lib/mock-data"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Plus, Edit } from "lucide-react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"

export default function SettingsPage() {
  const [showThemeDialog, setShowThemeDialog] = useState(false)
  const [showComunaDialog, setShowComunaDialog] = useState(false)

  const [themeName, setThemeName] = useState("")

  const [comunaName, setComunaName] = useState("")

  const handleAddTheme = () => {
    if (!themeName) {
      toast.error("Ingrese el nombre del tema")
      return
    }
    toast.success("Tema agregado correctamente")
    setShowThemeDialog(false)
    setThemeName("")
  }

  const handleAddComuna = () => {
    if (!comunaName || !comunaInspection) {
      toast.error("Complete todos los campos")
      return
    }
    toast.success("Comuna agregada correctamente")
    setShowComunaDialog(false)
    setComunaName("")
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-bold text-white">Configuración</h2>
          <p className="text-slate-400 mt-1">Administre inspecciones, temas y comunas</p>
        </div>

        <Tabs defaultValue="themes" className="space-y-4">
          <TabsList className="bg-slate-900 border border-slate-800">
            <TabsTrigger
              value="themes"
              className="text-white data-[state=active]:bg-blue-600 data-[state=active]:text-white"
            >
              Temas
            </TabsTrigger>
            <TabsTrigger
              value="comunas"
              className="text-white data-[state=active]:bg-blue-600 data-[state=active]:text-white"
            >
              Comunas
            </TabsTrigger>
          </TabsList>

          {/* Themes Tab */}
          <TabsContent value="themes">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-white">Temas</CardTitle>
                <Button
                  onClick={() => setShowThemeDialog(true)}
                  size="sm"
                  className="bg-blue-600 hover:bg-blue-700 text-white"
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Agregar
                </Button>
              </CardHeader>
              <CardContent>
                <div className="grid md:grid-cols-2 gap-3">
                  {mockThemes.map((theme) => (
                    <div
                      key={theme.id}
                      className="flex items-center justify-between p-4 bg-slate-800 rounded-lg border border-slate-700"
                    >
                      <p className="text-white font-medium">{theme.name}</p>
                      <div className="flex items-center gap-2">
                        <Badge
                          variant="outline"
                          className={
                            theme.status === "activo"
                              ? "bg-green-900/50 text-green-300 border-green-800"
                              : "bg-slate-700 text-slate-300 border-slate-600"
                          }
                        >
                          {theme.status}
                        </Badge>
                        <Button
                          size="icon"
                          variant="ghost"
                          className="text-slate-400 hover:text-white hover:bg-slate-700"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Comunas Tab */}
          <TabsContent value="comunas">
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="flex flex-row items-center justify-between">
                <CardTitle className="text-white">Comunas y Corregimientos</CardTitle>
                <Button
                  onClick={() => setShowComunaDialog(true)}
                  size="sm"
                  className="bg-blue-600 hover:bg-blue-700 text-white"
                >
                  <Plus className="mr-2 h-4 w-4" />
                  Agregar
                </Button>
              </CardHeader>
              <CardContent>
                <div className="grid md:grid-cols-2 gap-3">
                  {mockComunas.map((comuna) => (
                    <div
                      key={comuna.id}
                      className="flex items-center justify-between p-4 bg-slate-800 rounded-lg border border-slate-700"
                    >
                      <div>
                        <p className="text-white font-medium">{comuna.name}</p>
                      </div>
                      <Button
                        size="icon"
                        variant="ghost"
                        className="text-slate-400 hover:text-white hover:bg-slate-700"
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>

      {/* Add Theme Dialog */}
      <Dialog open={showThemeDialog} onOpenChange={setShowThemeDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Agregar Tema</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nombre del Tema *</Label>
              <Input
                value={themeName}
                onChange={(e) => setThemeName(e.target.value)}
                placeholder="Ej: Licencias"
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setShowThemeDialog(false)}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleAddTheme} className="bg-blue-600 hover:bg-blue-700 text-white">
                Agregar
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Add Comuna Dialog */}
      <Dialog open={showComunaDialog} onOpenChange={setShowComunaDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle>Agregar Comuna</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nombre *</Label>
              <Input
                value={comunaName}
                onChange={(e) => setComunaName(e.target.value)}
                placeholder="Ej: Comuna 7"
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>


            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setShowComunaDialog(false)}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleAddComuna} className="bg-blue-600 hover:bg-blue-700 text-white">
                Agregar
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </MainLayout>
  )
}

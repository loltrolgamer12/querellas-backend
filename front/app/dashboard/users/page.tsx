"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { useState } from "react"
import { mockUsers } from "@/lib/mock-data"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Badge } from "@/components/ui/badge"
import { Plus, Search, Edit, Lock } from "lucide-react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "sonner"

export default function UsersPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [showUserDialog, setShowUserDialog] = useState(false)
  const [editingUser, setEditingUser] = useState<any>(null)
  const [userName, setUserName] = useState("")
  const [userEmail, setUserEmail] = useState("")
  const [userRole, setUserRole] = useState("")
  const [userPhone, setUserPhone] = useState("")
  const [editUserName, setEditUserName] = useState("")
  const [editUserEmail, setEditUserEmail] = useState("")
  const [editUserRole, setEditUserRole] = useState("")
  const [editUserPhone, setEditUserPhone] = useState("")
  const [editUserStatus, setEditUserStatus] = useState("")

  const filteredUsers = mockUsers.filter(
    (user) =>
      (user.nombre || user.name || "").toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.email.toLowerCase().includes(searchTerm.toLowerCase()),
  )

  const handleAddUser = () => {
    if (!userName || !userEmail || !userRole) {
      toast.error("Complete todos los campos obligatorios")
      return
    }
    toast.success("Usuario agregado correctamente")
    setShowUserDialog(false)
    setUserName("")
    setUserEmail("")
    setUserRole("")
    setUserPhone("")
  }

  const handleEditUser = (user: any) => {
    setEditingUser(user)
    setEditUserName(user.nombre || user.name || "")
    setEditUserEmail(user.email)
    setEditUserRole(user.rol || user.role || "")
    setEditUserPhone(user.telefono || user.phone || "")
    setEditUserStatus(user.estado || user.status || "activo")
  }

  const handleSaveEdit = () => {
    if (!editUserName || !editUserEmail || !editUserRole) {
      toast.error("Complete todos los campos obligatorios")
      return
    }
    toast.success("Usuario actualizado correctamente")
    setEditingUser(null)
  }

  const handleBlockUser = (userId: string, currentStatus: string) => {
    const newStatus = currentStatus === "bloqueado" ? "activo" : "bloqueado"
    toast.success(`Usuario ${newStatus === "bloqueado" ? "bloqueado" : "desbloqueado"} correctamente`)
  }

  const getRoleBadge = (role: string) => {
    const variants: Record<string, { label: string; className: string }> = {
      inspector: { label: "Inspector", className: "bg-blue-900/50 text-blue-300 border-blue-800" },
      director: { label: "Director/a", className: "bg-purple-900/50 text-purple-300 border-purple-800" },
      auxiliar: { label: "Auxiliar", className: "bg-green-900/50 text-green-300 border-green-800" },
    }
    const variant = variants[role]
    return (
      <Badge variant="outline" className={variant.className}>
        {variant.label}
      </Badge>
    )
  }

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { label: string; className: string }> = {
      activo: { label: "Activo", className: "bg-green-900/50 text-green-300 border-green-800" },
      bloqueado: { label: "Bloqueado", className: "bg-red-900/50 text-red-300 border-red-800" },
      no_disponible: { label: "No Disponible", className: "bg-yellow-900/50 text-yellow-300 border-yellow-800" },
    }
    const variant = variants[status]
    return (
      <Badge variant="outline" className={variant.className}>
        {variant.label}
      </Badge>
    )
  }

  return (
    <MainLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-3xl font-bold text-white">Usuarios</h2>
            <p className="text-slate-400 mt-1">Administre usuarios y permisos del sistema</p>
          </div>
          <Button onClick={() => setShowUserDialog(true)} className="bg-blue-600 hover:bg-blue-700 text-white">
            <Plus className="mr-2 h-4 w-4" />
            Agregar Usuario
          </Button>
        </div>

        {/* Search */}
        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="pt-6">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-500" />
              <Input
                placeholder="Buscar por nombre o correo..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>
          </CardContent>
        </Card>

        {/* Users List */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white">Lista de Usuarios ({filteredUsers.length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {filteredUsers.map((user) => (
                <div
                  key={user.id}
                  className="flex items-center justify-between p-4 bg-slate-800 rounded-lg border border-slate-700"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <p className="text-white font-medium">{user.nombre || user.name || "Usuario"}</p>
                      {getRoleBadge(user.rol || user.role || "")}
                      {getStatusBadge(user.estado || user.status || "activo")}
                    </div>
                    <p className="text-sm text-slate-400">{user.email}</p>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      size="icon"
                      variant="ghost"
                      className="text-blue-400 hover:text-blue-300 hover:bg-slate-700"
                      onClick={() => handleEditUser(user)}
                    >
                      <Edit className="h-4 w-4" />
                    </Button>
                    <Button
                      size="icon"
                      variant="ghost"
                      className="text-amber-400 hover:text-amber-300 hover:bg-slate-700"
                      onClick={() => handleBlockUser(user.id, user.status)}
                    >
                      <Lock className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Add User Dialog */}
      <Dialog open={showUserDialog} onOpenChange={setShowUserDialog}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white max-w-md">
          <DialogHeader>
            <DialogTitle>Agregar Usuario</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nombre Completo *</Label>
              <Input
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                placeholder="Ej: Juan Pérez"
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Correo Electrónico *</Label>
              <Input
                type="email"
                value={userEmail}
                onChange={(e) => setUserEmail(e.target.value)}
                placeholder="usuario@neiva.gov.co"
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Teléfono</Label>
              <Input
                value={userPhone}
                onChange={(e) => setUserPhone(e.target.value)}
                placeholder="3001234567"
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Rol *</Label>
              <Select value={userRole} onValueChange={setUserRole}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue placeholder="Seleccionar rol" />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  <SelectItem value="inspector" className="text-white hover:bg-slate-800">
                    Inspector
                  </SelectItem>
                  <SelectItem value="director" className="text-white hover:bg-slate-800">
                    Director/a
                  </SelectItem>
                  <SelectItem value="auxiliar" className="text-white hover:bg-slate-800">
                    Auxiliar de Dirección
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>


            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setShowUserDialog(false)}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleAddUser} className="bg-blue-600 hover:bg-blue-700 text-white">
                Agregar Usuario
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit User Dialog */}
      <Dialog open={!!editingUser} onOpenChange={() => setEditingUser(null)}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white max-w-md">
          <DialogHeader>
            <DialogTitle>Editar Usuario</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label className="text-slate-300">Nombre Completo *</Label>
              <Input
                value={editUserName}
                onChange={(e) => setEditUserName(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Correo Electrónico *</Label>
              <Input
                type="email"
                value={editUserEmail}
                onChange={(e) => setEditUserEmail(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Teléfono</Label>
              <Input
                value={editUserPhone}
                onChange={(e) => setEditUserPhone(e.target.value)}
                className="bg-slate-800 border-slate-700 text-white placeholder:text-slate-500"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Estado *</Label>
              <Select value={editUserStatus} onValueChange={setEditUserStatus}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  <SelectItem value="activo" className="text-white hover:bg-slate-800">
                    Activo
                  </SelectItem>
                  <SelectItem value="bloqueado" className="text-white hover:bg-slate-800">
                    Bloqueado
                  </SelectItem>
                  <SelectItem value="no_disponible" className="text-white hover:bg-slate-800">
                    No Disponible
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-300">Rol *</Label>
              <Select value={editUserRole} onValueChange={setEditUserRole}>
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent className="bg-slate-900 border-slate-800">
                  <SelectItem value="inspector" className="text-white hover:bg-slate-800">
                    Inspector
                  </SelectItem>
                  <SelectItem value="director" className="text-white hover:bg-slate-800">
                    Director/a
                  </SelectItem>
                  <SelectItem value="auxiliar" className="text-white hover:bg-slate-800">
                    Auxiliar de Dirección
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>


            <div className="flex gap-2 justify-end">
              <Button
                variant="outline"
                onClick={() => setEditingUser(null)}
                className="bg-slate-800 border-slate-700 text-white hover:bg-slate-700"
              >
                Cancelar
              </Button>
              <Button onClick={handleSaveEdit} className="bg-blue-600 hover:bg-blue-700 text-white">
                Guardar Cambios
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </MainLayout>
  )
}

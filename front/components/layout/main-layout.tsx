"use client"

import type React from "react"

import { useAuth } from "@/lib/auth-context"
import { useRouter, usePathname } from "next/navigation"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { LayoutDashboard, FileText, Search, BarChart3, Settings, Users, LogOut, Bell, Menu, X } from "lucide-react"
import Link from "next/link"
import { Badge } from "@/components/ui/badge"
import { mockNotifications } from "@/lib/mock-data"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { ScrollArea } from "@/components/ui/scroll-area"
import Image from "next/image"

export function MainLayout({ children }: { children: React.ReactNode }) {
  const { user, logout } = useAuth()
  const router = useRouter()
  const pathname = usePathname()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [notifications, setNotifications] = useState(mockNotifications)

  const unreadCount = notifications.filter((n) => !n.read && n.userId === user?.id).length

  const handleLogout = () => {
    logout()
    router.push("/login")
  }

  const markAsRead = (id: string) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
  }

  // Función helper para mapear roles del backend a roles del menú
  const getUserRoleForMenu = (rol?: string): string => {
    if (!rol) return ""
    // Mapear roles del backend a roles del menú (legacy)
    if (rol === "INSPECTOR") return "inspector"
    if (rol === "DIRECTORA") return "director"
    if (rol === "AUXILIAR") return "auxiliar"
    return rol.toLowerCase()
  }

  const menuItems = [
    { icon: LayoutDashboard, label: "Dashboard", href: "/dashboard", roles: ["inspector", "director", "auxiliar"] },
    { icon: FileText, label: "Casos", href: "/dashboard/cases", roles: ["inspector", "director", "auxiliar"] },
    {
      icon: Search,
      label: "Búsqueda Avanzada",
      href: "/dashboard/search",
      roles: ["inspector", "director", "auxiliar"],
    },
    { icon: BarChart3, label: "Reportes", href: "/dashboard/reports", roles: ["director", "auxiliar"] },
    { icon: Users, label: "Usuarios", href: "/dashboard/users", roles: ["director"] },
    { icon: Settings, label: "Configuración", href: "/dashboard/settings", roles: ["director"] },
  ]

  const filteredMenuItems = menuItems.filter((item) => item.roles.includes(getUserRoleForMenu(user?.rol)))

  const userNotifications = notifications.filter((n) => n.userId === user?.id)

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="sticky top-0 z-50 police-header shadow-md">
        <div className="flex h-16 md:h-20 items-center px-4 md:px-6 gap-3 md:gap-4">
          <div className="hidden md:flex items-center justify-center" style={{ height: '70px', minWidth: '100px', paddingRight: '15px' }}>
            <img 
              src="/logo.png" 
              alt="Alcaldía de Neiva" 
              className="h-full w-auto object-contain"
            />
          </div>

          <Button
            variant="ghost"
            size="icon"
            className="lg:hidden text-white hover:text-yellow-400 hover:bg-green-800"
            onClick={() => setSidebarOpen(!sidebarOpen)}
          >
            {sidebarOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </Button>

          <div className="flex-1 min-w-0">
            <h1 className="text-base md:text-xl font-bold text-white truncate">Sistema de Gestión de Querellas</h1>
            <p className="text-xs md:text-sm text-yellow-400 font-medium hidden sm:block">
              Sistema de Gestión - Neiva
            </p>
          </div>

          {/* Notifications */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="relative text-white hover:text-yellow-400 hover:bg-green-800"
              >
                <Bell className="h-5 w-5" />
                {unreadCount > 0 && (
                  <Badge className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 bg-yellow-500 text-green-900 text-xs font-bold">
                    {unreadCount}
                  </Badge>
                )}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-80 md:w-96 bg-white border-green-200">
              <DropdownMenuLabel className="text-green-900">Notificaciones</DropdownMenuLabel>
              <DropdownMenuSeparator className="bg-green-200" />
              <ScrollArea className="h-[300px]">
                {userNotifications.length === 0 ? (
                  <div className="p-4 text-center text-sm text-gray-500">No hay notificaciones</div>
                ) : (
                  userNotifications.map((notification) => (
                    <DropdownMenuItem
                      key={notification.id}
                      className="flex flex-col items-start p-3 cursor-pointer hover:bg-green-50"
                      onClick={() => {
                        markAsRead(notification.id)
                        if (notification.caseId) {
                          router.push(`/dashboard/cases/${notification.caseId}`)
                        }
                      }}
                    >
                      <div className="flex items-start justify-between w-full">
                        <div className="flex-1 min-w-0">
                          <p
                            className={`text-sm font-medium ${notification.read ? "text-gray-600" : "text-green-900"}`}
                          >
                            {notification.title}
                          </p>
                          <p className="text-xs text-gray-600 mt-1">{notification.message}</p>
                        </div>
                        {!notification.read && (
                          <div className="h-2 w-2 rounded-full bg-yellow-500 ml-2 mt-1 flex-shrink-0" />
                        )}
                      </div>
                      <p className="text-xs text-gray-500 mt-1">
                        {new Date(notification.createdAt).toLocaleString("es-CO")}
                      </p>
                    </DropdownMenuItem>
                  ))
                )}
              </ScrollArea>
            </DropdownMenuContent>
          </DropdownMenu>

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="flex items-center gap-2 hover:bg-green-800 text-white px-2 md:px-3">
                <Avatar className="h-8 w-8 md:h-9 md:w-9 bg-yellow-500 border-2 border-white">
                  <AvatarFallback className="bg-yellow-500 text-green-900 font-bold text-sm">
                    {user?.nombre
                      ?.split(" ")
                      .map((n) => n[0])
                      .join("") || "U"}
                  </AvatarFallback>
                </Avatar>
                <div className="hidden md:block text-left">
                  <p className="text-sm font-semibold text-white">{user?.nombre || "Usuario"}</p>
                  <p className="text-xs text-yellow-400 capitalize">{getUserRoleForMenu(user?.rol) || "Usuario"}</p>
                </div>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-56 bg-white border-green-200">
              <DropdownMenuLabel className="text-green-900">Mi Cuenta</DropdownMenuLabel>
              <DropdownMenuSeparator className="bg-green-200" />
              <DropdownMenuItem
                onClick={handleLogout}
                className="text-red-600 hover:text-red-700 hover:bg-red-50 cursor-pointer"
              >
                <LogOut className="mr-2 h-4 w-4" />
                Cerrar Sesión
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>

      <div className="flex">
        <aside
          className={`
          fixed lg:sticky top-16 md:top-20 left-0 z-40 h-[calc(100vh-4rem)] md:h-[calc(100vh-5rem)]
          w-64 border-r border-green-200 bg-white shadow-lg
          transition-transform duration-200 ease-in-out
          ${sidebarOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"}
        `}
        >
          <nav className="space-y-1 p-4">
            {filteredMenuItems.map((item) => {
              const Icon = item.icon
              const isActive = item.href === "/dashboard" ? pathname === "/dashboard" : pathname.startsWith(item.href)

              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setSidebarOpen(false)}
                  className={`
                    flex items-center gap-3 rounded-lg px-3 py-3 text-sm font-medium
                    transition-all
                    ${
                      isActive
                        ? "bg-green-800 text-white shadow-md"
                        : "text-green-900 hover:bg-green-50 hover:text-green-800"
                    }
                  `}
                >
                  <Icon className="h-5 w-5 flex-shrink-0" />
                  <span className="truncate">{item.label}</span>
                </Link>
              )
            })}
          </nav>

          <div className="absolute bottom-4 left-4 right-4 p-3 bg-green-50 rounded-lg border border-green-200">
            <p className="text-xs text-green-800 font-medium">Policía Nacional de Colombia</p>
            <p className="text-xs text-green-600 mt-1">Dios y Patria</p>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-4 md:p-6 min-w-0 bg-gray-50">{children}</main>
      </div>

      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-30 bg-black/50 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}
    </div>
  )
}

"use client"

import { MainLayout } from "@/components/layout/main-layout"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { mockCases } from "@/lib/mock-data"
import { FileText, Clock, CheckCircle, AlertCircle } from "lucide-react"
import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis, Tooltip, CartesianGrid } from "recharts"
import Link from "next/link"
import { Badge } from "@/components/ui/badge"
import { useAuth } from "@/lib/auth-context"

export default function DashboardPage() {
  const { user } = useAuth()

  const userCases = user?.rol === "INSPECTOR" ? mockCases.filter((c) => c.assignedTo === String(user.id)) : mockCases

  const stats = {
    total: userCases.length,
    enTramite: userCases.filter((c) => c.status === "en_tramite").length,
    verificacion: userCases.filter((c) => c.status === "verificacion").length,
    cerradas: userCases.filter((c) => c.status === "cerrada").length,
  }

  const chartData = [
    { name: "Recibidas", value: userCases.filter((c) => c.status === "recibida").length },
    { name: "Verificación", value: stats.verificacion },
    { name: "En Trámite", value: stats.enTramite },
    { name: "Cerradas", value: stats.cerradas },
  ]

  const getStatusBadge = (status: string) => {
    const variants: Record<string, { label: string; className: string }> = {
      recibida: { label: "Recibida", className: "bg-blue-900/50 text-blue-300 border-blue-800" },
      verificacion: { label: "Verificación", className: "bg-yellow-900/50 text-yellow-300 border-yellow-800" },
      en_tramite: { label: "En Trámite", className: "bg-purple-900/50 text-purple-300 border-purple-800" },
      cerrada: { label: "Cerrada", className: "bg-green-900/50 text-green-300 border-green-800" },
      radicado: { label: "Radicado", className: "bg-blue-900/50 text-blue-300 border-blue-800" },
    }
    const variant = variants[status] || { label: status, className: "bg-slate-800 text-slate-300" }
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
          <h2 className="text-2xl md:text-3xl font-bold text-white">Dashboard</h2>
          <p className="text-slate-400 mt-1 text-sm md:text-base">Resumen general del sistema</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-3 md:gap-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs md:text-sm font-medium text-slate-400">Total de Casos</CardTitle>
              <FileText className="h-4 w-4 text-slate-500" />
            </CardHeader>
            <CardContent>
              <div className="text-xl md:text-2xl font-bold text-white">{stats.total}</div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs md:text-sm font-medium text-slate-400">En Verificación</CardTitle>
              <Clock className="h-4 w-4 text-yellow-500" />
            </CardHeader>
            <CardContent>
              <div className="text-xl md:text-2xl font-bold text-white">{stats.verificacion}</div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs md:text-sm font-medium text-slate-400">En Trámite</CardTitle>
              <AlertCircle className="h-4 w-4 text-purple-500" />
            </CardHeader>
            <CardContent>
              <div className="text-xl md:text-2xl font-bold text-white">{stats.enTramite}</div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
              <CardTitle className="text-xs md:text-sm font-medium text-slate-400">Cerradas</CardTitle>
              <CheckCircle className="h-4 w-4 text-green-500" />
            </CardHeader>
            <CardContent>
              <div className="text-xl md:text-2xl font-bold text-white">{stats.cerradas}</div>
            </CardContent>
          </Card>
        </div>

        {/* Chart */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white text-base md:text-lg">Casos por Estado</CardTitle>
          </CardHeader>
          <CardContent className="p-4 md:p-6">
            <div className="h-[250px] md:h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                  <XAxis
                    dataKey="name"
                    stroke="#94a3b8"
                    tick={{ fill: "#94a3b8", fontSize: 12 }}
                    angle={-45}
                    textAnchor="end"
                    height={60}
                  />
                  <YAxis stroke="#94a3b8" tick={{ fill: "#94a3b8", fontSize: 12 }} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: "#1e293b",
                      border: "1px solid #334155",
                      borderRadius: "8px",
                      color: "#fff",
                    }}
                  />
                  <Bar dataKey="value" fill="#3b82f6" radius={[8, 8, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </CardContent>
        </Card>

        {/* Recent Cases */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white text-base md:text-lg">Casos Recientes</CardTitle>
          </CardHeader>
          <CardContent className="p-4 md:p-6">
            <div className="space-y-2 md:space-y-3">
              {userCases.slice(0, 5).map((caso) => (
                <Link
                  key={caso.id}
                  href={`/dashboard/cases/${caso.id}`}
                  className="flex flex-col md:flex-row md:items-center justify-between p-3 rounded-lg bg-slate-800/50 hover:bg-slate-800 transition-colors border border-slate-700 hover:border-slate-600 gap-2"
                >
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-white truncate">{caso.internalId || caso.radicadoInterno || `Caso ${caso.id}`}</p>
                    <p className="text-xs text-slate-400 truncate mt-1">{caso.description}</p>
                  </div>
                  <div className="flex items-center gap-2">{getStatusBadge(caso.status)}</div>
                </Link>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </MainLayout>
  )
}

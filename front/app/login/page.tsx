"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Spinner } from "@/components/ui/spinner"
import Image from "next/image"

export default function LoginPage() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const { login } = useAuth()
  const router = useRouter()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    setIsLoading(true)

    try {
      const success = await login(email, password)

      if (success) {
        router.push("/dashboard")
      } else {
        setError("Credenciales inválidas. Verifique su usuario y contraseña.")
        setIsLoading(false)
      }
    } catch (error: any) {
      console.error("Login error in component:", error)
      
      // Si el auth-context retornó false, significa que no pudo hacer login
      // Pero si el error es de conexión, el auth-context debería haber manejado el modo mock
      // Si llegamos aquí, es porque el auth-context lanzó el error en lugar de retornar true
      const errorMessage = error?.message || error?.errors?.message || "Error al iniciar sesión. Verifique sus credenciales."
      
      // Si es un error de conexión, no mostrar error al usuario (el modo mock debería funcionar)
      if (errorMessage.includes('No se pudo conectar') || error?.status === 0) {
        console.warn("Error de conexión detectado, pero el modo mock debería haberse activado")
        // No establecer error, dejar que el usuario intente de nuevo o esperar a que el modo mock funcione
      } else {
        setError(errorMessage)
      }
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 via-white to-green-50 p-4">
      <div className="w-full max-w-lg">
        <Card className="border-2 border-green-200 bg-white shadow-xl">
          <CardHeader className="space-y-6 text-center pb-8 pt-8">
            {/* Logo */}
            <div className="flex justify-center items-center">
              <div className="bg-white rounded-lg p-4 shadow-sm">
                <img
                  src="/logo.png"
                  alt="Alcaldía de Neiva"
                  className="h-32 w-auto object-contain"
                />
              </div>
            </div>
            
            {/* Título y descripción */}
            <div className="space-y-2">
              <CardTitle className="text-3xl font-bold text-gray-800">
                Sistema de Gestión de Querellas
              </CardTitle>
              <CardDescription className="text-base text-gray-600 font-medium">
                Alcaldía de Neiva
              </CardDescription>
            </div>
          </CardHeader>
          
          <CardContent className="px-8 pb-8">
            <form onSubmit={handleSubmit} className="space-y-5">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-sm font-semibold text-gray-700">
                  Correo Electrónico o Usuario
                </Label>
                <Input
                  id="email"
                  type="text"
                  placeholder="usuario@neiva.gov.co o usuario"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  className="h-11 bg-gray-50 border-gray-300 focus:border-green-600 focus:ring-green-600 text-gray-900 placeholder:text-gray-400"
                  disabled={isLoading}
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="password" className="text-sm font-semibold text-gray-700">
                  Contraseña
                </Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="Ingrese su contraseña"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  className="h-11 bg-gray-50 border-gray-300 focus:border-green-600 focus:ring-green-600 text-gray-900"
                  disabled={isLoading}
                />
              </div>

              {error && (
                <Alert variant="destructive" className="bg-red-50 border-red-200 text-red-800">
                  <AlertDescription className="text-sm">{error}</AlertDescription>
                </Alert>
              )}

              <Button
                type="submit"
                className="w-full h-11 bg-green-700 hover:bg-green-800 text-white font-semibold text-base shadow-md transition-colors"
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <Spinner className="mr-2 h-4 w-4" />
                    Iniciando sesión...
                  </>
                ) : (
                  "Iniciar Sesión"
                )}
              </Button>
            </form>

            {/* Credenciales de prueba */}
            <div className="mt-8 p-4 bg-gradient-to-r from-green-50 to-emerald-50 rounded-lg border border-green-200">
              <p className="text-sm text-green-900 mb-3 font-semibold">🔑 Credenciales de Prueba</p>
              <div className="space-y-2 text-sm text-gray-700">
                <div className="flex flex-wrap gap-2">
                  <span className="font-semibold text-gray-800">Inspector:</span>
                  <span className="text-gray-600">inspector o inspector@neiva.gov.co</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  <span className="font-semibold text-gray-800">Directora:</span>
                  <span className="text-gray-600">directora o directora@neiva.gov.co</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  <span className="font-semibold text-gray-800">Auxiliar:</span>
                  <span className="text-gray-600">auxiliar o auxiliar@neiva.gov.co</span>
                </div>
                <div className="pt-2 border-t border-green-200">
                  <span className="font-semibold text-gray-800">Contraseña para todos:</span>
                  <span className="ml-2 text-gray-600 font-mono">demo123</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

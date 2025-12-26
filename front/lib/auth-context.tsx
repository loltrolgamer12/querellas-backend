"use client"

import { createContext, useContext, useState, useEffect, type ReactNode } from "react"
import type { User } from "./types"
import { authApi, usuariosApi, apiClient } from "./api"

interface AuthContextType {
  user: User | null
  login: (email: string, password: string) => Promise<boolean>
  logout: () => void
  isLoading: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    // Check if there's a token and try to get user info
    const token = apiClient.getToken()
    if (token) {
      loadUserFromToken()
    } else {
      setIsLoading(false)
    }
  }, [])

  const loadUserFromToken = async () => {
    try {
      // Try to get current user - you might need to add an endpoint for this
      // For now, we'll just check if token exists
      const token = apiClient.getToken()
      if (token && token !== 'mock-token-for-development') {
        // Token exists, but we don't have user info yet
        // You might want to add a /api/auth/me endpoint to get current user
        // For now, try to load from localStorage
        const storedUser = localStorage.getItem("user")
        if (storedUser) {
          try {
            const user = JSON.parse(storedUser)
            setUser(user)
          } catch (e) {
            // Invalid JSON, ignore
          }
        }
      } else if (token === 'mock-token-for-development') {
        // Load mock user from localStorage
        const storedUser = localStorage.getItem("user")
        if (storedUser) {
          try {
            const user = JSON.parse(storedUser)
            setUser(user)
          } catch (e) {
            // Invalid JSON, ignore
          }
        }
      }
      setIsLoading(false)
    } catch (error) {
      console.error("Error loading user:", error)
      apiClient.setToken(null)
      setIsLoading(false)
    }
  }

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      // El backend usa nombres de usuario simples, no emails
      // Intentamos primero con el email completo, luego con la parte antes del @
      let username = email
      if (email.includes('@')) {
        username = email.split('@')[0] // Extraer la parte antes del @
      }

      const response = await authApi.login({
        username: username,
        password: password,
      })

      if (response.token) {
        // Try to get user info from the users list
        try {
          const usuarios = await usuariosApi.list({ size: 100 })
          // Buscar por email completo o por nombre de usuario
          const foundUser = usuarios.items.find((u) => 
            u.email === email || u.email?.toLowerCase().startsWith(username.toLowerCase() + '@')
          )
          if (foundUser) {
            setUser(foundUser)
            localStorage.setItem("user", JSON.stringify(foundUser))
            return true
          } else {
            // Si no encontramos el usuario en la lista, creamos un objeto temporal
            // Esto puede pasar si el usuario existe en el backend pero no en la tabla de usuarios
            const tempUser: User = {
              id: 0,
              nombre: username,
              email: email,
              rol: 'INSPECTOR' as any, // Valor por defecto
              estado: 'ACTIVO' as any,
              creadoEn: new Date().toISOString(),
            }
            setUser(tempUser)
            localStorage.setItem("user", JSON.stringify(tempUser))
            return true
          }
        } catch (error) {
          console.error("Error loading user info:", error)
          // Aún así, si tenemos token, el login fue exitoso
          const tempUser: User = {
            id: 0,
            nombre: username,
            email: email,
            rol: 'INSPECTOR' as any,
            estado: 'ACTIVO' as any,
            creadoEn: new Date().toISOString(),
          }
          setUser(tempUser)
          localStorage.setItem("user", JSON.stringify(tempUser))
          return true
        }
      }
      return false
    } catch (error: any) {
      // Mejorar el logging del error de forma más simple
      const errorMessage = error?.message || (typeof error === 'string' ? error : 'Error desconocido')
      const errorStatus = error?.status
      
      // Logging simple y seguro
      console.error("Login error - Message:", errorMessage, "Status:", errorStatus)
      
      // SIEMPRE activar modo mock si el status es 0 (error de conexión)
      // Esto es para desarrollo cuando el backend no está disponible
      if (errorStatus === 0 || !errorStatus) {
        console.warn("Backend no disponible (status 0), usando modo desarrollo con datos mock")
        
        // Simular login exitoso con datos mock
        // Extraer username del email (parte antes del @)
        const username = email.includes('@') ? email.split('@')[0] : email
        const emailLower = email.toLowerCase()
        const usernameLower = username.toLowerCase()
        
        // Detectar rol basado en el email o username
        let rol: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR' = 'INSPECTOR'
        
        if (emailLower.includes('director') || usernameLower.includes('director')) {
          rol = 'DIRECTORA'
        } else if (emailLower.includes('auxiliar') || usernameLower.includes('auxiliar')) {
          rol = 'AUXILIAR'
        } else if (emailLower.includes('inspector') || usernameLower.includes('inspector')) {
          rol = 'INSPECTOR'
        }
        
        const mockUser: User = {
          id: 1,
          nombre: username.charAt(0).toUpperCase() + username.slice(1),
          email: email,
          rol: rol,
          estado: 'ACTIVO' as any,
          creadoEn: new Date().toISOString(),
        }
        
        setUser(mockUser)
        localStorage.setItem("user", JSON.stringify(mockUser))
        // Guardar un token mock para que las otras llamadas funcionen
        apiClient.setToken('mock-token-for-development')
        
        return true
      }
      
      // Detectar si el backend no está disponible por el mensaje
      const errorMessageStr = String(errorMessage || '')
      const hasConnectionMessage = 
        errorMessageStr.includes('No se pudo conectar') ||
        errorMessageStr.includes('Failed to fetch') ||
        errorMessageStr.includes('NetworkError') ||
        errorMessageStr.includes('fetch') ||
        errorMessageStr.includes('Network request failed')
      
      if (hasConnectionMessage || (error instanceof TypeError)) {
        // Modo desarrollo: permitir login sin backend
        console.warn("Backend no disponible, usando modo desarrollo con datos mock")
        
        // Simular login exitoso con datos mock
        // Extraer username del email (parte antes del @)
        const username = email.includes('@') ? email.split('@')[0] : email
        const emailLower = email.toLowerCase()
        const usernameLower = username.toLowerCase()
        
        // Detectar rol basado en el email o username
        let rol: 'INSPECTOR' | 'DIRECTORA' | 'AUXILIAR' = 'INSPECTOR'
        
        if (emailLower.includes('director') || usernameLower.includes('director')) {
          rol = 'DIRECTORA'
        } else if (emailLower.includes('auxiliar') || usernameLower.includes('auxiliar')) {
          rol = 'AUXILIAR'
        } else if (emailLower.includes('inspector') || usernameLower.includes('inspector')) {
          rol = 'INSPECTOR'
        }
        
        const mockUser: User = {
          id: 1,
          nombre: username.charAt(0).toUpperCase() + username.slice(1),
          email: email,
          rol: rol,
          estado: 'ACTIVO' as any,
          creadoEn: new Date().toISOString(),
        }
        
        setUser(mockUser)
        localStorage.setItem("user", JSON.stringify(mockUser))
        // Guardar un token mock para que las otras llamadas funcionen
        apiClient.setToken('mock-token-for-development')
        
        return true
      }
      
      // El error ya está manejado en el componente de login
      throw error // Re-lanzar para que el componente pueda mostrar el mensaje
    }
  }

  const logout = () => {
    authApi.logout()
    setUser(null)
    localStorage.removeItem("user")
  }

  return <AuthContext.Provider value={{ user, login, logout, isLoading }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

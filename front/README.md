# Frontend - Sistema Completo de Gestión de Querellas

Interfaz web moderna desarrollada con **Next.js 14**, **React 18** y **TypeScript** para la gestión de querellas y despachos comisorios.

## 🚀 Tecnologías

- **Next.js 14** (App Router)
- **React 18**
- **TypeScript**
- **Tailwind CSS**
- **shadcn/ui** (Componentes)
- **React Hook Form** + **Zod** (Formularios y validación)
- **date-fns** (Manejo de fechas)
- **Sonner** (Notificaciones)

## 📋 Requisitos Previos

- Node.js 18 o superior
- npm o pnpm
- Backend corriendo en http://localhost:8081

## 🛠️ Instalación

```bash
# Instalar dependencias
npm install

# o usando pnpm
pnpm install
```

## ⚙️ Configuración

Crear archivo `.env.local` en la raíz del proyecto:

```env
# URL del backend API
NEXT_PUBLIC_API_URL=http://localhost:8081
```

## 🚀 Ejecución

### Modo Desarrollo

```bash
npm run dev
# o
pnpm dev
```

La aplicación estará disponible en: **http://localhost:3000**

### Modo Producción

```bash
# Construir la aplicación
npm run build

# Ejecutar en producción
npm start
```

## 📁 Estructura del Proyecto

```
front/
├── app/                      # Pages (App Router de Next.js)
│   ├── dashboard/           # Páginas del dashboard
│   │   ├── cases/          # Gestión de querellas
│   │   ├── reports/        # Reportes
│   │   ├── search/         # Búsqueda
│   │   ├── settings/       # Configuración
│   │   └── users/          # Gestión de usuarios
│   ├── login/              # Página de login
│   ├── layout.tsx          # Layout principal
│   └── page.tsx            # Página de inicio
│
├── components/              # Componentes React
│   ├── layout/             # Componentes de layout
│   └── ui/                 # Componentes de UI (shadcn)
│
├── lib/                     # Utilidades y configuración
│   ├── api/                # Cliente API REST
│   │   ├── auth.ts        # Autenticación
│   │   ├── querellas.ts   # Querellas
│   │   ├── usuarios.ts    # Usuarios
│   │   └── ...
│   ├── auth-context.tsx    # Contexto de autenticación
│   ├── types.ts            # Tipos TypeScript
│   └── utils.ts            # Utilidades
│
├── hooks/                   # Custom hooks
├── public/                  # Archivos estáticos
└── package.json
```

## 🔐 Autenticación

El frontend utiliza JWT para autenticación. Los tokens se almacenan en `localStorage` y se envían automáticamente en cada petición al backend.

### Flujo de autenticación:

1. Usuario ingresa credenciales en `/login`
2. Se obtiene token JWT del backend
3. Token se almacena en localStorage
4. Token se incluye en header `Authorization: Bearer <token>`
5. Al cerrar sesión, se elimina el token

## 🎨 Componentes UI

El proyecto utiliza **shadcn/ui** para los componentes de interfaz. Los componentes están en `components/ui/`:

- **Button** - Botones con variantes
- **Input** - Campos de entrada
- **Select** - Selectores desplegables
- **Table** - Tablas con paginación
- **Dialog** - Modales y diálogos
- **Toast** - Notificaciones
- **Form** - Formularios con validación
- Y muchos más...

## 📊 Características Principales

### Dashboard
- ✅ Métricas en tiempo real
- ✅ Gráficos y estadísticas
- ✅ Resumen de querellas por estado

### Gestión de Querellas
- ✅ Listado con filtros avanzados
- ✅ Creación de nuevas querellas
- ✅ Edición y actualización
- ✅ Cambio de estado
- ✅ Asignación de inspectores
- ✅ Detección de duplicados
- ✅ Historial de cambios

### Gestión de Despachos
- ✅ CRUD completo
- ✅ Asignación de inspectores
- ✅ Control de estados

### Adjuntos
- ✅ Upload de archivos
- ✅ Download de archivos
- ✅ Gestión por querella

### Comunicaciones
- ✅ Registro de comunicaciones
- ✅ Tipos: Llamada, Email, Visita, Oficio
- ✅ Estados: Borrador, Enviada, Recibida

### Reportes
- ✅ Reportes trimestrales
- ✅ Filtros por fecha
- ✅ Exportación a Excel
- ✅ Estadísticas detalladas

### Usuarios
- ✅ Gestión de usuarios
- ✅ Roles: DIRECTOR, AUXILIAR, INSPECTOR
- ✅ Control de permisos

## 🔧 API Client

El cliente API está en `lib/api/` y proporciona funciones para interactuar con el backend:

```typescript
import { api } from '@/lib/api'

// Ejemplo: Obtener querellas
const querellas = await api.querellas.getAll({ page: 0, size: 10 })

// Ejemplo: Crear querella
const nueva = await api.querellas.create(data)

// Ejemplo: Cambiar estado
await api.querellas.cambiarEstado(id, { nuevoEstadoId, motivo })
```

## 🌐 Variables de Entorno

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `NEXT_PUBLIC_API_URL` | URL del backend | `http://localhost:8081` |

## 🚢 Despliegue

### Vercel (Recomendado)

```bash
# Instalar Vercel CLI
npm i -g vercel

# Desplegar
vercel
```

### Docker

```bash
# Construir imagen
docker build -t querellas-frontend .

# Ejecutar contenedor
docker run -p 3000:3000 -e NEXT_PUBLIC_API_URL=https://api.tudominio.com querellas-frontend
```

### Servidor VPS

```bash
# Construir para producción
npm run build

# Iniciar con PM2
pm2 start npm --name "querellas-front" -- start
```

## 🧪 Scripts Disponibles

```bash
npm run dev          # Modo desarrollo
npm run build        # Construir para producción
npm start            # Ejecutar en producción
npm run lint         # Linter
```

## 📝 Notas

- Asegúrate de que el backend esté corriendo antes de iniciar el frontend
- La configuración de CORS en el backend debe incluir `http://localhost:3000`
- Para producción, actualiza `NEXT_PUBLIC_API_URL` con la URL real del backend

## 🤝 Contribuir

1. Fork el repositorio
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**Versión**: 1.0.0
**Última actualización**: Diciembre 2025

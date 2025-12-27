# Instalación de Fly CLI en Windows

## Método 1: PowerShell (Recomendado)

Abre **PowerShell como Administrador** y ejecuta:

```powershell
iwr https://fly.io/install.ps1 -useb | iex
```

Espera a que termine la instalación y luego **cierra y vuelve a abrir** PowerShell.

## Método 2: Scoop

Si tienes Scoop instalado:

```powershell
scoop install flyctl
```

## Método 3: Descarga Manual

1. Ve a: https://github.com/superfly/flyctl/releases/latest
2. Descarga: `flyctl_x.x.x_Windows_x86_64.zip`
3. Extrae el archivo `flyctl.exe`
4. Mueve `flyctl.exe` a `C:\Windows\System32\` o agrega su ubicación al PATH

## Verificar Instalación

Abre una nueva terminal (PowerShell o CMD) y ejecuta:

```powershell
flyctl version
```

Deberías ver algo como:
```
flyctl v0.x.xxx windows/amd64 ...
```

## Siguiente Paso

Una vez instalado, ejecuta desde la raíz del proyecto:

```powershell
# En PowerShell
.\deploy-flyio.ps1
```

O sigue la guía manual en [DESPLIEGUE-FLYIO.md](DESPLIEGUE-FLYIO.md)

# Frontend

Este proyecto está construido con React, Vite y TypeScript.

## Estructura del Proyecto

- `src`: Contiene el código fuente principal de la aplicación.
  - `components`: Contiene componentes de React.
  - `assets`: Contiene assets estáticos como imágenes y fuentes.
  - `App.tsx`: El componente principal de la aplicación.
  - `main.tsx`: El punto de entrada de la aplicación.
- `public`: Contiene assets estáticos que se copian directamente en la compilación final.
- `index.html`: El archivo HTML principal.
- `package.json`: El archivo de configuración del proyecto para npm.
- `vite.config.ts`: El archivo de configuración para Vite.

## Cómo Ejecutar

1. Asegúrate de tener Node.js y npm (o yarn) instalados.
2. Navega a la carpeta `frontend` en tu terminal.
3. Instala las dependencias ejecutando `npm install` (o `yarn install`).

### Modo de Desarrollo

- Ejecuta el comando `npm run dev` (o `yarn dev`).
- El servidor de desarrollo del frontend se iniciará en el puerto predeterminado (generalmente 3000 o 5173).

### Compilación para Producción

- Ejecuta el comando `npm run build` (o `yarn build`).
- Esto creará una carpeta `dist` con los archivos listos para producción.

### Vista Previa de la Compilación de Producción

- Ejecuta el comando `npm run preview` (o `yarn preview`).
- Esto servirá la compilación de producción localmente para pruebas.

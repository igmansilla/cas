#!/bin/bash

# Script para iniciar el backend y el frontend del proyecto CAS

# Función para verificar si un comando existe
command_exists () {
    type "$1" &> /dev/null ;
}

echo "Iniciando el proyecto CAS..."

# Verificar dependencias necesarias
if ! command_exists gradle && ! command_exists ./gradlew ; then
    echo "Error: Gradle (o el wrapper ./gradlew) no encontrado. Asegúrate de que esté instalado y en el PATH o que el wrapper exista en la carpeta backend."
    exit 1
fi

if ! command_exists npm ; then
    echo "Error: npm no encontrado. Asegúrate de que Node.js y npm estén instalados."
    exit 1
fi

# Navegar al directorio del backend
echo "Cambiando al directorio backend..."
cd backend || { echo "Error: No se pudo encontrar el directorio backend."; exit 1; }

# Iniciar el backend (Spring Boot) en segundo plano
echo "Iniciando el backend (Spring Boot)..."
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew # Asegurar que el wrapper sea ejecutable
    ./gradlew bootRun &
else
    gradle bootRun &
fi
BACKEND_PID=$!
echo "Backend iniciado con PID: $BACKEND_PID. Puede tardar unos momentos en estar completamente disponible."
# Esperar un poco para que el backend pueda iniciarse antes que el frontend intente conectarse
# sleep 10 # Descomentar si el frontend se queja de que el backend no está listo inmediatamente

# Volver al directorio raíz del proyecto
echo "Volviendo al directorio raíz..."
cd ..

# Navegar al directorio del frontend
echo "Cambiando al directorio frontend..."
cd frontend || { echo "Error: No se pudo encontrar el directorio frontend."; exit 1; }

# Instalar dependencias del frontend (opcional, descomentar si se quiere asegurar)
# echo "Instalando dependencias del frontend (npm install)..."
# npm install

# Iniciar el frontend (Vite)
echo "Iniciando el frontend (Vite)..."
npm run dev

# Cuando npm run dev (frontend) se detenga (Ctrl+C), parar el backend
echo "Frontend detenido. Deteniendo el backend (PID: $BACKEND_PID)..."
kill $BACKEND_PID
echo "Backend detenido."

echo "Proyecto CAS detenido."

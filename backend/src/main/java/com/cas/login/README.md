# Módulo de Inicio de Sesión (Login)

Este módulo gestiona la autenticación y autorización de usuarios para la aplicación. Utiliza Spring Security para administrar roles y permisos, asegurando que los usuarios solo puedan acceder a los recursos apropiados según su rol.

Las funcionalidades clave incluyen:
- Inicio de sesión de usuario con nombre de usuario y contraseña.
- Control de acceso basado en roles para diferentes partes de la API.
- Inicialización de cuentas de usuario predeterminadas para roles administrativos y operativos estándar.

Para información más detallada sobre roles específicos, permisos y configuración inicial de usuarios, consulta las secciones siguientes.

## Roles

El sistema define los siguientes roles para gestionar el acceso de los usuarios:

*   **`ROLE_ADMIN`**:
    *   **Descripción**: Usuarios con este rol tienen privilegios administrativos completos sobre el sistema.
    *   **Capacidades**: Pueden gestionar todas las cuentas de usuario (Dirigentes, otros Admins), configurar ajustes del sistema y acceder a todos los datos, incluyendo información de acampantes y dirigentes.
    *   **Casos de uso típicos**: Configuración del sistema, gestión de usuarios, resolución de problemas, acceso a vistas de datos sensibles o completas.

*   **`ROLE_DIRIGENTE`**:
    *   **Descripción**: Este rol está destinado a líderes u organizadores de campamento (Dirigentes).
    *   **Capacidades**: Pueden gestionar información relacionada con los `acampantes`, ver su propia información de perfil. No pueden gestionar otras cuentas de Dirigente ni la configuración global del sistema a menos que también tengan el rol de administrador.
    *   **Casos de uso típicos**: Registrar y actualizar datos de acampantes, organizar grupos de acampantes, ver la logística del campamento relevante para su rol.

*   **`ROLE_ACAMPANTE`**:
    *   **Estado**: Actualmente, `ROLE_ACAMPANTE` está definido en el sistema pero no se utiliza para inicios de sesión activos. Los acampantes son gestionados como datos por usuarios con `ROLE_DIRIGENTE` o `ROLE_ADMIN` en lugar de tener cuentas propias.

## Permisos y Protección de Endpoints

La autenticación se maneja principalmente mediante inicio de sesión basado en formularios. Una vez autenticado, el acceso de los usuarios a los endpoints de la API está restringido según los roles asignados. La clase `SecurityConfig.java` define estas protecciones.

Principales grupos de endpoints protegidos de la API:

*   **`/api/acampantes/**`**:
    *   Accesible por: `ROLE_DIRIGENTE`, `ROLE_ADMIN`
    *   Propósito: Permite la gestión de la información de acampantes (crear, leer, actualizar, eliminar).

*   **`/api/dirigentes/**`**:
    *   Accesible por: `ROLE_ADMIN`
    *   Propósito: Permite la gestión de cuentas de usuario Dirigente y sus detalles.

*   **`/api/admin/**`**:
    *   Accesible por: `ROLE_ADMIN`
    *   Propósito: Proporciona acceso a funciones administrativas y configuraciones globales del sistema.

*   **`/api/user/me`**:
    *   Accesible por: Cualquier usuario autenticado.
    *   Propósito: Permite a los usuarios obtener su propia información de perfil.

Otras rutas como `/login`, recursos estáticos (`/css/**`, `/js/**`) y páginas de error están configuradas para ser accesibles públicamente.

## Inicialización de Usuarios

Al iniciar la aplicación, la clase `DataInitializer.java` se encarga de asegurar que exista un conjunto de usuarios y roles predeterminados en el sistema. Esto es especialmente útil para desarrollo y despliegue inicial.

Los usuarios predeterminados creados son:

*   **Usuario**: `admin`
    *   **Contraseña**: `adminpass`
    *   **Roles**: `ROLE_ADMIN`

*   **Usuario**: `dirigente1`
    *   **Contraseña**: `dirigentepass`
    *   **Roles**: `ROLE_DIRIGENTE`

*   **Usuario**: `superdirigente`
    *   **Contraseña**: `superpass`
    *   **Roles**: `ROLE_DIRIGENTE`, `ROLE_ADMIN`

Estos usuarios pueden utilizarse para iniciar sesión y probar la funcionalidad de la aplicación según los roles asignados. Para entornos de producción, se recomienda encarecidamente cambiar estas contraseñas por defecto.

---

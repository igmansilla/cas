package com.cas.login.config.security;

/**
 * Constantes para los roles de seguridad del sistema.
 * Esta clase centraliza la definición de roles para facilitar el mantenimiento
 * y evitar errores de tipeo en cadenas hardcodeadas.
 */
public final class SecurityRoles {
    
    private SecurityRoles() {
        // Prevenir instanciación de esta clase de utilidades
    }
    
    // Roles base (sin prefijo ROLE_)
    public static final String ADMIN = "ADMIN";
    public static final String DIRIGENTE = "DIRIGENTE";
    public static final String USER = "USER";
    
    // Roles con prefijo ROLE_ para Spring Security
    public static final String ROLE_ADMIN = "ROLE_" + ADMIN;
    public static final String ROLE_DIRIGENTE = "ROLE_" + DIRIGENTE;
    public static final String ROLE_USER = "ROLE_" + USER;
    
    /**
     * Verifica si un rol es válido
     * @param role el rol a verificar
     * @return true si es un rol válido, false en caso contrario
     */
    public static boolean isValidRole(String role) {
        return ADMIN.equals(role) || DIRIGENTE.equals(role) || USER.equals(role);
    }
    
    /**
     * Obtiene todos los roles disponibles
     * @return array con todos los roles base
     */
    public static String[] getAllRoles() {
        return new String[]{ADMIN, DIRIGENTE, USER};
    }
}

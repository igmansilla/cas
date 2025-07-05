package com.cas.login.config.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;

/**
 * Configuración centralizada de endpoints y permisos de seguridad.
 * Esta clase separa la lógica de autorización por endpoints del resto de la configuración,
 * facilitando el mantenimiento y la comprensión del sistema de permisos.
 */
public class SecurityEndpoints {
    
    private SecurityEndpoints() {
        // Prevenir instanciación de esta clase de utilidades
    }
    
    /**
     * Configura los endpoints de la API con sus respectivos permisos.
     * Aplica a todas las rutas que comienzan con /api/
     * 
     * @param auth el configurador de autorización de Spring Security
     */
    public static void configureApiEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {        auth
            // Endpoints públicos - accesibles sin autenticación
            .requestMatchers("/api/status", "/api/health").permitAll()
            
            // Endpoints de acampantes - requieren rol DIRIGENTE o ADMIN
            .requestMatchers("/api/acampantes/**").hasAnyRole(SecurityRoles.DIRIGENTE, SecurityRoles.ADMIN)
            
            // Endpoints de asistencias - requieren rol DIRIGENTE o ADMIN
            .requestMatchers("/api/asistencias/**").hasAnyRole(SecurityRoles.DIRIGENTE, SecurityRoles.ADMIN)
            
            // Endpoints de dirigentes - solo para ADMIN
            .requestMatchers("/api/dirigentes/**").hasRole(SecurityRoles.ADMIN)
            
            // Endpoints de packing-list - requieren rol DIRIGENTE o ADMIN
            .requestMatchers("/api/packing-list/**").hasAnyRole(SecurityRoles.DIRIGENTE, SecurityRoles.ADMIN)
            
            // Endpoints administrativos - solo para ADMIN
            .requestMatchers("/api/admin/**").hasRole(SecurityRoles.ADMIN)
            
            // Endpoints de usuario autenticado
            .requestMatchers("/api/user/me").authenticated()
            .requestMatchers("/api/logout").authenticated()
            
            // Cualquier otra petición a la API requiere autenticación
            .anyRequest().authenticated();
    }
    
    /**
     * Configura los endpoints para formularios web con sus respectivos permisos.
     * Aplica a todas las rutas que no sean API.
     * 
     * @param auth el configurador de autorización de Spring Security
     */
    public static void configureFormEndpoints(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            // Endpoints públicos para autenticación y errores
            .requestMatchers("/perform_login", "/error").permitAll()
            
            // Acceso público a la raíz (para status de la API)
            .requestMatchers("/").permitAll()
            
            // Cualquier otra petición requiere autenticación
            .anyRequest().authenticated();
    }
}

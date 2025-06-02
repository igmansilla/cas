package com.cas.login.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejadores de autenticación para respuestas JSON personalizadas.
 * Esta clase centraliza la lógica de respuesta para eventos de autenticación
 * exitosa y fallida, proporcionando respuestas consistentes en formato JSON.
 */
@RequiredArgsConstructor
public class SecurityHandlers {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Crea un manejador para autenticación exitosa.
     * Devuelve información del usuario y sus roles en formato JSON.
     * 
     * @return AuthenticationSuccessHandler configurado
     */
    public AuthenticationSuccessHandler createSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Map<String, Object> data = new HashMap<>();
            data.put("username", userDetails.getUsername());
            
            List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            data.put("roles", roleNames);
            data.put("success", true);
            data.put("message", "Authentication successful");

            response.getWriter().write(objectMapper.writeValueAsString(data));
            response.getWriter().flush();
        };
    }
    
    /**
     * Crea un manejador para autenticación fallida.
     * Devuelve información del error en formato JSON.
     * 
     * @return AuthenticationFailureHandler configurado
     */
    public AuthenticationFailureHandler createFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> data = new HashMap<>();
            data.put("success", false);
            data.put("error", "Authentication failed");
            data.put("message", exception.getMessage());

            response.getWriter().write(objectMapper.writeValueAsString(data));
            response.getWriter().flush();
        };
    }
    
    /**
     * Crea un manejador para logout exitoso.
     * Devuelve confirmación en formato JSON.
     * 
     * @return LogoutSuccessHandler como lambda
     */
    public static void handleLogoutSuccess(
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.security.core.Authentication authentication) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"success\": true, \"message\": \"Logout successful\"}");
        response.getWriter().flush();
    }
}

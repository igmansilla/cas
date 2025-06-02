package com.cas.login.config;

import com.cas.login.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.core.annotation.Order;


/**
 * Configuración de seguridad modularizada para la aplicación.
 * Esta clase configura Spring Security con dos cadenas de filtros separadas:
 * - Una para endpoints de API (/api/**)
 * - Otra para autenticación por formularios
 * 
 * La configuración utiliza clases auxiliares para mantener el código organizado:
 * - SecurityRoles: constantes de roles
 * - SecurityEndpoints: configuración de permisos por endpoint
 * - SecurityHandlers: manejadores de eventos de autenticación
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private SecurityHandlers securityHandlers;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }    @Bean
    @Order(1) // API specific configuration
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Apply this filter chain only to /api/** paths
            .authorizeHttpRequests(SecurityEndpoints::configureApiEndpoints)
            .httpBasic(customizer -> {}) // Enable HTTP Basic for APIs
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Return 401 for unauthenticated
            );

        return http.build();
    }    @Bean
    @Order(2) // Form login configuration for other paths
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        // Initialize security handlers
        if (securityHandlers == null) {
            securityHandlers = new SecurityHandlers(objectMapper);
        }
        
        http
            .authorizeHttpRequests(SecurityEndpoints::configureFormEndpoints)
            .formLogin(formLogin ->
                formLogin
                    .loginProcessingUrl("/perform_login") // API login endpoint
                    .successHandler(securityHandlers.createSuccessHandler()) // Custom success handler
                    .failureHandler(securityHandlers.createFailureHandler()) // Custom failure handler
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutUrl("/api/logout")
                    .logoutSuccessHandler(SecurityHandlers::handleLogoutSuccess)
                    .permitAll()
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API
            // Add the CookieLoggingFilter after CsrfFilter
            .addFilterAfter(new CookieLoggingFilter(), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(authenticationProvider());

        return http.build();
    }}

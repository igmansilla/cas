package com.cas.login.config;

// ... other imports (ensure UserDetailsServiceImpl is imported)
import com.cas.login.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.List; // Added import for List
import java.util.Map;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize, @PostAuthorize etc.
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    }

    @Bean
    @Order(1) // API specific configuration
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // Apply this filter chain only to /api/** paths
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/acampantes/**").hasAnyRole("DIRIGENTE", "ADMIN")
                    .requestMatchers("/api/dirigentes/**").hasRole("ADMIN")
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/user/me").authenticated()
                    .anyRequest().denyAll() // Or .authenticated() if other /api endpoints exist and need securing
            )
            .httpBasic(customizer -> {}) // Example: enable HTTP Basic for APIs, or use JWT, etc.
            .csrf(csrf -> csrf.disable()) // Typically disable CSRF for stateless APIs
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Return 401 for unauthenticated
            );
            // .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // If API is stateless

        return http.build();
    }

    @Bean
    @Order(2) // Form login configuration for other paths
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/login", "/perform_login", "/css/**", "/js/**", "/error", "/").permitAll() // Allow /perform_login
                    .anyRequest().authenticated() // All other paths require authentication
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .loginProcessingUrl("/perform_login") // Explicit login processing URL
                    .successHandler(successHandler()) // Custom success handler
                    .failureHandler(failureHandler()) // Custom failure handler
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // Keep CSRF for form login
            )
            // Add the CookieLoggingFilter after CsrfFilter
            .addFilterAfter(new CookieLoggingFilter(), org.springframework.security.web.csrf.CsrfFilter.class)
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    private AuthenticationSuccessHandler successHandler() {
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

            response.getWriter().write(objectMapper.writeValueAsString(data));
            response.getWriter().flush();
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> data = new HashMap<>();
            data.put("error", "Authentication failed");
            data.put("message", exception.getMessage());

            response.getWriter().write(objectMapper.writeValueAsString(data));
            response.getWriter().flush();
        };
    }
}

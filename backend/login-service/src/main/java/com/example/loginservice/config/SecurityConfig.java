package com.example.loginservice.config;

// ... other imports (ensure UserDetailsServiceImpl is imported)
import com.example.loginservice.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// Add if http.csrf(csrf -> csrf.disable()) is used:
// import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Enable @PreAuthorize, @PostAuthorize etc.
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // If stateless REST API is planned, CSRF can be disabled. For now, let's keep it default or explicitly enable if needed.
            // For simplicity in API development, often CSRF is disabled for /api/** paths or globally if using tokens.
            // Example: http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
            // Or disable globally if session management is stateless: http.csrf(csrf -> csrf.disable());
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/login", "/css/**", "/js/**", "/error", "/").permitAll() // Permit home page too
                    .requestMatchers("/api/acampantes/**").hasAnyRole("DIRIGENTE", "ADMIN") // Example for Acampante management
                    .requestMatchers("/api/dirigentes/**").hasRole("ADMIN")       // Example for Dirigente management
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")             // General admin functions
                    .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .authenticationProvider(authenticationProvider());

        // For H2 console if ever used (not in current plan, but good to know):
        // http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));


        return http.build();
    }
}

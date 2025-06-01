package com.example.loginservice.config;

import com.example.loginservice.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
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
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll() // Permit access to login and static resources
                    .anyRequest().authenticated() // All other requests need authentication
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login") // Custom login page
                    .defaultSuccessUrl("/", true) // Redirect to home page on successful login
                    .permitAll() // Allow everyone to see the login page
            )
            .logout(logout ->
                logout
                    .logoutSuccessUrl("/login?logout") // Redirect to login page after logout
                    .permitAll()
            )
            .authenticationProvider(authenticationProvider()); // Set the custom authentication provider

        return http.build();
    }
}

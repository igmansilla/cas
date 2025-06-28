package com.cas.login.service;

import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import com.cas.login.security.UserDetailsImpl; // Ruta corregida
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userSecurityService") // Nombre del bean para usar en expresiones SpEL
public class UserSecurityService {

    @Autowired
    private UserRepository userRepository; // Para obtener el User completo si es necesario

    /**
     * Verifica si el ID de usuario proporcionado coincide con el ID del usuario autenticado.
     *
     * @param userId El ID del usuario a verificar.
     * @param authentication El objeto de autenticación actual.
     * @return true si el ID coincide con el del usuario autenticado, false en caso contrario.
     */
    public boolean isSelf(Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            return userDetails.getId().equals(userId);
        } else if (principal instanceof String) {
            // Si el principal es solo el username (por ejemplo, en algunos flujos de token)
            // Necesitaríamos cargar el usuario para obtener su ID.
            // Esto es menos eficiente, es mejor si UserDetails ya tiene el ID.
            String username = (String) principal;
            Optional<User> userOpt = userRepository.findByUsername(username);
            return userOpt.map(user -> user.getId().equals(userId)).orElse(false);
        }

        // Considerar otros tipos de Principal si es necesario
        return false;
    }
}

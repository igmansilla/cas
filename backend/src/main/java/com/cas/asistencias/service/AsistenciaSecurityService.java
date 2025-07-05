package com.cas.asistencias.service;

import com.cas.asistencias.dto.AsistenciaDto;
import com.cas.asistencias.model.Asistencia;
import com.cas.asistencias.repository.AsistenciaRepository;
import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import com.cas.login.security.UserDetailsImpl;
import com.cas.login.service.UserSupervisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio especializado para validaciones de seguridad en asistencias.
 * Verifica que los dirigentes solo puedan gestionar asistencias de acampantes bajo su supervisión.
 */
@Service("asistenciaSecurityService")
@RequiredArgsConstructor
@Slf4j
public class AsistenciaSecurityService {

    private final UserRepository userRepository;
    private final UserSupervisionService userSupervisionService;
    private final AsistenciaRepository asistenciaRepository;

    /**
     * Verifica si el usuario autenticado puede gestionar la asistencia de un acampante específico.
     * 
     * @param usuarioId ID del acampante cuya asistencia se quiere gestionar
     * @param authentication Información de autenticación del usuario actual
     * @return true si puede gestionar la asistencia, false en caso contrario
     */
    public boolean puedeGestionarAsistencia(Long usuarioId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Usuario no autenticado");
            return false;
        }

        // Los ADMIN pueden gestionar cualquier asistencia
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            log.debug("Usuario ADMIN puede gestionar cualquier asistencia");
            return true;
        }

        // Para dirigentes, verificar que supervisen al acampante
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DIRIGENTE"))) {
            
            Long dirigenteId = obtenerIdUsuarioAutenticado(authentication);
            if (dirigenteId == null) {
                log.debug("No se pudo obtener ID del dirigente autenticado");
                return false;
            }

            boolean puedeSupervisar = userSupervisionService.dirigenteSuperviseAcampante(dirigenteId, usuarioId);
            log.debug("Dirigente {} {} supervisar acampante {}", 
                    dirigenteId, puedeSupervisar ? "SÍ puede" : "NO puede", usuarioId);
            return puedeSupervisar;
        }

        log.debug("Usuario no tiene rol apropiado para gestionar asistencias");
        return false;
    }

    /**
     * Verifica si el usuario autenticado puede gestionar una asistencia específica por su ID.
     * 
     * @param asistenciaId ID de la asistencia
     * @param authentication Información de autenticación del usuario actual
     * @return true si puede gestionar la asistencia, false en caso contrario
     */
    public boolean puedeGestionarAsistenciaPorId(Long asistenciaId, Authentication authentication) {
        // Los ADMIN pueden gestionar cualquier asistencia
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Para dirigentes, obtener el usuarioId de la asistencia y verificar supervisión
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DIRIGENTE"))) {
            
            Asistencia asistencia = asistenciaRepository.findById(asistenciaId).orElse(null);
            if (asistencia == null) {
                log.debug("Asistencia con ID {} no encontrada", asistenciaId);
                return false;
            }

            Long usuarioId = asistencia.getUsuario().getId();
            return puedeGestionarAsistencia(usuarioId, authentication);
        }

        return false;
    }

    /**
     * Obtiene el ID del usuario autenticado.
     * 
     * @param authentication Información de autenticación
     * @return ID del usuario o null si no se puede obtener
     */
    private Long obtenerIdUsuarioAutenticado(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        } else if (principal instanceof String) {
            // Si el principal es solo el username
            String username = (String) principal;
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(null);
        }
        
        return null;
    }

    /**
     * Verifica si el usuario autenticado puede gestionar múltiples asistencias.
     * 
     * @param asistenciasDtos Lista de DTOs de asistencias a validar
     * @param authentication Información de autenticación del usuario actual
     * @return true si puede gestionar todas las asistencias, false en caso contrario
     */
    public boolean puedeGestionarMultiplesAsistencias(java.util.List<AsistenciaDto> asistenciasDtos, Authentication authentication) {
        if (asistenciasDtos == null || asistenciasDtos.isEmpty()) {
            return true; // Lista vacía, no hay nada que validar
        }

        // Los ADMIN pueden gestionar cualquier asistencia
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Para dirigentes, verificar que supervise a todos los acampantes
        return asistenciasDtos.stream()
                .allMatch(dto -> puedeGestionarAsistencia(dto.getUsuarioId(), authentication));
    }
}

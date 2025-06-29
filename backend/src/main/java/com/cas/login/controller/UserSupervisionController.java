package com.cas.login.controller;

import com.cas.login.model.User;
import com.cas.login.service.UserSupervisionService;
import com.cas.login.dto.UserDto; // Asumiendo que tienes un DTO para User o lo crearemos
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/supervision")
// @CrossOrigin(origins = "http://localhost:5173") // Ajusta si es necesario
public class UserSupervisionController {

    private final UserSupervisionService userSupervisionService;

    @Autowired
    public UserSupervisionController(UserSupervisionService userSupervisionService) {
        this.userSupervisionService = userSupervisionService;
    }

    // Helper para convertir User a UserDto (simplificado)
    private UserDto convertToDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // No incluir la contraseña ni información sensible
        dto.setRoles(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()));
        // Podrías añadir más campos si UserDto los tiene y son seguros de exponer
        return dto;
    }

    private List<UserDto> convertToDtoList(Set<User> users) {
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }
     private List<UserDto> convertToDtoList(List<User> users) {
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }


    @PostMapping("/dirigente/{dirigenteId}/assign/{acampanteId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isSelf(#dirigenteId, authentication) or hasRole('DIRIGENTE')") // Admin o el propio dirigente
    public ResponseEntity<?> assignAcampanteToDirigente(@PathVariable Long dirigenteId, @PathVariable Long acampanteId) {
        try {
            boolean success = userSupervisionService.assignAcampanteToDirigente(dirigenteId, acampanteId);
            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Acampante asignado correctamente al dirigente."));
            } else {
                return ResponseEntity.ok().body(Map.of("message", "La asignación ya existía o no se realizó."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/dirigente/{dirigenteId}/remove/{acampanteId}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isSelf(#dirigenteId, authentication) or hasRole('DIRIGENTE')")
    public ResponseEntity<?> removeAcampanteFromDirigente(@PathVariable Long dirigenteId, @PathVariable Long acampanteId) {
        try {
            boolean success = userSupervisionService.removeAcampanteFromDirigente(dirigenteId, acampanteId);
            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Acampante removido correctamente del dirigente."));
            } else {
                return ResponseEntity.ok().body(Map.of("message", "El acampante no estaba asignado o no se realizó la remoción."));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dirigente/{dirigenteId}/campers")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isSelf(#dirigenteId, authentication) or hasRole('DIRIGENTE')")
    public ResponseEntity<?> getSupervisedCampers(@PathVariable Long dirigenteId) {
        try {
            Set<User> campers = userSupervisionService.getSupervisedCampers(dirigenteId);
            return ResponseEntity.ok(convertToDtoList(campers));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/acampante/{acampanteId}/supervisors")
    @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isSelf(#acampanteId, authentication) or hasRole('ACAMPANTE')")
    public ResponseEntity<?> getSupervisorsForAcampante(@PathVariable Long acampanteId) {
        try {
            Set<User> supervisors = userSupervisionService.getSupervisorsForAcampante(acampanteId);
            return ResponseEntity.ok(convertToDtoList(supervisors));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/dirigentes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DIRIGENTE')")
    public ResponseEntity<List<UserDto>> getAllDirigentes() {
        List<User> dirigentes = userSupervisionService.findAllDirigentes();
        return ResponseEntity.ok(convertToDtoList(dirigentes));
    }

    @GetMapping("/acampantes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('DIRIGENTE')") // Dirigentes pueden necesitar ver lista de todos los acampantes para asignar
    public ResponseEntity<List<UserDto>> getAllAcampantes() {
        List<User> acampantes = userSupervisionService.findAllAcampantes();
        return ResponseEntity.ok(convertToDtoList(acampantes));
    }
}

package com.example.login.controller;

import com.example.login.model.Dirigente;
import com.example.login.model.User; // For User in DTO
import com.example.login.service.DirigenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// --- DTOs for Dirigente ---
// (Typically in their own files, but here for brevity in the subtask)
class DirigenteCreateRequest {
    public String nombreCompleto;
    public String responsabilidades;
    public String username;
    public String password;
    public Set<String> roles; // e.g., ["ROLE_DIRIGENTE", "ROLE_ADMIN"]
}

class DirigenteResponse {
    public Long id;
    public String nombreCompleto;
    public String responsabilidades;
    public String username;
    public Set<String> roles;

    public static DirigenteResponse fromEntity(Dirigente dirigente) {
        DirigenteResponse dto = new DirigenteResponse();
        dto.id = dirigente.getId();
        dto.nombreCompleto = dirigente.getNombreCompleto();
        dto.responsabilidades = dirigente.getResponsabilidades();
        if (dirigente.getUserAccount() != null) {
            dto.username = dirigente.getUserAccount().getUsername();
            if (dirigente.getUserAccount().getRoles() != null) {
                dto.roles = dirigente.getUserAccount().getRoles().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.toSet());
            }
        }
        return dto;
    }
}

class DirigenteUpdateRequest {
    public String nombreCompleto;
    public String responsabilidades;
}


@RestController
@RequestMapping("/api/dirigentes")
public class DirigenteController {

    private final DirigenteService dirigenteService;

    @Autowired
    public DirigenteController(DirigenteService dirigenteService) {
        this.dirigenteService = dirigenteService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DirigenteResponse> createDirigente(@RequestBody DirigenteCreateRequest createRequest) {
        User userAccount = new User(createRequest.username, null); // Password set in service
        Dirigente dirigente = new Dirigente(createRequest.nombreCompleto, createRequest.responsabilidades, userAccount);
        Dirigente createdDirigente = dirigenteService.createDirigente(dirigente, createRequest.password, createRequest.roles);
        return new ResponseEntity<>(DirigenteResponse.fromEntity(createdDirigente), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRIGENTE')")
    public ResponseEntity<List<DirigenteResponse>> getAllDirigentes() {
        List<DirigenteResponse> responses = dirigenteService.getAllDirigentes().stream()
            .map(DirigenteResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRIGENTE')")
    public ResponseEntity<DirigenteResponse> getDirigenteById(@PathVariable Long id) {
        return dirigenteService.getDirigenteById(id)
                .map(dirigente -> ResponseEntity.ok(DirigenteResponse.fromEntity(dirigente)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/byUsername/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRIGENTE')") // Or just for the user themselves
    public ResponseEntity<DirigenteResponse> getDirigenteByUsername(@PathVariable String username) {
        // Add logic here to check if the authenticated user is requesting their own data if not ADMIN/DIRIGENTE for all
        return dirigenteService.getDirigenteByUsername(username)
                .map(dirigente -> ResponseEntity.ok(DirigenteResponse.fromEntity(dirigente)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DirigenteResponse> updateDirigente(@PathVariable Long id, @RequestBody DirigenteUpdateRequest updateRequest) {
        Dirigente dirigenteDetails = new Dirigente();
        dirigenteDetails.setNombreCompleto(updateRequest.nombreCompleto);
        dirigenteDetails.setResponsabilidades(updateRequest.responsabilidades);
        Dirigente updatedDirigente = dirigenteService.updateDirigente(id, dirigenteDetails);
        return ResponseEntity.ok(DirigenteResponse.fromEntity(updatedDirigente));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDirigente(@PathVariable Long id) {
        dirigenteService.deleteDirigente(id);
        return ResponseEntity.noContent().build();
    }
}

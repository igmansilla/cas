package com.cas.login.controller;

import com.cas.login.model.Dirigente;
import com.cas.login.model.User;
import com.cas.login.model.Role;
import com.cas.login.model.Acampante;
import com.cas.login.service.DirigenteService;
import com.cas.login.dto.AcampanteCreateRequest;
import com.cas.login.dto.AcampanteResponse;
import io.swagger.v3.oas.annotations.Operation; // Swagger
import io.swagger.v3.oas.annotations.media.Content; // Swagger
import io.swagger.v3.oas.annotations.media.Schema; // Swagger
import io.swagger.v3.oas.annotations.responses.ApiResponse; // Swagger
import io.swagger.v3.oas.annotations.tags.Tag; // Swagger
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Autowired; // Not needed with RequiredArgsConstructor
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// --- DTOs for Dirigente ---
// (Typically in their own files, but here for brevity in the subtask)
// Consider moving these to com.cas.login.dto package for consistency
@Data
@NoArgsConstructor
@AllArgsConstructor
class DirigenteCreateRequest { // This DTO is an inner class
    public String nombreCompleto;
    public String responsabilidades;
    public String username;
    public String password;
    public Set<String> roles; // e.g., ["ROLE_DIRIGENTE", "ROLE_ADMIN"]
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DirigenteResponse { // This DTO is an inner class
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
                                .map(Role::getName) // Method reference
                                .collect(Collectors.toSet());
            }
        }
        return dto;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class DirigenteUpdateRequest { // This DTO is an inner class
    public String nombreCompleto;
    public String responsabilidades;
}


@RestController
@RequestMapping("/api/dirigentes")
@RequiredArgsConstructor
@Tag(name = "Dirigente Management", description = "Endpoints for managing Dirigentes and their supervised Acampantes.")
public class DirigenteController {

    private final DirigenteService dirigenteService;

    @Operation(summary = "Create a new Dirigente user",
               description = "Allows an ADMIN to create a new Dirigente, which also creates an associated User account.")
    @ApiResponse(responseCode = "201", description = "Dirigente created successfully",
                 content = @Content(mediaType = "application/json", schema = @Schema(implementation = DirigenteResponse.class)))
    // Add other ApiResponses for 400, 403 etc. if desired
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

    // New endpoint for a Dirigente to create an Acampante
    @Operation(summary = "Register a new Acampante supervised by the current Dirigente",
               description = "Allows an authenticated Dirigente to create a new Acampante. This also creates an associated User account for the Acampante with ROLE_ACAMPANTE and links them for supervision by the calling Dirigente.")
    @ApiResponse(responseCode = "201", description = "Acampante registered successfully",
                 content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AcampanteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., missing fields, username taken, role not found). Body may contain error details.",
                 content = @Content(mediaType = "application/json")) // Adjust schema if a specific error DTO is used
    @ApiResponse(responseCode = "401", description = "Unauthorized (not logged in or invalid credentials).",
                 content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "403", description = "Forbidden (user is authenticated but does not have ROLE_DIRIGENTE).",
                 content = @Content(mediaType = "application/json"))
    @PostMapping("/me/acampantes")
    @PreAuthorize("hasRole('DIRIGENTE')")
    public ResponseEntity<AcampanteResponse> createAcampanteByDirigente(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the Acampante to create. 'password' is optional; if not provided, the backend will generate one.", required = true,
                    content = @Content(schema = @Schema(implementation = AcampanteCreateRequest.class)))
            @org.springframework.web.bind.annotation.RequestBody AcampanteCreateRequest createRequest) {
        Acampante createdAcampante = dirigenteService.createAcampanteForDirigente(createRequest);
        return new ResponseEntity<>(AcampanteResponse.fromEntity(createdAcampante), HttpStatus.CREATED);
    }
}

package com.cas.login.controller;

import com.cas.login.model.Acampante;
import com.cas.login.service.AcampanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// --- DTOs for Acampante ---
// (Typically in their own files, but here for brevity)
class AcampanteRequest { // Used for both create and update
    public String nombreCompleto;
    public int edad;
    public String contactoEmergenciaNombre;
    public String contactoEmergenciaTelefono;
}

// Acampante entity can serve as response DTO for now, or create a specific AcampanteResponse DTO

@RestController
@RequestMapping("/api/acampantes")
public class AcampanteController {

    private final AcampanteService acampanteService;

    @Autowired
    public AcampanteController(AcampanteService acampanteService) {
        this.acampanteService = acampanteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DIRIGENTE', 'ADMIN')")
    public ResponseEntity<Acampante> createAcampante(@RequestBody AcampanteRequest createRequest) {
        Acampante acampante = new Acampante(
            createRequest.nombreCompleto,
            createRequest.edad,
            createRequest.contactoEmergenciaNombre,
            createRequest.contactoEmergenciaTelefono
        );
        Acampante createdAcampante = acampanteService.createAcampante(acampante);
        return new ResponseEntity<>(createdAcampante, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRIGENTE', 'ADMIN')")
    public ResponseEntity<List<Acampante>> getAllAcampantes() {
        List<Acampante> acampantes = acampanteService.getAllAcampantes();
        return ResponseEntity.ok(acampantes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRIGENTE', 'ADMIN')")
    public ResponseEntity<Acampante> getAcampanteById(@PathVariable Long id) {
        return acampanteService.getAcampanteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRIGENTE', 'ADMIN')")
    public ResponseEntity<Acampante> updateAcampante(@PathVariable Long id, @RequestBody AcampanteRequest updateRequest) {
        Acampante acampanteDetails = new Acampante(
            updateRequest.nombreCompleto,
            updateRequest.edad,
            updateRequest.contactoEmergenciaNombre,
            updateRequest.contactoEmergenciaTelefono
        );
        Acampante updatedAcampante = acampanteService.updateAcampante(id, acampanteDetails);
        return ResponseEntity.ok(updatedAcampante);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DIRIGENTE', 'ADMIN')")
    public ResponseEntity<Void> deleteAcampante(@PathVariable Long id) {
        acampanteService.deleteAcampante(id);
        return ResponseEntity.noContent().build();
    }
}

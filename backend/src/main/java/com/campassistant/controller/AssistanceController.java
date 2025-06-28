package com.campassistant.controller;

import com.campassistant.model.Assistance;
import com.campassistant.service.AssistanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Considerar añadir @CrossOrigin si el frontend y backend están en diferentes dominios/puertos
// @CrossOrigin(origins = "http://localhost:5173") // Ajusta el origen según tu frontend
@RestController
@RequestMapping("/api/assistance")
public class AssistanceController {

    private final AssistanceService assistanceService;

    @Autowired
    public AssistanceController(AssistanceService assistanceService) {
        this.assistanceService = assistanceService;
    }

    // Registrar o actualizar la asistencia para un usuario en una fecha específica
    // El cuerpo de la solicitud podría ser: {"userId": 1, "date": "2024-07-30", "hasAttended": true}
    // O podríamos tomar userId del path y date/hasAttended del body
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')") // Solo admins o staff pueden registrar asistencia
    public ResponseEntity<Assistance> recordAssistance(@RequestBody AssistanceRecordRequest request) {
        try {
            Assistance assistance = assistanceService.recordAssistance(
                    request.getUserId(),
                    request.getDate(),
                    request.isHasAttended()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(assistance);
        } catch (RuntimeException e) {
            // Por ejemplo, si el usuario no se encuentra
            return ResponseEntity.badRequest().body(null); // Considera un DTO de error más específico
        }
    }

    // Obtener la asistencia de un usuario para una fecha específica
    @GetMapping("/user/{userId}/date/{date}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Assistance> getAssistance(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return assistanceService.getAssistance(userId, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener todos los registros de asistencia para un usuario
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Assistance>> getAssistanceForUser(@PathVariable Long userId) {
        List<Assistance> assistanceList = assistanceService.getAssistanceForUser(userId);
        return ResponseEntity.ok(assistanceList);
    }

    // Obtener todos los registros de asistencia para una fecha específica
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Assistance>> getAssistanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Assistance> assistanceList = assistanceService.getAssistanceByDate(date);
        return ResponseEntity.ok(assistanceList);
    }

    // Obtener todos los registros de asistencia (podría ser muchos datos, usar con precaución o paginar)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo Admin puede ver todos los registros
    public ResponseEntity<List<Assistance>> getAllAssistanceRecords() {
        List<Assistance> assistanceList = assistanceService.getAllAssistanceRecords();
        return ResponseEntity.ok(assistanceList);
    }

    // Endpoint para obtener la asistencia de un grupo de usuarios en una fecha
    // Se espera una lista de userIds en el cuerpo de la solicitud: { "userIds": [1, 2, 3], "date": "2024-07-30" }
    @PostMapping("/users-on-date")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Assistance>> getAssistanceForUsersOnDate(@RequestBody UserAssistanceOnDateRequest request) {
        try {
            List<Assistance> assistanceList = assistanceService.getAssistanceForUsersOnDate(request.getUserIds(), request.getDate());
            return ResponseEntity.ok(assistanceList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // O un DTO de error
        }
    }


    // Eliminar un registro de asistencia por su ID
    @DeleteMapping("/{assistanceId}")
    @PreAuthorize("hasRole('ADMIN')") // Solo Admin puede eliminar registros directamente
    public ResponseEntity<Void> deleteAssistance(@PathVariable Long assistanceId) {
        boolean deleted = assistanceService.deleteAssistance(assistanceId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // DTOs para las solicitudes (pueden ir en un paquete separado si prefieres, ej. com.campassistant.dto)
    static class AssistanceRecordRequest {
        private Long userId;
        private LocalDate date;
        private boolean hasAttended;

        // Getters y Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public boolean isHasAttended() { return hasAttended; }
        public void setHasAttended(boolean hasAttended) { this.hasAttended = hasAttended; }
    }

    static class UserAssistanceOnDateRequest {
        private List<Long> userIds;
        private LocalDate date;

        // Getters y Setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}

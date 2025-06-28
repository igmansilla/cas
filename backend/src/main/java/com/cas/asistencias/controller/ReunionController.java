package com.cas.asistencias.controller;

import com.cas.asistencias.dto.ReunionDto;
import com.cas.asistencias.model.Reunion;
import com.cas.asistencias.service.ReunionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reuniones")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reuniones", description = "API para gestión de reuniones")
public class ReunionController {

    private final ReunionService reunionService;

    @GetMapping
    @Operation(summary = "Obtener todas las reuniones", description = "Obtiene una lista paginada de todas las reuniones")
    public ResponseEntity<Page<ReunionDto>> obtenerReuniones(Pageable pageable) {
        log.info("Obteniendo reuniones con paginación");
        Page<ReunionDto> reuniones = reunionService.obtenerReuniones(pageable);
        return ResponseEntity.ok(reuniones);
    }

    @GetMapping("/todas")
    @Operation(summary = "Obtener todas las reuniones sin paginación")
    public ResponseEntity<List<ReunionDto>> obtenerTodasLasReuniones() {
        log.info("Obteniendo todas las reuniones");
        List<ReunionDto> reuniones = reunionService.obtenerTodasLasReuniones();
        return ResponseEntity.ok(reuniones);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reunión por ID")
    public ResponseEntity<ReunionDto> obtenerReunion(
            @Parameter(description = "ID de la reunión") @PathVariable Long id) {
        log.info("Obteniendo reunión con ID: {}", id);
        ReunionDto reunion = reunionService.obtenerReunionPorId(id);
        return ResponseEntity.ok(reunion);
    }

    @PostMapping
    @Operation(summary = "Crear nueva reunión")
    public ResponseEntity<ReunionDto> crearReunion(@RequestBody ReunionDto reunionDto) {
        log.info("Creando nueva reunión: {}", reunionDto.getNombre());
        ReunionDto reunionCreada = reunionService.crearReunion(reunionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(reunionCreada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reunión")
    public ResponseEntity<ReunionDto> actualizarReunion(
            @Parameter(description = "ID de la reunión") @PathVariable Long id,
            @RequestBody ReunionDto reunionDto) {
        log.info("Actualizando reunión con ID: {}", id);
        ReunionDto reunionActualizada = reunionService.actualizarReunion(id, reunionDto);
        return ResponseEntity.ok(reunionActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reunión")
    public ResponseEntity<Void> eliminarReunion(
            @Parameter(description = "ID de la reunión") @PathVariable Long id) {
        log.info("Eliminando reunión con ID: {}", id);
        reunionService.eliminarReunion(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Obtener reuniones por estado")
    public ResponseEntity<List<ReunionDto>> obtenerReunionePorEstado(
            @Parameter(description = "Estado de la reunión") @PathVariable Reunion.EstadoReunion estado) {
        log.info("Obteniendo reuniones con estado: {}", estado);
        List<ReunionDto> reuniones = reunionService.obtenerReunionePorEstado(estado);
        return ResponseEntity.ok(reuniones);
    }

    @GetMapping("/proximas")
    @Operation(summary = "Obtener próximas reuniones")
    public ResponseEntity<List<ReunionDto>> obtenerProximasReuniones() {
        log.info("Obteniendo próximas reuniones");
        List<ReunionDto> reuniones = reunionService.obtenerProximasReuniones();
        return ResponseEntity.ok(reuniones);
    }

    @GetMapping("/mes/{year}/{month}")
    @Operation(summary = "Obtener reuniones del mes")
    public ResponseEntity<List<ReunionDto>> obtenerReunionesDelMes(
            @Parameter(description = "Año") @PathVariable int year,
            @Parameter(description = "Mes") @PathVariable int month) {
        log.info("Obteniendo reuniones del mes {}/{}", month, year);
        List<ReunionDto> reuniones = reunionService.obtenerReunionesDelMes(year, month);
        return ResponseEntity.ok(reuniones);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar reuniones por nombre")
    public ResponseEntity<Page<ReunionDto>> buscarReuniones(
            @Parameter(description = "Nombre a buscar") @RequestParam String nombre,
            Pageable pageable) {
        log.info("Buscando reuniones con nombre: {}", nombre);
        Page<ReunionDto> reuniones = reunionService.buscarReuniones(nombre, pageable);
        return ResponseEntity.ok(reuniones);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado de reunión")
    public ResponseEntity<ReunionDto> cambiarEstadoReunion(
            @Parameter(description = "ID de la reunión") @PathVariable Long id,
            @Parameter(description = "Nuevo estado") @RequestParam Reunion.EstadoReunion estado) {
        log.info("Cambiando estado de reunión {} a {}", id, estado);
        ReunionDto reunion = reunionService.cambiarEstadoReunion(id, estado);
        return ResponseEntity.ok(reunion);
    }
}

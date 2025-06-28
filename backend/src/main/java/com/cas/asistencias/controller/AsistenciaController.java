package com.cas.asistencias.controller;

import com.cas.asistencias.dto.AsistenciaDto;
import com.cas.asistencias.dto.ReporteAsistenciaDto;
import com.cas.asistencias.service.AsistenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asistencias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Asistencias", description = "API para gestión de asistencias a reuniones")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    @PostMapping
    @Operation(summary = "Registrar asistencia", description = "Registra la asistencia de un usuario a una reunión")
    public ResponseEntity<AsistenciaDto> registrarAsistencia(@RequestBody AsistenciaDto asistenciaDto) {
        log.info("Registrando asistencia para usuario {} en reunión {}", 
                asistenciaDto.getUsuarioId(), asistenciaDto.getReunionId());
        AsistenciaDto asistencia = asistenciaService.registrarAsistencia(asistenciaDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(asistencia);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar asistencia")
    public ResponseEntity<AsistenciaDto> actualizarAsistencia(
            @Parameter(description = "ID de la asistencia") @PathVariable Long id,
            @RequestBody AsistenciaDto asistenciaDto) {
        log.info("Actualizando asistencia con ID: {}", id);
        AsistenciaDto asistencia = asistenciaService.actualizarAsistencia(id, asistenciaDto);
        return ResponseEntity.ok(asistencia);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar asistencia")
    public ResponseEntity<Void> eliminarAsistencia(
            @Parameter(description = "ID de la asistencia") @PathVariable Long id) {
        log.info("Eliminando asistencia con ID: {}", id);
        asistenciaService.eliminarAsistencia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reunion/{reunionId}")
    @Operation(summary = "Obtener asistencias por reunión")
    public ResponseEntity<List<AsistenciaDto>> obtenerAsistenciasPorReunion(
            @Parameter(description = "ID de la reunión") @PathVariable Long reunionId) {
        log.info("Obteniendo asistencias para reunión: {}", reunionId);
        List<AsistenciaDto> asistencias = asistenciaService.obtenerAsistenciasPorReunion(reunionId);
        return ResponseEntity.ok(asistencias);
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener asistencias por usuario")
    public ResponseEntity<List<AsistenciaDto>> obtenerAsistenciasPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("Obteniendo asistencias para usuario: {}", usuarioId);
        List<AsistenciaDto> asistencias = asistenciaService.obtenerAsistenciasPorUsuario(usuarioId);
        return ResponseEntity.ok(asistencias);
    }

    @GetMapping("/reunion/{reunionId}/usuario/{usuarioId}")
    @Operation(summary = "Obtener asistencia específica")
    public ResponseEntity<AsistenciaDto> obtenerAsistencia(
            @Parameter(description = "ID de la reunión") @PathVariable Long reunionId,
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("Obteniendo asistencia para reunión {} y usuario {}", reunionId, usuarioId);
        AsistenciaDto asistencia = asistenciaService.obtenerAsistencia(reunionId, usuarioId);
        return ResponseEntity.ok(asistencia);
    }

    @GetMapping("/reporte/reunion/{reunionId}")
    @Operation(summary = "Generar reporte de asistencia", description = "Genera un reporte completo de asistencia para una reunión")
    public ResponseEntity<ReporteAsistenciaDto> generarReporteAsistencia(
            @Parameter(description = "ID de la reunión") @PathVariable Long reunionId) {
        log.info("Generando reporte de asistencia para reunión: {}", reunionId);
        ReporteAsistenciaDto reporte = asistenciaService.generarReporteAsistencia(reunionId);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/historial/usuario/{usuarioId}")
    @Operation(summary = "Obtener historial de asistencias de un usuario")
    public ResponseEntity<List<AsistenciaDto>> obtenerHistorialUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        log.info("Obteniendo historial de asistencias para usuario: {}", usuarioId);
        List<AsistenciaDto> historial = asistenciaService.obtenerHistorialUsuario(usuarioId);
        return ResponseEntity.ok(historial);
    }

    @PatchMapping("/{id}/salida")
    @Operation(summary = "Marcar salida", description = "Registra la hora de salida de un asistente")
    public ResponseEntity<AsistenciaDto> marcarSalida(
            @Parameter(description = "ID de la asistencia") @PathVariable Long id) {
        log.info("Marcando salida para asistencia: {}", id);
        AsistenciaDto asistencia = asistenciaService.marcarSalida(id);
        return ResponseEntity.ok(asistencia);
    }

    @PostMapping("/multiple")
    @Operation(summary = "Registrar múltiples asistencias")
    public ResponseEntity<List<AsistenciaDto>> registrarMultiplesAsistencias(
            @RequestBody List<AsistenciaDto> asistenciasDto) {
        log.info("Registrando {} asistencias", asistenciasDto.size());
        
        List<AsistenciaDto> asistenciasRegistradas = asistenciasDto.stream()
                .map(asistenciaService::registrarAsistencia)
                .toList();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenciasRegistradas);
    }
}

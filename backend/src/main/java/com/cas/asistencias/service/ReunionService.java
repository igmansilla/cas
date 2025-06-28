package com.cas.asistencias.service;

import com.cas.asistencias.dto.ReunionDto;
import com.cas.asistencias.exception.ReunionNotFoundException;
import com.cas.asistencias.model.Reunion;
import com.cas.asistencias.repository.AsistenciaRepository;
import com.cas.asistencias.repository.ReunionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final AsistenciaRepository asistenciaRepository;

    public List<ReunionDto> obtenerTodasLasReuniones() {
        return reunionRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public Page<ReunionDto> obtenerReuniones(Pageable pageable) {
        return reunionRepository.findAll(pageable)
                .map(this::convertirADto);
    }

    public ReunionDto obtenerReunionPorId(Long id) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new ReunionNotFoundException(id));
        return convertirADto(reunion);
    }

    public ReunionDto crearReunion(ReunionDto reunionDto) {
        log.info("Creando nueva reunión: {}", reunionDto.getNombre());
        
        Reunion reunion = new Reunion();
        reunion.setNombre(reunionDto.getNombre());
        reunion.setDescripcion(reunionDto.getDescripcion());
        reunion.setFechaReunion(reunionDto.getFechaReunion());
        reunion.setLugar(reunionDto.getLugar());
        reunion.setEsObligatoria(reunionDto.getEsObligatoria() != null ? reunionDto.getEsObligatoria() : false);
        reunion.setEstado(Reunion.EstadoReunion.PROGRAMADA);

        Reunion reunionGuardada = reunionRepository.save(reunion);
        log.info("Reunión creada exitosamente con ID: {}", reunionGuardada.getId());
        
        return convertirADto(reunionGuardada);
    }

    public ReunionDto actualizarReunion(Long id, ReunionDto reunionDto) {
        log.info("Actualizando reunión con ID: {}", id);
        
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new ReunionNotFoundException(id));

        reunion.setNombre(reunionDto.getNombre());
        reunion.setDescripcion(reunionDto.getDescripcion());
        reunion.setFechaReunion(reunionDto.getFechaReunion());
        reunion.setLugar(reunionDto.getLugar());
        reunion.setEsObligatoria(reunionDto.getEsObligatoria());
        
        if (reunionDto.getEstado() != null) {
            reunion.setEstado(reunionDto.getEstado());
        }

        Reunion reunionActualizada = reunionRepository.save(reunion);
        log.info("Reunión actualizada exitosamente");
        
        return convertirADto(reunionActualizada);
    }

    public void eliminarReunion(Long id) {
        log.info("Eliminando reunión con ID: {}", id);
        
        if (!reunionRepository.existsById(id)) {
            throw new ReunionNotFoundException(id);
        }
        
        reunionRepository.deleteById(id);
        log.info("Reunión eliminada exitosamente");
    }

    @Transactional(readOnly = true)
    public List<ReunionDto> obtenerReunionePorEstado(Reunion.EstadoReunion estado) {
        return reunionRepository.findByEstado(estado).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReunionDto> obtenerProximasReuniones() {
        return reunionRepository.findProximasReuniones(LocalDateTime.now(), Reunion.EstadoReunion.PROGRAMADA)
                .stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReunionDto> obtenerReunionesDelMes(int year, int month) {
        return reunionRepository.findByMes(year, month).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReunionDto> buscarReuniones(String nombre, Pageable pageable) {
        return reunionRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(this::convertirADto);
    }

    public ReunionDto cambiarEstadoReunion(Long id, Reunion.EstadoReunion nuevoEstado) {
        log.info("Cambiando estado de reunión ID: {} a {}", id, nuevoEstado);
        
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new ReunionNotFoundException(id));
        
        reunion.setEstado(nuevoEstado);
        Reunion reunionActualizada = reunionRepository.save(reunion);
        
        log.info("Estado de reunión cambiado exitosamente");
        return convertirADto(reunionActualizada);
    }

    private ReunionDto convertirADto(Reunion reunion) {
        ReunionDto dto = new ReunionDto();
        dto.setId(reunion.getId());
        dto.setNombre(reunion.getNombre());
        dto.setDescripcion(reunion.getDescripcion());
        dto.setFechaReunion(reunion.getFechaReunion());
        dto.setLugar(reunion.getLugar());
        dto.setEsObligatoria(reunion.getEsObligatoria());
        dto.setEstado(reunion.getEstado());
        dto.setFechaCreacion(reunion.getFechaCreacion());
        dto.setFechaActualizacion(reunion.getFechaActualizacion());

        // Calcular estadísticas de asistencia
        long presentes = asistenciaRepository.countPresentesByReunion(reunion);
        long ausentes = asistenciaRepository.countAusentesByReunion(reunion);
        
        dto.setPresentes((int) presentes);
        dto.setAusentes((int) ausentes);
        dto.setTotalAsistentes((int) (presentes + ausentes));

        return dto;
    }
}

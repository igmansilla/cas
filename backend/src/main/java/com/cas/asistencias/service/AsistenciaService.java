package com.cas.asistencias.service;

import com.cas.asistencias.dto.AsistenciaDto;
import com.cas.asistencias.dto.ReporteAsistenciaDto;
import com.cas.asistencias.exception.AsistenciaDuplicadaException;
import com.cas.asistencias.exception.AsistenciaNotFoundException;
import com.cas.asistencias.exception.ReunionNotFoundException;
import com.cas.asistencias.model.Asistencia;
import com.cas.asistencias.model.Reunion;
import com.cas.asistencias.repository.AsistenciaRepository;
import com.cas.asistencias.repository.ReunionRepository;
import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final ReunionRepository reunionRepository;
    private final UserRepository userRepository;

    public AsistenciaDto registrarAsistencia(AsistenciaDto asistenciaDto) {
        log.info("Registrando asistencia para usuario {} en reunión {}", 
                asistenciaDto.getUsuarioId(), asistenciaDto.getReunionId());

        // Verificar que la reunión existe
        Reunion reunion = reunionRepository.findById(asistenciaDto.getReunionId())
                .orElseThrow(() -> new ReunionNotFoundException(asistenciaDto.getReunionId()));

        // Verificar que el usuario existe
        User usuario = userRepository.findById(asistenciaDto.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + asistenciaDto.getUsuarioId()));

        // Verificar que no existe ya una asistencia para este usuario en esta reunión
        if (asistenciaRepository.existsByReunionAndUsuario(reunion, usuario)) {
            throw new AsistenciaDuplicadaException(asistenciaDto.getReunionId(), asistenciaDto.getUsuarioId());
        }

        Asistencia asistencia = new Asistencia();
        asistencia.setReunion(reunion);
        asistencia.setUsuario(usuario);
        asistencia.setEstadoAsistencia(asistenciaDto.getEstadoAsistencia());
        asistencia.setObservaciones(asistenciaDto.getObservaciones());
        asistencia.setRegistradoPor(asistenciaDto.getRegistradoPor());
        
        if (asistenciaDto.getHoraLlegada() != null) {
            asistencia.setHoraLlegada(asistenciaDto.getHoraLlegada());
        }

        Asistencia asistenciaGuardada = asistenciaRepository.save(asistencia);
        log.info("Asistencia registrada exitosamente con ID: {}", asistenciaGuardada.getId());

        return convertirADto(asistenciaGuardada);
    }

    public AsistenciaDto actualizarAsistencia(Long id, AsistenciaDto asistenciaDto) {
        log.info("Actualizando asistencia con ID: {}", id);

        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new AsistenciaNotFoundException(id));

        asistencia.setEstadoAsistencia(asistenciaDto.getEstadoAsistencia());
        asistencia.setObservaciones(asistenciaDto.getObservaciones());
        asistencia.setHoraLlegada(asistenciaDto.getHoraLlegada());
        asistencia.setHoraSalida(asistenciaDto.getHoraSalida());

        Asistencia asistenciaActualizada = asistenciaRepository.save(asistencia);
        log.info("Asistencia actualizada exitosamente");

        return convertirADto(asistenciaActualizada);
    }

    public void eliminarAsistencia(Long id) {
        log.info("Eliminando asistencia con ID: {}", id);

        if (!asistenciaRepository.existsById(id)) {
            throw new AsistenciaNotFoundException(id);
        }

        asistenciaRepository.deleteById(id);
        log.info("Asistencia eliminada exitosamente");
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDto> obtenerAsistenciasPorReunion(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ReunionNotFoundException(reunionId));

        return asistenciaRepository.findByReunion(reunion).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDto> obtenerAsistenciasPorUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        return asistenciaRepository.findByUsuario(usuario).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AsistenciaDto obtenerAsistencia(Long reunionId, Long usuarioId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ReunionNotFoundException(reunionId));

        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Asistencia asistencia = asistenciaRepository.findByReunionAndUsuario(reunion, usuario)
                .orElseThrow(() -> new AsistenciaNotFoundException(reunionId, usuarioId));

        return convertirADto(asistencia);
    }

    @Transactional(readOnly = true)
    public ReporteAsistenciaDto generarReporteAsistencia(Long reunionId) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new ReunionNotFoundException(reunionId));

        List<Asistencia> asistencias = asistenciaRepository.findByReunion(reunion);
        
        int totalRegistrados = asistencias.size();
        int presentes = (int) asistencias.stream()
                .filter(a -> a.getEstadoAsistencia() == Asistencia.EstadoAsistencia.PRESENTE)
                .count();
        int ausentes = (int) asistencias.stream()
                .filter(a -> a.getEstadoAsistencia() == Asistencia.EstadoAsistencia.AUSENTE)
                .count();
        int tardanzas = (int) asistencias.stream()
                .filter(a -> a.getEstadoAsistencia() == Asistencia.EstadoAsistencia.TARDANZA)
                .count();
        int justificados = (int) asistencias.stream()
                .filter(a -> a.getEstadoAsistencia() == Asistencia.EstadoAsistencia.JUSTIFICADO)
                .count();

        ReporteAsistenciaDto reporte = new ReporteAsistenciaDto();
        reporte.setReunionId(reunion.getId());
        reporte.setNombreReunion(reunion.getNombre());
        reporte.setFechaReunion(reunion.getFechaReunion());
        reporte.setLugar(reunion.getLugar());
        reporte.setEsObligatoria(reunion.getEsObligatoria());
        reporte.setTotalRegistrados(totalRegistrados);
        reporte.setPresentes(presentes);
        reporte.setAusentes(ausentes);
        reporte.setTardanzas(tardanzas);
        reporte.setJustificados(justificados);
        reporte.calcularPorcentajeAsistencia();

        List<AsistenciaDto> detalleAsistencias = asistencias.stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
        reporte.setDetalleAsistencias(detalleAsistencias);

        return reporte;
    }

    @Transactional(readOnly = true)
    public List<AsistenciaDto> obtenerHistorialUsuario(Long usuarioId) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        return asistenciaRepository.findHistorialAsistencias(usuario).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    public AsistenciaDto marcarSalida(Long asistenciaId) {
        log.info("Marcando salida para asistencia ID: {}", asistenciaId);

        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new AsistenciaNotFoundException(asistenciaId));

        asistencia.setHoraSalida(LocalDateTime.now());
        Asistencia asistenciaActualizada = asistenciaRepository.save(asistencia);

        log.info("Salida marcada exitosamente");
        return convertirADto(asistenciaActualizada);
    }

    private AsistenciaDto convertirADto(Asistencia asistencia) {
        AsistenciaDto dto = new AsistenciaDto();
        dto.setId(asistencia.getId());
        dto.setReunionId(asistencia.getReunion().getId());
        dto.setNombreReunion(asistencia.getReunion().getNombre());
        dto.setFechaReunion(asistencia.getReunion().getFechaReunion());
        dto.setUsuarioId(asistencia.getUsuario().getId());
        dto.setUsernameUsuario(asistencia.getUsuario().getUsername());
        dto.setFechaRegistro(asistencia.getFechaRegistro());
        dto.setEstadoAsistencia(asistencia.getEstadoAsistencia());
        dto.setHoraLlegada(asistencia.getHoraLlegada());
        dto.setHoraSalida(asistencia.getHoraSalida());
        dto.setObservaciones(asistencia.getObservaciones());
        dto.setRegistradoPor(asistencia.getRegistradoPor());
        return dto;
    }
}

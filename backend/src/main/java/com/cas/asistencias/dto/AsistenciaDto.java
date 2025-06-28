package com.cas.asistencias.dto;

import com.cas.asistencias.model.Asistencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaDto {
    
    private Long id;
    private Long reunionId;
    private String nombreReunion;
    private LocalDateTime fechaReunion;
    private Long usuarioId;
    private String nombreUsuario;
    private String usernameUsuario;
    private LocalDateTime fechaRegistro;
    private Asistencia.EstadoAsistencia estadoAsistencia;
    private LocalDateTime horaLlegada;
    private LocalDateTime horaSalida;
    private String observaciones;
    private String registradoPor;

    // Constructor para registrar nueva asistencia
    public AsistenciaDto(Long reunionId, Long usuarioId, Asistencia.EstadoAsistencia estadoAsistencia) {
        this.reunionId = reunionId;
        this.usuarioId = usuarioId;
        this.estadoAsistencia = estadoAsistencia;
        this.fechaRegistro = LocalDateTime.now();
    }

    // Constructor con observaciones
    public AsistenciaDto(Long reunionId, Long usuarioId, Asistencia.EstadoAsistencia estadoAsistencia, 
                        String observaciones) {
        this(reunionId, usuarioId, estadoAsistencia);
        this.observaciones = observaciones;
    }

    // Constructor para mostrar información completa
    public AsistenciaDto(Long id, Long reunionId, String nombreReunion, Long usuarioId, 
                        String nombreUsuario, Asistencia.EstadoAsistencia estadoAsistencia, 
                        LocalDateTime fechaRegistro) {
        this.id = id;
        this.reunionId = reunionId;
        this.nombreReunion = nombreReunion;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.estadoAsistencia = estadoAsistencia;
        this.fechaRegistro = fechaRegistro;
    }
}

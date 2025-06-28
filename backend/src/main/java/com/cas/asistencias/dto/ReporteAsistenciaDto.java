package com.cas.asistencias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAsistenciaDto {
    
    private Long reunionId;
    private String nombreReunion;
    private LocalDateTime fechaReunion;
    private String lugar;
    private Boolean esObligatoria;
    private Integer totalRegistrados;
    private Integer presentes;
    private Integer ausentes;
    private Integer tardanzas;
    private Integer justificados;
    private Double porcentajeAsistencia;
    private List<AsistenciaDto> detalleAsistencias;

    // Constructor para resumen básico
    public ReporteAsistenciaDto(Long reunionId, String nombreReunion, LocalDateTime fechaReunion,
                               Integer totalRegistrados, Integer presentes, Integer ausentes) {
        this.reunionId = reunionId;
        this.nombreReunion = nombreReunion;
        this.fechaReunion = fechaReunion;
        this.totalRegistrados = totalRegistrados;
        this.presentes = presentes;
        this.ausentes = ausentes;
        this.porcentajeAsistencia = totalRegistrados > 0 ? 
            (double) presentes / totalRegistrados * 100 : 0.0;
    }

    // Método para calcular el porcentaje de asistencia
    public void calcularPorcentajeAsistencia() {
        if (totalRegistrados != null && totalRegistrados > 0 && presentes != null) {
            this.porcentajeAsistencia = (double) presentes / totalRegistrados * 100;
        } else {
            this.porcentajeAsistencia = 0.0;
        }
    }
}

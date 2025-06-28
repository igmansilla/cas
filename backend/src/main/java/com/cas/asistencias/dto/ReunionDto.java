package com.cas.asistencias.dto;

import com.cas.asistencias.model.Reunion;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReunionDto {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaReunion;
    private String lugar;
    private Boolean esObligatoria;
    private Reunion.EstadoReunion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Integer totalAsistentes;
    private Integer presentes;
    private Integer ausentes;

    // Constructor para crear una nueva reunión (sin id)
    public ReunionDto(String nombre, String descripcion, LocalDateTime fechaReunion, 
                     String lugar, Boolean esObligatoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaReunion = fechaReunion;
        this.lugar = lugar;
        this.esObligatoria = esObligatoria;
        this.estado = Reunion.EstadoReunion.PROGRAMADA;
    }

    // Constructor básico con información esencial
    public ReunionDto(Long id, String nombre, LocalDateTime fechaReunion, 
                     Reunion.EstadoReunion estado) {
        this.id = id;
        this.nombre = nombre;
        this.fechaReunion = fechaReunion;
        this.estado = estado;
    }
}

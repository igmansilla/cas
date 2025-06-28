package com.cas.asistencias.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reuniones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_reunion", nullable = false)
    private LocalDateTime fechaReunion;

    @Column(name = "lugar")
    private String lugar;

    @Column(name = "es_obligatoria")
    private Boolean esObligatoria = false;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoReunion estado = EstadoReunion.PROGRAMADA;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "reunion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Asistencia> asistencias = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public enum EstadoReunion {
        PROGRAMADA,
        EN_CURSO,
        FINALIZADA,
        CANCELADA
    }

    // Constructor sin id para creación
    public Reunion(String nombre, String descripcion, LocalDateTime fechaReunion, String lugar, Boolean esObligatoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaReunion = fechaReunion;
        this.lugar = lugar;
        this.esObligatoria = esObligatoria != null ? esObligatoria : false;
    }
}

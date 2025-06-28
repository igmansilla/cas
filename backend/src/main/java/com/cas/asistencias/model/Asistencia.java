package com.cas.asistencias.model;

import com.cas.login.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "asistencias", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"reunion_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reunion_id", nullable = false)
    private Reunion reunion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "estado_asistencia")
    @Enumerated(EnumType.STRING)
    private EstadoAsistencia estadoAsistencia = EstadoAsistencia.PRESENTE;

    @Column(name = "hora_llegada")
    private LocalDateTime horaLlegada;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "registrado_por")
    private String registradoPor;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
        if (horaLlegada == null && estadoAsistencia == EstadoAsistencia.PRESENTE) {
            horaLlegada = LocalDateTime.now();
        }
    }

    public enum EstadoAsistencia {
        PRESENTE,
        AUSENTE,
        TARDANZA,
        JUSTIFICADO
    }

    // Constructor para facilitar la creación
    public Asistencia(Reunion reunion, User usuario, EstadoAsistencia estadoAsistencia) {
        this.reunion = reunion;
        this.usuario = usuario;
        this.estadoAsistencia = estadoAsistencia != null ? estadoAsistencia : EstadoAsistencia.PRESENTE;
        this.fechaRegistro = LocalDateTime.now();
        if (this.estadoAsistencia == EstadoAsistencia.PRESENTE) {
            this.horaLlegada = LocalDateTime.now();
        }
    }

    // Constructor con observaciones
    public Asistencia(Reunion reunion, User usuario, EstadoAsistencia estadoAsistencia, String observaciones) {
        this(reunion, usuario, estadoAsistencia);
        this.observaciones = observaciones;
    }
}

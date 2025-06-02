package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Potentially: import jakarta.persistence.ManyToOne;
// Potentially: import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "acampantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Acampante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;
    private int edad;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;

    // Optional: Link to a Dirigente who registered them, or to a group.
    // @ManyToOne
    // @JoinColumn(name = "dirigente_responsable_id")
    // private Dirigente dirigenteResponsable;

    // Constructor without id (for creation)
    public Acampante(String nombreCompleto, int edad, String contactoEmergenciaNombre, String contactoEmergenciaTelefono) {
        this.nombreCompleto = nombreCompleto;
        this.edad = edad;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }
}

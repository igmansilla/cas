package com.example.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// Potentially: import jakarta.persistence.ManyToOne;
// Potentially: import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "acampantes")
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

    // Constructors
    public Acampante() {
    }

    public Acampante(String nombreCompleto, int edad, String contactoEmergenciaNombre, String contactoEmergenciaTelefono) {
        this.nombreCompleto = nombreCompleto;
        this.edad = edad;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public String getContactoEmergenciaNombre() { return contactoEmergenciaNombre; }
    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) { this.contactoEmergenciaNombre = contactoEmergenciaNombre; }
    public String getContactoEmergenciaTelefono() { return contactoEmergenciaTelefono; }
    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) { this.contactoEmergenciaTelefono = contactoEmergenciaTelefono; }
}

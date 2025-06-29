package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;

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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true, nullable = true)
    private User userAccount;

    // Constructor without id (for creation)
    public Acampante(String nombreCompleto, int edad, String contactoEmergenciaNombre, String contactoEmergenciaTelefono) {
        this.nombreCompleto = nombreCompleto;
        this.edad = edad;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }

    // Constructor to also include UserAccount
    public Acampante(String nombreCompleto, int edad, String contactoEmergenciaNombre, String contactoEmergenciaTelefono, User userAccount) {
        this.nombreCompleto = nombreCompleto;
        this.edad = edad;
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
        this.userAccount = userAccount;
    }
}

package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

@Entity
@Table(name = "dirigentes")
public class Dirigente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;
    private String responsabilidades; // e.g., "Logistics", "First Aid"

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // If a Dirigente is deleted, their User account is also deleted.
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User userAccount;

    // Constructors
    public Dirigente() {
    }

    public Dirigente(String nombreCompleto, String responsabilidades, User userAccount) {
        this.nombreCompleto = nombreCompleto;
        this.responsabilidades = responsabilidades;
        this.userAccount = userAccount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getResponsabilidades() { return responsabilidades; }
    public void setResponsabilidades(String responsabilidades) { this.responsabilidades = responsabilidades; }
    public User getUserAccount() { return userAccount; }
    public void setUserAccount(User userAccount) { this.userAccount = userAccount; }
}

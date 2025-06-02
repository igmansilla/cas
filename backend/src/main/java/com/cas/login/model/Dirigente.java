package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dirigentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dirigente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombreCompleto;
    private String responsabilidades; // e.g., "Logistics", "First Aid"

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // If a Dirigente is deleted, their User account is also deleted.
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User userAccount;

    // Constructor without id (for creation)
    public Dirigente(String nombreCompleto, String responsabilidades, User userAccount) {
        this.nombreCompleto = nombreCompleto;
        this.responsabilidades = responsabilidades;
        this.userAccount = userAccount;
    }
}

package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode; // Importar para excluir campos
import lombok.NoArgsConstructor;
import lombok.ToString; // Importar para excluir campos

import com.cas.packinglist.model.PackingList; // Added import
import jakarta.persistence.CascadeType; // Added import
import jakarta.persistence.OneToOne; // Added import
import jakarta.persistence.OneToMany; // Added import for Assistance relationship

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PackingList packingList;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<com.campassistant.model.Assistance> assistanceRecords = new HashSet<>();

    // Relación para Dirigentes: Acampantes que supervisa un Dirigente
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_supervision",
        joinColumns = @JoinColumn(name = "dirigente_id"), // El User que es Dirigente
        inverseJoinColumns = @JoinColumn(name = "acampante_id") // El User que es Acampante supervisado
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> supervisedCampers = new HashSet<>();

    // Relación para Acampantes: Dirigentes que supervisan a un Acampante
    // 'mappedBy' indica que la tabla de unión es gestionada por la otra entidad (supervisedCampers)
    @ManyToMany(mappedBy = "supervisedCampers", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<User> supervisors = new HashSet<>();

    // Constructor without id (for creation)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Custom method to get role names
    public List<String> getRoleNames() {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}

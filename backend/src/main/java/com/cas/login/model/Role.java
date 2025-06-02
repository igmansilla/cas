package com.cas.login.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Using Integer as ID for roles is common

    @Column(length = 20, unique = true, nullable = false)
    private String name; // e.g., ROLE_USER, ROLE_ADMIN

    // Constructor without id (for creation)
    public Role(String name) {
        this.name = name;
    }
}

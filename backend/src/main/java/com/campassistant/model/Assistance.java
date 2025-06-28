package com.campassistant.model;

import com.cas.login.model.User; // Import User from the correct package
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "assistance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "date"}) // Changed camper_id to user_id
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Changed camper_id to user_id and referenced User
    private User user; // Changed Camper to User

    private LocalDate date; // Fecha para la que se registra la asistencia

    private boolean hasAttended; // true si asistió, false si no

    // Podrías añadir más campos si es necesario, como la actividad específica
    // private String activity;
}

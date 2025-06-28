package com.campassistant.repository;

import com.campassistant.model.Assistance;
import com.cas.login.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssistanceRepository extends JpaRepository<Assistance, Long> {

    Optional<Assistance> findByUserAndDate(User user, LocalDate date);

    List<Assistance> findByUserId(Long userId);

    List<Assistance> findByDate(LocalDate date);

    List<Assistance> findByUserInAndDate(List<User> users, LocalDate date);

    // Puedes añadir métodos personalizados si necesitas consultas más específicas
    // Por ejemplo, para encontrar todas las asistencias de un usuario en un rango de fechas:
    // List<Assistance> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
}

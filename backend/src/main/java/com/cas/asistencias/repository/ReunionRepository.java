package com.cas.asistencias.repository;

import com.cas.asistencias.model.Reunion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    // Buscar reuniones por estado
    List<Reunion> findByEstado(Reunion.EstadoReunion estado);

    // Buscar reuniones por rango de fechas
    @Query("SELECT r FROM Reunion r WHERE r.fechaReunion BETWEEN :fechaInicio AND :fechaFin ORDER BY r.fechaReunion DESC")
    List<Reunion> findByFechaReunionBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                           @Param("fechaFin") LocalDateTime fechaFin);

    // Buscar reuniones próximas
    @Query("SELECT r FROM Reunion r WHERE r.fechaReunion >= :fecha AND r.estado = :estado ORDER BY r.fechaReunion ASC")
    List<Reunion> findProximasReuniones(@Param("fecha") LocalDateTime fecha, 
                                       @Param("estado") Reunion.EstadoReunion estado);

    // Buscar reuniones por nombre (búsqueda parcial)
    @Query("SELECT r FROM Reunion r WHERE LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY r.fechaReunion DESC")
    Page<Reunion> findByNombreContainingIgnoreCase(@Param("nombre") String nombre, Pageable pageable);

    // Buscar reuniones del mes actual
    @Query("SELECT r FROM Reunion r WHERE YEAR(r.fechaReunion) = :year AND MONTH(r.fechaReunion) = :month ORDER BY r.fechaReunion ASC")
    List<Reunion> findByMes(@Param("year") int year, @Param("month") int month);

    // Buscar reuniones obligatorias
    List<Reunion> findByEsObligatoriaTrue();

    // Contar reuniones por estado
    long countByEstado(Reunion.EstadoReunion estado);

    // Buscar reunión más reciente
    Optional<Reunion> findTopByOrderByFechaReunionDesc();
}

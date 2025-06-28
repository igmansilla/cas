package com.cas.asistencias.repository;

import com.cas.asistencias.model.Asistencia;
import com.cas.asistencias.model.Reunion;
import com.cas.login.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Buscar asistencias por reunión
    List<Asistencia> findByReunion(Reunion reunion);

    // Buscar asistencias por usuario
    List<Asistencia> findByUsuario(User usuario);

    // Buscar asistencia específica de un usuario en una reunión
    Optional<Asistencia> findByReunionAndUsuario(Reunion reunion, User usuario);

    // Verificar si un usuario ya tiene asistencia registrada en una reunión
    boolean existsByReunionAndUsuario(Reunion reunion, User usuario);

    // Buscar asistencias por estado
    List<Asistencia> findByEstadoAsistencia(Asistencia.EstadoAsistencia estadoAsistencia);

    // Buscar asistencias de una reunión por estado
    @Query("SELECT a FROM Asistencia a WHERE a.reunion = :reunion AND a.estadoAsistencia = :estado")
    List<Asistencia> findByReunionAndEstado(@Param("reunion") Reunion reunion, 
                                           @Param("estado") Asistencia.EstadoAsistencia estado);

    // Contar asistentes por reunión
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.reunion = :reunion AND a.estadoAsistencia = 'PRESENTE'")
    long countPresentesByReunion(@Param("reunion") Reunion reunion);

    // Contar ausentes por reunión
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.reunion = :reunion AND a.estadoAsistencia = 'AUSENTE'")
    long countAusentesByReunion(@Param("reunion") Reunion reunion);

    // Buscar asistencias de un usuario en un rango de fechas
    @Query("SELECT a FROM Asistencia a WHERE a.usuario = :usuario AND a.reunion.fechaReunion BETWEEN :fechaInicio AND :fechaFin ORDER BY a.reunion.fechaReunion DESC")
    List<Asistencia> findByUsuarioAndFechaRange(@Param("usuario") User usuario, 
                                               @Param("fechaInicio") LocalDateTime fechaInicio, 
                                               @Param("fechaFin") LocalDateTime fechaFin);

    // Obtener estadísticas de asistencia por usuario
    @Query("SELECT a.estadoAsistencia, COUNT(a) FROM Asistencia a WHERE a.usuario = :usuario GROUP BY a.estadoAsistencia")
    List<Object[]> getEstadisticasAsistenciaPorUsuario(@Param("usuario") User usuario);

    // Obtener el historial de asistencias de un usuario
    @Query("SELECT a FROM Asistencia a WHERE a.usuario = :usuario ORDER BY a.reunion.fechaReunion DESC")
    List<Asistencia> findHistorialAsistencias(@Param("usuario") User usuario);

    // Buscar usuarios que faltaron a reuniones obligatorias
    @Query("SELECT DISTINCT a.usuario FROM Asistencia a WHERE a.reunion.esObligatoria = true AND a.estadoAsistencia = 'AUSENTE'")
    List<User> findUsuariosConFaltasEnReunionesObligatorias();

    // Obtener asistencias registradas en las últimas horas
    @Query("SELECT a FROM Asistencia a WHERE a.fechaRegistro >= :fecha ORDER BY a.fechaRegistro DESC")
    List<Asistencia> findAsistenciasRecientes(@Param("fecha") LocalDateTime fecha);
}

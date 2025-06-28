package com.cas.asistencias.config;

import com.cas.asistencias.model.Asistencia;
import com.cas.asistencias.model.Reunion;
import com.cas.asistencias.repository.AsistenciaRepository;
import com.cas.asistencias.repository.ReunionRepository;
import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3) // Ejecutar después de los datos de usuarios
public class AsistenciasDataInitializer implements CommandLineRunner {

    private final ReunionRepository reunionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (reunionRepository.count() == 0) {
            log.info("Inicializando datos de ejemplo para reuniones y asistencias...");
            crearDatosDeEjemplo();
            log.info("Datos de ejemplo creados exitosamente");
        } else {
            log.info("Los datos de reuniones ya existen, omitiendo inicialización");
        }
    }

    private void crearDatosDeEjemplo() {
        // Crear reuniones de ejemplo
        Reunion reunion1 = new Reunion();
        reunion1.setNombre("Reunión de Planificación Mensual");
        reunion1.setDescripcion("Reunión para planificar las actividades del mes");
        reunion1.setFechaReunion(LocalDateTime.now().plusDays(7));
        reunion1.setLugar("Sala de Reuniones Principal");
        reunion1.setEsObligatoria(true);
        reunion1.setEstado(Reunion.EstadoReunion.PROGRAMADA);

        Reunion reunion2 = new Reunion();
        reunion2.setNombre("Taller de Capacitación");
        reunion2.setDescripcion("Taller sobre nuevas metodologías de trabajo");
        reunion2.setFechaReunion(LocalDateTime.now().minusDays(2));
        reunion2.setLugar("Auditorio");
        reunion2.setEsObligatoria(false);
        reunion2.setEstado(Reunion.EstadoReunion.FINALIZADA);

        Reunion reunion3 = new Reunion();
        reunion3.setNombre("Revisión de Proyectos");
        reunion3.setDescripcion("Evaluación del progreso de los proyectos actuales");
        reunion3.setFechaReunion(LocalDateTime.now().plusDays(14));
        reunion3.setLugar("Sala de Conferencias");
        reunion3.setEsObligatoria(true);
        reunion3.setEstado(Reunion.EstadoReunion.PROGRAMADA);

        List<Reunion> reuniones = List.of(reunion1, reunion2, reunion3);
        reunionRepository.saveAll(reuniones);

        // Obtener usuarios existentes
        List<User> usuarios = userRepository.findAll();
        
        if (!usuarios.isEmpty()) {
            // Crear asistencias de ejemplo para la reunión finalizada
            Reunion reunionFinalizada = reuniones.get(1); // Taller de Capacitación
            
            for (int i = 0; i < Math.min(usuarios.size(), 3); i++) {
                User usuario = usuarios.get(i);
                
                Asistencia asistencia = new Asistencia();
                asistencia.setReunion(reunionFinalizada);
                asistencia.setUsuario(usuario);
                asistencia.setFechaRegistro(reunionFinalizada.getFechaReunion().minusMinutes(5));
                asistencia.setHoraLlegada(reunionFinalizada.getFechaReunion().plusMinutes(i * 5));
                
                // Simular diferentes estados de asistencia
                switch (i % 4) {
                    case 0:
                        asistencia.setEstadoAsistencia(Asistencia.EstadoAsistencia.PRESENTE);
                        asistencia.setHoraSalida(reunionFinalizada.getFechaReunion().plusHours(2));
                        break;
                    case 1:
                        asistencia.setEstadoAsistencia(Asistencia.EstadoAsistencia.TARDANZA);
                        asistencia.setHoraLlegada(reunionFinalizada.getFechaReunion().plusMinutes(20));
                        asistencia.setObservaciones("Llegó 20 minutos tarde por tráfico");
                        break;
                    case 2:
                        asistencia.setEstadoAsistencia(Asistencia.EstadoAsistencia.AUSENTE);
                        asistencia.setHoraLlegada(null);
                        asistencia.setObservaciones("No se presentó a la reunión");
                        break;
                    case 3:
                        asistencia.setEstadoAsistencia(Asistencia.EstadoAsistencia.JUSTIFICADO);
                        asistencia.setHoraLlegada(null);
                        asistencia.setObservaciones("Ausencia justificada por motivos médicos");
                        break;
                }
                
                asistencia.setRegistradoPor("sistema");
                asistenciaRepository.save(asistencia);
            }
            
            log.info("Creadas {} reuniones y {} asistencias de ejemplo", 
                    reuniones.size(), Math.min(usuarios.size(), 3));
        } else {
            log.warn("No se encontraron usuarios para crear asistencias de ejemplo");
        }
    }
}

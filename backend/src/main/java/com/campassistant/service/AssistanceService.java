package com.campassistant.service;

import com.campassistant.model.Assistance;
import com.campassistant.repository.AssistanceRepository;
import com.cas.login.model.User;
import com.cas.login.repository.UserRepository;
import com.cas.login.service.UserSupervisionService; // Importar el nuevo servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AssistanceService {

    private final AssistanceRepository assistanceRepository;
    private final UserRepository userRepository;
    private final UserSupervisionService userSupervisionService; // Inyectar el servicio

    @Autowired
    public AssistanceService(AssistanceRepository assistanceRepository,
                             UserRepository userRepository,
                             UserSupervisionService userSupervisionService) {
        this.assistanceRepository = assistanceRepository;
        this.userRepository = userRepository;
        this.userSupervisionService = userSupervisionService;
    }

    @Transactional
    public Assistance recordAssistance(Long userId, LocalDate date, boolean hasAttended) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId)); // Considera una excepción más específica

        // Verificar si ya existe un registro para este usuario y fecha
        Optional<Assistance> existingAssistance = assistanceRepository.findByUserAndDate(user, date);

        Assistance assistanceRecord;
        if (existingAssistance.isPresent()) {
            // Actualizar el registro existente
            assistanceRecord = existingAssistance.get();
            assistanceRecord.setHasAttended(hasAttended);
        } else {
            // Crear un nuevo registro
            assistanceRecord = new Assistance();
            assistanceRecord.setUser(user);
            assistanceRecord.setDate(date);
            assistanceRecord.setHasAttended(hasAttended);
        }
        return assistanceRepository.save(assistanceRecord);
    }

    @Transactional(readOnly = true)
    public Optional<Assistance> getAssistance(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return assistanceRepository.findByUserAndDate(user, date);
    }

    @Transactional(readOnly = true)
    public List<Assistance> getAssistanceForUser(Long userId) {
        return assistanceRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Assistance> getAssistanceByDate(LocalDate date) {
        return assistanceRepository.findByDate(date);
    }

    @Transactional(readOnly = true)
    public List<Assistance> getAllAssistanceRecords() {
        return assistanceRepository.findAll();
    }

    // Método para obtener la asistencia de una lista de usuarios en una fecha específica
    @Transactional(readOnly = true)
    public List<Assistance> getAssistanceForUsersOnDate(List<Long> userIds, LocalDate date) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            // Manejar el caso donde algunos IDs de usuario no se encontraron
            // Podrías lanzar una excepción o filtrar los IDs no encontrados
            throw new RuntimeException("One or more users not found.");
        }
        return assistanceRepository.findByUserInAndDate(users, date);
    }

    @Transactional
    public boolean deleteAssistance(Long assistanceId) {
        if (assistanceRepository.existsById(assistanceId)) {
            assistanceRepository.deleteById(assistanceId);
            return true;
        }
        return false; // O lanzar una excepción si el registro no existe
    }

    @Transactional(readOnly = true)
    public List<Assistance> getAssistanceForSupervisedCampers(Long dirigenteId, LocalDate date) {
        // Obtener los IDs de los acampantes supervisados por el dirigente
        Set<User> supervisedCampers = userSupervisionService.getSupervisedCampers(dirigenteId);

        if (supervisedCampers.isEmpty()) {
            return List.of(); // Devuelve una lista vacía si no hay acampantes supervisados
        }

        // List<Long> supervisedCamperIds = supervisedCampers.stream()
        //                                                 .map(User::getId)
        //                                                 .collect(Collectors.toList());
        // return getAssistanceForUsersOnDate(supervisedCamperIds, date);
        // El método findByUserInAndDate espera List<User>, no List<Long>
        return assistanceRepository.findByUserInAndDate(List.copyOf(supervisedCampers), date);
    }
}

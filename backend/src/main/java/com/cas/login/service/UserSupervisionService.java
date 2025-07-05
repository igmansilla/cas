package com.cas.login.service;

import com.cas.login.model.User;
import com.cas.login.model.Role;
import com.cas.login.repository.UserRepository;
import com.cas.login.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserSupervisionService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Nombres de los roles (podrían venir de constantes)
    private static final String ROLE_DIRIGENTE_NAME = "ROLE_DIRIGENTE";
    private static final String ROLE_ACAMPANTE_NAME = "ROLE_ACAMPANTE";

    @Autowired
    public UserSupervisionService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private Role getDirigenteRole() {
        return roleRepository.findByName(ROLE_DIRIGENTE_NAME)
                .orElseThrow(() -> new IllegalStateException(ROLE_DIRIGENTE_NAME + " not found. Please ensure roles are loaded."));
    }

    private Role getAcampanteRole() {
        return roleRepository.findByName(ROLE_ACAMPANTE_NAME)
                .orElseThrow(() -> new IllegalStateException(ROLE_ACAMPANTE_NAME + " not found. Please ensure roles are loaded."));
    }

    @Transactional
    public boolean assignAcampanteToDirigente(Long dirigenteId, Long acampanteId) {
        User dirigente = userRepository.findById(dirigenteId)
                .orElseThrow(() -> new IllegalArgumentException("Dirigente not found with ID: " + dirigenteId));
        User acampante = userRepository.findById(acampanteId)
                .orElseThrow(() -> new IllegalArgumentException("Acampante not found with ID: " + acampanteId));

        // Validar roles (opcional pero recomendado)
        if (!dirigente.getRoles().contains(getDirigenteRole())) {
            throw new IllegalArgumentException("User " + dirigenteId + " is not a DIRIGENTE.");
        }
        if (!acampante.getRoles().contains(getAcampanteRole())) {
            throw new IllegalArgumentException("User " + acampanteId + " is not an ACAMPANTE.");
        }

        // Añadir a las colecciones gestionadas por JPA
        boolean addedToDirigente = dirigente.getSupervisedCampers().add(acampante);
        boolean addedToAcampante = acampante.getSupervisors().add(dirigente);

        // JPA guardará las relaciones al guardar la entidad propietaria de la relación @ManyToMany (la que no tiene mappedBy)
        // En este caso, User.supervisedCampers es la propietaria.
        userRepository.save(dirigente);
        // Si la relación fuera unidireccional desde Dirigente, solo se guardaría dirigente.
        // Como es bidireccional y queremos que ambas partes se actualicen en la sesión actual,
        // podemos guardar también acampante si es necesario, aunque la tabla de unión la gestiona el lado propietario.
        // userRepository.save(acampante); // Usualmente no es necesario si la relación está bien configurada.

        return addedToDirigente || addedToAcampante; // Retorna true si se hizo algún cambio
    }

    @Transactional
    public boolean removeAcampanteFromDirigente(Long dirigenteId, Long acampanteId) {
        User dirigente = userRepository.findById(dirigenteId)
                .orElseThrow(() -> new IllegalArgumentException("Dirigente not found with ID: " + dirigenteId));
        User acampante = userRepository.findById(acampanteId)
                .orElseThrow(() -> new IllegalArgumentException("Acampante not found with ID: " + acampanteId));

        boolean removedFromDirigente = dirigente.getSupervisedCampers().remove(acampante);
        boolean removedFromAcampante = acampante.getSupervisors().remove(dirigente);

        userRepository.save(dirigente);
        // userRepository.save(acampante); // Similar al de assign

        return removedFromDirigente || removedFromAcampante;
    }

    @Transactional(readOnly = true)
    public Set<User> getSupervisedCampers(Long dirigenteId) {
        User dirigente = userRepository.findById(dirigenteId)
                .orElseThrow(() -> new IllegalArgumentException("Dirigente not found with ID: " + dirigenteId));

        if (!dirigente.getRoles().contains(getDirigenteRole())) {
            // Opcional: lanzar error o devolver vacío si el usuario no es dirigente
            // throw new IllegalArgumentException("User " + dirigenteId + " is not a DIRIGENTE.");
             return Collections.emptySet();
        }
        // Forzar la carga si es LAZY y fuera de una transacción activa que lo haría (aunque aquí @Transactional lo maneja)
        // Hibernate.initialize(dirigente.getSupervisedCampers());
        return dirigente.getSupervisedCampers();
    }

    @Transactional(readOnly = true)
    public Set<User> getSupervisorsForAcampante(Long acampanteId) {
        User acampante = userRepository.findById(acampanteId)
                .orElseThrow(() -> new IllegalArgumentException("Acampante not found with ID: " + acampanteId));

        if (!acampante.getRoles().contains(getAcampanteRole())) {
            // Opcional: lanzar error o devolver vacío
            // throw new IllegalArgumentException("User " + acampanteId + " is not an ACAMPANTE.");
            return Collections.emptySet();
        }
        // Hibernate.initialize(acampante.getSupervisors());
        return acampante.getSupervisors();
    }

    @Transactional(readOnly = true)
    public List<User> findAllDirigentes() {
        Role dirigenteRole = getDirigenteRole();
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(dirigenteRole))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<User> findAllAcampantes() {
        Role acampanteRole = getAcampanteRole();
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(acampanteRole))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un dirigente específico supervisa a un acampante específico
     * 
     * @param dirigenteId ID del dirigente
     * @param acampanteId ID del acampante
     * @return true si el dirigente supervisa al acampante, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean dirigenteSuperviseAcampante(Long dirigenteId, Long acampanteId) {
        if (dirigenteId == null || acampanteId == null) {
            return false;
        }

        User dirigente = userRepository.findById(dirigenteId).orElse(null);
        if (dirigente == null || !dirigente.getRoles().contains(getDirigenteRole())) {
            return false;
        }

        User acampante = userRepository.findById(acampanteId).orElse(null);
        if (acampante == null || !acampante.getRoles().contains(getAcampanteRole())) {
            return false;
        }

        return dirigente.getSupervisedCampers().contains(acampante);
    }
}

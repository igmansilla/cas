package com.cas.login.service;

import com.cas.login.dto.AcampanteCreateRequest; // New DTO
import com.cas.login.model.Acampante;
import com.cas.login.model.Role; // Required
import com.cas.login.model.User; // Required
import com.cas.login.repository.AcampanteRepository;
import com.cas.login.repository.RoleRepository; // Required
import com.cas.login.repository.UserRepository; // Required
import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Autowired; // Not needed with RequiredArgsConstructor
import org.springframework.security.crypto.password.PasswordEncoder; // Required
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet; // Required
import java.util.List;
import java.util.Optional;
import java.util.Set; // Required

@Service
@RequiredArgsConstructor // This will inject all final fields
public class AcampanteService {

    private final AcampanteRepository acampanteRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Role name constant
    private static final String ROLE_ACAMPANTE_NAME = "ROLE_ACAMPANTE";

    @Transactional
    public Acampante createAcampante(AcampanteCreateRequest createRequest) {
        // Validate input
        if (createRequest.getUsername() == null || createRequest.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty for Acampante.");
        }
        if (userRepository.findByUsername(createRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + createRequest.getUsername());
        }
        if (createRequest.getNombreCompleto() == null || createRequest.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("Acampante name (nombreCompleto) cannot be empty.");
        }
        // Add other validations as necessary (e.g., for age, emergency contact)

        // Create User account
        User user = new User();
        user.setUsername(createRequest.getUsername());

        // Handle password - generate if not provided, or use provided
        String passwordToEncode = createRequest.getPassword();
        if (passwordToEncode == null || passwordToEncode.trim().isEmpty()) {
            // Consider a more robust random password generator if this is a common case
            passwordToEncode = "defaultPassword123!"; // Placeholder - use a strong, random password
        }
        user.setPassword(passwordEncoder.encode(passwordToEncode));

        // Assign ROLE_ACAMPANTE
        Role acampanteRole = roleRepository.findByName(ROLE_ACAMPANTE_NAME)
                .orElseThrow(() -> new RuntimeException("Role not found: " + ROLE_ACAMPANTE_NAME + ". Please ensure it exists in the database."));
        Set<Role> roles = new HashSet<>();
        roles.add(acampanteRole);
        user.setRoles(roles);

        // Create Acampante entity
        Acampante acampante = new Acampante(
                createRequest.getNombreCompleto(),
                createRequest.getEdad(),
                createRequest.getContactoEmergenciaNombre(),
                createRequest.getContactoEmergenciaTelefono()
        );
        acampante.setUserAccount(user); // Link the user account

        // Saving Acampante will cascade save User due to CascadeType.ALL
        return acampanteRepository.save(acampante);
    }

    public List<Acampante> getAllAcampantes() {
        return acampanteRepository.findAll();
    }

    public Optional<Acampante> getAcampanteById(Long id) {
        return acampanteRepository.findById(id);
    }

    @Transactional
    public Acampante updateAcampante(Long id, Acampante acampanteDetails) { // This DTO might need to be an UpdateRequest DTO
        Acampante existingAcampante = acampanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acampante not found with id: " + id));

        existingAcampante.setNombreCompleto(acampanteDetails.getNombreCompleto());
        existingAcampante.setEdad(acampanteDetails.getEdad());
        existingAcampante.setContactoEmergenciaNombre(acampanteDetails.getContactoEmergenciaNombre());
        existingAcampante.setContactoEmergenciaTelefono(acampanteDetails.getContactoEmergenciaTelefono());
        // Note: Updating User account details (username, password, roles) for an Acampante
        // would typically be a separate process, possibly through a UserService or dedicated endpoints.
        // This method focuses on Acampante entity fields.
        return acampanteRepository.save(existingAcampante);
    }

    @Transactional
    public void deleteAcampante(Long id) {
         if (!acampanteRepository.existsById(id)) {
            throw new RuntimeException("Acampante not found with id: " + id);
        }
        // Deleting Acampante will also delete associated User due to orphanRemoval=true
        acampanteRepository.deleteById(id);
    }
}

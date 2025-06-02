package com.cas.login.service;

import com.cas.login.model.Dirigente;
import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.DirigenteRepository;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository; // Needed for user creation
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Needed for encoding password
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors; // Required for stream operations if using loop fallback

@Service
public class DirigenteService {

    private final DirigenteRepository dirigenteRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DirigenteService(DirigenteRepository dirigenteRepository,
                            UserRepository userRepository,
                            RoleRepository roleRepository,
                            PasswordEncoder passwordEncoder) {
        this.dirigenteRepository = dirigenteRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Dirigente createDirigente(Dirigente dirigente, String rawPassword, Set<String> roleNames) {
        if (dirigente.getUserAccount() == null || dirigente.getUserAccount().getUsername() == null) {
            throw new IllegalArgumentException("User account and username must be provided for Dirigente.");
        }
        if (userRepository.findByUsername(dirigente.getUserAccount().getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + dirigente.getUserAccount().getUsername());
        }

        User user = dirigente.getUserAccount();
        user.setPassword(passwordEncoder.encode(rawPassword));

        // Assuming RoleRepository has findAllByNameIn method.
        // If not, this would require a loop and multiple findByName calls.
        Set<Role> roles = roleRepository.findAllByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
           // Identify which roles were not found for a more specific error message.
           Set<String> foundRoleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
           roleNames.removeAll(foundRoleNames);
           throw new RuntimeException("One or more roles not found: " + roleNames);
        }
        user.setRoles(roles);

        // User account is part of Dirigente and CascadeType.ALL is set on userAccount field in Dirigente entity.
        // So, saving Dirigente should cascade save to User.
        return dirigenteRepository.save(dirigente);
    }

    public List<Dirigente> getAllDirigentes() {
        return dirigenteRepository.findAll();
    }

    public Optional<Dirigente> getDirigenteById(Long id) {
        return dirigenteRepository.findById(id);
    }

    public Optional<Dirigente> getDirigenteByUsername(String username) {
        return dirigenteRepository.findByUserAccount_Username(username);
    }

    @Transactional
    public Dirigente updateDirigente(Long id, Dirigente dirigenteDetails) {
        Dirigente existingDirigente = dirigenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirigente not found with id: " + id));

        existingDirigente.setNombreCompleto(dirigenteDetails.getNombreCompleto());
        existingDirigente.setResponsabilidades(dirigenteDetails.getResponsabilidades());
        // User account details (username, password, roles) update would be more complex and typically handled
        // via a dedicated UserService or UserAccount management endpoint, often not directly through Dirigente update.
        return dirigenteRepository.save(existingDirigente);
    }

    @Transactional
    public void deleteDirigente(Long id) {
        if (!dirigenteRepository.existsById(id)) {
            throw new RuntimeException("Dirigente not found with id: " + id);
        }
        dirigenteRepository.deleteById(id); // User account should be deleted due to CascadeType.ALL and orphanRemoval=true on Dirigente.userAccount
    }
}

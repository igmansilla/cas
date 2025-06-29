package com.cas.login.service;

import com.cas.login.dto.AcampanteCreateRequest;
import com.cas.login.model.Acampante;
import com.cas.login.model.Dirigente;
import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.DirigenteRepository;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// import org.springframework.security.access.AccessDeniedException; // For more specific exception if needed

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirigenteService {

    private final DirigenteRepository dirigenteRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AcampanteService acampanteService; // Added AcampanteService

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

        Set<Role> roles = roleRepository.findAllByNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
           Set<String> foundRoleNames = roles.stream().map(Role::getName).collect(Collectors.toSet());
           roleNames.removeAll(foundRoleNames);
           throw new RuntimeException("One or more roles not found: " + roleNames);
        }
        user.setRoles(roles);
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
        return dirigenteRepository.save(existingDirigente);
    }

    @Transactional
    public void deleteDirigente(Long id) {
        if (!dirigenteRepository.existsById(id)) {
            throw new RuntimeException("Dirigente not found with id: " + id);
        }
        dirigenteRepository.deleteById(id);
    }

    @Transactional
    public Acampante createAcampanteForDirigente(AcampanteCreateRequest acampanteCreateRequest) {
        String currentDirigenteUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentDirigenteUser = userRepository.findByUsername(currentDirigenteUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Dirigente user not found: " + currentDirigenteUsername));

        // Optional: Check if currentDirigenteUser is actually a Dirigente by fetching Dirigente entity
        // dirigenteRepository.findByUserAccount_Id(currentDirigenteUser.getId())
        //    .orElseThrow(() -> new AccessDeniedException("Authenticated user is not a registered Dirigente."));


        Acampante newAcampante = acampanteService.createAcampante(acampanteCreateRequest);
        User newAcampanteUser = newAcampante.getUserAccount();

        if (newAcampanteUser == null) {
            throw new IllegalStateException("Acampante user account was not created successfully by AcampanteService.");
        }

        currentDirigenteUser.getSupervisedCampers().add(newAcampanteUser);
        // newAcampanteUser.getSupervisors().add(currentDirigenteUser); // This line is not needed due to mappedBy

        userRepository.save(currentDirigenteUser); // Save changes to the Dirigente's user entity

        return newAcampante;
    }
}

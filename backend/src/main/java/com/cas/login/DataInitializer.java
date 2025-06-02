package com.cas.login;

import com.cas.login.model.Dirigente;
import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository;
import com.cas.login.service.DirigenteService; // Using service for Dirigente creation
// import com.cas.login.service.AcampanteService; // If creating sample Acampantes
// import com.cas.login.model.Acampante; // If creating sample Acampantes

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // Added for transactional behavior

import java.util.Set;
import java.util.Arrays; // For Arrays.asList if converting to Set easily

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DirigenteService dirigenteService;
    // private final AcampanteService acampanteService; // Optional: if creating sample acampantes

    @Override
    @Transactional // It's good practice for data initialization involving multiple repository calls
    public void run(String... args) throws Exception {
        // Create Roles
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
        Role dirigenteRole = roleRepository.findByName("ROLE_DIRIGENTE").orElseGet(() -> roleRepository.save(new Role("ROLE_DIRIGENTE")));
        // Role acampanteRole = roleRepository.findByName("ROLE_ACAMPANTE").orElseGet(() -> roleRepository.save(new Role("ROLE_ACAMPANTE"))); // Even if Acampantes don't log in, the role might be used for other definitions

        // Create Admin User (if not exists)
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUserEntity = new User("admin", passwordEncoder.encode("adminpass"));
            adminUserEntity.setRoles(Set.of(adminRole));
            userRepository.save(adminUserEntity); // Save the user directly
            System.out.println("Created admin user: admin/adminpass with ROLE_ADMIN");
        }

        // Create Dirigente User (if not exists)
        // DirigenteService.createDirigente handles username check and user creation internally
        if (userRepository.findByUsername("dirigente1").isEmpty()) {
            // User object for Dirigente needs to be instantiated first
            User dirigenteUserAccount = new User();
            dirigenteUserAccount.setUsername("dirigente1");
            // Password will be set by DirigenteService.createDirigente

            Dirigente dirigente = new Dirigente("Juan Perez (Dirigente)", "Logistica Principal", dirigenteUserAccount);

            dirigenteService.createDirigente(dirigente, "dirigentepass", Set.of("ROLE_DIRIGENTE"));
            System.out.println("Created dirigente user: dirigente1/dirigentepass with ROLE_DIRIGENTE");
        }

        // Create another Dirigente who is also an Admin
        if (userRepository.findByUsername("superdirigente").isEmpty()) {
            User superDirigenteUserAccount = new User();
            superDirigenteUserAccount.setUsername("superdirigente");

            Dirigente superDirigente = new Dirigente("Super Admin Dirigente", "Overall Management", superDirigenteUserAccount);
            dirigenteService.createDirigente(superDirigente, "superpass", Set.of("ROLE_DIRIGENTE", "ROLE_ADMIN"));
            System.out.println("Created superdirigente user: superdirigente/superpass with ROLE_DIRIGENTE, ROLE_ADMIN");
        }

        // Optional: Create a sample Acampante record (does not involve user creation)
        // if (acampanteService != null && acampanteService.getAllAcampantes().isEmpty()) { // A simple check
        //     Acampante acampante1 = new Acampante("Laura Gomez", 10, "Maria Gomez", "123456789");
        //     acampanteService.createAcampante(acampante1);
        //     System.out.println("Created sample acampante: Laura Gomez");
        // }

        // Logic for removing the old "user" is intentionally omitted as per subtask note.
        // A proper migration strategy or manual cleanup is preferred for existing data.
    }
}

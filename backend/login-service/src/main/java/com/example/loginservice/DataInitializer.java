package com.example.loginservice;

import com.example.loginservice.model.User;
import com.example.loginservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if user already exists to avoid duplicates on restart
        if (userRepository.findByUsername("user").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("user");
            testUser.setPassword(passwordEncoder.encode("password")); // Encode the password
            testUser.setRoles("USER"); // Assign a role
            userRepository.save(testUser);
            System.out.println("Created test user: user/password");
        }

        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("adminpass")); // Encode the password
            adminUser.setRoles("ADMIN,USER"); // Assign multiple roles
            userRepository.save(adminUser);
            System.out.println("Created admin user: admin/adminpass");
        }
    }
}

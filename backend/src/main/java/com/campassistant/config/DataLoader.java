package com.campassistant.config;

import com.cas.login.model.Role;
import com.cas.login.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<String> roleNames = Arrays.asList("ROLE_USER", "ROLE_ADMIN", "ROLE_STAFF", "ROLE_ACAMPANTE", "ROLE_DIRIGENTE");

        for (String roleName : roleNames) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                System.out.println("Created role: " + roleName);
            }
        }
    }
}

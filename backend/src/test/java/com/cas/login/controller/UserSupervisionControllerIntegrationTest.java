package com.cas.login.controller;

import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository;
import com.cas.login.service.UserSupervisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserSupervisionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserSupervisionService userSupervisionService; // Para setup directo si es necesario

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User dirigente1, dirigente2, acampante1, acampante2, regularUser;
    private Role roleDirigente, roleAcampante, roleUser;

    @BeforeEach
    void setUp() {
        // Limpiar explícitamente para evitar problemas de orden de borrado con relaciones ManyToMany
        userRepository.findAll().forEach(user -> {
            user.getRoles().clear();
            user.getSupervisedCampers().clear();
            user.getSupervisors().clear();
            userRepository.save(user);
        });
        userRepository.deleteAll();
        roleRepository.deleteAll();


        roleDirigente = roleRepository.save(new Role("ROLE_DIRIGENTE"));
        roleAcampante = roleRepository.save(new Role("ROLE_ACAMPANTE"));
        roleUser = roleRepository.save(new Role("ROLE_USER"));

        dirigente1 = createUser("dirigente1", "password", roleDirigente);
        dirigente2 = createUser("dirigente2", "password", roleDirigente);
        acampante1 = createUser("acampante1", "password", roleAcampante);
        acampante2 = createUser("acampante2", "password", roleAcampante);
        regularUser = createUser("userNoRoles", "password", roleUser);
    }

    private User createUser(String username, String password, Role... roles) {
        User user = new User(username, passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(Set.of(roles)));
        return userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        // Asegurar limpieza después de cada test, especialmente de la tabla de unión
        userRepository.findAll().forEach(u -> {
            u.getSupervisedCampers().clear();
            u.getSupervisors().clear();
            userRepository.save(u);
        });
    }


    @Test
    @WithMockUser(username = "dirigente1", roles = {"DIRIGENTE"})
    void assignAcampanteToDirigente_asDirigente_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/supervision/dirigente/{dirigenteId}/assign/{acampanteId}", dirigente1.getId(), acampante1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Acampante asignado correctamente")));

        User fetchedDirigente = userRepository.findById(dirigente1.getId()).orElseThrow();
        User fetchedAcampante = userRepository.findById(acampante1.getId()).orElseThrow();
        assertTrue(fetchedDirigente.getSupervisedCampers().contains(fetchedAcampante));
        assertTrue(fetchedAcampante.getSupervisors().contains(fetchedDirigente));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void assignAcampanteToDirigente_asAdmin_shouldSucceed() throws Exception {
        mockMvc.perform(post("/api/supervision/dirigente/{dirigenteId}/assign/{acampanteId}", dirigente1.getId(), acampante1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "userNoRoles", roles = {"USER"})
    void assignAcampanteToDirigente_asUnrelatedUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/supervision/dirigente/{dirigenteId}/assign/{acampanteId}", dirigente1.getId(), acampante1.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dirigente2", authorities = {"ROLE_DIRIGENTE"}) // Dirigente 2 no es el dirigente1
    void assignAcampanteToDirigente_asOtherDirigente_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/api/supervision/dirigente/{dirigenteId}/assign/{acampanteId}", dirigente1.getId(), acampante1.getId()))
            .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "dirigente1", roles = {"DIRIGENTE"})
    void removeAcampanteFromDirigente_shouldSucceed() throws Exception {
        userSupervisionService.assignAcampanteToDirigente(dirigente1.getId(), acampante1.getId()); // Pre-asignar

        mockMvc.perform(delete("/api/supervision/dirigente/{dirigenteId}/remove/{acampanteId}", dirigente1.getId(), acampante1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Acampante removido correctamente")));

        User fetchedDirigente = userRepository.findById(dirigente1.getId()).orElseThrow();
        assertFalse(fetchedDirigente.getSupervisedCampers().contains(acampante1));
    }

    @Test
    @WithMockUser(username = "dirigente1", roles = {"DIRIGENTE"})
    void getSupervisedCampers_asDirigente_shouldReturnCampers() throws Exception {
        userSupervisionService.assignAcampanteToDirigente(dirigente1.getId(), acampante1.getId());
        userSupervisionService.assignAcampanteToDirigente(dirigente1.getId(), acampante2.getId());

        mockMvc.perform(get("/api/supervision/dirigente/{dirigenteId}/campers", dirigente1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.username == 'acampante1')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'acampante2')]").exists());
    }

    @Test
    @WithMockUser(username = "dirigente2", roles = {"DIRIGENTE"}) // Dirigente2 tratando de ver campistas de Dirigente1
    void getSupervisedCampers_asOtherDirigente_shouldBeForbidden() throws Exception {
        userSupervisionService.assignAcampanteToDirigente(dirigente1.getId(), acampante1.getId());

        mockMvc.perform(get("/api/supervision/dirigente/{dirigenteId}/campers", dirigente1.getId()))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "acampante1", roles = {"ACAMPANTE"})
    void getSupervisorsForAcampante_asAcampante_shouldReturnSupervisors() throws Exception {
        userSupervisionService.assignAcampanteToDirigente(dirigente1.getId(), acampante1.getId());
        userSupervisionService.assignAcampanteToDirigente(dirigente2.getId(), acampante1.getId());

        mockMvc.perform(get("/api/supervision/acampante/{acampanteId}/supervisors", acampante1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.username == 'dirigente1')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'dirigente2')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "STAFF"})
    void getAllDirigentes_asAdminOrStaff_shouldReturnAllDirigentes() throws Exception {
        mockMvc.perform(get("/api/supervision/dirigentes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // dirigente1, dirigente2
                .andExpect(jsonPath("$[?(@.username == 'dirigente1')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'dirigente2')]").exists());
    }

    @Test
    @WithMockUser(username = "userNoRoles", roles = {"USER"})
    void getAllDirigentes_asUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/supervision/dirigentes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "STAFF"})
    void getAllAcampantes_asAdminOrStaff_shouldReturnAllAcampantes() throws Exception {
        mockMvc.perform(get("/api/supervision/acampantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // acampante1, acampante2
                .andExpect(jsonPath("$[?(@.username == 'acampante1')]").exists())
                .andExpect(jsonPath("$[?(@.username == 'acampante2')]").exists());
    }

}

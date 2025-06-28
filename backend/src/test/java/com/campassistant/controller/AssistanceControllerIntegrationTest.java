package com.campassistant.controller;

import com.campassistant.model.Assistance;
import com.campassistant.repository.AssistanceRepository;
import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository;
import com.cas.login.service.UserSupervisionService; // Necesario para el setup
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.context.ActiveProfiles; // Removido para usar la DB principal de test
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Para asegurar que cada test corre en su propia transacción y se hace rollback
// @ActiveProfiles("test") // Removido para usar la DB principal de testeo configurada en application.properties
public class AssistanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssistanceRepository assistanceRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired // Inyectar UserSupervisionService
    private UserSupervisionService userSupervisionService;

    private ObjectMapper objectMapper;

    private User testUser1; // Cambiará a acampante1
    private User testUser2; // Cambiará a acampante2
    private User adminUser;
    private User dirigenteUser; // Nuevo dirigente para pruebas

    @BeforeEach
    void setUp() {
        // Limpiar explícitamente para evitar problemas de orden de borrado con relaciones ManyToMany
        // Esto es importante si las pruebas anteriores no limpiaron bien o si hay datos persistentes.
        // En un @Transactional, esto podría no ser estrictamente necesario si el rollback es completo.
        userRepository.findAll().forEach(user -> {
            user.getRoles().clear();
            user.getSupervisedCampers().clear();
            user.getSupervisors().clear();
            userRepository.save(user);
        });

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para serializar/deserializar LocalDate

        assistanceRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role roleUser = roleRepository.save(new Role("ROLE_USER")); // Será usado por acampantes también
        Role roleAdmin = roleRepository.save(new Role("ROLE_ADMIN"));
        Role roleStaff = roleRepository.save(new Role("ROLE_STAFF"));
        Role roleDirigente = roleRepository.save(new Role("ROLE_DIRIGENTE"));
        Role roleAcampante = roleRepository.save(new Role("ROLE_ACAMPANTE"));


        // Renombrar testUser1 y testUser2 a acampante1 y acampante2 para claridad
        testUser1 = new User("acampante1", passwordEncoder.encode("password"));
        testUser1.setRoles(new HashSet<>(Arrays.asList(roleUser, roleAcampante)));
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User("acampante2", passwordEncoder.encode("password"));
        testUser2.setRoles(new HashSet<>(Arrays.asList(roleUser, roleAcampante)));
        testUser2 = userRepository.save(testUser2);

        adminUser = new User("adminuser", passwordEncoder.encode("password"));
        adminUser.setRoles(new HashSet<>(Arrays.asList(roleAdmin, roleStaff, roleUser)));
        adminUser = userRepository.save(adminUser);

        dirigenteUser = new User("dirigente1", passwordEncoder.encode("password"));
        dirigenteUser.setRoles(new HashSet<>(Arrays.asList(roleUser, roleDirigente)));
        dirigenteUser = userRepository.save(dirigenteUser);
    }

    // Helper para crear usuario, si no existe uno global en la clase de test.
    private User createUser(String username, String password, Set<Role> roles) {
        User user = new User(username, passwordEncoder.encode(password));
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void recordAssistance_asAdmin_shouldCreateRecord() throws Exception {
        LocalDate date = LocalDate.now();
        AssistanceController.AssistanceRecordRequest request = new AssistanceController.AssistanceRecordRequest();
        request.setUserId(testUser1.getId());
        request.setDate(date);
        request.setHasAttended(true);

        mockMvc.perform(post("/api/assistance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(testUser1.getId().intValue())))
                .andExpect(jsonPath("$.date", is(date.toString())))
                .andExpect(jsonPath("$.hasAttended", is(true)));

        Optional<Assistance> savedAssistance = assistanceRepository.findByUserAndDate(testUser1, date);
        assertTrue(savedAssistance.isPresent());
        assertEquals(true, savedAssistance.get().isHasAttended());
    }

    @Test
    @WithMockUser(username = "testuser1", roles = {"USER"}) // Un usuario normal no debería poder grabar asistencia
    void recordAssistance_asUser_shouldBeForbidden() throws Exception {
        LocalDate date = LocalDate.now();
        AssistanceController.AssistanceRecordRequest request = new AssistanceController.AssistanceRecordRequest();
        request.setUserId(testUser1.getId());
        request.setDate(date);
        request.setHasAttended(true);

        mockMvc.perform(post("/api/assistance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "adminuser", roles = {"STAFF"}) // Staff sí puede
    void recordAssistance_asStaff_shouldCreateRecord() throws Exception {
        LocalDate date = LocalDate.now();
        AssistanceController.AssistanceRecordRequest request = new AssistanceController.AssistanceRecordRequest();
        request.setUserId(testUser1.getId());
        request.setDate(date);
        request.setHasAttended(true);

        mockMvc.perform(post("/api/assistance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }


    @Test
    @WithMockUser(username = "testuser1", roles = {"USER"})
    void getAssistance_forSelf_shouldReturnRecord() throws Exception {
        LocalDate date = LocalDate.now();
        Assistance assistance = new Assistance(null, testUser1, date, true);
        assistanceRepository.save(assistance);

        mockMvc.perform(get("/api/assistance/user/{userId}/date/{date}", testUser1.getId(), date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAttended", is(true)));
    }

    @Test
    @WithMockUser(username = "testuser2", roles = {"USER"}) // testuser2 intentando acceder a la asistencia de testuser1
    void getAssistance_forOtherUser_asUser_shouldReturnRecord() throws Exception {
        // La lógica actual del controller permite a cualquier USER autenticado ver la asistencia de otro
        // Si esto no es deseado, la anotación @PreAuthorize en el controller debería ser más restrictiva
        // por ejemplo: @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
        LocalDate date = LocalDate.now();
        Assistance assistance = new Assistance(null, testUser1, date, true);
        assistanceRepository.save(assistance);

        mockMvc.perform(get("/api/assistance/user/{userId}/date/{date}", testUser1.getId(), date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasAttended", is(true)));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void getAssistanceByDate_asAdmin_shouldReturnRecords() throws Exception {
        LocalDate date = LocalDate.now();
        assistanceRepository.save(new Assistance(null, testUser1, date, true));
        assistanceRepository.save(new Assistance(null, testUser2, date, false));

        mockMvc.perform(get("/api/assistance/date/{date}", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(testUser1.getId().intValue())))
                .andExpect(jsonPath("$[1].userId", is(testUser2.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void getAllAssistanceRecords_asAdmin_shouldReturnAllRecords() throws Exception {
        assistanceRepository.save(new Assistance(null, testUser1, LocalDate.now(), true));
        assistanceRepository.save(new Assistance(null, testUser2, LocalDate.now().minusDays(1), false));

        mockMvc.perform(get("/api/assistance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "testuser1", roles = {"USER"})
    void getAllAssistanceRecords_asUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/assistance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void deleteAssistance_asAdmin_shouldDeleteRecord() throws Exception {
        Assistance assistance = assistanceRepository.save(new Assistance(null, testUser1, LocalDate.now(), true));
        Long assistanceId = assistance.getId();

        mockMvc.perform(delete("/api/assistance/{assistanceId}", assistanceId))
                .andExpect(status().isNoContent());

        assertFalse(assistanceRepository.existsById(assistanceId));
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"STAFF"}) // Staff no debería poder borrar según la regla actual
    void deleteAssistance_asStaff_shouldBeForbidden() throws Exception {
        Assistance assistance = assistanceRepository.save(new Assistance(null, testUser1, LocalDate.now(), true));
        Long assistanceId = assistance.getId();

        mockMvc.perform(delete("/api/assistance/{assistanceId}", assistanceId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"})
    void getAssistanceForUsersOnDate_asAdmin_shouldReturnCorrectRecords() throws Exception {
        LocalDate date = LocalDate.now();
        assistanceRepository.save(new Assistance(null, testUser1, date, true));
        assistanceRepository.save(new Assistance(null, testUser2, date, false));
        // Otro usuario que no estará en la lista
        User otherUser = userRepository.save(new User("otheruser", "password"));
        assistanceRepository.save(new Assistance(null, otherUser, date, true));


        AssistanceController.UserAssistanceOnDateRequest request = new AssistanceController.UserAssistanceOnDateRequest();
        request.setUserIds(List.of(testUser1.getId(), testUser2.getId()));
        request.setDate(date);

        mockMvc.perform(post("/api/assistance/users-on-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.userId == %d && @.hasAttended == true)]", testUser1.getId().intValue()).exists())
                .andExpect(jsonPath("$[?(@.userId == %d && @.hasAttended == false)]", testUser2.getId().intValue()).exists());
    }

    @Test
    @WithMockUser(username = "dirigente1", roles = {"DIRIGENTE"})
    void getAssistanceForSupervisedCampers_asDirigente_shouldReturnCorrectAssistance() throws Exception {
        // Asignar acampantes al dirigenteUser (dirigente1)
        userSupervisionService.assignAcampanteToDirigente(dirigenteUser.getId(), testUser1.getId()); // acampante1
        userSupervisionService.assignAcampanteToDirigente(dirigenteUser.getId(), testUser2.getId()); // acampante2

        // Crear un tercer acampante no supervisado por este dirigente
        User acampante3 = createUser("acampante3", "password", new HashSet<>(Arrays.asList(roleRepository.findByName("ROLE_USER").get(), roleRepository.findByName("ROLE_ACAMPANTE").get())));

        LocalDate today = LocalDate.now();

        // Registrar asistencia
        assistanceRepository.save(new Assistance(null, testUser1, today, true));  // Supervisado, presente
        assistanceRepository.save(new Assistance(null, testUser2, today, false)); // Supervisado, ausente
        assistanceRepository.save(new Assistance(null, acampante3, today, true)); // No supervisado, presente

        mockMvc.perform(get("/api/assistance/dirigente/{dirigenteId}/supervised/date/{date}", dirigenteUser.getId(), today.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Solo los 2 supervisados
                .andExpect(jsonPath("$[?(@.userId == %d && @.hasAttended == true)]", testUser1.getId().intValue()).exists()) // acampante1 presente
                .andExpect(jsonPath("$[?(@.userId == %d && @.hasAttended == false)]", testUser2.getId().intValue()).exists()); // acampante2 ausente
    }

    @Test
    @WithMockUser(username = "otherdirigente", roles = {"DIRIGENTE"}) // Un dirigente diferente
    void getAssistanceForSupervisedCampers_asOtherDirigente_shouldBeForbidden() throws Exception {
        // No es el dirigenteUser (dirigente1) ni ADMIN
        mockMvc.perform(get("/api/assistance/dirigente/{dirigenteId}/supervised/date/{date}", dirigenteUser.getId(), LocalDate.now().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = {"ADMIN"}) // Admin sí puede acceder
    void getAssistanceForSupervisedCampers_asAdmin_shouldSucceed() throws Exception {
        userSupervisionService.assignAcampanteToDirigente(dirigenteUser.getId(), testUser1.getId());
        assistanceRepository.save(new Assistance(null, testUser1, LocalDate.now(), true));

        mockMvc.perform(get("/api/assistance/dirigente/{dirigenteId}/supervised/date/{date}", dirigenteUser.getId(), LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

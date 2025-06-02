package com.cas.login;

import com.cas.login.model.Role;
import com.cas.login.model.User;
import com.cas.login.repository.RoleRepository;
import com.cas.login.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
// import jakarta.persistence.EntityManager; // Removed flush

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensure tests are rolled back
class LoginServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	// @Autowired
	// private EntityManager entityManager; // Removed EntityManager

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ObjectMapper objectMapper; // For JSON comparison

	private User testUser;
	private User adminUser;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll(); // Clean up before each test
		roleRepository.deleteAll(); // Clean up roles as well

		// Save roles with test-specific names
		Role testUserRole = roleRepository.save(new Role("ROLE_TEST_USER"));
		Role testAdminRole = roleRepository.save(new Role("ROLE_TEST_ADMIN"));

		testUser = new User("testuser", passwordEncoder.encode("password"));
		testUser.setRoles(Set.of(testUserRole));
		userRepository.save(testUser);

		adminUser = new User("adminuser", passwordEncoder.encode("adminpass"));
		adminUser.setRoles(Set.of(testUserRole, testAdminRole));
		userRepository.save(adminUser);

		// entityManager.flush(); // Removed flush
	}

	@Test
	void contextLoads() {
	}

	@Test
	void testSuccessfulLoginReturnsJsonWithUserDetails() throws Exception {
		MvcResult result = mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "testuser")
						.param("password", "password")
						.with(csrf())) // Essential for POST requests if CSRF is enabled
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.username").value("testuser"))
				.andExpect(jsonPath("$.roles").isArray())
				.andExpect(jsonPath("$.roles[0]").value("ROLE_TEST_USER"))
				.andReturn();

		System.out.println("Login success response: " + result.getResponse().getContentAsString());
	}

	@Test
	void testFailedLoginReturnsUnauthorizedWithJsonError() throws Exception {
		MvcResult result = mockMvc.perform(post("/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("username", "testuser")
						.param("password", "wrongpassword")
						.with(csrf()))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.error").value("Authentication failed"))
				.andExpect(jsonPath("$.message").exists()) // Message can vary, just check existence
				.andReturn();
		System.out.println("Login failure response: " + result.getResponse().getContentAsString());
	}

	@Test
    void testSuccessfulLoginUsingSpringSecurityTestFormLogin() throws Exception {
        // This uses Spring Security Test's formLogin() helper which simplifies CSRF
        mockMvc.perform(formLogin("/login").user("adminuser").password("adminpass"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("adminuser"))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[?(@ == 'ROLE_TEST_ADMIN')]").exists())
            .andExpect(jsonPath("$.roles[?(@ == 'ROLE_TEST_USER')]").exists());
    }

    @Test
    void testFailedLoginUsingSpringSecurityTestFormLogin() throws Exception {
        mockMvc.perform(formLogin("/login").user("adminuser").password("wrongpass"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

}

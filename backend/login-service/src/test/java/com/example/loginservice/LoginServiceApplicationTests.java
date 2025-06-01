package com.example.loginservice;

import com.example.loginservice.model.Role;
import com.example.loginservice.model.User;
import com.example.loginservice.repository.UserRepository;
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
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ObjectMapper objectMapper; // For JSON comparison

	private User testUser;
	private User adminUser;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll(); // Clean up before each test

		Role userRole = new Role("ROLE_USER");
		Role adminRole = new Role("ROLE_ADMIN");
		// Note: In a real app, roles would likely be pre-populated or managed differently.
		// For simplicity, we assume roles could be created if not existing or handled by cascades if User owns them.
		// If Role entity has its own repository and needs to be saved, that should be done here.

		testUser = new User("testuser", passwordEncoder.encode("password"));
		testUser.setRoles(Set.of(userRole));
		userRepository.save(testUser);

		adminUser = new User("adminuser", passwordEncoder.encode("adminpass"));
		adminUser.setRoles(Set.of(userRole, adminRole));
		userRepository.save(adminUser);
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
				.andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
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
            .andExpect(jsonPath("$.roles[?(@ == 'ROLE_ADMIN')]").exists())
            .andExpect(jsonPath("$.roles[?(@ == 'ROLE_USER')]").exists());
    }

    @Test
    void testFailedLoginUsingSpringSecurityTestFormLogin() throws Exception {
        mockMvc.perform(formLogin("/login").user("adminuser").password("wrongpass"))
            .andExpect(status().isUnauthorized())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").value("Authentication failed"));
    }

}

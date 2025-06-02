package com.example.login.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensure test data is rolled back if any DB interaction happens via services
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "CAMPISTA"})
    void getUserDetails_whenAuthenticated_shouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[?(@ == 'ROLE_USER')]").exists())
                .andExpect(jsonPath("$.roles[?(@ == 'ROLE_CAMPISTA')]").exists());
    }

    @Test
    @WithMockUser(username = "anotheruser", authorities = {"ROLE_DIRIGENTE"}) // Using authorities directly
    void getUserDetails_whenAuthenticatedWithAuthorities_shouldReturnUserDetails() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("anotheruser"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_DIRIGENTE"));
    }

    @Test
    @WithAnonymousUser // Simulate an unauthenticated (anonymous) user
    void getUserDetails_whenUnauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Or isFound() if redirecting to login
        // If Spring Security redirects unauthenticated API access to login page (302),
        // then the expectation should be .andExpect(status().isFound())
        // .andExpect(redirectedUrlPattern("**/login"));
        // However, for a REST API, 401 is more appropriate.
        // The SecurityConfig directs to a 401 for form login failures,
        // and .anyRequest().authenticated() should also lead to 401 if not authenticated
        // for API endpoints if not caught by a login redirect.
        // The current setup should result in 401 because this is not a browser accessing,
        // but a direct API call.
    }
}

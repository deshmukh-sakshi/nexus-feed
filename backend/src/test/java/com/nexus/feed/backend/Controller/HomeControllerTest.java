package com.nexus.feed.backend.Controller;

import com.nexus.feed.backend.Auth.Service.JwtService;
import com.nexus.feed.backend.Auth.Service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeController.class, excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("HomeController Tests")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Should return welcome message for home endpoint")
    void shouldReturnWelcomeMessageForHomeEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("Nexus Feed Backend API is running!"));
    }

    @Test
    @DisplayName("Should return OK for health endpoint")
    void shouldReturnOkForHealthEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}

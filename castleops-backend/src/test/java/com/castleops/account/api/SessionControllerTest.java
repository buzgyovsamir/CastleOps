package com.castleops.account.api;

import com.castleops.account.RegisteredUser;
import com.castleops.account.RegistrationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SessionControllerTest {
    @Autowired
    RegistrationService registrationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldProvideCsrfTokenBeforeLogin() throws Exception {
        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").isNotEmpty())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldRecognizePlayerUntilLogout() throws Exception {
        String username = "SessionFlowTest";
        String password = "castleops session test";
        RegisteredUser registered = registrationService.register(username, password);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isNoContent())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) login.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(registered.id().toString()))
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}

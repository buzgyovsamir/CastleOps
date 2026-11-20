package com.castleops.account.api;

import com.castleops.account.RegistrationService;
import com.castleops.account.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegistrationControllerTest {
    @Autowired
    RegistrationService registrationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;

    private ResultActions register(String username, String password) throws Exception {
        String body = """
            {"username": "%s", "password": "%s"}
            """.formatted(username, password);

        return mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void shouldReturnCreatedWithPublicAccountDetails() throws Exception {
        register("ApiRegistrationTest", "castleops test password")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.username").value("ApiRegistrationTest"))
                .andExpect(jsonPath("$.password").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.passwordHash").doesNotHaveJsonPath());
    }

    @Test
    void shouldReturnConflictWhenUsernameDiffersOnlyByCase() throws Exception {
        register("ApiDuplicateTest", "castleops test password")
                .andExpect(status().isCreated());

        register("apiduplicatetest", "another test password")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username is already taken."));
    }

    @Test
    void shouldReturnBadRequestForShortPassword() throws Exception {
        register("ApiShortPasswordTest", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Password must contain at least 12 characters."));
    }
}

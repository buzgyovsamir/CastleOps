package com.castleops.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class RegistrationServiceTest {
    @Autowired
    RegistrationService registrationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    @Test
    void shouldRegisterUserWithHashedPassword() {
        String password = "castleops test password";

        RegisteredUser registered = registrationService.register("  Samir  ", password);

        UserAccount stored = userRepository.findByLoginName("samir").orElseThrow();

        assertThat(registered.id()).isEqualTo(stored.getId());
        assertThat(registered.username()).isEqualTo("Samir");
        assertThat(stored.getPasswordHash()).isNotEqualTo(password);
        assertThat(passwordEncoder.matches(password, stored.getPasswordHash())).isTrue();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRejectPasswordAboveBcryptByteLimit() {
        String password = "я".repeat(37);

        assertThatThrownBy(() -> registrationService.register("Samir", password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("72");

        assertThat(userRepository.findByLoginName("Samir")).isEmpty();
    }
}

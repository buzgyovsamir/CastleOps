package com.castleops.account;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
public class AccountAuthenticationTest {
    @Autowired
    RegistrationService registrationService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    PasswordEncoder passwordEncoder;


    @Test
    void shouldAuthenticateRegisteredPlayerIgnoringUsernameCase() {
        registrationService.register("LoginCaseTest", "castleops test password");

        Authentication result = authenticate("logincasetest", "castleops test password");

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getName()).isEqualTo("LoginCaseTest");
    }

    @Test
    void shouldRejectWrongPasswordForExistingPlayer() {
        registrationService.register("LoginPasswordTest", "castleops test password");

        assertThatThrownBy(() -> authenticate("LoginPasswordTest", "wrong password"))
                .isInstanceOf(BadCredentialsException.class);
    }



    private Authentication authenticate(String username, String password) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        var credentials = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        return provider.authenticate(credentials);
    }
}

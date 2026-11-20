package com.castleops.account;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class RegistrationService {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z0-9_]{3,32}");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUser register(String username, String password) {
        String cleanUsername = validateUsername(username);
        validatePassword(password);

        String passwordHash = passwordEncoder.encode(password);
        UserAccount account = new UserAccount(cleanUsername, passwordHash);
        UserAccount savedAccount = saveAccount(account);

        return new RegisteredUser(savedAccount.getId(), savedAccount.getUsername());
    }

    private String validateUsername(String username) {
        String cleanUsername = username == null ? "" : username.strip();

        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw new IllegalArgumentException(
                    "Username must contain 3 to 32 letters, digits or underscores."
            );
        }

        return cleanUsername;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()
                || password.codePointCount(0, password.length()) < 12) {
            throw new IllegalArgumentException(
                    "Password must contain at least 12 characters."
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException(
                    "Password must fit within 72 UTF-8 bytes."
            );
        }
    }

    private UserAccount saveAccount(UserAccount account) {
        try {
            return userRepository.saveAndFlush(account);
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "users_username_lower_uq".equals(violation.getConstraintName())) {
                    throw new UsernameAlreadyTakenException(exception);
                }
            }
            throw exception;
        }
    }
}
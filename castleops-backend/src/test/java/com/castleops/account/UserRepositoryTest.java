package com.castleops.account;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository users;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldLoadAccountByLoginNameIgnoringCase() {
        String username = uniqueUsername();
        String hash = "test-hash";

        UserAccount saved = users.saveAndFlush(new UserAccount(username, hash));
        UUID savedId = saved.getId();

        // Force the lookup to read the row written by PostgreSQL.
        entityManager.clear();

        UserAccount found = users.findByLoginName(username.toLowerCase(Locale.ROOT))
                .orElseThrow();

        assertAll(
                () -> assertNotNull(savedId),
                () -> assertEquals(savedId, found.getId()),
                () -> assertEquals(username, found.getUsername()),
                () -> assertEquals(hash, found.getPasswordHash()),
                () -> assertNotNull(found.getCreatedAt())
        );
    }

    @Test
    void shouldRejectDuplicateUsernameIgnoringCase() {
        String username = uniqueUsername();
        users.saveAndFlush(new UserAccount(username, "first-test-hash"));

        UserAccount duplicate = new UserAccount(
                username.toLowerCase(Locale.ROOT),
                "second-test-hash"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> users.saveAndFlush(duplicate)
        );
    }

    private String uniqueUsername() {
        return "Player_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
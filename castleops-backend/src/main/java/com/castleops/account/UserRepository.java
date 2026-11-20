package com.castleops.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    @Query("""
            select account
            from UserAccount account
            where lower(account.username) = lower(:username)
            """)
    Optional<UserAccount> findByLoginName(@Param("username") String username);
}
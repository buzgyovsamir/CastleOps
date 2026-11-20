package com.castleops.account;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AccountUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAccount account = userRepository.findByLoginName(username.strip())
                .orElseThrow(() -> new UsernameNotFoundException("Account not found."));

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles("PLAYER")
                .build();
    }
}

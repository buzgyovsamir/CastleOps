package com.castleops.account;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service
public class CurrentAccountService {

    UserRepository userRepository;

    public CurrentAccountService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public RegisteredUser findByPrincipal(Principal principal) {
        UserAccount account = userRepository.findByLoginName(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Account no longer exists."
                ));

        return new RegisteredUser(account.getId(), account.getUsername());
    }
}

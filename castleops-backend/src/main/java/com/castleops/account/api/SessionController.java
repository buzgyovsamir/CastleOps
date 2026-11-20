package com.castleops.account.api;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/auth")
public class SessionController {

    public record CsrfResponse(String headerName, String token) {}
    public record CurrentUserResponse(String username) {}

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(Principal principal) {
        return new CurrentUserResponse(principal.getName());
    }
}

package com.castleops.account.api;

import com.castleops.account.CurrentAccountService;
import com.castleops.account.RegisteredUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("api/auth")
public class SessionController {

    private final CurrentAccountService currentAccountService;

    public SessionController(CurrentAccountService currentAccountService) {
        this.currentAccountService = currentAccountService;
    }

    public record CsrfResponse(String headerName, String token) {}
    public record CurrentUserResponse(String username) {}

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    @GetMapping("/me")
    public RegisteredUser currentUser(Principal principal) {
        return currentAccountService.findByPrincipal(principal);
    }
}

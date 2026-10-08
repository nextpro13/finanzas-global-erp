package pe.finanzasglobal.divisas.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto.LoginRequest;
import pe.finanzasglobal.divisas.infrastructure.security.TokenService;

/** En la arquitectura completa este endpoint vive en un ms-auth detras del API Gateway. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger audit = LoggerFactory.getLogger("AUDITORIA");
    private final AuthenticationManager authManager;
    private final TokenService tokens;

    public AuthController(AuthenticationManager authManager, TokenService tokens) {
        this.authManager = authManager;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public TokenService.Token login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.usuario(), req.password()));
        audit.info("LOGIN_OK usuario={}", auth.getName());
        return tokens.generar(auth);
    }
}

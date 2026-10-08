package pe.finanzasglobal.divisas.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final SeguridadProperties props;

    public TokenService(JwtEncoder encoder, SeguridadProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    public record Token(String accessToken, String tokenType, long expiresIn) { }

    public Token generar(Authentication auth) {
        Instant ahora = Instant.now();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).map(r -> r.replace("ROLE_", "")).toList();
        String oficina = props.usuarios().stream().filter(u -> u.usuario().equals(auth.getName()))
                .map(SeguridadProperties.UsuarioDemo::oficina).findFirst().orElse("SIN-OFICINA");
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwtIssuer())
                .subject(auth.getName())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(props.jwtExpiracionMinutos(), ChronoUnit.MINUTES))
                .claim("roles", roles)
                .claim("oficina", oficina)
                .build();
        String jwt = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Token(jwt, "Bearer", props.jwtExpiracionMinutos() * 60);
    }
}

package pe.finanzasglobal.divisas.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.seguridad")
public record SeguridadProperties(String jwtSecret, String jwtIssuer, long jwtExpiracionMinutos,
                                  String demoPassword, List<UsuarioDemo> usuarios) {

    public record UsuarioDemo(String usuario, List<String> roles, String oficina) { }

    public SeguridadProperties {
        if (jwtSecret == null || jwtSecret.length() < 32)
            throw new IllegalStateException("app.seguridad.jwt-secret debe tener al menos 32 caracteres (HS256)");
        if (demoPassword == null || demoPassword.length() < 8)
            throw new IllegalStateException("app.seguridad.demo-password debe tener al menos 8 caracteres");
        usuarios = usuarios == null ? List.of() : usuarios;
    }
}

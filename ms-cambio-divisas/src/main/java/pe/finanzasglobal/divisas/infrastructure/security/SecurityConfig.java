package pe.finanzasglobal.divisas.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN", SUPERVISOR = "SUPERVISOR", CAJERO = "CAJERO", AUDITOR = "AUDITOR";

    /** Matriz de permisos centralizada. Todo lo no declarado se DENIEGA (deny by default). */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtConverter,
                                           RestSecurityHandlers handlers) throws Exception {
        http.csrf(c -> c.disable())                       // API stateless con JWT: no usa cookies de sesion
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/v1/tipos-cambio/*").hasAnyRole(ADMIN, SUPERVISOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/tipos-cambio/*/historial").hasAnyRole(ADMIN, SUPERVISOR, AUDITOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/tipos-cambio/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/operaciones/*/aprobar").hasRole(SUPERVISOR)
                .requestMatchers(HttpMethod.POST, "/api/v1/operaciones").hasAnyRole(CAJERO, SUPERVISOR)
                .requestMatchers(HttpMethod.GET, "/api/v1/operaciones/*").hasAnyRole(CAJERO, SUPERVISOR, ADMIN, AUDITOR)
                .anyRequest().denyAll())
            .oauth2ResourceServer(o -> o
                .jwt(j -> j.jwtAuthenticationConverter(jwtConverter))
                .authenticationEntryPoint(handlers)
                .accessDeniedHandler(handlers))
            .exceptionHandling(e -> e.authenticationEntryPoint(handlers).accessDeniedHandler(handlers))
            .headers(h -> h.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'")));
        return http.build();
    }

    @Bean
    public SecretKey jwtKey(SeguridadProperties props) {
        return new SecretKeySpec(props.jwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    /** Valida firma, expiracion y emisor del token. */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey key, SeguridadProperties props) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(props.jwtIssuer()));
        return decoder;
    }

    /** Convierte el claim "roles" del JWT en autoridades ROLE_*. */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    /** Usuarios de demostracion (en produccion: ms-usuarios / Keycloak). Contrasenas solo como hash BCrypt. */
    @Bean
    public UserDetailsService userDetailsService(SeguridadProperties props, PasswordEncoder encoder) {
        String hash = encoder.encode(props.demoPassword());
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        props.usuarios().forEach(u -> manager.createUser(User.withUsername(u.usuario())
                .password(hash).roles(u.roles().toArray(String[]::new)).build()));
        return manager;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }
}

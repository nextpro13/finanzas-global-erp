package pe.finanzasglobal.divisas.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Respuestas 401/403 en JSON (RFC 7807) y registro de intentos denegados. */
@Component
public class RestSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger audit = LoggerFactory.getLogger("AUDITORIA");
    private final ObjectMapper mapper;

    public RestSecurityHandlers(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        escribir(res, HttpStatus.UNAUTHORIZED, "Autenticacion requerida o token invalido", req);
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        String usuario = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : "anonimo";
        audit.warn("ACCESO_DENEGADO usuario={} metodo={} ruta={}", usuario, req.getMethod(), req.getRequestURI());
        escribir(res, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operacion", req);
    }

    private void escribir(HttpServletResponse res, HttpStatus status, String detalle, HttpServletRequest req)
            throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detalle);
        pd.setProperty("path", req.getRequestURI());
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(res.getOutputStream(), pd);
    }
}

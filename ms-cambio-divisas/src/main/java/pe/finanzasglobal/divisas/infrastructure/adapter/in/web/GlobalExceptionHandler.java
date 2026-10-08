package pe.finanzasglobal.divisas.infrastructure.adapter.in.web;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.finanzasglobal.divisas.domain.exception.RecursoNoEncontradoException;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Errores homogeneos en formato RFC 7807. Nunca expone trazas ni detalles internos. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos");
        pd.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    ProblemDetail reglaNegocio(ReglaNegocioException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** Dos peticiones simultaneas (misma Idempotency-Key o doble aprobacion): gana una, la otra recibe 409. */
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail conflicto(Exception ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operacion fue modificada o registrada por otra solicitud. Reintente la consulta.");
    }

    @ExceptionHandler(FeignException.class)
    ProblemDetail dependenciaCaida(FeignException ex) {
        log.error("Fallo al invocar ms-clientes: status={}", ex.status());
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Servicio de clientes no disponible. La operacion no fue registrada.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail credenciales(AuthenticationException ex) {
        LoggerFactory.getLogger("AUDITORIA").warn("LOGIN_FALLIDO motivo={}", ex.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accesoDenegado(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "No tiene permisos para esta operacion");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail inesperado(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Error no controlado errorId={}", errorId, ex);   // detalle solo en el log
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno. Codigo de referencia: " + errorId);
        pd.setProperty("errorId", errorId);
        return pd;
    }
}

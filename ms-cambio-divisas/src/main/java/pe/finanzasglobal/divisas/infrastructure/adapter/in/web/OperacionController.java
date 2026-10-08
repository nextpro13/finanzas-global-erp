package pe.finanzasglobal.divisas.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.finanzasglobal.divisas.application.port.in.AprobarOperacionUseCase;
import pe.finanzasglobal.divisas.application.port.in.ConsultarOperacionUseCase;
import pe.finanzasglobal.divisas.application.port.in.RegistrarOperacionUseCase;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto.OperacionResponse;
import pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto.RegistrarOperacionRequest;

import java.net.URI;

/** Adaptador de entrada REST: traduce HTTP <-> casos de uso. Sin logica de negocio. */
@RestController
@RequestMapping("/api/v1/operaciones")
public class OperacionController {

    private final RegistrarOperacionUseCase registrar;
    private final AprobarOperacionUseCase aprobar;
    private final ConsultarOperacionUseCase consultar;

    public OperacionController(RegistrarOperacionUseCase registrar, AprobarOperacionUseCase aprobar,
                               ConsultarOperacionUseCase consultar) {
        this.registrar = registrar;
        this.aprobar = aprobar;
        this.consultar = consultar;
    }

    @PostMapping
    public ResponseEntity<OperacionResponse> registrar(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RegistrarOperacionRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (!idempotencyKey.matches("[A-Za-z0-9-]{8,64}"))
            throw new ReglaNegocioException("Idempotency-Key invalida (8-64 caracteres alfanumericos o guion)");
        var op = registrar.registrar(new RegistrarOperacionUseCase.Comando(idempotencyKey, req.clienteId(),
                req.tipo(), req.moneda(), req.monto(), jwt.getSubject(), jwt.getClaimAsString("oficina")));
        return ResponseEntity.created(URI.create("/api/v1/operaciones/" + op.getId()))
                .body(OperacionResponse.de(op));
    }

    @GetMapping("/{id}")
    public OperacionResponse consultar(@PathVariable Long id) {
        return OperacionResponse.de(consultar.consultar(id));
    }

    @PostMapping("/{id}/aprobar")
    public OperacionResponse aprobar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return OperacionResponse.de(aprobar.aprobar(id, jwt.getSubject()));
    }
}

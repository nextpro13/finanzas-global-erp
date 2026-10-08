package pe.finanzasglobal.divisas.infrastructure.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.finanzasglobal.divisas.application.port.in.GestionarTipoCambioUseCase;
import pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto.TipoCambioRequest;
import pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto.TipoCambioResponse;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/tipos-cambio")
public class TipoCambioController {

    private final GestionarTipoCambioUseCase useCase;

    public TipoCambioController(GestionarTipoCambioUseCase useCase) { this.useCase = useCase; }

    @GetMapping("/{moneda}")
    public TipoCambioResponse vigente(@PathVariable String moneda) {
        return TipoCambioResponse.de(useCase.vigente(moneda.toUpperCase(Locale.ROOT)));
    }

    @GetMapping("/{moneda}/historial")
    public List<TipoCambioResponse> historial(@PathVariable String moneda) {
        return useCase.historial(moneda.toUpperCase(Locale.ROOT)).stream().map(TipoCambioResponse::de).toList();
    }

    @PutMapping("/{moneda}")
    public TipoCambioResponse actualizar(@PathVariable String moneda, @Valid @RequestBody TipoCambioRequest req,
                                         @AuthenticationPrincipal Jwt jwt) {
        return TipoCambioResponse.de(useCase.actualizar(moneda.toUpperCase(Locale.ROOT),
                req.compra(), req.venta(), jwt.getSubject()));
    }
}

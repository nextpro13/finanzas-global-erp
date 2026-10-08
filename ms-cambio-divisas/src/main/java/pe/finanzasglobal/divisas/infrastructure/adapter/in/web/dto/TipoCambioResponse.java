package pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto;

import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TipoCambioResponse(Long id, String moneda, BigDecimal compra, BigDecimal venta,
                                 String registradoPor, LocalDateTime fechaRegistro) {
    public static TipoCambioResponse de(TipoCambio t) {
        return new TipoCambioResponse(t.getId(), t.getMoneda(), t.getCompra(), t.getVenta(),
                t.getRegistradoPor(), t.getFechaRegistro());
    }
}

package pe.finanzasglobal.divisas.domain.event;

import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.util.UUID;

public record TipoCambioEvento(String eventId, String moneda, BigDecimal compra, BigDecimal venta,
                               String usuario, String fecha) {
    public static TipoCambioEvento de(TipoCambio tc) {
        return new TipoCambioEvento(UUID.randomUUID().toString(), tc.getMoneda(), tc.getCompra(),
                tc.getVenta(), tc.getRegistradoPor(), String.valueOf(tc.getFechaRegistro()));
    }
}

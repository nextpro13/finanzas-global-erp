package pe.finanzasglobal.divisas.domain.service;

import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Logica de calculo pura: sin frameworks, facil de probar. Usa BigDecimal (nunca double). */
public final class CalculadoraCambio {

    private static final int ESCALA = 2;
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private CalculadoraCambio() { }

    public record Resultado(BigDecimal montoSoles, BigDecimal comision, BigDecimal totalSoles) { }

    public static Resultado calcular(TipoOperacion tipo, BigDecimal montoDivisa,
                                     BigDecimal tasa, BigDecimal comisionPorcentaje) {
        if (tipo == null) throw new ReglaNegocioException("Tipo de operacion requerido");
        if (montoDivisa == null || montoDivisa.signum() <= 0)
            throw new ReglaNegocioException("El monto debe ser mayor a cero");
        if (montoDivisa.stripTrailingZeros().scale() > ESCALA)
            throw new ReglaNegocioException("El monto admite como maximo 2 decimales");

        BigDecimal soles = montoDivisa.multiply(tasa).setScale(ESCALA, RoundingMode.HALF_EVEN);
        BigDecimal comision = soles.multiply(comisionPorcentaje)
                .divide(CIEN, ESCALA, RoundingMode.HALF_EVEN);
        // COMPRA: el cliente recibe soles (se descuenta comision). VENTA: el cliente paga soles (+comision).
        BigDecimal total = tipo == TipoOperacion.COMPRA ? soles.subtract(comision) : soles.add(comision);
        return new Resultado(soles, comision, total);
    }
}

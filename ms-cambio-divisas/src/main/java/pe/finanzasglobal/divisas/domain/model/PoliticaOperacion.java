package pe.finanzasglobal.divisas.domain.model;

import java.math.BigDecimal;

/**
 * Condiciones comerciales vigentes.
 * @param comisionPorcentaje  comision aplicada sobre el importe en soles (ej. 0.50 = 0.5 %)
 * @param limiteSinAprobacion importe maximo en soles que se registra sin aprobacion de supervisor
 */
public record PoliticaOperacion(BigDecimal comisionPorcentaje, BigDecimal limiteSinAprobacion) {
    public PoliticaOperacion {
        if (comisionPorcentaje == null || comisionPorcentaje.signum() < 0)
            throw new IllegalArgumentException("Comision invalida");
        if (limiteSinAprobacion == null || limiteSinAprobacion.signum() <= 0)
            throw new IllegalArgumentException("Limite invalido");
    }
}

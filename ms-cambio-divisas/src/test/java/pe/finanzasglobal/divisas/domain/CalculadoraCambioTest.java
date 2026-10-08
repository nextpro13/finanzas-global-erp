package pe.finanzasglobal.divisas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;
import pe.finanzasglobal.divisas.domain.service.CalculadoraCambio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraCambioTest {

    private static final BigDecimal COMISION = new BigDecimal("0.50");

    @Test
    @DisplayName("VENTA: el cliente paga importe + comision")
    void ventaSumaComision() {
        var r = CalculadoraCambio.calcular(TipoOperacion.VENTA, new BigDecimal("1000"), new BigDecimal("3.7400"), COMISION);
        assertEquals(new BigDecimal("3740.00"), r.montoSoles());
        assertEquals(new BigDecimal("18.70"), r.comision());
        assertEquals(new BigDecimal("3758.70"), r.totalSoles());
    }

    @Test
    @DisplayName("COMPRA: el cliente recibe importe - comision")
    void compraRestaComision() {
        var r = CalculadoraCambio.calcular(TipoOperacion.COMPRA, new BigDecimal("1000"), new BigDecimal("3.7000"), COMISION);
        assertEquals(new BigDecimal("3700.00"), r.montoSoles());
        assertEquals(new BigDecimal("3681.50"), r.totalSoles());
    }

    @Test
    @DisplayName("Redondeo bancario HALF_EVEN a 2 decimales")
    void redondeoBancario() {
        var r = CalculadoraCambio.calcular(TipoOperacion.VENTA, new BigDecimal("0.01"), new BigDecimal("3.7450"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.04"), r.montoSoles()); // 0.03745 -> 0.04
    }

    @Test
    void rechazaMontoCeroONegativo() {
        assertThrows(ReglaNegocioException.class, () ->
                CalculadoraCambio.calcular(TipoOperacion.VENTA, BigDecimal.ZERO, BigDecimal.ONE, COMISION));
        assertThrows(ReglaNegocioException.class, () ->
                CalculadoraCambio.calcular(TipoOperacion.VENTA, new BigDecimal("-5"), BigDecimal.ONE, COMISION));
    }

    @Test
    void rechazaMasDeDosDecimales() {
        assertThrows(ReglaNegocioException.class, () ->
                CalculadoraCambio.calcular(TipoOperacion.COMPRA, new BigDecimal("10.001"), BigDecimal.ONE, COMISION));
    }

    @Test
    void rechazaTipoNulo() {
        assertThrows(ReglaNegocioException.class, () ->
                CalculadoraCambio.calcular(null, BigDecimal.TEN, BigDecimal.ONE, COMISION));
    }
}

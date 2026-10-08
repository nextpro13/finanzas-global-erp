package pe.finanzasglobal.divisas.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.TipoCambio;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TipoCambioTest {

    @Test
    void creaCotizacionValidaYAplicaTasaSegunOperacion() {
        TipoCambio tc = TipoCambio.nuevo("USD", new BigDecimal("3.70"), new BigDecimal("3.74"), "admin01");
        assertEquals(new BigDecimal("3.70"), tc.tasaPara(TipoOperacion.COMPRA));
        assertEquals(new BigDecimal("3.74"), tc.tasaPara(TipoOperacion.VENTA));
        assertEquals("admin01", tc.getRegistradoPor());
        assertNotNull(tc.getFechaRegistro());
    }

    @Test
    void compraNoPuedeSerMayorOIgualQueVenta() {
        assertThrows(ReglaNegocioException.class, () ->
                TipoCambio.nuevo("USD", new BigDecimal("3.80"), new BigDecimal("3.74"), "admin01"));
        assertThrows(ReglaNegocioException.class, () ->
                TipoCambio.nuevo("USD", new BigDecimal("3.74"), new BigDecimal("3.74"), "admin01"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"usd", "US", "DOLAR", "PEN", "U$D"})
    void rechazaMonedasInvalidas(String moneda) {
        assertThrows(ReglaNegocioException.class, () ->
                TipoCambio.nuevo(moneda, new BigDecimal("3.70"), new BigDecimal("3.74"), "admin01"));
    }

    @Test
    void rechazaValoresNoPositivosYUsuarioVacio() {
        assertThrows(ReglaNegocioException.class, () ->
                TipoCambio.nuevo("USD", BigDecimal.ZERO, new BigDecimal("3.74"), "admin01"));
        assertThrows(ReglaNegocioException.class, () ->
                TipoCambio.nuevo("USD", new BigDecimal("3.70"), new BigDecimal("3.74"), " "));
    }
}

package pe.finanzasglobal.divisas.domain;

import org.junit.jupiter.api.Test;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OperacionCambioTest {

    private final TipoCambio usd = new TipoCambio(7L, "USD", new BigDecimal("3.7000"),
            new BigDecimal("3.7400"), "admin01", LocalDateTime.now());
    private final PoliticaOperacion politica =
            new PoliticaOperacion(new BigDecimal("0.50"), new BigDecimal("30000.00"));

    @Test
    void operacionBajoLimiteQuedaRegistradaConTrazabilidad() {
        OperacionCambio op = OperacionCambio.registrar("k-1", 1L, TipoOperacion.VENTA,
                new BigDecimal("1000"), usd, politica, "cajero01", "LIMA-01");
        assertEquals(EstadoOperacion.REGISTRADA, op.getEstado());
        assertEquals(7L, op.getTipoCambioId());           // que cotizacion se uso
        assertEquals(new BigDecimal("3.7400"), op.getTasaAplicada());
        assertEquals("cajero01", op.getUsuario());
        assertEquals("LIMA-01", op.getOficina());
    }

    @Test
    void operacionSobreLimiteRequiereAprobacion() {
        OperacionCambio op = OperacionCambio.registrar("k-2", 1L, TipoOperacion.VENTA,
                new BigDecimal("10000"), usd, politica, "cajero01", "LIMA-01");
        assertEquals(EstadoOperacion.PENDIENTE_APROBACION, op.getEstado());
    }

    @Test
    void supervisorDistintoApruebaOperacionPendiente() {
        OperacionCambio op = OperacionCambio.registrar("k-3", 1L, TipoOperacion.VENTA,
                new BigDecimal("10000"), usd, politica, "supervisor01", "LIMA-01");
        op.aprobar("supervisor02");
        assertEquals(EstadoOperacion.APROBADA, op.getEstado());
        assertEquals("supervisor02", op.getAprobadoPor());
        assertNotNull(op.getFechaAprobacion());
    }

    @Test
    void nadieApruebaSuPropiaOperacion() {
        OperacionCambio op = OperacionCambio.registrar("k-4", 1L, TipoOperacion.VENTA,
                new BigDecimal("10000"), usd, politica, "supervisor01", "LIMA-01");
        assertThrows(ReglaNegocioException.class, () -> op.aprobar("supervisor01"));
    }

    @Test
    void noSeApruebaUnaOperacionQueNoEstaPendiente() {
        OperacionCambio op = OperacionCambio.registrar("k-5", 1L, TipoOperacion.COMPRA,
                new BigDecimal("100"), usd, politica, "cajero01", "LIMA-01");
        assertThrows(ReglaNegocioException.class, () -> op.aprobar("supervisor02"));
    }

    @Test
    void exigeUsuarioYOficina() {
        assertThrows(ReglaNegocioException.class, () -> OperacionCambio.registrar("k-6", 1L,
                TipoOperacion.COMPRA, BigDecimal.TEN, usd, politica, "cajero01", ""));
    }
}

package pe.finanzasglobal.divisas.application;

import org.junit.jupiter.api.Test;
import pe.finanzasglobal.divisas.application.port.out.EventoPublisherPort;
import pe.finanzasglobal.divisas.application.port.out.TipoCambioRepositoryPort;
import pe.finanzasglobal.divisas.application.service.TipoCambioService;
import pe.finanzasglobal.divisas.domain.event.TipoCambioEvento;
import pe.finanzasglobal.divisas.domain.exception.RecursoNoEncontradoException;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TipoCambioServiceTest {

    private final TipoCambioRepositoryPort repo = mock(TipoCambioRepositoryPort.class);
    private final EventoPublisherPort eventos = mock(EventoPublisherPort.class);
    private final TipoCambioService servicio = new TipoCambioService(repo, eventos);

    @Test
    void actualizarInsertaNuevaCotizacionYPublicaEvento() {
        when(repo.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        TipoCambio tc = servicio.actualizar("USD", new BigDecimal("3.71"), new BigDecimal("3.75"), "admin01");
        assertEquals("admin01", tc.getRegistradoPor());
        verify(eventos).publicar(any(TipoCambioEvento.class));
    }

    @Test
    void cotizacionInvalidaNoSePersiste() {
        assertThrows(ReglaNegocioException.class, () ->
                servicio.actualizar("USD", new BigDecimal("3.90"), new BigDecimal("3.75"), "admin01"));
        verify(repo, never()).guardar(any());
    }

    @Test
    void vigenteInexistenteYHistorial() {
        when(repo.vigente("EUR")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.vigente("EUR"));
        when(repo.historial("USD")).thenReturn(List.of());
        assertTrue(servicio.historial("USD").isEmpty());
        assertThrows(ReglaNegocioException.class, () -> servicio.historial("usd"));
    }
}

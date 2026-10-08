package pe.finanzasglobal.divisas.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.finanzasglobal.divisas.application.port.in.RegistrarOperacionUseCase.Comando;
import pe.finanzasglobal.divisas.application.port.out.*;
import pe.finanzasglobal.divisas.application.service.OperacionCambioService;
import pe.finanzasglobal.divisas.domain.event.OperacionCambioEvento;
import pe.finanzasglobal.divisas.domain.exception.RecursoNoEncontradoException;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Prueba del caso de uso con puertos simulados: sin base de datos, sin red, sin Spring. */
class OperacionCambioServiceTest {

    private OperacionRepositoryPort operaciones;
    private TipoCambioRepositoryPort tipos;
    private ClienteConsultaPort clientes;
    private EventoPublisherPort eventos;
    private OperacionCambioService servicio;

    private final TipoCambio usd = new TipoCambio(1L, "USD", new BigDecimal("3.70"),
            new BigDecimal("3.74"), "admin01", LocalDateTime.now());

    @BeforeEach
    void setUp() {
        operaciones = mock(OperacionRepositoryPort.class);
        tipos = mock(TipoCambioRepositoryPort.class);
        clientes = mock(ClienteConsultaPort.class);
        eventos = mock(EventoPublisherPort.class);
        servicio = new OperacionCambioService(operaciones, tipos, clientes, eventos,
                new PoliticaOperacion(new BigDecimal("0.50"), new BigDecimal("30000")));
        when(operaciones.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Comando comando(String key, long cliente) {
        return new Comando(key, cliente, TipoOperacion.VENTA, "USD", new BigDecimal("500"), "cajero01", "LIMA-01");
    }

    @Test
    void registraOperacionYPublicaEvento() {
        when(operaciones.buscarPorIdempotencyKey("k1")).thenReturn(Optional.empty());
        when(clientes.buscar(1L)).thenReturn(Optional.of(new ClienteInfo(1L, "ACME SAC", true)));
        when(tipos.vigente("USD")).thenReturn(Optional.of(usd));

        OperacionCambio op = servicio.registrar(comando("k1", 1L));

        assertEquals(new BigDecimal("1879.35"), op.getTotalSoles()); // 1870.00 + 9.35
        verify(operaciones).guardar(any());
        verify(eventos).publicar(any(OperacionCambioEvento.class));
    }

    @Test
    void reintentoConMismaClaveNoDuplicaOperacion() {
        OperacionCambio existente = OperacionCambio.registrar("k1", 1L, TipoOperacion.VENTA,
                new BigDecimal("500"), usd, new PoliticaOperacion(BigDecimal.ZERO, BigDecimal.TEN.pow(6)),
                "cajero01", "LIMA-01");
        when(operaciones.buscarPorIdempotencyKey("k1")).thenReturn(Optional.of(existente));

        assertSame(existente, servicio.registrar(comando("k1", 1L)));
        verify(operaciones, never()).guardar(any());
        verify(eventos, never()).publicar(any(OperacionCambioEvento.class));
    }

    @Test
    void clienteInhabilitadoNoPuedeOperar() {
        when(operaciones.buscarPorIdempotencyKey("k2")).thenReturn(Optional.empty());
        when(clientes.buscar(2L)).thenReturn(Optional.of(new ClienteInfo(2L, "MOROSO SAC", false)));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(comando("k2", 2L)));
        verify(operaciones, never()).guardar(any());
    }

    @Test
    void clienteInexistenteDevuelveNoEncontrado() {
        when(operaciones.buscarPorIdempotencyKey("k3")).thenReturn(Optional.empty());
        when(clientes.buscar(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(comando("k3", 99L)));
    }

    @Test
    void sinTipoDeCambioVigenteNoSeOpera() {
        when(operaciones.buscarPorIdempotencyKey("k4")).thenReturn(Optional.empty());
        when(clientes.buscar(1L)).thenReturn(Optional.of(new ClienteInfo(1L, "ACME SAC", true)));
        when(tipos.vigente("USD")).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrar(comando("k4", 1L)));
    }

    @Test
    void aprobarOperacionInexistenteLanzaNoEncontrado() {
        when(operaciones.buscarPorId(5L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.aprobar(5L, "supervisor02"));
    }

    @Test
    void apruebaOperacionPendienteYPublicaEvento() {
        OperacionCambio pendiente = OperacionCambio.registrar("k5", 1L, TipoOperacion.VENTA,
                new BigDecimal("10000"), usd, new PoliticaOperacion(BigDecimal.ZERO, new BigDecimal("30000")),
                "cajero01", "LIMA-01");
        when(operaciones.buscarPorId(5L)).thenReturn(Optional.of(pendiente));

        assertEquals(EstadoOperacion.APROBADA, servicio.aprobar(5L, "supervisor02").getEstado());
        verify(eventos).publicar(any(OperacionCambioEvento.class));
    }
}

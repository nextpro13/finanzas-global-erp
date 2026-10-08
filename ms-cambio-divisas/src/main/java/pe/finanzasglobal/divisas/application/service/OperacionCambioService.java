package pe.finanzasglobal.divisas.application.service;

import pe.finanzasglobal.divisas.application.port.in.AprobarOperacionUseCase;
import pe.finanzasglobal.divisas.application.port.in.ConsultarOperacionUseCase;
import pe.finanzasglobal.divisas.application.port.in.RegistrarOperacionUseCase;
import pe.finanzasglobal.divisas.application.port.out.ClienteConsultaPort;
import pe.finanzasglobal.divisas.application.port.out.EventoPublisherPort;
import pe.finanzasglobal.divisas.application.port.out.OperacionRepositoryPort;
import pe.finanzasglobal.divisas.application.port.out.TipoCambioRepositoryPort;
import pe.finanzasglobal.divisas.domain.event.OperacionCambioEvento;
import pe.finanzasglobal.divisas.domain.exception.RecursoNoEncontradoException;
import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.model.ClienteInfo;
import pe.finanzasglobal.divisas.domain.model.OperacionCambio;
import pe.finanzasglobal.divisas.domain.model.PoliticaOperacion;
import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.util.Optional;

/** Orquesta los casos de uso. No conoce HTTP, JPA, Feign ni RabbitMQ: solo puertos. */
public class OperacionCambioService
        implements RegistrarOperacionUseCase, AprobarOperacionUseCase, ConsultarOperacionUseCase {

    private final OperacionRepositoryPort operaciones;
    private final TipoCambioRepositoryPort tiposCambio;
    private final ClienteConsultaPort clientes;
    private final EventoPublisherPort eventos;
    private final PoliticaOperacion politica;

    public OperacionCambioService(OperacionRepositoryPort operaciones, TipoCambioRepositoryPort tiposCambio,
                                  ClienteConsultaPort clientes, EventoPublisherPort eventos,
                                  PoliticaOperacion politica) {
        this.operaciones = operaciones;
        this.tiposCambio = tiposCambio;
        this.clientes = clientes;
        this.eventos = eventos;
        this.politica = politica;
    }

    @Override
    public OperacionCambio registrar(Comando c) {
        // 1. Idempotencia: un reintento con la misma clave devuelve la operacion original (evita duplicados)
        Optional<OperacionCambio> previa = operaciones.buscarPorIdempotencyKey(c.idempotencyKey());
        if (previa.isPresent()) {
            return previa.get();
        }
        // 2. Validacion sincrona del cliente (OpenFeign -> ms-clientes)
        ClienteInfo cliente = clientes.buscar(c.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + c.clienteId()));
        if (!cliente.habilitado()) {
            throw new ReglaNegocioException("El cliente " + c.clienteId() + " no esta habilitado para operar");
        }
        // 3. Cotizacion vigente unica para todas las oficinas
        TipoCambio.validarMoneda(c.moneda());
        TipoCambio tc = tiposCambio.vigente(c.moneda())
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay tipo de cambio vigente para " + c.moneda()));
        // 4. Reglas de dominio + persistencia
        OperacionCambio op = OperacionCambio.registrar(c.idempotencyKey(), c.clienteId(), c.tipo(), c.monto(),
                tc, politica, c.usuario(), c.oficina());
        OperacionCambio guardada = operaciones.guardar(op);
        // 5. Evento asincrono (notificaciones, auditoria, indicadores)
        eventos.publicar(OperacionCambioEvento.de("OPERACION_REGISTRADA", guardada));
        return guardada;
    }

    @Override
    public OperacionCambio aprobar(Long operacionId, String supervisor) {
        OperacionCambio op = consultar(operacionId);
        op.aprobar(supervisor);
        OperacionCambio guardada = operaciones.guardar(op);
        eventos.publicar(OperacionCambioEvento.de("OPERACION_APROBADA", guardada));
        return guardada;
    }

    @Override
    public OperacionCambio consultar(Long operacionId) {
        return operaciones.buscarPorId(operacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Operacion no encontrada: " + operacionId));
    }
}

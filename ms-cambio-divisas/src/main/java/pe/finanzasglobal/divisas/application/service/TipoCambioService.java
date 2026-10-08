package pe.finanzasglobal.divisas.application.service;

import pe.finanzasglobal.divisas.application.port.in.GestionarTipoCambioUseCase;
import pe.finanzasglobal.divisas.application.port.out.EventoPublisherPort;
import pe.finanzasglobal.divisas.application.port.out.TipoCambioRepositoryPort;
import pe.finanzasglobal.divisas.domain.event.TipoCambioEvento;
import pe.finanzasglobal.divisas.domain.exception.RecursoNoEncontradoException;
import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.util.List;

public class TipoCambioService implements GestionarTipoCambioUseCase {

    private final TipoCambioRepositoryPort repositorio;
    private final EventoPublisherPort eventos;

    public TipoCambioService(TipoCambioRepositoryPort repositorio, EventoPublisherPort eventos) {
        this.repositorio = repositorio;
        this.eventos = eventos;
    }

    /** No se sobrescribe la cotizacion anterior: se inserta una nueva (historial + autor). */
    @Override
    public TipoCambio actualizar(String moneda, BigDecimal compra, BigDecimal venta, String usuario) {
        TipoCambio guardado = repositorio.guardar(TipoCambio.nuevo(moneda, compra, venta, usuario));
        eventos.publicar(TipoCambioEvento.de(guardado));
        return guardado;
    }

    @Override
    public TipoCambio vigente(String moneda) {
        TipoCambio.validarMoneda(moneda);
        return repositorio.vigente(moneda)
                .orElseThrow(() -> new RecursoNoEncontradoException("No hay tipo de cambio vigente para " + moneda));
    }

    @Override
    public List<TipoCambio> historial(String moneda) {
        TipoCambio.validarMoneda(moneda);
        return repositorio.historial(moneda);
    }
}

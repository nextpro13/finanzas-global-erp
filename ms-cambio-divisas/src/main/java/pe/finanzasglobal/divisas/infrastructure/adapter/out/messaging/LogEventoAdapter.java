package pe.finanzasglobal.divisas.infrastructure.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.EventoPublisherPort;
import pe.finanzasglobal.divisas.domain.event.OperacionCambioEvento;
import pe.finanzasglobal.divisas.domain.event.TipoCambioEvento;

/** Implementacion para desarrollo/pruebas sin broker: deja el evento en el log de auditoria. */
@Component
@ConditionalOnProperty(name = "app.mensajeria.modo", havingValue = "log", matchIfMissing = true)
public class LogEventoAdapter implements EventoPublisherPort {

    private static final Logger log = LoggerFactory.getLogger("AUDITORIA");

    @Override
    public void publicar(OperacionCambioEvento e) { log.info("EVENTO {}", e); }

    @Override
    public void publicar(TipoCambioEvento e) { log.info("EVENTO TIPO_CAMBIO_ACTUALIZADO {}", e); }
}

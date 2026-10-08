package pe.finanzasglobal.divisas.infrastructure.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.EventoPublisherPort;
import pe.finanzasglobal.divisas.domain.event.OperacionCambioEvento;
import pe.finanzasglobal.divisas.domain.event.TipoCambioEvento;
import pe.finanzasglobal.divisas.infrastructure.config.RabbitConfig;

/** Comunicacion ASINCRONA: publica y no espera a los consumidores. */
@Component
@ConditionalOnProperty(name = "app.mensajeria.modo", havingValue = "rabbit")
public class RabbitEventoAdapter implements EventoPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventoAdapter.class);
    private final RabbitTemplate rabbit;

    public RabbitEventoAdapter(RabbitTemplate rabbit) { this.rabbit = rabbit; }

    @Override
    public void publicar(OperacionCambioEvento e) {
        enviar("divisas.operacion." + e.tipoEvento().toLowerCase(), e, e.eventId());
    }

    @Override
    public void publicar(TipoCambioEvento e) {
        enviar("divisas.tipocambio.actualizado", e, e.eventId());
    }

    private void enviar(String routingKey, Object evento, String eventId) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, evento, m -> {
                m.getMessageProperties().setMessageId(eventId);                       // deduplicacion
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT); // sobrevive reinicios
                return m;
            });
        } catch (AmqpException ex) {
            // La operacion financiera ya se registro: no se revierte por una caida del broker.
            // Mejora planificada: patron Transactional Outbox para reintentar el envio.
            log.error("No se pudo publicar evento {} ({}): {}", eventId, routingKey, ex.getMessage());
        }
    }
}

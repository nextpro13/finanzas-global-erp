package pe.finanzasglobal.divisas.application.port.out;

import pe.finanzasglobal.divisas.domain.event.OperacionCambioEvento;
import pe.finanzasglobal.divisas.domain.event.TipoCambioEvento;

/** Puerto de salida para eventos asincronos (implementado con RabbitMQ). */
public interface EventoPublisherPort {
    void publicar(OperacionCambioEvento evento);
    void publicar(TipoCambioEvento evento);
}

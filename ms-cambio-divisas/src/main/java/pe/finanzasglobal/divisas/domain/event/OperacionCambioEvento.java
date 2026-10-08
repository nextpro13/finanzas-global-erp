package pe.finanzasglobal.divisas.domain.event;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Evento de integracion. eventId permite a los consumidores descartar duplicados. */
public record OperacionCambioEvento(String eventId, String tipoEvento, Long operacionId, Long clienteId,
                                    String tipo, String moneda, BigDecimal montoDivisa, BigDecimal totalSoles,
                                    String estado, String usuario, String oficina, String fecha) {

    public static OperacionCambioEvento de(String tipoEvento, OperacionCambio op) {
        String actor = op.getAprobadoPor() != null ? op.getAprobadoPor() : op.getUsuario();
        return new OperacionCambioEvento(UUID.randomUUID().toString(), tipoEvento, op.getId(), op.getClienteId(),
                op.getTipo().name(), op.getMoneda(), op.getMontoDivisa(), op.getTotalSoles(),
                op.getEstado().name(), actor, op.getOficina(), LocalDateTime.now().toString());
    }
}

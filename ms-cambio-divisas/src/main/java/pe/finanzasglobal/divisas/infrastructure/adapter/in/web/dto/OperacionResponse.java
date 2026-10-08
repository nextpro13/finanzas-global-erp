package pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperacionResponse(Long id, Long clienteId, String tipo, String moneda, BigDecimal montoDivisa,
                                BigDecimal tasaAplicada, Long tipoCambioId, BigDecimal montoSoles,
                                BigDecimal comision, BigDecimal totalSoles, String estado, String usuario,
                                String oficina, LocalDateTime fechaRegistro, String aprobadoPor,
                                LocalDateTime fechaAprobacion) {
    public static OperacionResponse de(OperacionCambio o) {
        return new OperacionResponse(o.getId(), o.getClienteId(), o.getTipo().name(), o.getMoneda(),
                o.getMontoDivisa(), o.getTasaAplicada(), o.getTipoCambioId(), o.getMontoSoles(), o.getComision(),
                o.getTotalSoles(), o.getEstado().name(), o.getUsuario(), o.getOficina(), o.getFechaRegistro(),
                o.getAprobadoPor(), o.getFechaAprobacion());
    }
}

package pe.finanzasglobal.divisas.application.port.in;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;

import java.math.BigDecimal;

public interface RegistrarOperacionUseCase {

    record Comando(String idempotencyKey, Long clienteId, TipoOperacion tipo, String moneda,
                   BigDecimal monto, String usuario, String oficina) { }

    OperacionCambio registrar(Comando comando);
}

package pe.finanzasglobal.divisas.application.port.out;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;

import java.util.Optional;

public interface OperacionRepositoryPort {
    OperacionCambio guardar(OperacionCambio operacion);
    Optional<OperacionCambio> buscarPorId(Long id);
    Optional<OperacionCambio> buscarPorIdempotencyKey(String key);
}

package pe.finanzasglobal.divisas.application.port.out;

import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.util.List;
import java.util.Optional;

public interface TipoCambioRepositoryPort {
    TipoCambio guardar(TipoCambio tipoCambio);
    Optional<TipoCambio> vigente(String moneda);
    List<TipoCambio> historial(String moneda);
}

package pe.finanzasglobal.divisas.application.port.in;

import pe.finanzasglobal.divisas.domain.model.TipoCambio;

import java.math.BigDecimal;
import java.util.List;

public interface GestionarTipoCambioUseCase {
    TipoCambio actualizar(String moneda, BigDecimal compra, BigDecimal venta, String usuario);
    TipoCambio vigente(String moneda);
    List<TipoCambio> historial(String moneda);
}

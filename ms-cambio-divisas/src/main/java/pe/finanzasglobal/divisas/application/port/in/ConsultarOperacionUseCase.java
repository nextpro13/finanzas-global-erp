package pe.finanzasglobal.divisas.application.port.in;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;

public interface ConsultarOperacionUseCase {
    OperacionCambio consultar(Long operacionId);
}

package pe.finanzasglobal.divisas.application.port.in;

import pe.finanzasglobal.divisas.domain.model.OperacionCambio;

public interface AprobarOperacionUseCase {
    OperacionCambio aprobar(Long operacionId, String supervisor);
}

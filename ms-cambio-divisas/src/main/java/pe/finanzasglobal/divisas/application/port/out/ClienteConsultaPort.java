package pe.finanzasglobal.divisas.application.port.out;

import pe.finanzasglobal.divisas.domain.model.ClienteInfo;

import java.util.Optional;

/** Puerto de salida hacia ms-clientes (implementado con OpenFeign o con un adaptador local). */
public interface ClienteConsultaPort {
    Optional<ClienteInfo> buscar(Long clienteId);
}

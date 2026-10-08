package pe.finanzasglobal.divisas.infrastructure.adapter.out.feign;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.ClienteConsultaPort;
import pe.finanzasglobal.divisas.domain.model.ClienteInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Adaptador alternativo para desarrollo y pruebas sin ms-clientes levantado.
 * Demuestra la ventaja hexagonal: se cambia la implementacion sin tocar el caso de uso.
 */
@Component
@ConditionalOnProperty(name = "app.integraciones.clientes.modo", havingValue = "local", matchIfMissing = true)
public class ClienteLocalAdapter implements ClienteConsultaPort {

    private static final Map<Long, ClienteInfo> CLIENTES = Map.of(
            1L, new ClienteInfo(1L, "IMPORTADORA ANDINA SAC", true),
            2L, new ClienteInfo(2L, "COMERCIAL MOROSA EIRL", false),
            3L, new ClienteInfo(3L, "JUAN PEREZ", true));

    @Override
    public Optional<ClienteInfo> buscar(Long clienteId) {
        return Optional.ofNullable(CLIENTES.get(clienteId));
    }
}

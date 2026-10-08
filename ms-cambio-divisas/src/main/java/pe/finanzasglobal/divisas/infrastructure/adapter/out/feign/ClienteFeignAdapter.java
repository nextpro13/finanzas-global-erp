package pe.finanzasglobal.divisas.infrastructure.adapter.out.feign;

import feign.FeignException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.finanzasglobal.divisas.application.port.out.ClienteConsultaPort;
import pe.finanzasglobal.divisas.domain.model.ClienteInfo;

import java.util.Optional;

/** Comunicacion SINCRONA: se necesita la respuesta de ms-clientes antes de continuar. */
@Component
@ConditionalOnProperty(name = "app.integraciones.clientes.modo", havingValue = "feign")
public class ClienteFeignAdapter implements ClienteConsultaPort {

    private final ClienteFeignClient client;

    public ClienteFeignAdapter(ClienteFeignClient client) { this.client = client; }

    @Override
    public Optional<ClienteInfo> buscar(Long clienteId) {
        try {
            ClienteResponse r = client.obtener(clienteId);
            return Optional.of(new ClienteInfo(r.id(), r.razonSocial(), "ACTIVO".equalsIgnoreCase(r.estado())));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
        // Otros errores (timeout, 5xx) se propagan: el handler global responde 503 y NO se registra la operacion.
    }
}

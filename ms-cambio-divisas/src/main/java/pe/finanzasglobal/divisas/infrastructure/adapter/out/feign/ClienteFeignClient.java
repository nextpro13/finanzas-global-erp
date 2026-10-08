package pe.finanzasglobal.divisas.infrastructure.adapter.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-clientes", url = "${app.integraciones.clientes.url:http://localhost:8081}")
public interface ClienteFeignClient {
    @GetMapping("/api/v1/clientes/{id}")
    ClienteResponse obtener(@PathVariable("id") Long id);
}

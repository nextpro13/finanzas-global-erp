package pe.finanzasglobal.divisas.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    /** Propaga el JWT del usuario a ms-clientes: el servicio destino tambien valida identidad y permisos. */
    @Bean
    public RequestInterceptor propagarToken() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                String auth = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null) template.header(HttpHeaders.AUTHORIZATION, auth);
            }
        };
    }
}

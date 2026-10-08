package pe.finanzasglobal.divisas.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.finanzasglobal.divisas.application.port.out.*;
import pe.finanzasglobal.divisas.application.service.OperacionCambioService;
import pe.finanzasglobal.divisas.application.service.TipoCambioService;
import pe.finanzasglobal.divisas.domain.model.PoliticaOperacion;

import java.math.BigDecimal;

/** Aqui se "conecta" el hexagono: los casos de uso no llevan anotaciones de Spring. */
@Configuration
public class BeanConfig {

    @Bean
    public PoliticaOperacion politicaOperacion(
            @Value("${app.negocio.comision-porcentaje}") BigDecimal comision,
            @Value("${app.negocio.limite-sin-aprobacion}") BigDecimal limite) {
        return new PoliticaOperacion(comision, limite);
    }

    @Bean
    public OperacionCambioService operacionCambioService(OperacionRepositoryPort operaciones,
                                                         TipoCambioRepositoryPort tipos,
                                                         ClienteConsultaPort clientes,
                                                         EventoPublisherPort eventos,
                                                         PoliticaOperacion politica) {
        return new OperacionCambioService(operaciones, tipos, clientes, eventos, politica);
    }

    @Bean
    public TipoCambioService tipoCambioService(TipoCambioRepositoryPort tipos, EventoPublisherPort eventos) {
        return new TipoCambioService(tipos, eventos);
    }
}

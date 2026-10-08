package pe.finanzasglobal.divisas.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.mensajeria.modo", havingValue = "rabbit")
public class RabbitConfig {

    public static final String EXCHANGE = "finanzas.eventos";

    /** Exchange durable de tipo topic: cada consumidor (auditoria, notificaciones) enlaza su propia cola. */
    @Bean
    public TopicExchange eventosExchange() { return new TopicExchange(EXCHANGE, true, false); }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}

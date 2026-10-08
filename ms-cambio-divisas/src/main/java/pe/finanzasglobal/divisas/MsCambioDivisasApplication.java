package pe.finanzasglobal.divisas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class MsCambioDivisasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsCambioDivisasApplication.class, args);
    }
}

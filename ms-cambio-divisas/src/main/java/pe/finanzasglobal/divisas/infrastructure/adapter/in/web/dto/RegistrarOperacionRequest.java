package pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;

import java.math.BigDecimal;

/** El usuario y la oficina NO se reciben en el body: se toman del JWT (evita suplantacion). */
public record RegistrarOperacionRequest(
        @NotNull @Positive Long clienteId,
        @NotNull TipoOperacion tipo,
        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "debe ser un codigo ISO 4217 (ej. USD)") String moneda,
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "1000000.00")
        @Digits(integer = 9, fraction = 2) BigDecimal monto) { }

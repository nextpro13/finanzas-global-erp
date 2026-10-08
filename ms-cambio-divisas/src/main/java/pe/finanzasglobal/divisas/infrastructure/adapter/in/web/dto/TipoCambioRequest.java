package pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TipoCambioRequest(
        @NotNull @DecimalMin("0.0001") @Digits(integer = 4, fraction = 4) BigDecimal compra,
        @NotNull @DecimalMin("0.0001") @Digits(integer = 4, fraction = 4) BigDecimal venta) { }

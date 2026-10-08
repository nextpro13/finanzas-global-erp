package pe.finanzasglobal.divisas.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Size(max = 50) String usuario,
                           @NotBlank @Size(max = 100) String password) { }

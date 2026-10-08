package pe.finanzasglobal.divisas.infrastructure.adapter.out.feign;

/** Contrato JSON expuesto por ms-clientes. */
public record ClienteResponse(Long id, String razonSocial, String estado) { }

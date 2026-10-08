package pe.finanzasglobal.divisas.domain.model;

/** Vista minima del cliente que necesita este servicio (el dueno del dato es ms-clientes). */
public record ClienteInfo(Long id, String nombre, boolean habilitado) { }

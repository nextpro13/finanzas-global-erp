package pe.finanzasglobal.divisas.domain.model;

import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Cotizacion inmutable. Cada cambio genera un registro nuevo => historial completo. */
public final class TipoCambio {

    private final Long id;
    private final String moneda;
    private final BigDecimal compra;
    private final BigDecimal venta;
    private final String registradoPor;
    private final LocalDateTime fechaRegistro;

    public TipoCambio(Long id, String moneda, BigDecimal compra, BigDecimal venta,
                      String registradoPor, LocalDateTime fechaRegistro) {
        validarMoneda(moneda);
        if (compra == null || venta == null || compra.signum() <= 0 || venta.signum() <= 0)
            throw new ReglaNegocioException("Los tipos de cambio deben ser mayores a cero");
        if (compra.compareTo(venta) >= 0)
            throw new ReglaNegocioException("El tipo de cambio de compra debe ser menor al de venta");
        if (registradoPor == null || registradoPor.isBlank())
            throw new ReglaNegocioException("Se requiere el usuario que registra la cotizacion");
        this.id = id;
        this.moneda = moneda;
        this.compra = compra;
        this.venta = venta;
        this.registradoPor = registradoPor;
        this.fechaRegistro = fechaRegistro;
    }

    public static TipoCambio nuevo(String moneda, BigDecimal compra, BigDecimal venta, String usuario) {
        return new TipoCambio(null, moneda, compra, venta, usuario, LocalDateTime.now());
    }

    public static void validarMoneda(String moneda) {
        if (moneda == null || !moneda.matches("[A-Z]{3}"))
            throw new ReglaNegocioException("Codigo de moneda invalido (formato ISO 4217, ej. USD)");
        if ("PEN".equals(moneda))
            throw new ReglaNegocioException("PEN es la moneda base y no requiere tipo de cambio");
    }

    /** Tasa que corresponde aplicar segun el tipo de operacion. */
    public BigDecimal tasaPara(TipoOperacion tipo) {
        return tipo == TipoOperacion.COMPRA ? compra : venta;
    }

    public Long getId() { return id; }
    public String getMoneda() { return moneda; }
    public BigDecimal getCompra() { return compra; }
    public BigDecimal getVenta() { return venta; }
    public String getRegistradoPor() { return registradoPor; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}

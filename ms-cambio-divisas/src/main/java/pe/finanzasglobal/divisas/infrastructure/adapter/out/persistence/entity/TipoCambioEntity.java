package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tipo_cambio", indexes = @Index(name = "ix_tc_moneda_fecha", columnList = "moneda, fecha_registro"))
public class TipoCambioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal compra;
    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal venta;
    @Column(name = "registrado_por", nullable = false, length = 50)
    private String registradoPor;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    protected TipoCambioEntity() { }

    public TipoCambioEntity(Long id, String moneda, BigDecimal compra, BigDecimal venta,
                            String registradoPor, LocalDateTime fechaRegistro) {
        this.id = id; this.moneda = moneda; this.compra = compra; this.venta = venta;
        this.registradoPor = registradoPor; this.fechaRegistro = fechaRegistro;
    }

    public Long getId() { return id; }
    public String getMoneda() { return moneda; }
    public BigDecimal getCompra() { return compra; }
    public BigDecimal getVenta() { return venta; }
    public String getRegistradoPor() { return registradoPor; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}

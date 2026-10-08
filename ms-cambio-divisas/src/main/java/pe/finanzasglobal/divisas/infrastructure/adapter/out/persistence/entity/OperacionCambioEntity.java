package pe.finanzasglobal.divisas.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import pe.finanzasglobal.divisas.domain.model.EstadoOperacion;
import pe.finanzasglobal.divisas.domain.model.TipoOperacion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "operacion_cambio",
       uniqueConstraints = @UniqueConstraint(name = "uk_op_idempotency", columnNames = "idempotency_key"))
public class OperacionCambioEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private TipoOperacion tipo;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(name = "monto_divisa", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoDivisa;
    @Column(name = "tasa_aplicada", nullable = false, precision = 10, scale = 4)
    private BigDecimal tasaAplicada;
    @Column(name = "tipo_cambio_id", nullable = false)
    private Long tipoCambioId;
    @Column(name = "monto_soles", nullable = false, precision = 16, scale = 2)
    private BigDecimal montoSoles;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal comision;
    @Column(name = "total_soles", nullable = false, precision = 16, scale = 2)
    private BigDecimal totalSoles;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private EstadoOperacion estado;
    @Column(nullable = false, length = 50)
    private String usuario;
    @Column(nullable = false, length = 20)
    private String oficina;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    @Column(name = "aprobado_por", length = 50)
    private String aprobadoPor;
    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;
    @Version
    private Long version;

    protected OperacionCambioEntity() { }

    @SuppressWarnings("java:S107")
    public OperacionCambioEntity(Long id, String idempotencyKey, Long clienteId, TipoOperacion tipo, String moneda,
                                 BigDecimal montoDivisa, BigDecimal tasaAplicada, Long tipoCambioId,
                                 BigDecimal montoSoles, BigDecimal comision, BigDecimal totalSoles,
                                 EstadoOperacion estado, String usuario, String oficina, LocalDateTime fechaRegistro,
                                 String aprobadoPor, LocalDateTime fechaAprobacion, Long version) {
        this.id = id; this.idempotencyKey = idempotencyKey; this.clienteId = clienteId; this.tipo = tipo;
        this.moneda = moneda; this.montoDivisa = montoDivisa; this.tasaAplicada = tasaAplicada;
        this.tipoCambioId = tipoCambioId; this.montoSoles = montoSoles; this.comision = comision;
        this.totalSoles = totalSoles; this.estado = estado; this.usuario = usuario; this.oficina = oficina;
        this.fechaRegistro = fechaRegistro; this.aprobadoPor = aprobadoPor;
        this.fechaAprobacion = fechaAprobacion; this.version = version;
    }

    public Long getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Long getClienteId() { return clienteId; }
    public TipoOperacion getTipo() { return tipo; }
    public String getMoneda() { return moneda; }
    public BigDecimal getMontoDivisa() { return montoDivisa; }
    public BigDecimal getTasaAplicada() { return tasaAplicada; }
    public Long getTipoCambioId() { return tipoCambioId; }
    public BigDecimal getMontoSoles() { return montoSoles; }
    public BigDecimal getComision() { return comision; }
    public BigDecimal getTotalSoles() { return totalSoles; }
    public EstadoOperacion getEstado() { return estado; }
    public String getUsuario() { return usuario; }
    public String getOficina() { return oficina; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getAprobadoPor() { return aprobadoPor; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public Long getVersion() { return version; }
}

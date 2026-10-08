package pe.finanzasglobal.divisas.domain.model;

import pe.finanzasglobal.divisas.domain.exception.ReglaNegocioException;
import pe.finanzasglobal.divisas.domain.service.CalculadoraCambio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Agregado principal: una operacion de compra/venta de divisas con su trazabilidad. */
public final class OperacionCambio {

    private final Long id;
    private final String idempotencyKey;
    private final Long clienteId;
    private final TipoOperacion tipo;
    private final String moneda;
    private final BigDecimal montoDivisa;
    private final BigDecimal tasaAplicada;
    private final Long tipoCambioId;      // que cotizacion exacta se uso
    private final BigDecimal montoSoles;
    private final BigDecimal comision;
    private final BigDecimal totalSoles;
    private EstadoOperacion estado;
    private final String usuario;          // quien la registro
    private final String oficina;          // desde que oficina
    private final LocalDateTime fechaRegistro;
    private String aprobadoPor;
    private LocalDateTime fechaAprobacion;
    private final Long version;            // bloqueo optimista

    @SuppressWarnings("java:S107")
    public OperacionCambio(Long id, String idempotencyKey, Long clienteId, TipoOperacion tipo, String moneda,
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

    /** Fabrica: aplica calculo, comision y la regla de limite que exige aprobacion. */
    public static OperacionCambio registrar(String idempotencyKey, Long clienteId, TipoOperacion tipo,
                                            BigDecimal montoDivisa, TipoCambio tipoCambio,
                                            PoliticaOperacion politica, String usuario, String oficina) {
        if (usuario == null || usuario.isBlank() || oficina == null || oficina.isBlank())
            throw new ReglaNegocioException("La operacion requiere usuario y oficina para trazabilidad");
        BigDecimal tasa = tipoCambio.tasaPara(tipo);
        CalculadoraCambio.Resultado r =
                CalculadoraCambio.calcular(tipo, montoDivisa, tasa, politica.comisionPorcentaje());
        EstadoOperacion estado = r.montoSoles().compareTo(politica.limiteSinAprobacion()) > 0
                ? EstadoOperacion.PENDIENTE_APROBACION : EstadoOperacion.REGISTRADA;
        return new OperacionCambio(null, idempotencyKey, clienteId, tipo, tipoCambio.getMoneda(),
                montoDivisa, tasa, tipoCambio.getId(), r.montoSoles(), r.comision(), r.totalSoles(),
                estado, usuario, oficina, LocalDateTime.now(), null, null, null);
    }

    /** Segregacion de funciones: quien registra no puede aprobar su propia operacion. */
    public void aprobar(String supervisor) {
        if (estado != EstadoOperacion.PENDIENTE_APROBACION)
            throw new ReglaNegocioException("Solo se aprueban operaciones en estado PENDIENTE_APROBACION");
        if (supervisor == null || supervisor.equals(usuario))
            throw new ReglaNegocioException("Un usuario no puede aprobar una operacion registrada por el mismo");
        this.estado = EstadoOperacion.APROBADA;
        this.aprobadoPor = supervisor;
        this.fechaAprobacion = LocalDateTime.now();
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

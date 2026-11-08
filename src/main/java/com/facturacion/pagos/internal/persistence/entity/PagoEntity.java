package com.facturacion.pagos.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "pago",
        indexes = {
                @Index(name = "ix_pago_factura", columnList = "factura_id"),
                @Index(name = "ix_pago_empresa_fecha", columnList = "empresa_id, fecha")})
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factura_id", nullable = false)
    private Long facturaId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(name = "metodo", nullable = false, length = 20)
    private String metodo;

    @Column(name = "referencia", length = 100)
    private String referencia;

    protected PagoEntity() {
    }

    public PagoEntity(
            Long id,
            Long facturaId,
            Long empresaId,
            String tipo,
            LocalDate fecha,
            BigDecimal importe,
            String metodo,
            String referencia) {
        this.id = id;
        this.facturaId = facturaId;
        this.empresaId = empresaId;
        this.tipo = tipo;
        this.fecha = fecha;
        this.importe = importe;
        this.metodo = metodo;
        this.referencia = referencia;
    }

    public Long getId() {
        return id;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public String getMetodo() {
        return metodo;
    }

    public String getReferencia() {
        return referencia;
    }
}

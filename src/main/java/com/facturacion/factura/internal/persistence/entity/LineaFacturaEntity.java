package com.facturacion.factura.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "linea_factura")
public class LineaFacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factura_id", nullable = false)
    private FacturaEntity factura;

    @Column(name = "posicion", nullable = false)
    private int posicion;

    @Column(name = "concepto", nullable = false, length = 500)
    private String concepto;

    @Column(name = "cantidad", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    protected LineaFacturaEntity() {
    }

    public LineaFacturaEntity(int posicion, String concepto, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.posicion = posicion;
        this.concepto = concepto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    void asignarFactura(FacturaEntity factura) {
        this.factura = factura;
    }

    public Long getId() {
        return id;
    }

    public int getPosicion() {
        return posicion;
    }

    public String getConcepto() {
        return concepto;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
}

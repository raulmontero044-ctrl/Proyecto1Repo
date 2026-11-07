package com.facturacion.factura.internal.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura")
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true, length = 50)
    private String numero;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "cliente_nombre", nullable = false, length = 200)
    private String clienteNombre;

    @Column(name = "cliente_nif", nullable = false, length = 20)
    private String clienteNif;

    @Column(name = "porcentaje_iva", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIva;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    private List<LineaFacturaEntity> lineas = new ArrayList<>();

    protected FacturaEntity() {
    }

    public FacturaEntity(
            Long id,
            String numero,
            String tipo,
            LocalDate fecha,
            LocalDate fechaVencimiento,
            String clienteNombre,
            String clienteNif,
            BigDecimal porcentajeIva) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.fecha = fecha;
        this.fechaVencimiento = fechaVencimiento;
        this.clienteNombre = clienteNombre;
        this.clienteNif = clienteNif;
        this.porcentajeIva = porcentajeIva;
    }

    public void agregarLinea(LineaFacturaEntity linea) {
        linea.asignarFactura(this);
        lineas.add(linea);
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteNif() {
        return clienteNif;
    }

    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }

    public List<LineaFacturaEntity> getLineas() {
        return lineas;
    }
}

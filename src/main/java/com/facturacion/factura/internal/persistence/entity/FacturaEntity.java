package com.facturacion.factura.internal.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factura", indexes = @Index(name = "ix_factura_empresa_numero", columnList = "empresa_id, numero"))
public class FacturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "empresa_razon_social", length = 200)
    private String empresaRazonSocial;

    @Column(name = "empresa_nif", length = 20)
    private String empresaNif;

    @Column(name = "empresa_direccion", length = 600)
    private String empresaDireccion;

    @Column(name = "tercero_id")
    private Long terceroId;

    @Column(name = "tercero_nombre", nullable = false, length = 200)
    private String terceroNombre;

    @Column(name = "tercero_nif", nullable = false, length = 20)
    private String terceroNif;

    @Column(name = "tercero_direccion", length = 600)
    private String terceroDireccion;

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
            Long empresaId,
            String empresaRazonSocial,
            String empresaNif,
            String empresaDireccion,
            Long terceroId,
            String terceroNombre,
            String terceroNif,
            String terceroDireccion,
            BigDecimal porcentajeIva) {
        this.id = id;
        this.numero = numero;
        this.tipo = tipo;
        this.fecha = fecha;
        this.fechaVencimiento = fechaVencimiento;
        this.empresaId = empresaId;
        this.empresaRazonSocial = empresaRazonSocial;
        this.empresaNif = empresaNif;
        this.empresaDireccion = empresaDireccion;
        this.terceroId = terceroId;
        this.terceroNombre = terceroNombre;
        this.terceroNif = terceroNif;
        this.terceroDireccion = terceroDireccion;
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

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getEmpresaRazonSocial() {
        return empresaRazonSocial;
    }

    public String getEmpresaNif() {
        return empresaNif;
    }

    public String getEmpresaDireccion() {
        return empresaDireccion;
    }

    public Long getTerceroId() {
        return terceroId;
    }

    public String getTerceroNombre() {
        return terceroNombre;
    }

    public String getTerceroNif() {
        return terceroNif;
    }

    public String getTerceroDireccion() {
        return terceroDireccion;
    }

    public BigDecimal getPorcentajeIva() {
        return porcentajeIva;
    }

    public List<LineaFacturaEntity> getLineas() {
        return lineas;
    }
}

package com.facturacion.terceros.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "tercero",
        uniqueConstraints = @UniqueConstraint(name = "uk_tercero_empresa_nif", columnNames = {"empresa_id", "nif"}))
public class TerceroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nif", nullable = false, length = 20)
    private String nif;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(name = "municipio", length = 100)
    private String municipio;

    @Column(name = "provincia", length = 100)
    private String provincia;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "es_cliente", nullable = false)
    private boolean esCliente;

    @Column(name = "es_proveedor", nullable = false)
    private boolean esProveedor;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected TerceroEntity() {
    }

    public TerceroEntity(
            Long id,
            Long empresaId,
            String nif,
            String razonSocial,
            String direccion,
            String codigoPostal,
            String municipio,
            String provincia,
            String pais,
            String email,
            String telefono,
            boolean esCliente,
            boolean esProveedor,
            boolean activo) {
        this.id = id;
        this.empresaId = empresaId;
        this.nif = nif;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.municipio = municipio;
        this.provincia = provincia;
        this.pais = pais;
        this.email = email;
        this.telefono = telefono;
        this.esCliente = esCliente;
        this.esProveedor = esProveedor;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public String getNif() {
        return nif;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getProvincia() {
        return provincia;
    }

    public String getPais() {
        return pais;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isEsCliente() {
        return esCliente;
    }

    public boolean isEsProveedor() {
        return esProveedor;
    }

    public boolean isActivo() {
        return activo;
    }
}

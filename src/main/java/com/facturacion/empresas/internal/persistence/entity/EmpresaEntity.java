package com.facturacion.empresas.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa")
public class EmpresaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "nif", nullable = false, unique = true, length = 20)
    private String nif;

    @Column(name = "direccion", nullable = false, length = 300)
    private String direccion;

    @Column(name = "codigo_postal", nullable = false, length = 10)
    private String codigoPostal;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "provincia", nullable = false, length = 100)
    private String provincia;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "telefono", length = 30)
    private String telefono;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    protected EmpresaEntity() {
    }

    public EmpresaEntity(
            Long id,
            String razonSocial,
            String nif,
            String direccion,
            String codigoPostal,
            String municipio,
            String provincia,
            String pais,
            String email,
            String telefono,
            boolean activa) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.nif = nif;
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.municipio = municipio;
        this.provincia = provincia;
        this.pais = pais;
        this.email = email;
        this.telefono = telefono;
        this.activa = activa;
    }

    public Long getId() {
        return id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNif() {
        return nif;
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

    public boolean isActiva() {
        return activa;
    }
}

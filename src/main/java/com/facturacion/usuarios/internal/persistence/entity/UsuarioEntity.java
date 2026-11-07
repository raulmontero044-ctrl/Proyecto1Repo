package com.facturacion.usuarios.internal.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @ElementCollection
    @CollectionTable(name = "usuario_empresa", joinColumns = @JoinColumn(name = "usuario_id"))
    @OrderBy("empresaId ASC")
    private Set<AccesoEmpresaEmbeddable> accesos = new LinkedHashSet<>();

    protected UsuarioEntity() {
    }

    public UsuarioEntity(
            Long id,
            String email,
            String nombre,
            String passwordHash,
            boolean activo,
            List<AccesoEmpresaEmbeddable> accesos) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.activo = activo;
        this.accesos = new LinkedHashSet<>(accesos);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isActivo() {
        return activo;
    }

    public List<AccesoEmpresaEmbeddable> getAccesos() {
        return List.copyOf(accesos);
    }
}

package com.facturacion.auth.internal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "hash", nullable = false, unique = true, length = 64)
    private String hash;

    @Column(name = "expira_en", nullable = false, columnDefinition = "DATETIME(6)")
    private Instant expiraEn;

    @Column(name = "revocado", nullable = false)
    private boolean revocado;

    protected RefreshTokenEntity() {
    }

    public RefreshTokenEntity(Long id, Long usuarioId, String hash, Instant expiraEn, boolean revocado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.hash = hash;
        this.expiraEn = expiraEn;
        this.revocado = revocado;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getHash() {
        return hash;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public boolean isRevocado() {
        return revocado;
    }
}

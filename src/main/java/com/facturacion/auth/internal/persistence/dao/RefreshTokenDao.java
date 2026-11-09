package com.facturacion.auth.internal.persistence.dao;

import com.facturacion.auth.internal.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenDao extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByHash(String hash);
}

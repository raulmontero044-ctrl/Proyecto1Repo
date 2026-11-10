package com.facturacion.auth.internal.persistence.dao;

import com.facturacion.auth.internal.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenDao extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByHash(String hash);

    List<RefreshTokenEntity> findByUsuarioIdAndRevocadoFalseAndExpiraEnAfter(Long usuarioId, Instant instante);

    @Modifying
    @Query("delete from RefreshTokenEntity t where t.revocado = true or t.expiraEn <= :instante")
    void deleteRevocadosOCaducados(@Param("instante") Instant instante);
}

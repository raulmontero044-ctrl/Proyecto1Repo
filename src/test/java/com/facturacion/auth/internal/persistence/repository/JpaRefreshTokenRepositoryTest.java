package com.facturacion.auth.internal.persistence.repository;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.persistence.dao.RefreshTokenDao;
import com.facturacion.auth.internal.persistence.entity.RefreshTokenEntity;
import com.facturacion.auth.internal.persistence.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaRefreshTokenRepositoryTest {

    private static final String HASH = "c".repeat(64);
    private static final Instant CADUCIDAD = Instant.parse("2026-11-19T10:00:00Z");

    private final RefreshTokenDao dao = mock(RefreshTokenDao.class);
    private final JpaRefreshTokenRepository repository = new JpaRefreshTokenRepository(dao, new RefreshTokenMapper());

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(RefreshTokenEntity.class))).thenReturn(new RefreshTokenEntity(9L, 7L, HASH, CADUCIDAD, false));

        RefreshToken guardado = repository.guardar(new RefreshToken(null, 7L, HASH, CADUCIDAD, false));

        assertThat(guardado).isEqualTo(new RefreshToken(9L, 7L, HASH, CADUCIDAD, false));
        ArgumentCaptor<RefreshTokenEntity> enviada = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getHash()).isEqualTo(HASH);
    }

    @Test
    void guardarUnTokenRevocadoEnviaLaRevocacionConSuId() {
        when(dao.save(any(RefreshTokenEntity.class))).thenReturn(new RefreshTokenEntity(9L, 7L, HASH, CADUCIDAD, true));

        repository.guardar(new RefreshToken(9L, 7L, HASH, CADUCIDAD, true));

        ArgumentCaptor<RefreshTokenEntity> enviada = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isEqualTo(9L);
        assertThat(enviada.getValue().isRevocado()).isTrue();
    }

    @Test
    void buscarPorHashDevuelveElDominio() {
        when(dao.findByHash(HASH)).thenReturn(Optional.of(new RefreshTokenEntity(9L, 7L, HASH, CADUCIDAD, false)));

        assertThat(repository.buscarPorHash(HASH)).contains(new RefreshToken(9L, 7L, HASH, CADUCIDAD, false));
    }

    @Test
    void buscarPorHashDesconocidoDevuelveVacio() {
        when(dao.findByHash(HASH)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorHash(HASH)).isEmpty();
    }
}

package com.facturacion.auth.internal.persistence.mapper;

import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.persistence.entity.RefreshTokenEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenMapperTest {

    private static final String HASH = "b".repeat(64);
    private static final Instant CADUCIDAD = Instant.parse("2026-11-19T10:00:00.123456Z");

    private final RefreshTokenMapper mapper = new RefreshTokenMapper();

    @Test
    void idaYVueltaConservaTodosLosCampos() {
        RefreshToken original = new RefreshToken(3L, 7L, HASH, CADUCIDAD, true);

        assertThat(mapper.aDominio(mapper.aEntidad(original))).isEqualTo(original);
    }

    @Test
    void aEntidadDeUnTokenNuevoNoTieneIdentificador() {
        RefreshTokenEntity entidad = mapper.aEntidad(RefreshToken.emitir(7L, HASH, Instant.parse("2026-11-09T10:00:00Z")));

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getUsuarioId()).isEqualTo(7L);
        assertThat(entidad.getHash()).isEqualTo(HASH);
        assertThat(entidad.getExpiraEn()).isEqualTo(Instant.parse("2026-11-19T10:00:00Z"));
        assertThat(entidad.isRevocado()).isFalse();
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        RefreshTokenEntity entidad = new RefreshTokenEntity(3L, 7L, "no-es-un-hash", CADUCIDAD, false);

        assertThatThrownBy(() -> mapper.aDominio(entidad)).isInstanceOf(RefreshTokenInvalidoException.class);
    }
}

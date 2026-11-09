package com.facturacion.auth.internal.persistence.mapper;

import com.facturacion.auth.internal.domain.model.RefreshToken;
import com.facturacion.auth.internal.persistence.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public RefreshTokenEntity aEntidad(RefreshToken refreshToken) {
        return new RefreshTokenEntity(
                refreshToken.id(),
                refreshToken.usuarioId(),
                refreshToken.hash(),
                refreshToken.expiraEn(),
                refreshToken.revocado());
    }

    public RefreshToken aDominio(RefreshTokenEntity entidad) {
        return new RefreshToken(
                entidad.getId(),
                entidad.getUsuarioId(),
                entidad.getHash(),
                entidad.getExpiraEn(),
                entidad.isRevocado());
    }
}

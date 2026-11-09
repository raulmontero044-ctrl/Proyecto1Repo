package com.facturacion.auth.internal.infrastructure;

import com.facturacion.auth.internal.domain.model.TokenAcceso;
import com.facturacion.auth.internal.domain.service.FirmadorTokenAcceso;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class JwtFirmadorTokenAcceso implements FirmadorTokenAcceso {

    static final String CLAIM_ROL = "rol";

    private final JwtEncoder encoder;

    JwtFirmadorTokenAcceso(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String firmar(TokenAcceso tokenAcceso) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(String.valueOf(tokenAcceso.usuarioId()))
                .issuedAt(tokenAcceso.emitidoEn())
                .expiresAt(tokenAcceso.expiraEn());
        tokenAcceso.rolEfectivo().ifPresent(rol -> claims.claim(CLAIM_ROL, rol.name()));
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims.build())).getTokenValue();
    }
}

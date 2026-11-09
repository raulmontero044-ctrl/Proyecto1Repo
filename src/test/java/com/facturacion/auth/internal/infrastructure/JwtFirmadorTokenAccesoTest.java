package com.facturacion.auth.internal.infrastructure;

import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.app.PropiedadesJwt;
import com.facturacion.auth.internal.domain.model.Rol;
import com.facturacion.auth.internal.domain.model.TokenAcceso;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtFirmadorTokenAccesoTest {

    private static final PropiedadesJwt PROPIEDADES = new PropiedadesJwt("clave-de-prueba-del-firmador-de-32-bytes-o-mas");
    private static final Instant EMITIDO = Instant.parse("2026-11-09T10:00:00Z");

    private final ConfiguracionSeguridad configuracion = new ConfiguracionSeguridad();
    private final JwtFirmadorTokenAcceso firmador = new JwtFirmadorTokenAcceso(configuracion.jwtEncoder(PROPIEDADES));

    private JwtDecoder decoderEn(Instant instante) {
        return configuracion.jwtDecoder(PROPIEDADES, Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void firmaConHs256ElSujetoElRolYLasFechas() {
        String token = firmador.firmar(TokenAcceso.emitir(7L, Rol.GESTOR, EMITIDO));

        Jwt jwt = decoderEn(EMITIDO.plusSeconds(60)).decode(token);

        assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("rol")).isEqualTo("GESTOR");
        assertThat(jwt.getIssuedAt()).isEqualTo(EMITIDO);
        assertThat(jwt.getExpiresAt()).isEqualTo(Instant.parse("2026-11-09T10:05:00Z"));
    }

    @Test
    void elClaimDeRolEsElQueLeeLaConfiguracionDeSeguridad() {
        assertThat(JwtFirmadorTokenAcceso.CLAIM_ROL).isEqualTo(ConfiguracionSeguridad.CLAIM_ROL);
    }

    @Test
    void laConfiguracionDeSeguridadConvierteElRolEnLaAutoridadDelRol() {
        String token = firmador.firmar(TokenAcceso.emitir(7L, Rol.CONSULTA, EMITIDO));
        Jwt jwt = decoderEn(EMITIDO).decode(token);

        assertThat(configuracion.conversorAutenticacion().convert(jwt).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CONSULTA");
    }

    @Test
    void unTokenSinRolNoLlevaElClaimNiAutoridades() {
        String token = firmador.firmar(TokenAcceso.emitir(7L, null, EMITIDO));
        Jwt jwt = decoderEn(EMITIDO).decode(token);

        assertThat(jwt.hasClaim("rol")).isFalse();
        assertThat(configuracion.conversorAutenticacion().convert(jwt).getAuthorities()).isEmpty();
    }

    @Test
    void elTokenSeRechazaDesdeElInstanteDeCaducidad() {
        String token = firmador.firmar(TokenAcceso.emitir(7L, Rol.GESTOR, EMITIDO));

        assertThat(decoderEn(Instant.parse("2026-11-09T10:04:59Z")).decode(token).getSubject()).isEqualTo("7");
        assertThatThrownBy(() -> decoderEn(Instant.parse("2026-11-09T10:05:01Z")).decode(token))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        JwtFirmadorTokenAcceso otroFirmador = new JwtFirmadorTokenAcceso(
                configuracion.jwtEncoder(new PropiedadesJwt("otra-clave-distinta-de-32-bytes-o-mas!!")));
        String token = otroFirmador.firmar(TokenAcceso.emitir(7L, Rol.ADMINISTRADOR, EMITIDO));

        assertThatThrownBy(() -> decoderEn(EMITIDO).decode(token)).isInstanceOf(BadJwtException.class);
    }
}

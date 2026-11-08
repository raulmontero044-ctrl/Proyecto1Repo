package com.facturacion.app;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracionRelojTest {

    @Test
    void elRelojEsElDelSistemaEnLaZonaPorDefecto() {
        Clock reloj = new ConfiguracionReloj().reloj();

        assertThat(reloj).isEqualTo(Clock.systemDefaultZone());
        assertThat(reloj.getZone()).isEqualTo(ZoneId.systemDefault());
    }
}

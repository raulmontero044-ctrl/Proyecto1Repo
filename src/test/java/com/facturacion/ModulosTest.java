package com.facturacion;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.NamedInterface;

import static org.assertj.core.api.Assertions.assertThat;

class ModulosTest {

    private static final ApplicationModules MODULOS = ApplicationModules.of(FacturacionApplication.class);

    @Test
    void verificaLosLimitesDeLosModulos() {
        MODULOS.verify();
    }

    @Test
    void detectaComoModulosSoloAppYFactura() {
        assertThat(MODULOS.stream().map(ApplicationModule::getName))
                .containsExactlyInAnyOrder("app", "factura");
    }

    @Test
    void facturaSoloExponeLaFacadeYSusDto() {
        ApplicationModule factura = MODULOS.getModuleByName("factura").orElseThrow();

        assertThat(factura.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }
}

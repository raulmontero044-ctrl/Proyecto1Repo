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
    void detectaComoModulosAppFacturaEmpresasUsuariosTercerosPagosYDocumentos() {
        assertThat(MODULOS.stream().map(ApplicationModule::getName))
                .containsExactlyInAnyOrder(
                        "app", "factura", "empresas", "usuarios", "terceros", "pagos", "documentos");
    }

    @Test
    void facturaSoloExponeLaFacadeYSusDto() {
        ApplicationModule factura = MODULOS.getModuleByName("factura").orElseThrow();

        assertThat(factura.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }

    @Test
    void facturaSoloDependeDeEmpresasYTerceros() {
        ApplicationModule factura = MODULOS.getModuleByName("factura").orElseThrow();

        assertThat(factura.getDirectDependencies(MODULOS).uniqueModules().map(ApplicationModule::getName))
                .containsExactlyInAnyOrder("empresas", "terceros");
    }

    @Test
    void empresasSoloExponeLaFacadeYSusDto() {
        ApplicationModule empresas = MODULOS.getModuleByName("empresas").orElseThrow();

        assertThat(empresas.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }

    @Test
    void usuariosSoloExponeLaFacadeYSusDto() {
        ApplicationModule usuarios = MODULOS.getModuleByName("usuarios").orElseThrow();

        assertThat(usuarios.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }

    @Test
    void tercerosSoloExponeLaFacadeYSusDto() {
        ApplicationModule terceros = MODULOS.getModuleByName("terceros").orElseThrow();

        assertThat(terceros.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }

    @Test
    void pagosSoloExponeLaFacadeYSusDto() {
        ApplicationModule pagos = MODULOS.getModuleByName("pagos").orElseThrow();

        assertThat(pagos.getNamedInterfaces().stream()
                .filter(interfaz -> !interfaz.isUnnamed())
                .map(NamedInterface::getName))
                .containsExactlyInAnyOrder("facade", "facade-dto");
    }

    @Test
    void pagosSoloDependeDeFactura() {
        ApplicationModule pagos = MODULOS.getModuleByName("pagos").orElseThrow();

        assertThat(pagos.getDirectDependencies(MODULOS).uniqueModules().map(ApplicationModule::getName))
                .containsExactlyInAnyOrder("factura");
    }
}

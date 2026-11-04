package com.facturacion;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReglaContratoEntreModulosTest {

    private static final JavaClasses CLASES_FICTICIAS = new ClassFileImporter()
            .importPackages("com.facturacion.ficticio", "com.facturacion.app.ficticio");

    private static JavaClasses clase(String nombreSimple) {
        JavaClasses seleccionadas = CLASES_FICTICIAS.that(simpleName(nombreSimple));
        assertThat(seleccionadas).hasSize(1);
        return seleccionadas;
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelContratoDeFactura",
            "ConsumidorDeLaFacadeDeFactura"})
    void aceptaQueLaAplicacionDeOtroModuloUseLosDtoDeContratoDeFactura(String origen) {
        JavaClasses clases = clase(origen);

        assertThatCode(() -> ArquitecturaTest.modulosSoloUsanLaFacadeDeOtrosDesdeAplicacion.check(clases))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelRequestHttpDeFactura, com.facturacion.factura.api.http.dto.CrearFacturaRequest",
            "ConsumidorDeLaRespuestaHttpDeFactura, com.facturacion.factura.api.http.dto.FacturaResponse",
            "ConsumidorDelDominioDeFactura, com.facturacion.factura.internal.domain.model.Factura",
            "ConsumidorDeUnCasoDeUsoDeFactura, com.facturacion.factura.internal.application.CrearFactura",
            "ControladorConsumidorDelContratoDeFactura, com.facturacion.factura.api.facade.dto.FacturaDto",
            "ServicioConsumidorDelContratoDeFactura, com.facturacion.factura.api.facade.dto.FacturaDto",
            "PersistenciaConsumidoraDeEntidadesDeFactura, com.facturacion.factura.internal.persistence.entity.FacturaEntity",
            "DominioConsumidorDelContratoDeFactura, com.facturacion.factura.api.facade.dto.FacturaDto"})
    void rechazaLosAccesosDeOtroModuloFueraDelContratoApiDeFactura(String origen, String destino) {
        JavaClasses clases = clase(origen);

        assertThatThrownBy(() -> ArquitecturaTest.modulosSoloUsanLaFacadeDeOtrosDesdeAplicacion.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(origen)
                .hasMessageContaining(destino);
    }

    @Test
    void lasClasesDelPropioModuloFacturaNoSeConsideranAccesosEntreModulos() {
        JavaClasses clases = new ClassFileImporter().importPackages("com.facturacion.factura.api.http");

        assertThat(clases).isNotEmpty();
        assertThatCode(() -> ArquitecturaTest.modulosSoloUsanLaFacadeDeOtrosDesdeAplicacion.check(clases))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelDominioDeFactura, com.facturacion.factura.internal.domain.model.Factura",
            "ConsumidorDeUnCasoDeUsoDeFactura, com.facturacion.factura.internal.application.CrearFactura",
            "PersistenciaConsumidoraDeEntidadesDeFactura, com.facturacion.factura.internal.persistence.entity.FacturaEntity",
            "ComposicionConsumidoraDelInternalDeFactura, com.facturacion.factura.internal.domain.repository.FacturaRepository"})
    void rechazaQueOtroModuloOAppAccedanAlInternalDeFactura(String origen, String destino) {
        JavaClasses clases = clase(origen);

        assertThatThrownBy(() -> ArquitecturaTest.internalNoAccesibleDesdeOtrosModulos.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(origen)
                .hasMessageContaining(destino);
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelContratoDeFactura",
            "ConsumidorDeLaFacadeDeFactura",
            "ComposicionConsumidoraDeLaFacadeDeFactura"})
    void aceptaQueOtroModuloOAppUsenElContratoSinTocarElInternalDeFactura(String origen) {
        JavaClasses clases = clase(origen);

        assertThatCode(() -> ArquitecturaTest.internalNoAccesibleDesdeOtrosModulos.check(clases))
                .doesNotThrowAnyException();
    }

    @Test
    void elPropioModuloFacturaPuedeUsarSuInternal() {
        JavaClasses clases = new ClassFileImporter().importPackages("com.facturacion.factura");

        assertThat(clases).isNotEmpty();
        assertThatCode(() -> ArquitecturaTest.internalNoAccesibleDesdeOtrosModulos.check(clases))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaQueUnControladorDelegueEnUnaFacade() {
        JavaClasses clases = clase("ControladorConsumidorDeLaFacadeDeFactura");

        assertThatThrownBy(() -> ArquitecturaTest.controladoresNoDeleganEnLaFacade.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("ControladorConsumidorDeLaFacadeDeFactura")
                .hasMessageContaining("com.facturacion.factura.api.facade.FacturaFacade");
    }

    @Test
    void aceptaQueUnControladorUseLosDtoDeLaFacade() {
        JavaClasses clases = clase("ControladorConsumidorDelContratoDeFactura");

        assertThatCode(() -> ArquitecturaTest.controladoresNoDeleganEnLaFacade.check(clases))
                .doesNotThrowAnyException();
    }

    @Test
    void rechazaQueUnModuloDependaDeApp() {
        JavaClasses clases = clase("ConsumidorDeLaComposicionApp");

        assertThatThrownBy(() -> ArquitecturaTest.ningunModuloDependeDeApp.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("ConsumidorDeLaComposicionApp")
                .hasMessageContaining("com.facturacion.app.ficticio.ConfiguracionFicticia");
    }

    @Test
    void appPuedeUsarLaFacadeDeFactura() {
        JavaClasses clases = clase("ComposicionConsumidoraDeLaFacadeDeFactura");

        assertThatCode(() -> ArquitecturaTest.ningunModuloDependeDeApp.check(clases))
                .doesNotThrowAnyException();
        assertThatCode(() -> ArquitecturaTest.modulosSoloUsanLaFacadeDeOtrosDesdeAplicacion.check(clases))
                .doesNotThrowAnyException();
        assertThatCode(() -> ArquitecturaTest.appNoDependeDelAdaptadorHttp.check(clases))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "ComposicionConsumidoraDelControladorDeFactura, com.facturacion.factura.api.http.controllers.FacturaController",
            "ComposicionConsumidoraDelRequestHttpDeFactura, com.facturacion.factura.api.http.dto.CrearFacturaRequest"})
    void rechazaQueAppDependaDelAdaptadorHttpDeFactura(String origen, String destino) {
        JavaClasses clases = clase(origen);

        assertThatThrownBy(() -> ArquitecturaTest.appNoDependeDelAdaptadorHttp.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(origen)
                .hasMessageContaining(destino);
    }
}

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
            .importPackages("com.facturacion.ficticio");

    private static JavaClasses clase(String nombreSimple) {
        JavaClasses seleccionadas = CLASES_FICTICIAS.that(simpleName(nombreSimple));
        assertThat(seleccionadas).hasSize(1);
        return seleccionadas;
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelContratoDeFactura",
            "ServicioConsumidorDelContratoDeFactura"})
    void aceptaQueLaAplicacionDeOtroModuloUseLosDtoDeContratoDeFactura(String origen) {
        JavaClasses clases = clase(origen);

        assertThatCode(() -> ArquitecturaTest.modulosSoloUsanElContratoApiDeOtrosDesdeAplicacion.check(clases))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "ConsumidorDelRequestHttpDeFactura, com.facturacion.factura.api.dto.CrearFacturaRequest",
            "ConsumidorDelDominioDeFactura, com.facturacion.factura.internal.domain.model.Factura",
            "ConsumidorDeUnCasoDeUsoDeFactura, com.facturacion.factura.apln.CrearFactura",
            "ControladorConsumidorDelContratoDeFactura, com.facturacion.factura.api.dto.FacturaDto",
            "PersistenciaConsumidoraDeEntidadesDeFactura, com.facturacion.factura.internal.persistence.entity.FacturaEntity",
            "DominioConsumidorDelContratoDeFactura, com.facturacion.factura.api.dto.FacturaDto"})
    void rechazaLosAccesosDeOtroModuloFueraDelContratoApiDeFactura(String origen, String destino) {
        JavaClasses clases = clase(origen);

        assertThatThrownBy(() -> ArquitecturaTest.modulosSoloUsanElContratoApiDeOtrosDesdeAplicacion.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(origen)
                .hasMessageContaining(destino);
    }

    @Test
    void lasClasesDelPropioModuloFacturaNoSeConsideranAccesosEntreModulos() {
        JavaClasses clases = new ClassFileImporter().importPackages("com.facturacion.factura.controller");

        assertThat(clases).isNotEmpty();
        assertThatCode(() -> ArquitecturaTest.modulosSoloUsanElContratoApiDeOtrosDesdeAplicacion.check(clases))
                .doesNotThrowAnyException();
    }
}

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

class ReglaHandlersSeparadosTest {

    private static final JavaClasses HANDLERS_FICTICIOS = new ClassFileImporter()
            .importPackages("com.facturacion.ficticio.api.http.handlers");

    private static JavaClasses clase(String nombreSimple) {
        JavaClasses seleccionadas = HANDLERS_FICTICIOS.that(simpleName(nombreSimple));
        assertThat(seleccionadas).hasSize(1);
        return seleccionadas;
    }

    @Test
    void aceptaUnHandlerQueTraduceUnaSolaExcepcion() {
        JavaClasses clases = clase("UnaExcepcionExceptionHandler");

        assertThatCode(() -> ArquitecturaTest.cadaHandlerTraduceUnaSolaExcepcion.check(clases))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "DosMetodosExceptionHandler",
            "ExcepcionesAgrupadasExceptionHandler",
            "SinAnotacionExceptionHandler"})
    void rechazaLosHandlersQueNoEstanSeparadosPorExcepcion(String handler) {
        JavaClasses clases = clase(handler);

        assertThatThrownBy(() -> ArquitecturaTest.cadaHandlerTraduceUnaSolaExcepcion.check(clases))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(handler);
    }

    @Test
    void losHandlersDeFacturaTraducenCadaUnoUnaSolaExcepcion() {
        JavaClasses clases = new ClassFileImporter().importPackages("com.facturacion.factura.api.http.handlers");

        assertThat(clases).hasSize(3);
        assertThatCode(() -> ArquitecturaTest.cadaHandlerTraduceUnaSolaExcepcion.check(clases))
                .doesNotThrowAnyException();
    }
}

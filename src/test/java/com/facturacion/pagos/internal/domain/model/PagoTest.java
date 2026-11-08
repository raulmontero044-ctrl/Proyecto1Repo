package com.facturacion.pagos.internal.domain.model;

import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagoTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 8);

    private static Pago pago(BigDecimal importe, String referencia) {
        return new Pago(1L, 10L, 3L, TipoPago.COBRO, HOY, importe, MetodoPago.TRANSFERENCIA, referencia);
    }

    private static Pago nuevo(LocalDate fecha) {
        return Pago.nuevo(10L, 3L, TipoPago.PAGO, fecha, new BigDecimal("50.00"), MetodoPago.EFECTIVO, null, HOY);
    }

    @Test
    void conservaTodosSusDatos() {
        Pago pago = new Pago(
                1L, 10L, 3L, TipoPago.COBRO, HOY, new BigDecimal("121.00"), MetodoPago.TARJETA, "REF-1");

        assertThat(pago.id()).isEqualTo(1L);
        assertThat(pago.facturaId()).isEqualTo(10L);
        assertThat(pago.empresaId()).isEqualTo(3L);
        assertThat(pago.tipo()).isEqualTo(TipoPago.COBRO);
        assertThat(pago.fecha()).isEqualTo(HOY);
        assertThat(pago.importe()).isEqualByComparingTo("121.00");
        assertThat(pago.metodo()).isEqualTo(MetodoPago.TARJETA);
        assertThat(pago.referencia()).isEqualTo("REF-1");
    }

    @Test
    void nuevoNoTieneIdentificador() {
        assertThat(nuevo(HOY).id()).isNull();
    }

    @Test
    void nuevoAceptaLaFechaDeHoy() {
        assertThat(nuevo(HOY).fecha()).isEqualTo(HOY);
    }

    @Test
    void nuevoAceptaUnaFechaPasada() {
        assertThat(nuevo(HOY.minusDays(1)).fecha()).isEqualTo(HOY.minusDays(1));
    }

    @Test
    void nuevoRechazaUnaFechaPosteriorAHoy() {
        assertThatThrownBy(() -> nuevo(HOY.plusDays(1)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La fecha del pago no puede ser posterior a hoy");
    }

    @Test
    void rechazaFacturaAusente() {
        assertThatThrownBy(() -> new Pago(
                null, null, 3L, TipoPago.COBRO, HOY, BigDecimal.TEN, MetodoPago.OTRO, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La factura del pago es obligatoria");
    }

    @Test
    void rechazaEmpresaAusente() {
        assertThatThrownBy(() -> new Pago(
                null, 10L, null, TipoPago.COBRO, HOY, BigDecimal.TEN, MetodoPago.OTRO, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La empresa del pago es obligatoria");
    }

    @Test
    void rechazaTipoAusente() {
        assertThatThrownBy(() -> new Pago(
                null, 10L, 3L, null, HOY, BigDecimal.TEN, MetodoPago.OTRO, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El tipo del pago es obligatorio");
    }

    @Test
    void rechazaFechaAusente() {
        assertThatThrownBy(() -> new Pago(
                null, 10L, 3L, TipoPago.COBRO, null, BigDecimal.TEN, MetodoPago.OTRO, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La fecha del pago es obligatoria");
    }

    @Test
    void rechazaMetodoAusente() {
        assertThatThrownBy(() -> new Pago(
                null, 10L, 3L, TipoPago.COBRO, HOY, BigDecimal.TEN, null, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El método del pago es obligatorio");
    }

    @Test
    void rechazaImporteAusente() {
        assertThatThrownBy(() -> pago(null, null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El importe del pago debe ser mayor que cero");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-10"})
    void rechazaImporteCeroONegativo(String importe) {
        assertThatThrownBy(() -> pago(new BigDecimal(importe), null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El importe del pago debe ser mayor que cero");
    }

    @Test
    void aceptaElImporteMinimoPositivo() {
        assertThat(pago(new BigDecimal("0.01"), null).importe()).isEqualByComparingTo("0.01");
    }

    @ParameterizedTest
    @ValueSource(strings = {"10", "10.5", "10.50", "10.500"})
    void normalizaElImporteAEscalaDos(String importe) {
        BigDecimal normalizado = pago(new BigDecimal(importe), null).importe();

        assertThat(normalizado.scale()).isEqualTo(2);
        assertThat(normalizado).isEqualByComparingTo(importe);
    }

    @ParameterizedTest
    @ValueSource(strings = {"10.001", "0.005"})
    void rechazaImporteConMasDeDosDecimales(String importe) {
        assertThatThrownBy(() -> pago(new BigDecimal(importe), null))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("El importe del pago no puede tener más de dos decimales");
    }

    @Test
    void aceptaReferenciaAusente() {
        assertThat(pago(BigDecimal.TEN, null).referencia()).isNull();
    }

    @Test
    void aceptaReferenciaDeCienCaracteres() {
        assertThat(pago(BigDecimal.TEN, "R".repeat(100)).referencia()).hasSize(100);
    }

    @Test
    void rechazaReferenciaDeMasDeCienCaracteres() {
        assertThatThrownBy(() -> pago(BigDecimal.TEN, "R".repeat(101)))
                .isInstanceOf(PagoInvalidoException.class)
                .hasMessage("La referencia del pago no puede superar 100 caracteres");
    }
}

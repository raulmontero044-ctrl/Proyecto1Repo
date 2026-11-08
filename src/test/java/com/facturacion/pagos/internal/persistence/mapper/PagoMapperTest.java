package com.facturacion.pagos.internal.persistence.mapper;

import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.persistence.entity.PagoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagoMapperTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    private final PagoMapper mapper = new PagoMapper();

    @Test
    void conservaTodosLosDatosEnIdaYVuelta() {
        Pago pago = new Pago(
                7L, 10L, 3L, TipoPago.PAGO, FECHA, new BigDecimal("60.50"), MetodoPago.DOMICILIACION, "REF-7");

        assertThat(mapper.aDominio(mapper.aEntidad(pago))).isEqualTo(pago);
    }

    @Test
    void pagoNuevoGeneraEntidadSinIdYConDatosCopiados() {
        Pago nuevo = Pago.nuevo(
                10L, 3L, TipoPago.COBRO, FECHA, new BigDecimal("121.00"), MetodoPago.TARJETA, "REF-1", FECHA);

        PagoEntity entidad = mapper.aEntidad(nuevo);

        assertThat(entidad.getId()).isNull();
        assertThat(entidad.getFacturaId()).isEqualTo(10L);
        assertThat(entidad.getEmpresaId()).isEqualTo(3L);
        assertThat(entidad.getTipo()).isEqualTo("COBRO");
        assertThat(entidad.getFecha()).isEqualTo(FECHA);
        assertThat(entidad.getImporte()).isEqualByComparingTo("121.00");
        assertThat(entidad.getMetodo()).isEqualTo("TARJETA");
        assertThat(entidad.getReferencia()).isEqualTo("REF-1");
    }

    @Test
    void aDominioConservaLaReferenciaAusente() {
        PagoEntity entidad = new PagoEntity(
                2L, 10L, 3L, "PAGO", FECHA, new BigDecimal("5.00"), "EFECTIVO", null);

        assertThat(mapper.aDominio(entidad)).isEqualTo(new Pago(
                2L, 10L, 3L, TipoPago.PAGO, FECHA, new BigDecimal("5.00"), MetodoPago.EFECTIVO, null));
    }

    @Test
    void aDominioReconstruyeConElConstructorYAplicaLasInvariantes() {
        PagoEntity importeCero = new PagoEntity(
                2L, 10L, 3L, "PAGO", FECHA, BigDecimal.ZERO, "EFECTIVO", null);

        assertThatThrownBy(() -> mapper.aDominio(importeCero)).isInstanceOf(PagoInvalidoException.class);
    }
}

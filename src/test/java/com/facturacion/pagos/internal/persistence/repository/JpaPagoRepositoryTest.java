package com.facturacion.pagos.internal.persistence.repository;

import com.facturacion.pagos.internal.domain.model.MetodoPago;
import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.persistence.dao.PagoDao;
import com.facturacion.pagos.internal.persistence.entity.PagoEntity;
import com.facturacion.pagos.internal.persistence.mapper.PagoMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaPagoRepositoryTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    private final PagoDao dao = mock(PagoDao.class);
    private final JpaPagoRepository repository = new JpaPagoRepository(dao, new PagoMapper());

    private static PagoEntity entidad(Long id, String tipo, String importe) {
        return new PagoEntity(id, 10L, 3L, tipo, FECHA, new BigDecimal(importe), "TRANSFERENCIA", "REF-" + id);
    }

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(PagoEntity.class))).thenReturn(entidad(5L, "COBRO", "40.00"));
        Pago nuevo = Pago.nuevo(
                10L, 3L, TipoPago.COBRO, FECHA, new BigDecimal("40"), MetodoPago.TRANSFERENCIA, "REF-5", FECHA);

        Pago guardado = repository.guardar(nuevo);

        ArgumentCaptor<PagoEntity> enviada = ArgumentCaptor.forClass(PagoEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getFacturaId()).isEqualTo(10L);
        assertThat(enviada.getValue().getTipo()).isEqualTo("COBRO");
        assertThat(enviada.getValue().getImporte().toPlainString()).isEqualTo("40.00");
        assertThat(guardado.id()).isEqualTo(5L);
        assertThat(guardado.referencia()).isEqualTo("REF-5");
    }

    @Test
    void buscarPorIdConvierteLaEntidadEncontrada() {
        when(dao.findById(5L)).thenReturn(Optional.of(entidad(5L, "PAGO", "12.00")));

        assertThat(repository.buscarPorId(5L)).hasValueSatisfying(pago -> {
            assertThat(pago.id()).isEqualTo(5L);
            assertThat(pago.tipo()).isEqualTo(TipoPago.PAGO);
            assertThat(pago.importe()).isEqualByComparingTo("12.00");
        });
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void listarPorFacturaUsaLaConsultaDeLaFacturaOrdenada() {
        when(dao.findAllByFacturaIdOrderByFechaAscIdAsc(10L))
                .thenReturn(List.of(entidad(1L, "COBRO", "1.00"), entidad(2L, "COBRO", "2.00")));

        assertThat(repository.listarPorFactura(10L)).extracting(Pago::id).containsExactly(1L, 2L);
        verify(dao, never()).findAll();
    }

    @Test
    void listarPorEmpresaUsaLaConsultaDeLaEmpresaOrdenada() {
        when(dao.findAllByEmpresaIdOrderByFechaAscIdAsc(3L))
                .thenReturn(List.of(entidad(1L, "COBRO", "1.00"), entidad(2L, "PAGO", "2.00")));

        assertThat(repository.listarPorEmpresa(3L)).extracting(Pago::id).containsExactly(1L, 2L);
        verify(dao, never()).findAll();
    }

    @Test
    void listarPorEmpresaYTipoTrasladaElNombreDelTipo() {
        when(dao.findAllByEmpresaIdAndTipoOrderByFechaAscIdAsc(3L, "PAGO"))
                .thenReturn(List.of(entidad(2L, "PAGO", "2.00")));

        assertThat(repository.listarPorEmpresaYTipo(3L, TipoPago.PAGO)).extracting(Pago::id).containsExactly(2L);
        verify(dao, never()).findAllByEmpresaIdOrderByFechaAscIdAsc(anyLong());
    }

    @Test
    void listarPorEmpresaEntreFechasTrasladaElRango() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 12, 31);
        when(dao.findAllByEmpresaIdAndFechaBetweenOrderByFechaAscIdAsc(3L, desde, hasta))
                .thenReturn(List.of(entidad(1L, "COBRO", "1.00")));

        assertThat(repository.listarPorEmpresaEntreFechas(3L, desde, hasta))
                .extracting(Pago::id).containsExactly(1L);
    }

    @Test
    void eliminarBorraPorIdentificador() {
        repository.eliminar(5L);

        verify(dao).deleteById(5L);
        verify(dao, never()).deleteByFacturaId(anyLong());
    }

    @Test
    void eliminarPorFacturaBorraTodosLosPagosDeLaFactura() {
        repository.eliminarPorFactura(10L);

        verify(dao).deleteByFacturaId(10L);
        verify(dao, never()).deleteById(anyLong());
    }
}

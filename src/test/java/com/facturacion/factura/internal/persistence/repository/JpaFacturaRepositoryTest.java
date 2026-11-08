package com.facturacion.factura.internal.persistence.repository;

import com.facturacion.factura.internal.domain.model.Factura;
import com.facturacion.factura.internal.domain.model.LineaFactura;
import com.facturacion.factura.internal.domain.model.TipoFactura;
import com.facturacion.factura.internal.persistence.dao.FacturaDao;
import com.facturacion.factura.internal.persistence.entity.FacturaEntity;
import com.facturacion.factura.internal.persistence.entity.LineaFacturaEntity;
import com.facturacion.factura.internal.persistence.mapper.FacturaMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaFacturaRepositoryTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    private final FacturaDao dao = mock(FacturaDao.class);
    private final JpaFacturaRepository repository = new JpaFacturaRepository(dao, new FacturaMapper());

    private static FacturaEntity entidad(Long id, String numero) {
        FacturaEntity entidad = new FacturaEntity(
                id, numero, "EMITIDA", FECHA, null, 1L, "Empresa SL", "B00000001", "Calle Mayor 1", 10L, "Cliente SL", "B12345678", null, new BigDecimal("21.00"));
        entidad.agregarLinea(new LineaFacturaEntity(0, "Primera", new BigDecimal("2"), new BigDecimal("10.50")));
        entidad.agregarLinea(new LineaFacturaEntity(1, "Segunda", new BigDecimal("1"), new BigDecimal("4.25")));
        return entidad;
    }

    @Test
    void guardarConvierteAEntidadYDevuelveElDominioConElIdAsignado() {
        when(dao.save(any(FacturaEntity.class))).thenReturn(entidad(5L, "F-001"));
        Factura nueva = Factura.nueva(
                "F-001",
                TipoFactura.EMITIDA,
                FECHA,
                null,
                1L,
                "Empresa SL",
                "B00000001",
                "Calle Mayor 1, 28001, Madrid, Madrid, España",
                10L,
                "Cliente SL",
                "B12345678",
                "Avenida Diagonal 2, 08001, Barcelona, Barcelona, España",
                new BigDecimal("21.00"),
                List.of(
                        new LineaFactura("Primera", new BigDecimal("2"), new BigDecimal("10.50")),
                        new LineaFactura("Segunda", new BigDecimal("1"), new BigDecimal("4.25"))));

        Factura guardada = repository.guardar(nueva);

        ArgumentCaptor<FacturaEntity> enviada = ArgumentCaptor.forClass(FacturaEntity.class);
        verify(dao).save(enviada.capture());
        assertThat(enviada.getValue().getId()).isNull();
        assertThat(enviada.getValue().getNumero()).isEqualTo("F-001");
        assertThat(enviada.getValue().getLineas()).extracting(LineaFacturaEntity::getConcepto)
                .containsExactly("Primera", "Segunda");
        assertThat(guardada.id()).isEqualTo(5L);
        assertThat(guardada.lineas()).isEqualTo(nueva.lineas());
    }

    @Test
    void buscarPorIdCargaLasLineasEnLaMismaConsulta() {
        when(dao.findWithLineasById(5L)).thenReturn(Optional.of(entidad(5L, "F-001")));

        Optional<Factura> encontrada = repository.buscarPorId(5L);

        assertThat(encontrada).hasValueSatisfying(factura -> {
            assertThat(factura.id()).isEqualTo(5L);
            assertThat(factura.lineas()).extracting(LineaFactura::concepto).containsExactly("Primera", "Segunda");
        });
        verify(dao, never()).findById(any());
    }

    @Test
    void buscarPorIdInexistenteDevuelveVacio() {
        when(dao.findWithLineasById(99L)).thenReturn(Optional.empty());

        assertThat(repository.buscarPorId(99L)).isEmpty();
    }

    @Test
    void listarUsaLaConsultaOrdenadaPorIdConLineas() {
        when(dao.findAllByOrderByIdAsc()).thenReturn(List.of(entidad(1L, "F-001"), entidad(2L, "F-002")));

        List<Factura> facturas = repository.listar();

        assertThat(facturas).extracting(Factura::numero).containsExactly("F-001", "F-002");
        verify(dao, never()).findAll();
    }

    @Test
    void eliminarBorraPorId() {
        repository.eliminar(7L);

        verify(dao).deleteById(7L);
    }

    @Test
    void existePorNumeroUsaLaConsultaDeExistencia() {
        when(dao.existsByNumero("F-001")).thenReturn(true);

        assertThat(repository.existePorNumero("F-001")).isTrue();
        assertThat(repository.existePorNumero("F-002")).isFalse();
        verify(dao, never()).findAll();
    }
}

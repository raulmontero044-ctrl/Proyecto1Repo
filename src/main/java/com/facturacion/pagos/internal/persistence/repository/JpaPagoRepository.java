package com.facturacion.pagos.internal.persistence.repository;

import com.facturacion.pagos.internal.domain.model.Pago;
import com.facturacion.pagos.internal.domain.model.TipoPago;
import com.facturacion.pagos.internal.domain.repository.PagoRepository;
import com.facturacion.pagos.internal.persistence.dao.PagoDao;
import com.facturacion.pagos.internal.persistence.entity.PagoEntity;
import com.facturacion.pagos.internal.persistence.mapper.PagoMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaPagoRepository implements PagoRepository {

    private final PagoDao dao;
    private final PagoMapper mapper;

    public JpaPagoRepository(PagoDao dao, PagoMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Pago guardar(Pago pago) {
        return mapper.aDominio(dao.save(mapper.aEntidad(pago)));
    }

    @Override
    public Optional<Pago> buscarPorId(Long id) {
        return dao.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Pago> listarPorFactura(Long facturaId) {
        return aDominio(dao.findAllByFacturaIdOrderByFechaAscIdAsc(facturaId));
    }

    @Override
    public List<Pago> listarPorEmpresa(Long empresaId) {
        return aDominio(dao.findAllByEmpresaIdOrderByFechaAscIdAsc(empresaId));
    }

    @Override
    public List<Pago> listarPorEmpresaYTipo(Long empresaId, TipoPago tipo) {
        return aDominio(dao.findAllByEmpresaIdAndTipoOrderByFechaAscIdAsc(empresaId, tipo.name()));
    }

    @Override
    public List<Pago> listarPorEmpresaEntreFechas(Long empresaId, LocalDate desde, LocalDate hasta) {
        return aDominio(dao.findAllByEmpresaIdAndFechaBetweenOrderByFechaAscIdAsc(empresaId, desde, hasta));
    }

    @Override
    public void eliminar(Long id) {
        dao.deleteById(id);
    }

    @Override
    public void eliminarPorFactura(Long facturaId) {
        dao.deleteByFacturaId(facturaId);
    }

    private List<Pago> aDominio(List<PagoEntity> entidades) {
        return entidades.stream().map(mapper::aDominio).toList();
    }
}

package com.facturacion.factura.persistence;

import com.facturacion.factura.domain.model.Factura;
import com.facturacion.factura.domain.repository.FacturaRepository;
import com.facturacion.factura.persistence.dao.FacturaDao;
import com.facturacion.factura.persistence.mapper.FacturaMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaFacturaRepository implements FacturaRepository {

    private final FacturaDao dao;
    private final FacturaMapper mapper;

    public JpaFacturaRepository(FacturaDao dao, FacturaMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Factura guardar(Factura factura) {
        return mapper.aDominio(dao.save(mapper.aEntidad(factura)));
    }

    @Override
    public Optional<Factura> buscarPorId(Long id) {
        return dao.findWithLineasById(id).map(mapper::aDominio);
    }

    @Override
    public List<Factura> listar() {
        return dao.findAllByOrderByIdAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public void eliminar(Long id) {
        dao.deleteById(id);
    }

    @Override
    public boolean existePorNumero(String numero) {
        return dao.existsByNumero(numero);
    }
}

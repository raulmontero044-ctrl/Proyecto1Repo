package com.facturacion.empresas.internal.persistence.repository;

import com.facturacion.empresas.internal.domain.model.Empresa;
import com.facturacion.empresas.internal.domain.repository.EmpresaRepository;
import com.facturacion.empresas.internal.persistence.dao.EmpresaDao;
import com.facturacion.empresas.internal.persistence.mapper.EmpresaMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaEmpresaRepository implements EmpresaRepository {

    private final EmpresaDao dao;
    private final EmpresaMapper mapper;

    public JpaEmpresaRepository(EmpresaDao dao, EmpresaMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        return mapper.aDominio(dao.save(mapper.aEntidad(empresa)));
    }

    @Override
    public Optional<Empresa> buscarPorId(Long id) {
        return dao.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Empresa> listar() {
        return dao.findAllByOrderByIdAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existePorNif(String nif) {
        return dao.existsByNif(nif);
    }

    @Override
    public boolean existePorNifEnOtraEmpresa(String nif, Long id) {
        return dao.existsByNifAndIdNot(nif, id);
    }
}

package com.facturacion.terceros.internal.persistence.repository;

import com.facturacion.terceros.internal.domain.model.Tercero;
import com.facturacion.terceros.internal.domain.model.TipoTercero;
import com.facturacion.terceros.internal.domain.repository.TerceroRepository;
import com.facturacion.terceros.internal.persistence.dao.TerceroDao;
import com.facturacion.terceros.internal.persistence.entity.TerceroEntity;
import com.facturacion.terceros.internal.persistence.mapper.TerceroMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaTerceroRepository implements TerceroRepository {

    private final TerceroDao dao;
    private final TerceroMapper mapper;

    public JpaTerceroRepository(TerceroDao dao, TerceroMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Tercero guardar(Tercero tercero) {
        return mapper.aDominio(dao.save(mapper.aEntidad(tercero)));
    }

    @Override
    public Optional<Tercero> buscarPorId(Long id) {
        return dao.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Tercero> listarPorEmpresa(Long empresaId) {
        return aDominio(dao.findAllByEmpresaIdOrderByIdAsc(empresaId));
    }

    @Override
    public List<Tercero> listarPorEmpresaYTipo(Long empresaId, TipoTercero tipo) {
        return aDominio(switch (tipo) {
            case CLIENTE -> dao.findAllByEmpresaIdAndEsClienteTrueOrderByIdAsc(empresaId);
            case PROVEEDOR -> dao.findAllByEmpresaIdAndEsProveedorTrueOrderByIdAsc(empresaId);
        });
    }

    @Override
    public boolean existePorEmpresaYNif(Long empresaId, String nif) {
        return dao.existsByEmpresaIdAndNif(empresaId, nif);
    }

    @Override
    public boolean existePorEmpresaYNifEnOtroTercero(Long empresaId, String nif, Long id) {
        return dao.existsByEmpresaIdAndNifAndIdNot(empresaId, nif, id);
    }

    private List<Tercero> aDominio(List<TerceroEntity> entidades) {
        return entidades.stream().map(mapper::aDominio).toList();
    }
}

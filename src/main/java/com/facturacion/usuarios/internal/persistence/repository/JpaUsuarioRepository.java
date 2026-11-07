package com.facturacion.usuarios.internal.persistence.repository;

import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import com.facturacion.usuarios.internal.persistence.dao.UsuarioDao;
import com.facturacion.usuarios.internal.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaUsuarioRepository implements UsuarioRepository {

    private final UsuarioDao dao;
    private final UsuarioMapper mapper;

    public JpaUsuarioRepository(UsuarioDao dao, UsuarioMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return mapper.aDominio(dao.save(mapper.aEntidad(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return dao.findWithAccesosById(id).map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return dao.findWithAccesosByEmail(email).map(mapper::aDominio);
    }

    @Override
    public List<Usuario> listar() {
        return dao.findAllByOrderByIdAsc().stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Usuario> listarPorEmpresa(Long empresaId) {
        return dao.findAllConAccesoAEmpresa(empresaId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existePorEmail(String email) {
        return dao.existsByEmail(email);
    }

    @Override
    public boolean existePorEmailEnOtroUsuario(String email, Long id) {
        return dao.existsByEmailAndIdNot(email, id);
    }
}

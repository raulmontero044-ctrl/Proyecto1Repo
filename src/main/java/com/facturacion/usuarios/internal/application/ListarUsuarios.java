package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListarUsuarios {

    private final UsuarioRepository repository;

    public ListarUsuarios(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> ejecutar(Long empresaId) {
        List<Usuario> usuarios = empresaId == null ? repository.listar() : repository.listarPorEmpresa(empresaId);
        return usuarios.stream().map(UsuarioDtoConverter::aDto).toList();
    }
}

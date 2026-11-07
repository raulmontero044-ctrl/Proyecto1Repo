package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.DatosUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.EmailUsuarioDuplicadoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModificarUsuario {

    private final UsuarioRepository repository;

    public ModificarUsuario(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UsuarioDto ejecutar(Long id, DatosUsuarioDto datos) {
        Usuario modificado = repository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id))
                .conDatos(datos.nombre(), datos.email());
        if (repository.existePorEmailEnOtroUsuario(modificado.email(), id)) {
            throw new EmailUsuarioDuplicadoException();
        }
        return UsuarioDtoConverter.aDto(repository.guardar(modificado));
    }
}

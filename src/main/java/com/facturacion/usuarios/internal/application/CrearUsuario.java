package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.api.facade.dto.NuevoUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.domain.exception.EmailUsuarioDuplicadoException;
import com.facturacion.usuarios.internal.domain.model.AccesoEmpresa;
import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrearUsuario {

    private final UsuarioRepository repository;
    private final CifradorPassword cifrador;
    private final ComprobadorEmpresa comprobadorEmpresa;

    CrearUsuario(UsuarioRepository repository, CifradorPassword cifrador, ComprobadorEmpresa comprobadorEmpresa) {
        this.repository = repository;
        this.cifrador = cifrador;
        this.comprobadorEmpresa = comprobadorEmpresa;
    }

    @Transactional
    public UsuarioDto ejecutar(NuevoUsuarioDto nuevoUsuario) {
        PasswordEnClaro password = new PasswordEnClaro(nuevoUsuario.password());
        List<AccesoEmpresa> accesos = UsuarioDtoConverter.aAccesos(nuevoUsuario.accesos());
        accesos.forEach(acceso -> comprobadorEmpresa.exigirExistente(acceso.empresaId()));
        Usuario usuario = Usuario.nuevo(
                nuevoUsuario.email(), nuevoUsuario.nombre(), cifrador.cifrar(password), accesos);
        if (repository.existePorEmail(usuario.email())) {
            throw new EmailUsuarioDuplicadoException();
        }
        return UsuarioDtoConverter.aDto(repository.guardar(usuario));
    }
}

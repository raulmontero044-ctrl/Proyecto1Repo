package com.facturacion.usuarios.internal.application;

import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import com.facturacion.usuarios.internal.domain.model.Usuario;
import com.facturacion.usuarios.internal.domain.repository.UsuarioRepository;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearAdministradorInicial {

    static final String NOMBRE = "Administrador";

    private final UsuarioRepository repository;
    private final CifradorPassword cifrador;

    public CrearAdministradorInicial(UsuarioRepository repository, CifradorPassword cifrador) {
        this.repository = repository;
        this.cifrador = cifrador;
    }

    @Transactional
    public void ejecutar(String email, String password) {
        if (estaVacio(email) || estaVacio(password) || repository.existeAlguno()) {
            return;
        }
        String passwordHash = cifrador.cifrar(new PasswordEnClaro(password));
        repository.guardar(Usuario.nuevoAdministrador(email, NOMBRE, passwordHash));
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}

package com.facturacion.usuarios.internal.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class CrearAdministradorInicialAlArrancar implements ApplicationRunner {

    private final CrearAdministradorInicial crearAdministradorInicial;
    private final String email;
    private final String password;

    CrearAdministradorInicialAlArrancar(
            CrearAdministradorInicial crearAdministradorInicial,
            @Value("${facturacion.administrador-inicial.email:}") String email,
            @Value("${facturacion.administrador-inicial.password:}") String password) {
        this.crearAdministradorInicial = crearAdministradorInicial;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        crearAdministradorInicial.ejecutar(email, password);
    }
}

package com.facturacion.auth.internal.domain.service;

import com.facturacion.auth.internal.domain.model.Rol;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraRolEfectivoTest {

    @Test
    void sinAccesosNiMarcaDeAdministradorNoHayRol() {
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of())).isEmpty();
    }

    @Test
    void conUnSoloAccesoEsElRolDeEseAcceso() {
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of(Rol.CONSULTA))).contains(Rol.CONSULTA);
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of(Rol.GESTOR))).contains(Rol.GESTOR);
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of(Rol.ADMINISTRADOR))).contains(Rol.ADMINISTRADOR);
    }

    @Test
    void elGestorPrevaleceSobreConsulta() {
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of(Rol.CONSULTA, Rol.GESTOR, Rol.CONSULTA)))
                .contains(Rol.GESTOR);
    }

    @Test
    void elAdministradorPrevaleceSobreLosDemas() {
        assertThat(CalculadoraRolEfectivo.calcular(false, List.of(Rol.GESTOR, Rol.ADMINISTRADOR, Rol.CONSULTA)))
                .contains(Rol.ADMINISTRADOR);
    }

    @Test
    void laMarcaDeAdministradorDaElRolAdministradorSinAccesos() {
        assertThat(CalculadoraRolEfectivo.calcular(true, List.of())).contains(Rol.ADMINISTRADOR);
    }

    @Test
    void laMarcaDeAdministradorPrevaleceSobreRolesInferiores() {
        assertThat(CalculadoraRolEfectivo.calcular(true, List.of(Rol.CONSULTA))).contains(Rol.ADMINISTRADOR);
    }
}

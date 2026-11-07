package com.facturacion.empresas.internal.application;

import com.facturacion.empresas.api.facade.dto.ContactoEmpresaDto;
import com.facturacion.empresas.api.facade.dto.DatosFiscalesEmpresaDto;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.api.facade.dto.NuevaEmpresaDto;
import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import com.facturacion.empresas.internal.domain.exception.NifEmpresaDuplicadoException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoEmpresaTest {

    private final EmpresaRepositoryEnMemoria repository = new EmpresaRepositoryEnMemoria();
    private final CrearEmpresa crear = new CrearEmpresa(repository);
    private final ObtenerEmpresa obtener = new ObtenerEmpresa(repository);
    private final ListarEmpresas listar = new ListarEmpresas(repository);
    private final ModificarDatosFiscalesEmpresa modificarDatosFiscales = new ModificarDatosFiscalesEmpresa(repository);
    private final ModificarContactoEmpresa modificarContacto = new ModificarContactoEmpresa(repository);
    private final BuscarEmpresa buscar = new BuscarEmpresa(repository);

    private static NuevaEmpresaDto nueva(String nif) {
        return new NuevaEmpresaDto(
                "Empresa SL", nif, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000");
    }

    private static DatosFiscalesEmpresaDto datosFiscales(String nif) {
        return new DatosFiscalesEmpresaDto("Otra SA", nif, "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal");
    }

    @Test
    void creaEmpresaActivaConTodosSusDatos() {
        EmpresaDto creada = crear.ejecutar(nueva("B12345678"));

        assertThat(creada).isEqualTo(new EmpresaDto(
                creada.id(), "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", true));
        assertThat(creada.id()).isNotNull();
        assertThat(obtener.ejecutar(creada.id())).isEqualTo(creada);
    }

    @Test
    void creaEmpresaSinContacto() {
        EmpresaDto creada = crear.ejecutar(new NuevaEmpresaDto(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null));

        assertThat(creada.email()).isNull();
        assertThat(creada.telefono()).isNull();
        assertThat(creada.activa()).isTrue();
    }

    @Test
    void rechazaNifDuplicadoSinGuardarOtraEmpresa() {
        crear.ejecutar(nueva("B12345678"));

        assertThatThrownBy(() -> crear.ejecutar(nueva("B12345678")))
                .isInstanceOf(NifEmpresaDuplicadoException.class)
                .hasMessage("Ya existe otra empresa con ese NIF");
        assertThat(listar.ejecutar()).hasSize(1);
    }

    @Test
    void rechazaEmpresaInvalidaSinGuardarla() {
        NuevaEmpresaDto sinRazonSocial = new NuevaEmpresaDto(
                " ", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null);

        assertThatThrownBy(() -> crear.ejecutar(sinRazonSocial)).isInstanceOf(EmpresaInvalidaException.class);
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void obtieneEmpresaExistenteOFalla() {
        Long id = crear.ejecutar(nueva("B12345678")).id();

        assertThat(obtener.ejecutar(id).nif()).isEqualTo("B12345678");
        assertThatThrownBy(() -> obtener.ejecutar(99L))
                .isInstanceOf(EmpresaNoEncontradaException.class)
                .hasMessage("No existe la empresa con id 99");
    }

    @Test
    void listaVaciaSinEmpresas() {
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void listaEmpresasEnOrdenDeCreacion() {
        crear.ejecutar(nueva("B11111111"));
        crear.ejecutar(nueva("B22222222"));

        assertThat(listar.ejecutar()).extracting(EmpresaDto::nif).containsExactly("B11111111", "B22222222");
    }

    @Test
    void modificaLosDatosFiscalesConservandoContactoYEstado() {
        Long id = crear.ejecutar(nueva("B12345678")).id();

        EmpresaDto modificada = modificarDatosFiscales.ejecutar(id, datosFiscales("A87654321"));

        EmpresaDto esperada = new EmpresaDto(
                id, "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "info@empresa.es", "910000000", true);
        assertThat(modificada).isEqualTo(esperada);
        assertThat(obtener.ejecutar(id)).isEqualTo(esperada);
    }

    @Test
    void modificarDatosFiscalesPermiteConservarElPropioNif() {
        Long id = crear.ejecutar(nueva("B12345678")).id();

        EmpresaDto modificada = modificarDatosFiscales.ejecutar(id, datosFiscales("B12345678"));

        assertThat(modificada.nif()).isEqualTo("B12345678");
        assertThat(modificada.razonSocial()).isEqualTo("Otra SA");
    }

    @Test
    void modificarDatosFiscalesRechazaElNifDeOtraEmpresaSinCambiarNada() {
        Long primera = crear.ejecutar(nueva("B11111111")).id();
        crear.ejecutar(nueva("B22222222"));

        assertThatThrownBy(() -> modificarDatosFiscales.ejecutar(primera, datosFiscales("B22222222")))
                .isInstanceOf(NifEmpresaDuplicadoException.class);
        assertThat(obtener.ejecutar(primera).nif()).isEqualTo("B11111111");
        assertThat(obtener.ejecutar(primera).razonSocial()).isEqualTo("Empresa SL");
    }

    @Test
    void modificarDatosFiscalesDeEmpresaInexistenteFalla() {
        assertThatThrownBy(() -> modificarDatosFiscales.ejecutar(99L, datosFiscales("B12345678")))
                .isInstanceOf(EmpresaNoEncontradaException.class);
        assertThat(listar.ejecutar()).isEmpty();
    }

    @Test
    void modificarDatosFiscalesInvalidosNoCambiaNada() {
        Long id = crear.ejecutar(nueva("B12345678")).id();
        DatosFiscalesEmpresaDto sinPais = new DatosFiscalesEmpresaDto(
                "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "");

        assertThatThrownBy(() -> modificarDatosFiscales.ejecutar(id, sinPais))
                .isInstanceOf(EmpresaInvalidaException.class);
        assertThat(obtener.ejecutar(id).nif()).isEqualTo("B12345678");
    }

    @Test
    void modificaElContactoConservandoLosDatosFiscales() {
        Long id = crear.ejecutar(nueva("B12345678")).id();

        EmpresaDto modificada = modificarContacto.ejecutar(id, new ContactoEmpresaDto("nuevo@empresa.es", "600000000"));

        EmpresaDto esperada = new EmpresaDto(
                id, "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "nuevo@empresa.es", "600000000", true);
        assertThat(modificada).isEqualTo(esperada);
        assertThat(obtener.ejecutar(id)).isEqualTo(esperada);
    }

    @Test
    void modificarContactoPermiteVaciarlo() {
        Long id = crear.ejecutar(nueva("B12345678")).id();

        EmpresaDto modificada = modificarContacto.ejecutar(id, new ContactoEmpresaDto(null, null));

        assertThat(modificada.email()).isNull();
        assertThat(modificada.telefono()).isNull();
    }

    @Test
    void buscaEmpresaExistenteConTodosSusDatos() {
        EmpresaDto creada = crear.ejecutar(nueva("B12345678"));

        assertThat(buscar.ejecutar(creada.id())).contains(creada);
    }

    @Test
    void buscarEmpresaInexistenteDevuelveVacioSinLanzarExcepcion() {
        crear.ejecutar(nueva("B12345678"));

        assertThat(buscar.ejecutar(99L)).isEmpty();
    }

    @Test
    void modificarContactoDeEmpresaInexistenteFalla() {
        assertThatThrownBy(() -> modificarContacto.ejecutar(99L, new ContactoEmpresaDto("a@b.es", null)))
                .isInstanceOf(EmpresaNoEncontradaException.class);
        assertThat(listar.ejecutar()).isEmpty();
    }
}

package com.facturacion.terceros.internal.application;

import com.facturacion.empresas.api.facade.EmpresaFacade;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.terceros.api.facade.dto.DatosTerceroDto;
import com.facturacion.terceros.api.facade.dto.NuevoTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.domain.exception.EmpresaDelTerceroInexistenteException;
import com.facturacion.terceros.internal.domain.exception.NifTerceroDuplicadoException;
import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import com.facturacion.terceros.internal.domain.model.Tercero;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoTerceroTest {

    private static final Set<Long> EMPRESAS_EXISTENTES = Set.of(1L, 2L);

    private final TerceroRepositoryEnMemoria repository = new TerceroRepositoryEnMemoria();
    private final List<Long> empresasConsultadas = new ArrayList<>();
    private final EmpresaFacade empresaFacade = id -> {
        empresasConsultadas.add(id);
        return EMPRESAS_EXISTENTES.contains(id) ? Optional.of(empresa(id)) : Optional.empty();
    };

    private final CrearTercero crearTercero = new CrearTercero(repository, empresaFacade);
    private final ObtenerTercero obtenerTercero = new ObtenerTercero(repository);
    private final ListarTerceros listarTerceros = new ListarTerceros(repository);
    private final ModificarTercero modificarTercero = new ModificarTercero(repository);
    private final DesactivarTercero desactivarTercero = new DesactivarTercero(repository);

    private static EmpresaDto empresa(Long id) {
        return new EmpresaDto(
                id, "Empresa SL", "B1234567" + id, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                null, null, true);
    }

    private static NuevoTerceroDto nuevo(Long empresaId, String nif, boolean esCliente, boolean esProveedor) {
        return new NuevoTerceroDto(
                empresaId, nif, "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", esCliente, esProveedor);
    }

    private static DatosTerceroDto datos(String nif, boolean esCliente, boolean esProveedor) {
        return new DatosTerceroDto(
                nif, "Otro SA", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "otro@tercero.es", "600000000", esCliente, esProveedor);
    }

    private Long guardado(Long empresaId, String nif, boolean esCliente, boolean esProveedor) {
        return repository.guardar(new Tercero(
                null, empresaId, nif, "Tercero SL", null, null, null, null, null, null, null,
                esCliente, esProveedor, true)).id();
    }

    @Test
    void crearDevuelveElTerceroActivoConTodosSusDatos() {
        TerceroDto creado = crearTercero.ejecutar(nuevo(1L, "B12345678", true, true));

        assertThat(creado).isEqualTo(new TerceroDto(
                1L, 1L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, true, true));
        assertThat(repository.buscarPorId(1L)).map(Tercero::activo).contains(true);
        assertThat(empresasConsultadas).containsExactly(1L);
    }

    @Test
    void crearAceptaElMismoNifEnOtraEmpresa() {
        guardado(1L, "B12345678", true, false);

        TerceroDto creado = crearTercero.ejecutar(nuevo(2L, "B12345678", true, false));

        assertThat(creado.empresaId()).isEqualTo(2L);
        assertThat(repository.listarPorEmpresa(1L)).hasSize(1);
        assertThat(repository.listarPorEmpresa(2L)).hasSize(1);
    }

    @Test
    void crearRechazaUnNifDuplicadoEnLaMismaEmpresaSinGuardar() {
        guardado(1L, "B12345678", false, true);

        assertThatThrownBy(() -> crearTercero.ejecutar(nuevo(1L, "B12345678", true, false)))
                .isInstanceOf(NifTerceroDuplicadoException.class)
                .hasMessage("Ya existe otro tercero con ese NIF en la empresa");
        assertThat(repository.listarPorEmpresa(1L)).hasSize(1);
    }

    @Test
    void crearRechazaUnaEmpresaInexistenteSinGuardar() {
        assertThatThrownBy(() -> crearTercero.ejecutar(nuevo(9L, "B12345678", true, false)))
                .isInstanceOf(EmpresaDelTerceroInexistenteException.class)
                .hasMessage("No existe la empresa con id 9");
        assertThat(repository.listarPorEmpresa(9L)).isEmpty();
        assertThat(empresasConsultadas).containsExactly(9L);
    }

    @Test
    void crearRechazaUnTerceroQueNoEsClienteNiProveedorSinConsultarLaEmpresa() {
        assertThatThrownBy(() -> crearTercero.ejecutar(nuevo(1L, "B12345678", false, false)))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El tercero debe ser cliente, proveedor o ambos");
        assertThat(repository.listarPorEmpresa(1L)).isEmpty();
        assertThat(empresasConsultadas).isEmpty();
    }

    @Test
    void crearRechazaUnTerceroSinEmpresa() {
        assertThatThrownBy(() -> crearTercero.ejecutar(nuevo(null, "B12345678", true, false)))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("La empresa del tercero es obligatoria");
        assertThat(empresasConsultadas).isEmpty();
    }

    @Test
    void obtenerDevuelveElTercero() {
        Long id = guardado(1L, "B12345678", false, true);

        assertThat(obtenerTercero.ejecutar(id)).isEqualTo(new TerceroDto(
                id, 1L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, false, true, true));
    }

    @Test
    void obtenerUnTerceroInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> obtenerTercero.ejecutar(99L))
                .isInstanceOf(TerceroNoEncontradoException.class)
                .hasMessage("No existe el tercero con id 99");
    }

    @Test
    void listarSinTipoDevuelveSoloLosTercerosDeLaEmpresa() {
        Long cliente = guardado(1L, "B11111111", true, false);
        Long proveedor = guardado(1L, "B22222222", false, true);
        guardado(2L, "B33333333", true, true);

        assertThat(listarTerceros.ejecutar(1L, null)).extracting(TerceroDto::id).containsExactly(cliente, proveedor);
    }

    @Test
    void listarClientesIncluyeLosQueSonClienteYProveedor() {
        Long cliente = guardado(1L, "B11111111", true, false);
        guardado(1L, "B22222222", false, true);
        Long ambos = guardado(1L, "B33333333", true, true);
        guardado(2L, "B44444444", true, false);

        assertThat(listarTerceros.ejecutar(1L, "CLIENTE")).extracting(TerceroDto::id).containsExactly(cliente, ambos);
    }

    @Test
    void listarProveedoresIncluyeLosQueSonClienteYProveedor() {
        guardado(1L, "B11111111", true, false);
        Long proveedor = guardado(1L, "B22222222", false, true);
        Long ambos = guardado(1L, "B33333333", true, true);

        assertThat(listarTerceros.ejecutar(1L, "PROVEEDOR"))
                .extracting(TerceroDto::id)
                .containsExactly(proveedor, ambos);
    }

    @Test
    void listarUnaEmpresaSinTercerosDevuelveListaVacia() {
        guardado(1L, "B11111111", true, false);

        assertThat(listarTerceros.ejecutar(2L, null)).isEmpty();
        assertThat(listarTerceros.ejecutar(2L, "CLIENTE")).isEmpty();
    }

    @Test
    void listarRechazaUnTipoDesconocido() {
        assertThatThrownBy(() -> listarTerceros.ejecutar(1L, "AMBOS"))
                .isInstanceOf(TerceroInvalidoException.class)
                .hasMessage("El tipo de tercero no es válido");
    }

    @Test
    void modificarCambiaLosDatosYConservaEmpresaYEstado() {
        Long id = guardado(1L, "B12345678", true, false);
        desactivarTercero.ejecutar(id);

        TerceroDto modificado = modificarTercero.ejecutar(id, datos("A87654321", false, true));

        TerceroDto esperado = new TerceroDto(
                id, 1L, "A87654321", "Otro SA", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal",
                "otro@tercero.es", "600000000", false, true, false);
        assertThat(modificado).isEqualTo(esperado);
        assertThat(obtenerTercero.ejecutar(id)).isEqualTo(esperado);
    }

    @Test
    void modificarConservandoSuPropioNifNoEsDuplicado() {
        Long id = guardado(1L, "B12345678", true, false);

        TerceroDto modificado = modificarTercero.ejecutar(id, datos("B12345678", true, true));

        assertThat(modificado.nif()).isEqualTo("B12345678");
        assertThat(modificado.esProveedor()).isTrue();
    }

    @Test
    void modificarAceptaUnNifQueSoloExisteEnOtraEmpresa() {
        guardado(2L, "A87654321", true, false);
        Long id = guardado(1L, "B12345678", true, false);

        assertThat(modificarTercero.ejecutar(id, datos("A87654321", true, false)).nif()).isEqualTo("A87654321");
    }

    @Test
    void modificarRechazaUnNifDeOtroTerceroDeLaEmpresaSinGuardar() {
        guardado(1L, "A87654321", true, false);
        Long id = guardado(1L, "B12345678", true, false);

        assertThatThrownBy(() -> modificarTercero.ejecutar(id, datos("A87654321", true, false)))
                .isInstanceOf(NifTerceroDuplicadoException.class);
        assertThat(repository.buscarPorId(id)).map(Tercero::nif).contains("B12345678");
    }

    @Test
    void modificarRechazaDejarDeSerClienteYProveedorSinGuardar() {
        Long id = guardado(1L, "B12345678", true, false);

        assertThatThrownBy(() -> modificarTercero.ejecutar(id, datos("B12345678", false, false)))
                .isInstanceOf(TerceroInvalidoException.class);
        assertThat(repository.buscarPorId(id)).map(Tercero::esCliente).contains(true);
    }

    @Test
    void modificarUnTerceroInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> modificarTercero.ejecutar(99L, datos("B12345678", true, false)))
                .isInstanceOf(TerceroNoEncontradoException.class)
                .hasMessage("No existe el tercero con id 99");
    }

    @Test
    void desactivarSoloDesactivaElTerceroIndicadoSinBorrarlo() {
        Long id = guardado(1L, "B11111111", true, false);
        Long otro = guardado(1L, "B22222222", true, false);

        desactivarTercero.ejecutar(id);

        assertThat(repository.buscarPorId(id)).map(Tercero::activo).contains(false);
        assertThat(repository.buscarPorId(otro)).map(Tercero::activo).contains(true);
        assertThat(listarTerceros.ejecutar(1L, null)).hasSize(2);
    }

    @Test
    void desactivarUnTerceroInexistenteLanzaNoEncontrado() {
        assertThatThrownBy(() -> desactivarTercero.ejecutar(99L))
                .isInstanceOf(TerceroNoEncontradoException.class)
                .hasMessage("No existe el tercero con id 99");
    }
}

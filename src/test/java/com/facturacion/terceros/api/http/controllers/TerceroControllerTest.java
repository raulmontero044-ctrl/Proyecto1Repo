package com.facturacion.terceros.api.http.controllers;

import com.facturacion.app.ConfiguracionReloj;
import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.terceros.api.facade.dto.DatosTerceroDto;
import com.facturacion.terceros.api.facade.dto.NuevoTerceroDto;
import com.facturacion.terceros.api.facade.dto.TerceroDto;
import com.facturacion.terceros.internal.application.CrearTercero;
import com.facturacion.terceros.internal.application.DesactivarTercero;
import com.facturacion.terceros.internal.application.ListarTerceros;
import com.facturacion.terceros.internal.application.ModificarTercero;
import com.facturacion.terceros.internal.application.ObtenerTercero;
import com.facturacion.terceros.internal.domain.exception.EmpresaDelTerceroInexistenteException;
import com.facturacion.terceros.internal.domain.exception.NifTerceroDuplicadoException;
import com.facturacion.terceros.internal.domain.exception.TerceroInvalidoException;
import com.facturacion.terceros.internal.domain.exception.TerceroNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TerceroController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
@WithMockUser(roles = "ADMINISTRADOR")
class TerceroControllerTest {

    private static final String EMAIL_DE_200 =
            "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(7);
    private static final String EMAIL_DE_201 =
            "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(8);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearTercero crearTercero;

    @MockitoBean
    private ObtenerTercero obtenerTercero;

    @MockitoBean
    private ListarTerceros listarTerceros;

    @MockitoBean
    private ModificarTercero modificarTercero;

    @MockitoBean
    private DesactivarTercero desactivarTercero;

    private static Map<String, String> datosValidos() {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("nif", texto("B12345678"));
        campos.put("razonSocial", texto("Tercero SL"));
        campos.put("direccion", texto("Calle Mayor 1"));
        campos.put("codigoPostal", texto("28001"));
        campos.put("municipio", texto("Madrid"));
        campos.put("provincia", texto("Madrid"));
        campos.put("pais", texto("España"));
        campos.put("email", texto("info@tercero.es"));
        campos.put("telefono", texto("910000000"));
        campos.put("esCliente", "true");
        campos.put("esProveedor", "false");
        return campos;
    }

    private static Map<String, String> creacionValida() {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("empresaId", "3");
        campos.putAll(datosValidos());
        return campos;
    }

    private static String texto(String valor) {
        return "\"" + valor + "\"";
    }

    private static String json(Map<String, String> campos) {
        return campos.entrySet().stream()
                .map(campo -> "\"" + campo.getKey() + "\": " + campo.getValue())
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String conCampo(Map<String, String> campos, String nombre, String valorJson) {
        Map<String, String> copia = new LinkedHashMap<>(campos);
        if (valorJson == null) {
            copia.remove(nombre);
        } else {
            copia.put(nombre, valorJson);
        }
        return json(copia);
    }

    private static TerceroDto tercero(Long id) {
        return new TerceroDto(
                id, 3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false, true);
    }

    private static Stream<Arguments> datosInvalidos() {
        return Stream.of(
                Arguments.of("nif", null),
                Arguments.of("nif", "null"),
                Arguments.of("nif", texto("")),
                Arguments.of("nif", texto("   ")),
                Arguments.of("nif", texto("N".repeat(21))),
                Arguments.of("razonSocial", null),
                Arguments.of("razonSocial", texto(" ")),
                Arguments.of("razonSocial", texto("R".repeat(201))),
                Arguments.of("direccion", texto("D".repeat(301))),
                Arguments.of("codigoPostal", texto("C".repeat(11))),
                Arguments.of("municipio", texto("M".repeat(101))),
                Arguments.of("provincia", texto("P".repeat(101))),
                Arguments.of("pais", texto("S".repeat(101))),
                Arguments.of("email", texto("no-es-un-email")),
                Arguments.of("email", texto(EMAIL_DE_201)),
                Arguments.of("telefono", texto("T".repeat(31))),
                Arguments.of("esCliente", null),
                Arguments.of("esCliente", "null"),
                Arguments.of("esProveedor", null),
                Arguments.of("esProveedor", "null"));
    }

    private static Stream<Arguments> creacionesInvalidas() {
        return Stream.concat(
                Stream.of(
                        Arguments.of("empresaId", null),
                        Arguments.of("empresaId", "null"),
                        Arguments.of("empresaId", texto("abc"))),
                datosInvalidos());
    }

    @Test
    void creaTerceroYDevuelve201ConLocationYCuerpoCompleto() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class))).thenReturn(tercero(1L));

        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content(json(creacionValida())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/terceros/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empresaId").value(3))
                .andExpect(jsonPath("$.nif").value("B12345678"))
                .andExpect(jsonPath("$.razonSocial").value("Tercero SL"))
                .andExpect(jsonPath("$.direccion").value("Calle Mayor 1"))
                .andExpect(jsonPath("$.codigoPostal").value("28001"))
                .andExpect(jsonPath("$.municipio").value("Madrid"))
                .andExpect(jsonPath("$.provincia").value("Madrid"))
                .andExpect(jsonPath("$.pais").value("España"))
                .andExpect(jsonPath("$.email").value("info@tercero.es"))
                .andExpect(jsonPath("$.telefono").value("910000000"))
                .andExpect(jsonPath("$.esCliente").value(true))
                .andExpect(jsonPath("$.esProveedor").value(false))
                .andExpect(jsonPath("$.activo").value(true));

        verify(crearTercero).ejecutar(new NuevoTerceroDto(
                3L, "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false));
    }

    @Test
    void crearAceptaLosDatosOpcionalesAusentes() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class))).thenReturn(tercero(1L));
        String cuerpo = """
                {"empresaId": 3, "nif": "B12345678", "razonSocial": "Tercero SL",
                 "esCliente": false, "esProveedor": true}
                """;

        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());

        verify(crearTercero).ejecutar(new NuevoTerceroDto(
                3L, "B12345678", "Tercero SL", null, null, null, null, null, null, null, false, true));
    }

    @Test
    void crearAceptaValoresEnElLimiteDeSusColumnas() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class))).thenReturn(tercero(1L));
        Map<String, String> campos = creacionValida();
        campos.put("nif", texto("N".repeat(20)));
        campos.put("razonSocial", texto("R".repeat(200)));
        campos.put("direccion", texto("D".repeat(300)));
        campos.put("codigoPostal", texto("C".repeat(10)));
        campos.put("municipio", texto("M".repeat(100)));
        campos.put("provincia", texto("P".repeat(100)));
        campos.put("pais", texto("S".repeat(100)));
        campos.put("email", texto(EMAIL_DE_200));
        campos.put("telefono", texto("T".repeat(30)));

        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content(json(campos)))
                .andExpect(status().isCreated());

        verify(crearTercero).ejecutar(new NuevoTerceroDto(
                3L, "N".repeat(20), "R".repeat(200), "D".repeat(300), "C".repeat(10), "M".repeat(100),
                "P".repeat(100), "S".repeat(100), EMAIL_DE_200, "T".repeat(30), true, false));
    }

    @ParameterizedTest
    @MethodSource("creacionesInvalidas")
    void crearConCampoInvalidoDevuelve400SinInvocarElCasoDeUso(String campo, String valorJson) throws Exception {
        mockMvc.perform(post("/api/terceros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo(creacionValida(), campo, valorJson)))
                .andExpect(status().isBadRequest());

        verify(crearTercero, never()).ejecutar(any());
    }

    @Test
    void crearConCuerpoMalFormadoOAusenteDevuelve400() throws Exception {
        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(crearTercero, never()).ejecutar(any());
    }

    @Test
    void crearConNifDuplicadoDevuelve409ConProblemDetail() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class))).thenThrow(new NifTerceroDuplicadoException());

        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content(json(creacionValida())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ya existe otro tercero con ese NIF en la empresa"));
    }

    @Test
    void crearConEmpresaInexistenteDevuelve400ConProblemDetail() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class)))
                .thenThrow(new EmpresaDelTerceroInexistenteException(3L));

        mockMvc.perform(post("/api/terceros").contentType(MediaType.APPLICATION_JSON).content(json(creacionValida())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 3"));
    }

    @Test
    void crearQueNoEsClienteNiProveedorDevuelve400ConProblemDetail() throws Exception {
        when(crearTercero.ejecutar(any(NuevoTerceroDto.class)))
                .thenThrow(new TerceroInvalidoException("El tercero debe ser cliente, proveedor o ambos"));

        mockMvc.perform(post("/api/terceros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo(creacionValida(), "esCliente", "false")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("El tercero debe ser cliente, proveedor o ambos"));
    }

    @Test
    void obtenerDevuelve200ConElTercero() throws Exception {
        when(obtenerTercero.ejecutar(1L)).thenReturn(tercero(1L));

        mockMvc.perform(get("/api/terceros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.empresaId").value(3))
                .andExpect(jsonPath("$.nif").value("B12345678"))
                .andExpect(jsonPath("$.esCliente").value(true))
                .andExpect(jsonPath("$.esProveedor").value(false))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void obtenerTerceroInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(obtenerTercero.ejecutar(99L)).thenThrow(new TerceroNoEncontradoException(99L));

        mockMvc.perform(get("/api/terceros/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe el tercero con id 99"));
    }

    @Test
    void obtenerConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/terceros/abc")).andExpect(status().isBadRequest());

        verify(obtenerTercero, never()).ejecutar(anyLong());
    }

    @Test
    void listarPorEmpresaSinTipoDevuelve200() throws Exception {
        when(listarTerceros.ejecutar(3L, null)).thenReturn(List.of(tercero(1L), tercero(2L)));

        mockMvc.perform(get("/api/terceros").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(listarTerceros).ejecutar(3L, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLIENTE", "PROVEEDOR"})
    void listarPorEmpresaYTipoTrasladaElTipo(String tipo) throws Exception {
        when(listarTerceros.ejecutar(3L, tipo)).thenReturn(List.of(tercero(1L)));

        mockMvc.perform(get("/api/terceros").param("empresaId", "3").param("tipo", tipo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(listarTerceros).ejecutar(3L, tipo);
    }

    @Test
    void listarSinEmpresaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/terceros")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/terceros").param("tipo", "CLIENTE")).andExpect(status().isBadRequest());

        verify(listarTerceros, never()).ejecutar(any(), any());
    }

    @Test
    void listarConEmpresaNoNumericaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/terceros").param("empresaId", "abc")).andExpect(status().isBadRequest());

        verify(listarTerceros, never()).ejecutar(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AMBOS", "cliente", ""})
    void listarConTipoDesconocidoDevuelve400(String tipo) throws Exception {
        mockMvc.perform(get("/api/terceros").param("empresaId", "3").param("tipo", tipo))
                .andExpect(status().isBadRequest());

        verify(listarTerceros, never()).ejecutar(any(), any());
    }

    @Test
    void modificarDevuelve200YTrasladaLosDatos() throws Exception {
        when(modificarTercero.ejecutar(eq(1L), any(DatosTerceroDto.class))).thenReturn(tercero(1L));

        mockMvc.perform(put("/api/terceros/1").contentType(MediaType.APPLICATION_JSON).content(json(datosValidos())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nif").value("B12345678"));

        verify(modificarTercero).ejecutar(1L, new DatosTerceroDto(
                "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false));
    }

    @Test
    void modificarIgnoraEmpresaYEstadoEnElCuerpo() throws Exception {
        when(modificarTercero.ejecutar(eq(1L), any(DatosTerceroDto.class))).thenReturn(tercero(1L));
        Map<String, String> campos = datosValidos();
        campos.put("empresaId", "9");
        campos.put("activo", "false");

        mockMvc.perform(put("/api/terceros/1").contentType(MediaType.APPLICATION_JSON).content(json(campos)))
                .andExpect(status().isOk());

        verify(modificarTercero).ejecutar(1L, new DatosTerceroDto(
                "B12345678", "Tercero SL", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@tercero.es", "910000000", true, false));
    }

    @Test
    void modificarAceptaValoresEnElLimiteDeSusColumnas() throws Exception {
        when(modificarTercero.ejecutar(eq(1L), any(DatosTerceroDto.class))).thenReturn(tercero(1L));
        Map<String, String> campos = datosValidos();
        campos.put("nif", texto("N".repeat(20)));
        campos.put("razonSocial", texto("R".repeat(200)));
        campos.put("direccion", texto("D".repeat(300)));
        campos.put("codigoPostal", texto("C".repeat(10)));
        campos.put("municipio", texto("M".repeat(100)));
        campos.put("provincia", texto("P".repeat(100)));
        campos.put("pais", texto("S".repeat(100)));
        campos.put("email", texto(EMAIL_DE_200));
        campos.put("telefono", texto("T".repeat(30)));

        mockMvc.perform(put("/api/terceros/1").contentType(MediaType.APPLICATION_JSON).content(json(campos)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("datosInvalidos")
    void modificarConCampoInvalidoDevuelve400SinInvocarElCasoDeUso(String campo, String valorJson) throws Exception {
        mockMvc.perform(put("/api/terceros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo(datosValidos(), campo, valorJson)))
                .andExpect(status().isBadRequest());

        verify(modificarTercero, never()).ejecutar(anyLong(), any());
    }

    @Test
    void modificarTerceroInexistenteDevuelve404() throws Exception {
        when(modificarTercero.ejecutar(eq(99L), any(DatosTerceroDto.class)))
                .thenThrow(new TerceroNoEncontradoException(99L));

        mockMvc.perform(put("/api/terceros/99").contentType(MediaType.APPLICATION_JSON).content(json(datosValidos())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el tercero con id 99"));
    }

    @Test
    void modificarConNifDuplicadoDevuelve409() throws Exception {
        when(modificarTercero.ejecutar(eq(1L), any(DatosTerceroDto.class)))
                .thenThrow(new NifTerceroDuplicadoException());

        mockMvc.perform(put("/api/terceros/1").contentType(MediaType.APPLICATION_JSON).content(json(datosValidos())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe otro tercero con ese NIF en la empresa"));
    }

    @Test
    void modificarQueNoEsClienteNiProveedorDevuelve400() throws Exception {
        when(modificarTercero.ejecutar(eq(1L), any(DatosTerceroDto.class)))
                .thenThrow(new TerceroInvalidoException("El tercero debe ser cliente, proveedor o ambos"));

        mockMvc.perform(put("/api/terceros/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo(datosValidos(), "esCliente", "false")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El tercero debe ser cliente, proveedor o ambos"));
    }

    @Test
    void modificarConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(put("/api/terceros/abc").contentType(MediaType.APPLICATION_JSON).content(json(datosValidos())))
                .andExpect(status().isBadRequest());

        verify(modificarTercero, never()).ejecutar(anyLong(), any());
    }

    @Test
    void desactivarDevuelve204SinCuerpo() throws Exception {
        mockMvc.perform(post("/api/terceros/1/desactivar"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(desactivarTercero).ejecutar(1L);
    }

    @Test
    void desactivarTerceroInexistenteDevuelve404() throws Exception {
        doThrow(new TerceroNoEncontradoException(99L)).when(desactivarTercero).ejecutar(99L);

        mockMvc.perform(post("/api/terceros/99/desactivar"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el tercero con id 99"));
    }

    @Test
    void desactivarConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/terceros/abc/desactivar")).andExpect(status().isBadRequest());

        verify(desactivarTercero, never()).ejecutar(anyLong());
    }
}

package com.facturacion.pagos.api.http.controllers;

import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.NuevoPagoDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.application.AnularPago;
import com.facturacion.pagos.internal.application.ConsultarEstadoFacturas;
import com.facturacion.pagos.internal.application.ListarPagos;
import com.facturacion.pagos.internal.application.ObtenerPago;
import com.facturacion.pagos.internal.application.RegistrarPago;
import com.facturacion.pagos.internal.domain.exception.ImportePagoExcedidoException;
import com.facturacion.pagos.internal.domain.exception.PagoInvalidoException;
import com.facturacion.pagos.internal.domain.exception.PagoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
class PagoControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 3);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrarPago registrarPago;

    @MockitoBean
    private ObtenerPago obtenerPago;

    @MockitoBean
    private ListarPagos listarPagos;

    @MockitoBean
    private ConsultarEstadoFacturas consultarEstadoFacturas;

    @MockitoBean
    private AnularPago anularPago;

    private static Map<String, String> registroValido() {
        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("facturaId", "10");
        campos.put("fecha", texto("2026-11-03"));
        campos.put("importe", "21.50");
        campos.put("metodo", texto("TRANSFERENCIA"));
        campos.put("referencia", texto("REF-1"));
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

    private static String conCampo(String nombre, String valorJson) {
        Map<String, String> campos = registroValido();
        if (valorJson == null) {
            campos.remove(nombre);
        } else {
            campos.put(nombre, valorJson);
        }
        return json(campos);
    }

    private static PagoDto pago(Long id) {
        return new PagoDto(id, 10L, 3L, "COBRO", FECHA, new BigDecimal("21.50"), "TRANSFERENCIA", "REF-1");
    }

    private static Stream<Arguments> registrosInvalidos() {
        return Stream.of(
                Arguments.of("facturaId", null),
                Arguments.of("facturaId", "null"),
                Arguments.of("facturaId", texto("abc")),
                Arguments.of("fecha", null),
                Arguments.of("fecha", "null"),
                Arguments.of("fecha", texto("03/11/2026")),
                Arguments.of("importe", null),
                Arguments.of("importe", "null"),
                Arguments.of("importe", "0"),
                Arguments.of("importe", "0.00"),
                Arguments.of("importe", "-1"),
                Arguments.of("importe", "1.001"),
                Arguments.of("importe", "12345678901"),
                Arguments.of("metodo", null),
                Arguments.of("metodo", "null"),
                Arguments.of("metodo", texto("CHEQUE")),
                Arguments.of("metodo", texto("transferencia")),
                Arguments.of("metodo", texto("")),
                Arguments.of("referencia", texto("R".repeat(101))));
    }

    @Test
    void registraPagoYDevuelve201ConLocationYCuerpoCompleto() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(pago(1L));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json(registroValido())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pagos/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.facturaId").value(10))
                .andExpect(jsonPath("$.empresaId").value(3))
                .andExpect(jsonPath("$.tipo").value("COBRO"))
                .andExpect(jsonPath("$.fecha").value("2026-11-03"))
                .andExpect(jsonPath("$.importe").value(21.5))
                .andExpect(jsonPath("$.metodo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.referencia").value("REF-1"));

        verify(registrarPago).ejecutar(
                new NuevoPagoDto(10L, FECHA, new BigDecimal("21.50"), "TRANSFERENCIA", "REF-1"));
    }

    @Test
    void registrarAceptaLaReferenciaAusente() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(pago(1L));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo("referencia", null)))
                .andExpect(status().isCreated());

        verify(registrarPago).ejecutar(new NuevoPagoDto(10L, FECHA, new BigDecimal("21.50"), "TRANSFERENCIA", null));
    }

    @Test
    void registrarAceptaValoresEnElLimiteDeSusColumnas() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(pago(1L));
        Map<String, String> campos = registroValido();
        campos.put("importe", "9999999999.99");
        campos.put("referencia", texto("R".repeat(100)));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json(campos)))
                .andExpect(status().isCreated());

        verify(registrarPago).ejecutar(new NuevoPagoDto(
                10L, FECHA, new BigDecimal("9999999999.99"), "TRANSFERENCIA", "R".repeat(100)));
    }

    @Test
    void registrarAceptaElImporteMinimoPositivo() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(pago(1L));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo("importe", "0.01")))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"TRANSFERENCIA", "TARJETA", "EFECTIVO", "DOMICILIACION", "OTRO"})
    void registrarAceptaTodosLosMetodos(String metodo) throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(pago(1L));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo("metodo", texto(metodo))))
                .andExpect(status().isCreated());

        verify(registrarPago).ejecutar(new NuevoPagoDto(10L, FECHA, new BigDecimal("21.50"), metodo, "REF-1"));
    }

    @ParameterizedTest
    @MethodSource("registrosInvalidos")
    void registrarConDatosInvalidosDevuelve400SinInvocarElCasoDeUso(String campo, String valorJson)
            throws Exception {
        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON)
                        .content(conCampo(campo, valorJson)))
                .andExpect(status().isBadRequest());

        verify(registrarPago, never()).ejecutar(any());
    }

    @Test
    void registrarSinCuerpoOMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());

        verify(registrarPago, never()).ejecutar(any());
    }

    @Test
    void registrarUnaFacturaInexistenteDevuelve400ConProblemDetail() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class)))
                .thenThrow(new PagoInvalidoException("No existe la factura con id 10"));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json(registroValido())))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("No existe la factura con id 10"));
    }

    @Test
    void registrarUnPagoQueSuperaElTotalDevuelve409ConProblemDetail() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class)))
                .thenThrow(new ImportePagoExcedidoException(10L, new BigDecimal("5.00")));

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(json(registroValido())))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("El importe del pago supera el pendiente de la factura con id 10 (pendiente: 5.00)"));
    }

    @Test
    void obtenerDevuelve200ConElPago() throws Exception {
        when(obtenerPago.ejecutar(1L)).thenReturn(pago(1L));

        mockMvc.perform(get("/api/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipo").value("COBRO"))
                .andExpect(jsonPath("$.importe").value(21.5));
    }

    @Test
    void obtenerUnPagoInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(obtenerPago.ejecutar(99L)).thenThrow(new PagoNoEncontradoException(99L));

        mockMvc.perform(get("/api/pagos/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe el pago con id 99"));
    }

    @Test
    void obtenerConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/pagos/abc")).andExpect(status().isBadRequest());

        verify(obtenerPago, never()).ejecutar(anyLong());
    }

    @Test
    void listarPorFacturaTrasladaLaFacturaYDevuelve200() throws Exception {
        when(listarPagos.ejecutar(10L, null, null)).thenReturn(List.of(pago(1L), pago(2L)));

        mockMvc.perform(get("/api/pagos").param("facturaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(listarPagos).ejecutar(10L, null, null);
    }

    @Test
    void listarPorEmpresaSinTipoDevuelve200() throws Exception {
        when(listarPagos.ejecutar(null, 3L, null)).thenReturn(List.of(pago(1L)));

        mockMvc.perform(get("/api/pagos").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(listarPagos).ejecutar(null, 3L, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"COBRO", "PAGO"})
    void listarPorEmpresaYTipoTrasladaElTipo(String tipo) throws Exception {
        when(listarPagos.ejecutar(null, 3L, tipo)).thenReturn(List.of(pago(1L)));

        mockMvc.perform(get("/api/pagos").param("empresaId", "3").param("tipo", tipo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(listarPagos).ejecutar(null, 3L, tipo);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cobro", "AMBOS", ""})
    void listarConTipoDesconocidoDevuelve400SinInvocarElCasoDeUso(String tipo) throws Exception {
        mockMvc.perform(get("/api/pagos").param("empresaId", "3").param("tipo", tipo))
                .andExpect(status().isBadRequest());

        verify(listarPagos, never()).ejecutar(any(), any(), any());
    }

    @Test
    void listarConIdentificadoresNoNumericosDevuelve400() throws Exception {
        mockMvc.perform(get("/api/pagos").param("facturaId", "abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pagos").param("empresaId", "abc")).andExpect(status().isBadRequest());

        verify(listarPagos, never()).ejecutar(any(), any(), any());
    }

    @Test
    void listarConUnaCombinacionInvalidaDeParametrosDevuelve400ConProblemDetail() throws Exception {
        when(listarPagos.ejecutar(10L, 3L, null))
                .thenThrow(new PagoInvalidoException("Indique facturaId, o bien empresaId con tipo opcional"));

        mockMvc.perform(get("/api/pagos").param("facturaId", "10").param("empresaId", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Indique facturaId, o bien empresaId con tipo opcional"));
    }

    @Test
    void consultarEstadoFacturasDevuelve200ConTodosLosCampos() throws Exception {
        when(consultarEstadoFacturas.ejecutar(3L, null)).thenReturn(List.of(new EstadoFacturaDto(
                10L, "F-10", "EMITIDA", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1),
                new BigDecimal("121.00"), new BigDecimal("21.00"), new BigDecimal("100.00"), "PARCIAL", true)));

        mockMvc.perform(get("/api/pagos/estado-facturas").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].facturaId").value(10))
                .andExpect(jsonPath("$[0].numero").value("F-10"))
                .andExpect(jsonPath("$[0].tipo").value("EMITIDA"))
                .andExpect(jsonPath("$[0].fecha").value("2026-10-01"))
                .andExpect(jsonPath("$[0].fechaVencimiento").value("2026-11-01"))
                .andExpect(jsonPath("$[0].total").value(121.0))
                .andExpect(jsonPath("$[0].pagado").value(21.0))
                .andExpect(jsonPath("$[0].pendiente").value(100.0))
                .andExpect(jsonPath("$[0].estado").value("PARCIAL"))
                .andExpect(jsonPath("$[0].vencida").value(true));

        verify(consultarEstadoFacturas).ejecutar(3L, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDIENTE", "PARCIAL", "PAGADA", "VENCIDA"})
    void consultarEstadoFacturasTrasladaElEstado(String estado) throws Exception {
        when(consultarEstadoFacturas.ejecutar(3L, estado)).thenReturn(List.of());

        mockMvc.perform(get("/api/pagos/estado-facturas").param("empresaId", "3").param("estado", estado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(consultarEstadoFacturas).ejecutar(3L, estado);
    }

    @Test
    void consultarEstadoFacturasSinEmpresaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/pagos/estado-facturas")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pagos/estado-facturas").param("estado", "PAGADA"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/pagos/estado-facturas").param("empresaId", "abc"))
                .andExpect(status().isBadRequest());

        verify(consultarEstadoFacturas, never()).ejecutar(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pagada", "COBRADA", ""})
    void consultarEstadoFacturasConEstadoDesconocidoDevuelve400(String estado) throws Exception {
        mockMvc.perform(get("/api/pagos/estado-facturas").param("empresaId", "3").param("estado", estado))
                .andExpect(status().isBadRequest());

        verify(consultarEstadoFacturas, never()).ejecutar(any(), any());
    }

    @Test
    void anularDevuelve204SinCuerpo() throws Exception {
        mockMvc.perform(delete("/api/pagos/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(anularPago).ejecutar(1L);
    }

    @Test
    void anularUnPagoInexistenteDevuelve404ConProblemDetail() throws Exception {
        doThrow(new PagoNoEncontradoException(99L)).when(anularPago).ejecutar(99L);

        mockMvc.perform(delete("/api/pagos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe el pago con id 99"));
    }

    @Test
    void anularConIdNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(delete("/api/pagos/abc")).andExpect(status().isBadRequest());

        verify(anularPago, never()).ejecutar(anyLong());
    }
}

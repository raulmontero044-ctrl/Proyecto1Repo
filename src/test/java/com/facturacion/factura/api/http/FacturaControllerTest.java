package com.facturacion.factura.api.http;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaLineaFacturaDto;
import com.facturacion.factura.internal.application.CrearFactura;
import com.facturacion.factura.internal.application.EliminarFactura;
import com.facturacion.factura.internal.application.ListarFacturas;
import com.facturacion.factura.internal.application.ObtenerFactura;
import com.facturacion.factura.internal.domain.exception.FacturaInvalidaException;
import com.facturacion.factura.internal.domain.exception.FacturaNoEncontradaException;
import com.facturacion.factura.internal.domain.exception.NumeroFacturaDuplicadoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

@WebMvcTest(FacturaController.class)
class FacturaControllerTest {

    private static final String PETICION_VALIDA = """
            {
              "numero": "F-001",
              "fecha": "2026-11-03",
              "clienteNombre": "Cliente SL",
              "clienteNif": "B12345678",
              "porcentajeIva": 21,
              "lineas": [
                {"concepto": "Servicio", "cantidad": 2, "precioUnitario": 10.50}
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearFactura crearFactura;

    @MockitoBean
    private ObtenerFactura obtenerFactura;

    @MockitoBean
    private ListarFacturas listarFacturas;

    @MockitoBean
    private EliminarFactura eliminarFactura;

    private static FacturaDto factura(Long id) {
        return new FacturaDto(
                id,
                "F-001",
                LocalDate.of(2026, 11, 3),
                "Cliente SL",
                "B12345678",
                new BigDecimal("21"),
                List.of(new LineaFacturaDto("Servicio", new BigDecimal("2"), new BigDecimal("10.50"), new BigDecimal("21.00"))),
                new BigDecimal("21.00"),
                new BigDecimal("4.41"),
                new BigDecimal("25.41"));
    }

    @Test
    void creaFacturaYDevuelve201ConLocation() throws Exception {
        when(crearFactura.ejecutar(any(NuevaFacturaDto.class))).thenReturn(factura(1L));

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/facturas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(25.41));
    }

    @Test
    void rechazaPeticionInvalidaCon400SinInvocarCasoDeUso() throws Exception {
        String invalida = """
                {"numero": "", "fecha": "2026-11-03", "clienteNombre": "Cliente SL",
                 "clienteNif": "B12345678", "porcentajeIva": -1, "lineas": []}
                """;

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(invalida))
                .andExpect(status().isBadRequest());

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void rechazaLineaInvalidaCon400() throws Exception {
        String lineaInvalida = PETICION_VALIDA.replace("\"cantidad\": 2", "\"cantidad\": 0");

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(lineaInvalida))
                .andExpect(status().isBadRequest());

        verify(crearFactura, never()).ejecutar(any());
    }

    private static String peticion(
            String numero, String clienteNombre, String clienteNif, String porcentajeIva,
            String concepto, String cantidad, String precioUnitario) {
        return """
                {"numero": "%s", "fecha": "2026-11-03", "clienteNombre": "%s", "clienteNif": "%s",
                 "porcentajeIva": %s,
                 "lineas": [{"concepto": "%s", "cantidad": %s, "precioUnitario": %s}]}
                """.formatted(numero, clienteNombre, clienteNif, porcentajeIva, concepto, cantidad, precioUnitario);
    }

    private void esperarRechazo(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rechazaTextosMasLargosQueSusColumnas() throws Exception {
        esperarRechazo(peticion("F".repeat(51), "Cliente SL", "B12345678", "21", "Servicio", "1", "1"));
        esperarRechazo(peticion("F-001", "C".repeat(201), "B12345678", "21", "Servicio", "1", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "N".repeat(21), "21", "Servicio", "1", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21", "S".repeat(501), "1", "1"));

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void rechazaNumerosFueraDeLaPrecisionDeSusColumnas() throws Exception {
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21.555", "Servicio", "1", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "1000", "Servicio", "1", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21", "Servicio", "0.0004", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21", "Servicio", "1000000000", "1"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21", "Servicio", "1", "10.555"));
        esperarRechazo(peticion("F-001", "Cliente SL", "B12345678", "21", "Servicio", "1", "10000000000"));

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void aceptaValoresEnElLimiteDeSusColumnas() throws Exception {
        when(crearFactura.ejecutar(any(NuevaFacturaDto.class))).thenReturn(factura(1L));
        String enLimite = peticion(
                "F".repeat(50), "C".repeat(200), "N".repeat(20), "999.99",
                "S".repeat(500), "999999999.001", "9999999999.99");

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(enLimite))
                .andExpect(status().isCreated());
    }

    @Test
    void rechazaCamposObligatoriosAusentes() throws Exception {
        esperarRechazo("""
                {"numero": "F-001", "clienteNombre": "Cliente SL", "clienteNif": "B12345678",
                 "porcentajeIva": 21, "lineas": [{"concepto": "Servicio", "cantidad": 1, "precioUnitario": 1}]}
                """);
        esperarRechazo("""
                {"numero": "F-001", "fecha": "2026-11-03", "clienteNombre": "Cliente SL", "clienteNif": "B12345678",
                 "lineas": [{"concepto": "Servicio", "cantidad": 1, "precioUnitario": 1}]}
                """);
        esperarRechazo("""
                {"numero": "F-001", "fecha": "2026-11-03", "clienteNombre": "Cliente SL", "clienteNif": "B12345678",
                 "porcentajeIva": 21, "lineas": [{"concepto": "Servicio", "cantidad": 1}]}
                """);
        esperarRechazo("""
                {"numero": "F-001", "fecha": "2026-11-03", "clienteNombre": "Cliente SL", "clienteNif": "B12345678",
                 "porcentajeIva": 21, "lineas": [null]}
                """);

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void rechazaCuerpoMalFormadoOAusente() throws Exception {
        esperarRechazo("{\"numero\": ");
        esperarRechazo(PETICION_VALIDA.replace("\"2026-11-03\"", "\"03/11/2026\""));
        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void trasladaLaPeticionAlCasoDeUso() throws Exception {
        when(crearFactura.ejecutar(any(NuevaFacturaDto.class))).thenReturn(factura(1L));

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated());

        verify(crearFactura).ejecutar(new NuevaFacturaDto(
                "F-001",
                LocalDate.of(2026, 11, 3),
                "Cliente SL",
                "B12345678",
                new BigDecimal("21"),
                List.of(new NuevaLineaFacturaDto("Servicio", new BigDecimal("2"), new BigDecimal("10.50")))));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/facturas/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(delete("/api/facturas/abc")).andExpect(status().isBadRequest());

        verify(obtenerFactura, never()).ejecutar(any());
        verify(eliminarFactura, never()).ejecutar(any());
    }

    @Test
    void numeroDuplicadoDevuelve409() throws Exception {
        when(crearFactura.ejecutar(any())).thenThrow(new NumeroFacturaDuplicadoException("F-001"));

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ya existe una factura con el número F-001"));
    }

    @Test
    void facturaInvalidaEnDominioDevuelve400() throws Exception {
        when(crearFactura.ejecutar(any())).thenThrow(new FacturaInvalidaException("inválida"));

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("inválida"));
    }

    @Test
    void obtieneFacturaPorId() throws Exception {
        when(obtenerFactura.ejecutar(1L)).thenReturn(factura(1L));

        mockMvc.perform(get("/api/facturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("F-001"))
                .andExpect(jsonPath("$.lineas[0].importe").value(21.00));
    }

    @Test
    void facturaInexistenteDevuelve404() throws Exception {
        when(obtenerFactura.ejecutar(99L)).thenThrow(new FacturaNoEncontradaException(99L));

        mockMvc.perform(get("/api/facturas/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe la factura con id 99"));
    }

    @Test
    void listaFacturas() throws Exception {
        when(listarFacturas.ejecutar()).thenReturn(List.of(factura(1L), factura(2L)));

        mockMvc.perform(get("/api/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void eliminaFacturaYDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/facturas/1")).andExpect(status().isNoContent());

        verify(eliminarFactura).ejecutar(1L);
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        doThrow(new FacturaNoEncontradaException(99L)).when(eliminarFactura).ejecutar(99L);

        mockMvc.perform(delete("/api/facturas/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("No existe la factura con id 99"));
    }
}

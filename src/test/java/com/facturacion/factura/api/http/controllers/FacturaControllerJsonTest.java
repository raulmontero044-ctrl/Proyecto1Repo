package com.facturacion.factura.api.http.controllers;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.LineaFacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;
import com.facturacion.factura.internal.application.CrearFactura;
import com.facturacion.factura.internal.application.EliminarFactura;
import com.facturacion.factura.internal.application.ListarFacturas;
import com.facturacion.factura.internal.application.ObtenerFactura;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacturaController.class)
class FacturaControllerJsonTest {

    private static final String PETICION_VALIDA = """
            {
              "numero": "F-001", "tipo": "EMITIDA", "empresaId": 1, "terceroId": 10,
              "fecha": "2026-11-03",
              "clienteNombre": "Cliente SL",
              "clienteNif": "B12345678",
              "porcentajeIva": 21,
              "lineas": [
                {"concepto": "Primera", "cantidad": 2, "precioUnitario": 10.50},
                {"concepto": "Segunda", "cantidad": 1.5, "precioUnitario": 4.25}
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

    private static FacturaDto factura(Long id, String numero) {
        return new FacturaDto(
                id,
                numero,
                "EMITIDA",
                LocalDate.of(2026, 11, 3),
                LocalDate.of(2026, 12, 3),
                1L,
                10L,
                "Cliente SL",
                "B12345678",
                new BigDecimal("21"),
                List.of(
                        new LineaFacturaDto("Primera", new BigDecimal("2"), new BigDecimal("10.50"), new BigDecimal("21.00")),
                        new LineaFacturaDto("Segunda", new BigDecimal("1.5"), new BigDecimal("4.25"), new BigDecimal("6.38"))),
                new BigDecimal("27.38"),
                new BigDecimal("5.75"),
                new BigDecimal("33.13"));
    }

    private static String json(Long id, String numero) {
        return "{\"id\":" + id + ",\"numero\":\"" + numero + "\",\"tipo\":\"EMITIDA\",\"fecha\":\"2026-11-03\",\"fechaVencimiento\":\"2026-12-03\",\"empresaId\":1,\"terceroId\":10,"
                + "\"clienteNombre\":\"Cliente SL\",\"clienteNif\":\"B12345678\",\"porcentajeIva\":21,"
                + "\"lineas\":["
                + "{\"concepto\":\"Primera\",\"cantidad\":2,\"precioUnitario\":10.50,\"importe\":21.00},"
                + "{\"concepto\":\"Segunda\",\"cantidad\":1.5,\"precioUnitario\":4.25,\"importe\":6.38}],"
                + "\"base\":27.38,\"cuotaIva\":5.75,\"total\":33.13}";
    }

    @Test
    void crearDevuelveElJsonCompletoDeLaFacturaCreada() throws Exception {
        when(crearFactura.ejecutar(any(NuevaFacturaDto.class))).thenReturn(factura(1L, "F-001"));

        mockMvc.perform(post("/api/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(json(1L, "F-001")));
    }

    @Test
    void obtenerDevuelveElJsonCompletoConTodosLosCamposEnSuOrden() throws Exception {
        when(obtenerFactura.ejecutar(7L)).thenReturn(factura(7L, "F-007"));

        mockMvc.perform(get("/api/facturas/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(json(7L, "F-007")));
    }

    @Test
    void listarDevuelveUnArrayJsonConCadaFacturaCompleta() throws Exception {
        when(listarFacturas.ejecutar()).thenReturn(List.of(factura(1L, "F-001"), factura(2L, "F-002")));

        mockMvc.perform(get("/api/facturas"))
                .andExpect(status().isOk())
                .andExpect(content().string("[" + json(1L, "F-001") + "," + json(2L, "F-002") + "]"));
    }

    @Test
    void listarSinFacturasDevuelveUnArrayJsonVacio() throws Exception {
        when(listarFacturas.ejecutar()).thenReturn(List.of());

        mockMvc.perform(get("/api/facturas"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}

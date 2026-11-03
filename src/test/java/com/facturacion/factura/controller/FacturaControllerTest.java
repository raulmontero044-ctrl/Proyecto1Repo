package com.facturacion.factura.controller;

import com.facturacion.factura.api.dto.FacturaDto;
import com.facturacion.factura.api.dto.LineaFacturaDto;
import com.facturacion.factura.api.dto.NuevaFacturaDto;
import com.facturacion.factura.apln.CrearFactura;
import com.facturacion.factura.apln.EliminarFactura;
import com.facturacion.factura.apln.ListarFacturas;
import com.facturacion.factura.apln.ObtenerFactura;
import com.facturacion.factura.domain.model.FacturaInvalidaException;
import com.facturacion.factura.domain.model.FacturaNoEncontradaException;
import com.facturacion.factura.domain.model.NumeroFacturaDuplicadoException;
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

        mockMvc.perform(post("/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/facturas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(25.41));
    }

    @Test
    void rechazaPeticionInvalidaCon400SinInvocarCasoDeUso() throws Exception {
        String invalida = """
                {"numero": "", "fecha": "2026-11-03", "clienteNombre": "Cliente SL",
                 "clienteNif": "B12345678", "porcentajeIva": -1, "lineas": []}
                """;

        mockMvc.perform(post("/facturas").contentType(MediaType.APPLICATION_JSON).content(invalida))
                .andExpect(status().isBadRequest());

        verify(crearFactura, never()).ejecutar(any());
    }

    @Test
    void rechazaLineaInvalidaCon400() throws Exception {
        String lineaInvalida = PETICION_VALIDA.replace("\"cantidad\": 2", "\"cantidad\": 0");

        mockMvc.perform(post("/facturas").contentType(MediaType.APPLICATION_JSON).content(lineaInvalida))
                .andExpect(status().isBadRequest());
    }

    @Test
    void numeroDuplicadoDevuelve409() throws Exception {
        when(crearFactura.ejecutar(any())).thenThrow(new NumeroFacturaDuplicadoException("F-001"));

        mockMvc.perform(post("/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe una factura con el número F-001"));
    }

    @Test
    void facturaInvalidaEnDominioDevuelve400() throws Exception {
        when(crearFactura.ejecutar(any())).thenThrow(new FacturaInvalidaException("inválida"));

        mockMvc.perform(post("/facturas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtieneFacturaPorId() throws Exception {
        when(obtenerFactura.ejecutar(1L)).thenReturn(factura(1L));

        mockMvc.perform(get("/facturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("F-001"))
                .andExpect(jsonPath("$.lineas[0].importe").value(21.00));
    }

    @Test
    void facturaInexistenteDevuelve404() throws Exception {
        when(obtenerFactura.ejecutar(99L)).thenThrow(new FacturaNoEncontradaException(99L));

        mockMvc.perform(get("/facturas/99")).andExpect(status().isNotFound());
    }

    @Test
    void listaFacturas() throws Exception {
        when(listarFacturas.ejecutar()).thenReturn(List.of(factura(1L), factura(2L)));

        mockMvc.perform(get("/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void eliminaFacturaYDevuelve204() throws Exception {
        mockMvc.perform(delete("/facturas/1")).andExpect(status().isNoContent());

        verify(eliminarFactura).ejecutar(1L);
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        doThrow(new FacturaNoEncontradaException(99L)).when(eliminarFactura).ejecutar(99L);

        mockMvc.perform(delete("/facturas/99")).andExpect(status().isNotFound());
    }
}

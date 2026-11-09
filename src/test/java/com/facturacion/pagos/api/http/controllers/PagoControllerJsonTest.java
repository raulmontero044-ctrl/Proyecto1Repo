package com.facturacion.pagos.api.http.controllers;

import com.facturacion.app.ConfiguracionReloj;
import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.pagos.api.facade.dto.EstadoFacturaDto;
import com.facturacion.pagos.api.facade.dto.NuevoPagoDto;
import com.facturacion.pagos.api.facade.dto.PagoDto;
import com.facturacion.pagos.internal.application.AnularPago;
import com.facturacion.pagos.internal.application.ConsultarEstadoFacturas;
import com.facturacion.pagos.internal.application.ListarPagos;
import com.facturacion.pagos.internal.application.ObtenerPago;
import com.facturacion.pagos.internal.application.RegistrarPago;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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

@WebMvcTest(PagoController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
@WithMockUser(roles = "GESTOR")
class PagoControllerJsonTest {

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

    @Test
    void registrarDevuelveElJsonCompletoDelPago() throws Exception {
        when(registrarPago.ejecutar(any(NuevoPagoDto.class))).thenReturn(new PagoDto(
                1L, 10L, 3L, "PAGO", LocalDate.of(2026, 11, 3), new BigDecimal("21.50"), "EFECTIVO", null));
        String peticion = """
                {"facturaId": 10, "fecha": "2026-11-03", "importe": 21.50, "metodo": "EFECTIVO"}
                """;

        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON).content(peticion))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {"id": 1, "facturaId": 10, "empresaId": 3, "tipo": "PAGO", "fecha": "2026-11-03",
                         "importe": 21.50, "metodo": "EFECTIVO", "referencia": null}
                        """, true));
    }

    @Test
    void estadoFacturasDevuelveElJsonCompletoDeCadaFactura() throws Exception {
        when(consultarEstadoFacturas.ejecutar(3L, null)).thenReturn(List.of(new EstadoFacturaDto(
                10L, "F-10", "RECIBIDA", LocalDate.of(2026, 10, 1), null,
                new BigDecimal("50.00"), new BigDecimal("0.00"), new BigDecimal("50.00"), "PENDIENTE", false)));

        mockMvc.perform(get("/api/pagos/estado-facturas").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"facturaId": 10, "numero": "F-10", "tipo": "RECIBIDA", "fecha": "2026-10-01",
                          "fechaVencimiento": null, "total": 50.00, "pagado": 0.00, "pendiente": 50.00,
                          "estado": "PENDIENTE", "vencida": false}]
                        """, true));
    }
}

package com.facturacion.informes.api.http.controllers;

import com.facturacion.app.ConfiguracionReloj;
import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.informes.internal.application.ConsultarEstadisticas;
import com.facturacion.informes.internal.application.ConsultarImpuestos;
import com.facturacion.informes.internal.application.ConsultarIngresosGastos;
import com.facturacion.informes.internal.application.ConsultarLibroFacturas;
import com.facturacion.informes.internal.application.ConsultarPendientes;
import com.facturacion.informes.internal.application.ConsultarTesoreria;
import com.facturacion.informes.internal.application.EstadisticaPeriodoDto;
import com.facturacion.informes.internal.application.FacturaResumidaDto;
import com.facturacion.informes.internal.application.IngresosGastosDto;
import com.facturacion.informes.internal.application.ResumenFacturasDto;
import com.facturacion.informes.internal.application.ResumenImpuestosDto;
import com.facturacion.informes.internal.application.ResumenPendientesDto;
import com.facturacion.informes.internal.application.ResumenTesoreriaDto;
import com.facturacion.informes.internal.application.SaldoFacturaDto;
import com.facturacion.informes.internal.application.TramoIvaDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InformeController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
@WithMockUser(roles = "CONSULTA")
class InformeControllerJsonTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 3, 31);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultarLibroFacturas consultarLibroFacturas;

    @MockitoBean
    private ConsultarIngresosGastos consultarIngresosGastos;

    @MockitoBean
    private ConsultarImpuestos consultarImpuestos;

    @MockitoBean
    private ConsultarEstadisticas consultarEstadisticas;

    @MockitoBean
    private ConsultarPendientes consultarPendientes;

    @MockitoBean
    private ConsultarTesoreria consultarTesoreria;

    private static MockHttpServletRequestBuilder conPeriodo(String ruta) {
        return get(ruta).param("empresaId", "3").param("desde", "2026-01-01").param("hasta", "2026-03-31");
    }

    private static BigDecimal importe(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void facturasDevuelveElJsonCompletoDelLibro() throws Exception {
        when(consultarLibroFacturas.ejecutar(3L, DESDE, HASTA, "RECIBIDA")).thenReturn(new ResumenFacturasDto(
                List.of(new FacturaResumidaDto(
                        2L, "R-2", "RECIBIDA", LocalDate.of(2026, 1, 20), 20L, "Proveedor SA", importe("21.00"),
                        importe("40.00"), importe("8.40"), importe("48.40"))),
                1, importe("40.00"), importe("8.40"), importe("48.40")));

        mockMvc.perform(conPeriodo("/api/informes/facturas").param("tipo", "RECIBIDA"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"facturas": [{"id": 2, "numero": "R-2", "tipo": "RECIBIDA", "fecha": "2026-01-20",
                                       "terceroId": 20, "terceroNombre": "Proveedor SA", "porcentajeIva": 21.00,
                                       "base": 40.00, "cuotaIva": 8.40, "total": 48.40}],
                         "numeroFacturas": 1, "base": 40.00, "cuotaIva": 8.40, "total": 48.40}
                        """, true));
    }

    @Test
    void ingresosGastosDevuelveElJsonCompleto() throws Exception {
        when(consultarIngresosGastos.ejecutar(3L, DESDE, HASTA))
                .thenReturn(new IngresosGastosDto(importe("150.00"), importe("40.00"), importe("110.00")));

        mockMvc.perform(conPeriodo("/api/informes/ingresos-gastos"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"ingresos": 150.00, "gastos": 40.00, "resultado": 110.00}
                        """, true));
    }

    @Test
    void impuestosDevuelveElJsonCompletoConLosTramos() throws Exception {
        when(consultarImpuestos.ejecutar(3L, DESDE, HASTA)).thenReturn(new ResumenImpuestosDto(
                List.of(
                        new TramoIvaDto(importe("10.00"), importe("50.00"), importe("5.00")),
                        new TramoIvaDto(importe("21.00"), importe("100.00"), importe("21.00"))),
                importe("26.00"),
                List.of(new TramoIvaDto(importe("21.00"), importe("40.00"), importe("8.40"))),
                importe("8.40"),
                importe("17.60")));

        mockMvc.perform(conPeriodo("/api/informes/impuestos"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"repercutido": [{"porcentajeIva": 10.00, "base": 50.00, "cuota": 5.00},
                                         {"porcentajeIva": 21.00, "base": 100.00, "cuota": 21.00}],
                         "cuotaRepercutida": 26.00,
                         "soportado": [{"porcentajeIva": 21.00, "base": 40.00, "cuota": 8.40}],
                         "cuotaSoportada": 8.40,
                         "resultado": 17.60}
                        """, true));
    }

    @Test
    void estadisticasDevuelveElJsonCompletoDeCadaPeriodo() throws Exception {
        when(consultarEstadisticas.ejecutar(3L, DESDE, HASTA, "MES")).thenReturn(List.of(
                new EstadisticaPeriodoDto("2026-01", 1, importe("55.00"), 1, importe("48.40")),
                new EstadisticaPeriodoDto("2026-02", 1, importe("121.00"), 0, importe("0.00"))));

        mockMvc.perform(conPeriodo("/api/informes/estadisticas").param("agrupacion", "MES"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"periodo": "2026-01", "numeroEmitidas": 1, "totalEmitidas": 55.00,
                          "numeroRecibidas": 1, "totalRecibidas": 48.40},
                         {"periodo": "2026-02", "numeroEmitidas": 1, "totalEmitidas": 121.00,
                          "numeroRecibidas": 0, "totalRecibidas": 0.00}]
                        """, true));
    }

    @Test
    void pendientesDevuelveElJsonCompletoConElDetalle() throws Exception {
        when(consultarPendientes.ejecutar(3L)).thenReturn(new ResumenPendientesDto(
                importe("121.00"), importe("121.00"), importe("50.00"), importe("0.00"),
                List.of(
                        new SaldoFacturaDto(
                                1L, "F-1", "EMITIDA", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 2, 10),
                                importe("121.00"), importe("0.00"), importe("121.00"), "PENDIENTE", true),
                        new SaldoFacturaDto(
                                4L, "R-4", "RECIBIDA", LocalDate.of(2026, 3, 1), null,
                                importe("60.00"), importe("10.00"), importe("50.00"), "PARCIAL", false))));

        mockMvc.perform(get("/api/informes/pendientes").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"pendienteCobro": 121.00, "vencidoCobro": 121.00,
                         "pendientePago": 50.00, "vencidoPago": 0.00,
                         "facturas": [
                           {"facturaId": 1, "numero": "F-1", "tipo": "EMITIDA", "fecha": "2026-01-10",
                            "fechaVencimiento": "2026-02-10", "total": 121.00, "pagado": 0.00,
                            "pendiente": 121.00, "estado": "PENDIENTE", "vencida": true},
                           {"facturaId": 4, "numero": "R-4", "tipo": "RECIBIDA", "fecha": "2026-03-01",
                            "fechaVencimiento": null, "total": 60.00, "pagado": 10.00,
                            "pendiente": 50.00, "estado": "PARCIAL", "vencida": false}]}
                        """, true));
    }

    @Test
    void tesoreriaDevuelveElJsonCompleto() throws Exception {
        when(consultarTesoreria.ejecutar(3L, DESDE, HASTA))
                .thenReturn(new ResumenTesoreriaDto(importe("146.00"), importe("48.40"), importe("97.60")));

        mockMvc.perform(conPeriodo("/api/informes/tesoreria"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"cobros": 146.00, "pagos": 48.40, "saldoNeto": 97.60}
                        """, true));
    }
}

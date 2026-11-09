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
import com.facturacion.informes.internal.application.IngresosGastosDto;
import com.facturacion.informes.internal.application.ResumenFacturasDto;
import com.facturacion.informes.internal.application.ResumenImpuestosDto;
import com.facturacion.informes.internal.application.ResumenPendientesDto;
import com.facturacion.informes.internal.application.ResumenTesoreriaDto;
import com.facturacion.informes.internal.domain.exception.InformeInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InformeController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
@WithMockUser(roles = "CONSULTA")
class InformeControllerTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 1, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 3, 31);
    private static final BigDecimal CERO = new BigDecimal("0.00");

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

    @Test
    void facturasTrasladaLosParametrosYDevuelve200() throws Exception {
        when(consultarLibroFacturas.ejecutar(3L, DESDE, HASTA, "EMITIDA"))
                .thenReturn(new ResumenFacturasDto(List.of(), 0, CERO, CERO, CERO));

        mockMvc.perform(conPeriodo("/api/informes/facturas").param("tipo", "EMITIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroFacturas").value(0));

        verify(consultarLibroFacturas).ejecutar(3L, DESDE, HASTA, "EMITIDA");
    }

    @Test
    void ingresosGastosTrasladaLosParametrosYDevuelve200() throws Exception {
        when(consultarIngresosGastos.ejecutar(3L, DESDE, HASTA)).thenReturn(new IngresosGastosDto(
                new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("6.00")));

        mockMvc.perform(conPeriodo("/api/informes/ingresos-gastos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value(6.00));

        verify(consultarIngresosGastos).ejecutar(3L, DESDE, HASTA);
    }

    @Test
    void impuestosTrasladaLosParametrosYDevuelve200() throws Exception {
        when(consultarImpuestos.ejecutar(3L, DESDE, HASTA))
                .thenReturn(new ResumenImpuestosDto(List.of(), CERO, List.of(), CERO, CERO));

        mockMvc.perform(conPeriodo("/api/informes/impuestos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repercutido").isEmpty());

        verify(consultarImpuestos).ejecutar(3L, DESDE, HASTA);
    }

    @Test
    void estadisticasTrasladaLosParametrosYDevuelve200() throws Exception {
        when(consultarEstadisticas.ejecutar(3L, DESDE, HASTA, "TRIMESTRE")).thenReturn(List.of(
                new EstadisticaPeriodoDto("2026-T1", 1, new BigDecimal("121.00"), 0, CERO)));

        mockMvc.perform(conPeriodo("/api/informes/estadisticas").param("agrupacion", "TRIMESTRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].periodo").value("2026-T1"));

        verify(consultarEstadisticas).ejecutar(3L, DESDE, HASTA, "TRIMESTRE");
    }

    @Test
    void pendientesSoloNecesitaLaEmpresaYDevuelve200() throws Exception {
        when(consultarPendientes.ejecutar(3L)).thenReturn(new ResumenPendientesDto(CERO, CERO, CERO, CERO, List.of()));

        mockMvc.perform(get("/api/informes/pendientes").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facturas").isEmpty());

        verify(consultarPendientes).ejecutar(3L);
    }

    @Test
    void tesoreriaTrasladaLosParametrosYDevuelve200() throws Exception {
        when(consultarTesoreria.ejecutar(3L, DESDE, HASTA)).thenReturn(new ResumenTesoreriaDto(
                new BigDecimal("146.00"), new BigDecimal("48.40"), new BigDecimal("97.60")));

        mockMvc.perform(conPeriodo("/api/informes/tesoreria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoNeto").value(97.60));

        verify(consultarTesoreria).ejecutar(3L, DESDE, HASTA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"empresaId", "desde", "hasta", "tipo"})
    void facturasSinUnParametroObligatorioDevuelve400SinInvocarElCasoDeUso(String ausente) throws Exception {
        mockMvc.perform(sin(ausente, "/api/informes/facturas", "tipo", "EMITIDA"))
                .andExpect(status().isBadRequest());

        verify(consultarLibroFacturas, never()).ejecutar(any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"empresaId", "desde", "hasta", "agrupacion"})
    void estadisticasSinUnParametroObligatorioDevuelve400SinInvocarElCasoDeUso(String ausente) throws Exception {
        mockMvc.perform(sin(ausente, "/api/informes/estadisticas", "agrupacion", "MES"))
                .andExpect(status().isBadRequest());

        verify(consultarEstadisticas, never()).ejecutar(any(), any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"empresaId", "desde", "hasta"})
    void ingresosGastosImpuestosYTesoreriaSinUnParametroObligatorioDevuelven400(String ausente) throws Exception {
        mockMvc.perform(sin(ausente, "/api/informes/ingresos-gastos", null, null))
                .andExpect(status().isBadRequest());
        mockMvc.perform(sin(ausente, "/api/informes/impuestos", null, null))
                .andExpect(status().isBadRequest());
        mockMvc.perform(sin(ausente, "/api/informes/tesoreria", null, null))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultarIngresosGastos, consultarImpuestos, consultarTesoreria);
    }

    @Test
    void pendientesSinEmpresaDevuelve400SinInvocarElCasoDeUso() throws Exception {
        mockMvc.perform(get("/api/informes/pendientes")).andExpect(status().isBadRequest());

        verify(consultarPendientes, never()).ejecutar(any());
    }

    @Test
    void unaEmpresaNoNumericaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/informes/pendientes").param("empresaId", "abc"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/informes/tesoreria")
                        .param("empresaId", "abc").param("desde", "2026-01-01").param("hasta", "2026-03-31"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultarPendientes, consultarTesoreria);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-13-01", "01/01/2026", "ayer", "2026-02-30"})
    void unaFechaQueNoEsIsoValidaDevuelve400SinInvocarElCasoDeUso(String fecha) throws Exception {
        mockMvc.perform(get("/api/informes/ingresos-gastos")
                        .param("empresaId", "3").param("desde", fecha).param("hasta", "2026-03-31"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/informes/ingresos-gastos")
                        .param("empresaId", "3").param("desde", "2026-01-01").param("hasta", fecha))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultarIngresosGastos);
    }

    @Test
    void facturasConUnTipoDesconocidoDevuelve400ConProblemDetail() throws Exception {
        when(consultarLibroFacturas.ejecutar(3L, DESDE, HASTA, "PROFORMA"))
                .thenThrow(new InformeInvalidoException("El tipo de la factura debe ser EMITIDA o RECIBIDA"));

        mockMvc.perform(conPeriodo("/api/informes/facturas").param("tipo", "PROFORMA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("El tipo de la factura debe ser EMITIDA o RECIBIDA"));
    }

    @Test
    void estadisticasConUnaAgrupacionDesconocidaDevuelve400ConProblemDetail() throws Exception {
        when(consultarEstadisticas.ejecutar(3L, DESDE, HASTA, "SEMANA"))
                .thenThrow(new InformeInvalidoException("La agrupación del informe debe ser MES, TRIMESTRE o ANIO"));

        mockMvc.perform(conPeriodo("/api/informes/estadisticas").param("agrupacion", "SEMANA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("La agrupación del informe debe ser MES, TRIMESTRE o ANIO"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"facturas", "ingresos-gastos", "impuestos", "estadisticas", "tesoreria"})
    void unPeriodoConDesdePosteriorAHastaDevuelve400ConProblemDetail(String informe) throws Exception {
        String detalle = "La fecha desde del informe no puede ser posterior a la fecha hasta";
        InformeInvalidoException periodoInvalido = new InformeInvalidoException(detalle);
        when(consultarLibroFacturas.ejecutar(any(), any(), any(), any())).thenThrow(periodoInvalido);
        when(consultarIngresosGastos.ejecutar(any(), any(), any())).thenThrow(periodoInvalido);
        when(consultarImpuestos.ejecutar(any(), any(), any())).thenThrow(periodoInvalido);
        when(consultarEstadisticas.ejecutar(any(), any(), any(), any())).thenThrow(periodoInvalido);
        when(consultarTesoreria.ejecutar(any(), any(), any())).thenThrow(periodoInvalido);

        mockMvc.perform(get("/api/informes/" + informe)
                        .param("empresaId", "3")
                        .param("desde", "2026-04-01")
                        .param("hasta", "2026-03-31")
                        .param("tipo", "EMITIDA")
                        .param("agrupacion", "MES"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(detalle));
    }

    @Test
    void pendientesConUnInformeInvalidoDevuelve400ConProblemDetail() throws Exception {
        when(consultarPendientes.ejecutar(3L))
                .thenThrow(new InformeInvalidoException("El estado de la factura debe ser PENDIENTE, PARCIAL o PAGADA"));

        mockMvc.perform(get("/api/informes/pendientes").param("empresaId", "3"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("El estado de la factura debe ser PENDIENTE, PARCIAL o PAGADA"));
    }

    private static MockHttpServletRequestBuilder sin(
            String ausente, String ruta, String parametroExtra, String valorExtra) {
        MockHttpServletRequestBuilder peticion = get(ruta);
        incluirSalvo(peticion, ausente, "empresaId", "3");
        incluirSalvo(peticion, ausente, "desde", "2026-01-01");
        incluirSalvo(peticion, ausente, "hasta", "2026-03-31");
        if (parametroExtra != null) {
            incluirSalvo(peticion, ausente, parametroExtra, valorExtra);
        }
        return peticion;
    }

    private static void incluirSalvo(
            MockHttpServletRequestBuilder peticion, String ausente, String parametro, String valor) {
        if (!parametro.equals(ausente)) {
            peticion.param(parametro, valor);
        }
    }
}

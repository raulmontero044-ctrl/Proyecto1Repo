package com.facturacion.auth.api.http.controllers;

import com.facturacion.app.ConfiguracionReloj;
import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.auth.internal.application.CerrarSesion;
import com.facturacion.auth.internal.application.IniciarSesion;
import com.facturacion.auth.internal.application.RenovarSesion;
import com.facturacion.auth.internal.application.SesionDto;
import com.facturacion.auth.internal.domain.exception.CredencialesInvalidasException;
import com.facturacion.auth.internal.domain.exception.RefreshTokenInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
class AuthControllerTest {

    private static final SesionDto SESION =
            new SesionDto("acceso.jwt", "refresh-1", Instant.parse("2026-11-09T10:05:00Z"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IniciarSesion iniciarSesion;

    @MockitoBean
    private RenovarSesion renovarSesion;

    @MockitoBean
    private CerrarSesion cerrarSesion;

    private static String login(String email, String password) {
        return """
                {"email": %s, "password": %s}
                """.formatted(email, password);
    }

    private static String refresh(String valor) {
        return """
                {"refreshToken": %s}
                """.formatted(valor);
    }

    @Test
    void loginDevuelveElParDeTokensYSuCaducidad() throws Exception {
        when(iniciarSesion.ejecutar("ana@empresa.es", "secreta123")).thenReturn(SESION);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("\"ana@empresa.es\"", "\"secreta123\"")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"accessToken": "acceso.jwt", "refreshToken": "refresh-1", "expiraEn": "2026-11-09T10:05:00Z"}
                        """, true));
    }

    @Test
    void loginConCredencialesIncorrectasResponde401ConProblemDetail() throws Exception {
        when(iniciarSesion.ejecutar(anyString(), anyString())).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("\"ana@empresa.es\"", "\"otraClave1\"")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("El email o la contraseña no son correctos"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"password\": \"secreta123\"}",
            "{\"email\": \"\", \"password\": \"secreta123\"}",
            "{\"email\": \"   \", \"password\": \"secreta123\"}",
            "{\"email\": \"ana@empresa.es\"}",
            "{\"email\": \"ana@empresa.es\", \"password\": \"\"}",
            "{\"email\": \"ana@empresa.es\", \"password\": null}",
            "no es json"})
    void loginConCuerpoInvalidoResponde400SinInvocarElCasoDeUso(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(iniciarSesion);
    }

    @Test
    void loginAceptaUnEmailDeDoscientosCaracteresYRechazaElSiguiente() throws Exception {
        when(iniciarSesion.ejecutar(anyString(), anyString())).thenReturn(SESION);
        String doscientos = "a".repeat(189) + "@empresa.es";
        String doscientosUno = "a".repeat(190) + "@empresa.es";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("\"" + doscientos + "\"", "\"secreta123\"")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login("\"" + doscientosUno + "\"", "\"secreta123\"")))
                .andExpect(status().isBadRequest());

        verify(iniciarSesion).ejecutar(doscientos, "secreta123");
        verify(iniciarSesion, never()).ejecutar(doscientosUno, "secreta123");
    }

    @Test
    void refreshDevuelveUnParNuevo() throws Exception {
        when(renovarSesion.ejecutar("refresh-0")).thenReturn(SESION);

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refresh("\"refresh-0\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("acceso.jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-1"))
                .andExpect(jsonPath("$.expiraEn").value("2026-11-09T10:05:00Z"));
    }

    @Test
    void refreshConUnTokenRevocadoCaducadoODesconocidoResponde401ConProblemDetail() throws Exception {
        when(renovarSesion.ejecutar("usado"))
                .thenThrow(new RefreshTokenInvalidoException("El refresh token no es válido"));

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(refresh("\"usado\"")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("El refresh token no es válido"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"refreshToken\": null}", "{\"refreshToken\": \"\"}", "{\"refreshToken\": \" \"}"})
    void refreshSinTokenResponde400SinInvocarElCasoDeUso(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());

        verify(renovarSesion, never()).ejecutar(any());
    }

    @Test
    void logoutRevocaElRefreshYResponde204SinCuerpo() throws Exception {
        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(refresh("\"refresh-1\"")))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(cerrarSesion).ejecutar("refresh-1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"refreshToken\": null}", "{\"refreshToken\": \"\"}"})
    void logoutSinTokenResponde400SinInvocarElCasoDeUso(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());

        verify(cerrarSesion, never()).ejecutar(any());
    }

    @Test
    void elLoginSoloAdmitePostYUnGetAnonimoExigeToken() throws Exception {
        mockMvc.perform(get("/api/auth/login")).andExpect(status().isUnauthorized());

        verifyNoInteractions(iniciarSesion);
    }
}

package com.facturacion.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfiguracionSeguridadTest.ControladorFicticio.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class, ConfiguracionSeguridadTest.ControladorFicticio.class})
class ConfiguracionSeguridadTest {

    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String GESTOR = "GESTOR";
    private static final String CONSULTA = "CONSULTA";
    private static final List<String> ROLES = List.of(ADMINISTRADOR, GESTOR, CONSULTA);
    private static final Set<String> TODOS = Set.of(ADMINISTRADOR, GESTOR, CONSULTA);
    private static final Set<String> ESCRITURA_OPERACIONES = Set.of(ADMINISTRADOR, GESTOR);
    private static final Set<String> SOLO_ADMINISTRADOR = Set.of(ADMINISTRADOR);
    private static final String OTRA_CLAVE = "clave-distinta-de-la-configurada-en-los-tests";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder encoder;

    @RestController
    static class ControladorFicticio {

        @RequestMapping("/**")
        String responder() {
            return "permitido";
        }
    }

    private record Fila(HttpMethod metodo, String ruta, Set<String> permitidos) {

        @Override
        public String toString() {
            return metodo + " " + ruta;
        }
    }

    private static List<Fila> matriz() {
        return List.of(
                new Fila(HttpMethod.GET, "/api/facturas", TODOS),
                new Fila(HttpMethod.GET, "/api/facturas/1", TODOS),
                new Fila(HttpMethod.POST, "/api/facturas", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.PUT, "/api/facturas/1", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.DELETE, "/api/facturas/1", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.GET, "/api/pagos", TODOS),
                new Fila(HttpMethod.GET, "/api/pagos/estado", TODOS),
                new Fila(HttpMethod.POST, "/api/pagos", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.POST, "/api/pagos/1/anular", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.GET, "/api/documentos", TODOS),
                new Fila(HttpMethod.GET, "/api/documentos/1", TODOS),
                new Fila(HttpMethod.GET, "/api/documentos/1/contenido", TODOS),
                new Fila(HttpMethod.POST, "/api/documentos", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.POST, "/api/documentos/facturas/1/pdf", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.DELETE, "/api/documentos/1", ESCRITURA_OPERACIONES),
                new Fila(HttpMethod.GET, "/api/terceros", TODOS),
                new Fila(HttpMethod.GET, "/api/terceros/1", TODOS),
                new Fila(HttpMethod.POST, "/api/terceros", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.PUT, "/api/terceros/1", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.POST, "/api/terceros/1/desactivar", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/empresas", TODOS),
                new Fila(HttpMethod.GET, "/api/empresas/1", TODOS),
                new Fila(HttpMethod.POST, "/api/empresas", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.PUT, "/api/empresas/1/contacto", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/informes/libro-facturas", TODOS),
                new Fila(HttpMethod.GET, "/api/informes/tesoreria", TODOS),
                new Fila(HttpMethod.POST, "/api/informes/libro-facturas", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/usuarios", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/usuarios/1", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.POST, "/api/usuarios", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.PUT, "/api/usuarios/1", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.DELETE, "/api/usuarios/1/empresas/2", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/no-listada", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.DELETE, "/api/no-listada/1", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/api/auth/login", SOLO_ADMINISTRADOR),
                new Fila(HttpMethod.GET, "/fuera-de-la-api", SOLO_ADMINISTRADOR));
    }

    private static Stream<Arguments> filasPorRol() {
        return matriz().stream().flatMap(fila -> ROLES.stream().map(rol -> Arguments.of(fila, rol)));
    }

    private static Stream<Fila> filas() {
        return matriz().stream();
    }

    private static Stream<String> rutasPublicas() {
        return Stream.of("/api/auth/login", "/api/auth/refresh", "/api/auth/logout");
    }

    private String token(String rol, Instant expiraEn, JwtEncoder firmante) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject("7")
                .issuedAt(expiraEn.minusSeconds(300))
                .expiresAt(expiraEn);
        if (rol != null) {
            claims.claim("rol", rol);
        }
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return firmante.encode(JwtEncoderParameters.from(cabecera, claims.build())).getTokenValue();
    }

    private String tokenVigente(String rol) {
        return token(rol, Instant.now().plusSeconds(300), encoder);
    }

    private String tokenCaducado(String rol) {
        return token(rol, Instant.now().minusSeconds(1), encoder);
    }

    private String tokenConOtraFirma(String rol) {
        JwtEncoder otro = new ConfiguracionSeguridad().jwtEncoder(new PropiedadesJwt(OTRA_CLAVE));
        return token(rol, Instant.now().plusSeconds(300), otro);
    }

    private static ResultMatcher[] noAutenticado(String ruta) {
        return new ResultMatcher[] {
                status().isUnauthorized(),
                header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                jsonPath("$.type").value("about:blank"),
                jsonPath("$.title").value("Unauthorized"),
                jsonPath("$.status").value(401),
                jsonPath("$.detail").value("Se requiere un token de acceso válido"),
                jsonPath("$.instance").value(ruta)};
    }

    private static ResultMatcher[] sinPermiso(String ruta) {
        return new ResultMatcher[] {
                status().isForbidden(),
                header().doesNotExist(HttpHeaders.WWW_AUTHENTICATE),
                content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                jsonPath("$.type").value("about:blank"),
                jsonPath("$.title").value("Forbidden"),
                jsonPath("$.status").value(403),
                jsonPath("$.detail").value("No tiene permiso para realizar esta operación"),
                jsonPath("$.instance").value(ruta)};
    }

    private ResultActions pedir(Fila fila, String token) throws Exception {
        if (token == null) {
            return mockMvc.perform(request(fila.metodo(), fila.ruta()));
        }
        return mockMvc.perform(request(fila.metodo(), fila.ruta()).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    @ParameterizedTest(name = "{0} con {1}")
    @MethodSource("filasPorRol")
    void cadaRolSoloAccedeALoQueLaMatrizLePermite(Fila fila, String rol) throws Exception {
        ResultActions respuesta = pedir(fila, tokenVigente(rol));

        if (fila.permitidos().contains(rol)) {
            respuesta.andExpect(status().isOk()).andExpect(content().string("permitido"));
        } else {
            respuesta.andExpectAll(sinPermiso(fila.ruta()));
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filas")
    void unUsuarioSinRolRecibe403EnTodoLoProtegido(Fila fila) throws Exception {
        pedir(fila, tokenVigente(null)).andExpectAll(sinPermiso(fila.ruta()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filas")
    void sinTokenSeResponde401(Fila fila) throws Exception {
        pedir(fila, null).andExpectAll(noAutenticado(fila.ruta()));
    }

    @ParameterizedTest(name = "{0} con {1}")
    @MethodSource("filasPorRol")
    void conTokenCaducadoSeResponde401(Fila fila, String rol) throws Exception {
        pedir(fila, tokenCaducado(rol)).andExpectAll(noAutenticado(fila.ruta()));
    }

    @ParameterizedTest(name = "{0} con {1}")
    @MethodSource("filasPorRol")
    void conTokenFirmadoConOtraClaveSeResponde401(Fila fila, String rol) throws Exception {
        pedir(fila, tokenConOtraFirma(rol)).andExpectAll(noAutenticado(fila.ruta()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filas")
    void conUnTokenMalFormadoSeResponde401(Fila fila) throws Exception {
        pedir(fila, "no-es-un-jwt").andExpectAll(noAutenticado(fila.ruta()));
    }

    @Test
    void losCuerposDe401Y403SonProblemDetailSinCamposAdicionales() throws Exception {
        pedir(new Fila(HttpMethod.GET, "/api/usuarios/1", SOLO_ADMINISTRADOR), null)
                .andExpect(content().json("""
                        {"type": "about:blank", "title": "Unauthorized", "status": 401,
                         "detail": "Se requiere un token de acceso válido", "instance": "/api/usuarios/1"}
                        """, true));
        pedir(new Fila(HttpMethod.GET, "/api/usuarios/1", SOLO_ADMINISTRADOR), tokenVigente(CONSULTA))
                .andExpect(content().json("""
                        {"type": "about:blank", "title": "Forbidden", "status": 403,
                         "detail": "No tiene permiso para realizar esta operación", "instance": "/api/usuarios/1"}
                        """, true));
    }

    @Test
    void unTokenConAlgoritmoNoneSeRechaza() throws Exception {
        String sinFirma = tokenVigente(ADMINISTRADOR);
        String[] partes = sinFirma.split("\\.");
        String cabeceraNone = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));

        pedir(new Fila(HttpMethod.GET, "/api/usuarios", SOLO_ADMINISTRADOR), cabeceraNone + "." + partes[1] + ".")
                .andExpectAll(noAutenticado("/api/usuarios"));
    }

    @ParameterizedTest
    @MethodSource("rutasPublicas")
    void lasRutasDeAutenticacionSonPublicasPorPost(String ruta) throws Exception {
        pedir(new Fila(HttpMethod.POST, ruta, TODOS), null).andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("rutasPublicas")
    void lasRutasDeAutenticacionIgnoranUnTokenDeAccesoCaducadoOInvalido(String ruta) throws Exception {
        Fila fila = new Fila(HttpMethod.POST, ruta, TODOS);

        pedir(fila, tokenCaducado(GESTOR)).andExpect(status().isOk());
        pedir(fila, "no-es-un-jwt").andExpect(status().isOk());
    }

    @Test
    void laApiNoCreaSesionHttp() throws Exception {
        pedir(new Fila(HttpMethod.GET, "/api/facturas", TODOS), tokenVigente(GESTOR))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getRequest().getSession(false))
                        .isNull());
    }

    @Test
    void lasEscriturasNoExigenTokenCsrf() throws Exception {
        pedir(new Fila(HttpMethod.POST, "/api/facturas", ESCRITURA_OPERACIONES), tokenVigente(GESTOR))
                .andExpect(status().isOk());
    }
}

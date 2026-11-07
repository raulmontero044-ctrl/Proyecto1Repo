package com.facturacion.usuarios.api.http.controllers;

import com.facturacion.usuarios.api.facade.dto.AccesoEmpresaDto;
import com.facturacion.usuarios.api.facade.dto.DatosUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.NuevoUsuarioDto;
import com.facturacion.usuarios.api.facade.dto.UsuarioDto;
import com.facturacion.usuarios.internal.application.AsignarEmpresaUsuario;
import com.facturacion.usuarios.internal.application.CrearUsuario;
import com.facturacion.usuarios.internal.application.DesactivarUsuario;
import com.facturacion.usuarios.internal.application.ListarUsuarios;
import com.facturacion.usuarios.internal.application.ModificarUsuario;
import com.facturacion.usuarios.internal.application.ObtenerUsuario;
import com.facturacion.usuarios.internal.application.RetirarEmpresaUsuario;
import com.facturacion.usuarios.internal.domain.exception.AccesoEmpresaNoEncontradoException;
import com.facturacion.usuarios.internal.domain.exception.EmailUsuarioDuplicadoException;
import com.facturacion.usuarios.internal.domain.exception.EmpresaInexistenteException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioInvalidoException;
import com.facturacion.usuarios.internal.domain.exception.UsuarioNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {

    private static final String PETICION_VALIDA = """
            {
              "email": "Ana@Empresa.es",
              "nombre": "Ana",
              "password": "secreta123",
              "accesos": [{"empresaId": 1, "rol": "ADMINISTRADOR"}, {"empresaId": 2, "rol": "CONSULTA"}]
            }
            """;

    private static final String MODIFICACION_VALIDA = """
            {"nombre": "Ana María", "email": "ana.maria@empresa.es"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearUsuario crearUsuario;

    @MockitoBean
    private ObtenerUsuario obtenerUsuario;

    @MockitoBean
    private ListarUsuarios listarUsuarios;

    @MockitoBean
    private ModificarUsuario modificarUsuario;

    @MockitoBean
    private DesactivarUsuario desactivarUsuario;

    @MockitoBean
    private AsignarEmpresaUsuario asignarEmpresaUsuario;

    @MockitoBean
    private RetirarEmpresaUsuario retirarEmpresaUsuario;

    private static UsuarioDto usuario(Long id, String email) {
        return new UsuarioDto(id, email, "Ana", true, List.of(
                new AccesoEmpresaDto(1L, "ADMINISTRADOR"), new AccesoEmpresaDto(2L, "CONSULTA")));
    }

    private static String peticion(String email, String nombre, String password) {
        return """
                {"email": "%s", "nombre": "%s", "password": "%s"}
                """.formatted(email, nombre, password);
    }

    private void esperarRechazoAlCrear(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
        verify(crearUsuario, never()).ejecutar(any());
    }

    private void esperarRechazoAlModificar(String cuerpo) throws Exception {
        mockMvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
        verify(modificarUsuario, never()).ejecutar(anyLong(), any());
    }

    private void esperarRechazoAlAsignar(String cuerpo) throws Exception {
        mockMvc.perform(put("/api/usuarios/1/empresas/2").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
        verify(asignarEmpresaUsuario, never()).ejecutar(anyLong(), anyLong(), anyString());
    }

    @Test
    void creaUsuarioYDevuelve201ConLocationYCuerpoSinContrasena() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenReturn(usuario(1L, "ana@empresa.es"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/usuarios/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@empresa.es"))
                .andExpect(jsonPath("$.nombre").value("Ana"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.accesos[0].empresaId").value(1))
                .andExpect(jsonPath("$.accesos[0].rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.accesos[1].empresaId").value(2))
                .andExpect(jsonPath("$.accesos[1].rol").value("CONSULTA"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(content().string(not(containsString("secreta123"))));
    }

    @Test
    void creaUsuarioTrasladandoLaPeticionCompletaAlCasoDeUso() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenReturn(usuario(1L, "ana@empresa.es"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated());

        verify(crearUsuario).ejecutar(new NuevoUsuarioDto("Ana@Empresa.es", "Ana", "secreta123", List.of(
                new AccesoEmpresaDto(1L, "ADMINISTRADOR"), new AccesoEmpresaDto(2L, "CONSULTA"))));
    }

    @Test
    void creaUsuarioSinAccesosPasandoAccesosNulos() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenReturn(usuario(1L, "ana@empresa.es"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(peticion("ana@empresa.es", "Ana", "secreta123")))
                .andExpect(status().isCreated());

        verify(crearUsuario).ejecutar(new NuevoUsuarioDto("ana@empresa.es", "Ana", "secreta123", null));
    }

    @Test
    void crearConEmailDuplicadoDevuelve409() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenThrow(new EmailUsuarioDuplicadoException());

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ya existe otro usuario con ese email"));
    }

    @Test
    void crearConEmpresaInexistenteDevuelve400() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenThrow(new EmpresaInexistenteException(9L));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 9"));
    }

    @Test
    void crearConContrasenaFueraDeLongitudDevuelve400SinMostrarla() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class)))
                .thenThrow(new UsuarioInvalidoException("La contraseña debe tener entre 8 y 72 caracteres"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(peticion("ana@empresa.es", "Ana", "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La contraseña debe tener entre 8 y 72 caracteres"))
                .andExpect(content().string(not(containsString("corta"))));
    }

    @Test
    void crearAceptaEmailYNombreEnElLimiteDe200() throws Exception {
        when(crearUsuario.ejecutar(any(NuevoUsuarioDto.class))).thenReturn(usuario(1L, "ana@empresa.es"));
        String email = emailDeLongitud(200);

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(peticion(email, "n".repeat(200), "secreta123")))
                .andExpect(status().isCreated());
        verify(crearUsuario).ejecutar(new NuevoUsuarioDto(email, "n".repeat(200), "secreta123", null));
    }

    @Test
    void crearRechazaEmailDe201Caracteres() throws Exception {
        esperarRechazoAlCrear(peticion(emailDeLongitud(201), "Ana", "secreta123"));
    }

    private static String emailDeLongitud(int longitud) {
        String dominio = "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(63) + ".es";
        return "a".repeat(longitud - dominio.length()) + dominio;
    }

    @Test
    void crearRechazaNombreDe201Caracteres() throws Exception {
        esperarRechazoAlCrear(peticion("ana@empresa.es", "n".repeat(201), "secreta123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "no-es-un-email"})
    void crearRechazaEmailVacioOConFormatoIncorrecto(String email) throws Exception {
        esperarRechazoAlCrear(peticion(email, "Ana", "secreta123"));
    }

    @Test
    void crearRechazaNombreVacio() throws Exception {
        esperarRechazoAlCrear(peticion("ana@empresa.es", " ", "secreta123"));
    }

    @Test
    void crearRechazaCamposObligatoriosAusentes() throws Exception {
        esperarRechazoAlCrear("""
                {"nombre": "Ana", "password": "secreta123"}
                """);
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "password": "secreta123"}
                """);
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "nombre": "Ana"}
                """);
    }

    @Test
    void crearRechazaAccesosConRolInvalidoEmpresaAusenteOElementoNulo() throws Exception {
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "nombre": "Ana", "password": "secreta123",
                 "accesos": [{"empresaId": 1, "rol": "JEFE"}]}
                """);
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "nombre": "Ana", "password": "secreta123",
                 "accesos": [{"rol": "GESTOR"}]}
                """);
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "nombre": "Ana", "password": "secreta123",
                 "accesos": [{"empresaId": 1}]}
                """);
        esperarRechazoAlCrear("""
                {"email": "ana@empresa.es", "nombre": "Ana", "password": "secreta123", "accesos": [null]}
                """);
    }

    @Test
    void crearRechazaCuerpoMalFormadoOAusente() throws Exception {
        esperarRechazoAlCrear("{");
        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtieneUnUsuario() throws Exception {
        when(obtenerUsuario.ejecutar(1L)).thenReturn(usuario(1L, "ana@empresa.es"));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ana@empresa.es"))
                .andExpect(jsonPath("$.accesos.length()").value(2))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void usuarioInexistenteDevuelve404() throws Exception {
        when(obtenerUsuario.ejecutar(99L)).thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe el usuario con id 99"));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/usuarios/abc")).andExpect(status().isBadRequest());
        verify(obtenerUsuario, never()).ejecutar(anyLong());
    }

    @Test
    void listaTodosSinFiltro() throws Exception {
        when(listarUsuarios.ejecutar(null)).thenReturn(List.of(usuario(1L, "a@empresa.es"), usuario(2L, "b@empresa.es")));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("a@empresa.es"))
                .andExpect(jsonPath("$[1].email").value("b@empresa.es"));
        verify(listarUsuarios).ejecutar(null);
    }

    @Test
    void listaFiltrandoPorEmpresa() throws Exception {
        when(listarUsuarios.ejecutar(3L)).thenReturn(List.of(usuario(2L, "b@empresa.es")));

        mockMvc.perform(get("/api/usuarios").param("empresaId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));
        verify(listarUsuarios).ejecutar(3L);
    }

    @Test
    void listarConEmpresaNoNumericaDevuelve400() throws Exception {
        mockMvc.perform(get("/api/usuarios").param("empresaId", "abc")).andExpect(status().isBadRequest());
        verify(listarUsuarios, never()).ejecutar(any());
    }

    @Test
    void modificaUsuarioYDevuelve200() throws Exception {
        when(modificarUsuario.ejecutar(eq(1L), any(DatosUsuarioDto.class))).thenReturn(usuario(1L, "ana.maria@empresa.es"));

        mockMvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(MODIFICACION_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana.maria@empresa.es"));
        verify(modificarUsuario).ejecutar(1L, new DatosUsuarioDto("Ana María", "ana.maria@empresa.es"));
    }

    @Test
    void modificarUsuarioInexistenteDevuelve404() throws Exception {
        when(modificarUsuario.ejecutar(eq(99L), any(DatosUsuarioDto.class))).thenThrow(new UsuarioNoEncontradoException(99L));

        mockMvc.perform(put("/api/usuarios/99").contentType(MediaType.APPLICATION_JSON).content(MODIFICACION_VALIDA))
                .andExpect(status().isNotFound());
    }

    @Test
    void modificarConEmailDeOtroUsuarioDevuelve409() throws Exception {
        when(modificarUsuario.ejecutar(eq(1L), any(DatosUsuarioDto.class))).thenThrow(new EmailUsuarioDuplicadoException());

        mockMvc.perform(put("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(MODIFICACION_VALIDA))
                .andExpect(status().isConflict());
    }

    @Test
    void modificarRechazaDatosInvalidos() throws Exception {
        esperarRechazoAlModificar("""
                {"nombre": "", "email": "ana@empresa.es"}
                """);
        esperarRechazoAlModificar("""
                {"nombre": "Ana", "email": "no-es-un-email"}
                """);
        esperarRechazoAlModificar("""
                {"nombre": "%s", "email": "ana@empresa.es"}
                """.formatted("n".repeat(201)));
        esperarRechazoAlModificar("""
                {"nombre": "Ana"}
                """);
    }

    @Test
    void desactivaUsuarioYDevuelve204() throws Exception {
        mockMvc.perform(post("/api/usuarios/1/desactivar"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(desactivarUsuario).ejecutar(1L);
    }

    @Test
    void desactivarUsuarioInexistenteDevuelve404() throws Exception {
        doThrow(new UsuarioNoEncontradoException(99L)).when(desactivarUsuario).ejecutar(99L);

        mockMvc.perform(post("/api/usuarios/99/desactivar")).andExpect(status().isNotFound());
    }

    @Test
    void asignaEmpresaYDevuelve200ConElUsuario() throws Exception {
        when(asignarEmpresaUsuario.ejecutar(1L, 2L, "GESTOR")).thenReturn(usuario(1L, "ana@empresa.es"));

        mockMvc.perform(put("/api/usuarios/1/empresas/2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol": "GESTOR"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accesos.length()").value(2));
        verify(asignarEmpresaUsuario).ejecutar(1L, 2L, "GESTOR");
    }

    @Test
    void asignarEmpresaInexistenteDevuelve400() throws Exception {
        when(asignarEmpresaUsuario.ejecutar(1L, 2L, "GESTOR")).thenThrow(new EmpresaInexistenteException(2L));

        mockMvc.perform(put("/api/usuarios/1/empresas/2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol": "GESTOR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 2"));
    }

    @Test
    void asignarAUsuarioInexistenteDevuelve404() throws Exception {
        when(asignarEmpresaUsuario.ejecutar(1L, 2L, "GESTOR")).thenThrow(new UsuarioNoEncontradoException(1L));

        mockMvc.perform(put("/api/usuarios/1/empresas/2").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol": "GESTOR"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void asignarRechazaRolInvalidoOAusente() throws Exception {
        esperarRechazoAlAsignar("""
                {"rol": "JEFE"}
                """);
        esperarRechazoAlAsignar("""
                {"rol": "gestor"}
                """);
        esperarRechazoAlAsignar("{}");
        mockMvc.perform(put("/api/usuarios/1/empresas/abc").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rol": "GESTOR"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retiraEmpresaYDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1/empresas/2"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(retirarEmpresaUsuario).ejecutar(1L, 2L);
    }

    @Test
    void retirarAccesoInexistenteDevuelve404() throws Exception {
        doThrow(new AccesoEmpresaNoEncontradoException(1L, 2L)).when(retirarEmpresaUsuario).ejecutar(1L, 2L);

        mockMvc.perform(delete("/api/usuarios/1/empresas/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("El usuario con id 1 no tiene acceso a la empresa con id 2"));
    }

    @Test
    void retirarDeUsuarioInexistenteDevuelve404() throws Exception {
        doThrow(new UsuarioNoEncontradoException(99L)).when(retirarEmpresaUsuario).ejecutar(99L, 2L);

        mockMvc.perform(delete("/api/usuarios/99/empresas/2")).andExpect(status().isNotFound());
    }
}

package com.facturacion.empresas.api.http.controllers;

import com.facturacion.app.ConfiguracionReloj;
import com.facturacion.app.ConfiguracionSeguridad;
import com.facturacion.empresas.api.facade.dto.ContactoEmpresaDto;
import com.facturacion.empresas.api.facade.dto.DatosFiscalesEmpresaDto;
import com.facturacion.empresas.api.facade.dto.EmpresaDto;
import com.facturacion.empresas.api.facade.dto.NuevaEmpresaDto;
import com.facturacion.empresas.internal.application.CrearEmpresa;
import com.facturacion.empresas.internal.application.ListarEmpresas;
import com.facturacion.empresas.internal.application.ModificarContactoEmpresa;
import com.facturacion.empresas.internal.application.ModificarDatosFiscalesEmpresa;
import com.facturacion.empresas.internal.application.ObtenerEmpresa;
import com.facturacion.empresas.internal.domain.exception.EmpresaInvalidaException;
import com.facturacion.empresas.internal.domain.exception.EmpresaNoEncontradaException;
import com.facturacion.empresas.internal.domain.exception.NifEmpresaDuplicadoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmpresaController.class)
@Import({ConfiguracionSeguridad.class, ConfiguracionReloj.class})
@WithMockUser(roles = "ADMINISTRADOR")
class EmpresaControllerTest {

    private static final String PETICION_VALIDA = """
            {
              "razonSocial": "Empresa SL",
              "nif": "B12345678",
              "direccion": "Calle Mayor 1",
              "codigoPostal": "28001",
              "municipio": "Madrid",
              "provincia": "Madrid",
              "pais": "España",
              "email": "info@empresa.es",
              "telefono": "910000000"
            }
            """;

    private static final String DATOS_FISCALES_VALIDOS = """
            {
              "razonSocial": "Otra SA",
              "nif": "A87654321",
              "direccion": "Avenida Sur 2",
              "codigoPostal": "41001",
              "municipio": "Sevilla",
              "provincia": "Sevilla",
              "pais": "Portugal"
            }
            """;

    private static final String CONTACTO_VALIDO = """
            {"email": "nuevo@empresa.es", "telefono": "600000000"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrearEmpresa crearEmpresa;

    @MockitoBean
    private ObtenerEmpresa obtenerEmpresa;

    @MockitoBean
    private ListarEmpresas listarEmpresas;

    @MockitoBean
    private ModificarDatosFiscalesEmpresa modificarDatosFiscalesEmpresa;

    @MockitoBean
    private ModificarContactoEmpresa modificarContactoEmpresa;

    private static EmpresaDto empresa(Long id, String nif) {
        return new EmpresaDto(
                id, "Empresa SL", nif, "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000", true);
    }

    private static String peticion(
            String razonSocial, String nif, String direccion, String codigoPostal,
            String municipio, String provincia, String pais, String email, String telefono) {
        return """
                {"razonSocial": "%s", "nif": "%s", "direccion": "%s", "codigoPostal": "%s",
                 "municipio": "%s", "provincia": "%s", "pais": "%s", "email": "%s", "telefono": "%s"}
                """.formatted(razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais, email, telefono);
    }

    private static String datosFiscales(
            String razonSocial, String nif, String direccion, String codigoPostal,
            String municipio, String provincia, String pais) {
        return """
                {"razonSocial": "%s", "nif": "%s", "direccion": "%s", "codigoPostal": "%s",
                 "municipio": "%s", "provincia": "%s", "pais": "%s"}
                """.formatted(razonSocial, nif, direccion, codigoPostal, municipio, provincia, pais);
    }

    private void esperarRechazoAlCrear(String cuerpo) throws Exception {
        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    private void esperarRechazoDeDatosFiscales(String cuerpo) throws Exception {
        mockMvc.perform(put("/api/empresas/1/datos-fiscales").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    private void esperarRechazoDeContacto(String cuerpo) throws Exception {
        mockMvc.perform(put("/api/empresas/1/contacto").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creaEmpresaYDevuelve201ConLocationYCuerpoCompleto() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class))).thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/empresas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.razonSocial").value("Empresa SL"))
                .andExpect(jsonPath("$.nif").value("B12345678"))
                .andExpect(jsonPath("$.direccion").value("Calle Mayor 1"))
                .andExpect(jsonPath("$.codigoPostal").value("28001"))
                .andExpect(jsonPath("$.municipio").value("Madrid"))
                .andExpect(jsonPath("$.provincia").value("Madrid"))
                .andExpect(jsonPath("$.pais").value("España"))
                .andExpect(jsonPath("$.email").value("info@empresa.es"))
                .andExpect(jsonPath("$.telefono").value("910000000"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void trasladaLaPeticionDeAltaAlCasoDeUso() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class))).thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isCreated());

        verify(crearEmpresa).ejecutar(new NuevaEmpresaDto(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España",
                "info@empresa.es", "910000000"));
    }

    @Test
    void aceptaAltaSinContacto() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class))).thenReturn(empresa(1L, "B12345678"));
        String sinContacto = """
                {"razonSocial": "Empresa SL", "nif": "B12345678", "direccion": "Calle Mayor 1",
                 "codigoPostal": "28001", "municipio": "Madrid", "provincia": "Madrid", "pais": "España"}
                """;

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(sinContacto))
                .andExpect(status().isCreated());

        verify(crearEmpresa).ejecutar(new NuevaEmpresaDto(
                "Empresa SL", "B12345678", "Calle Mayor 1", "28001", "Madrid", "Madrid", "España", null, null));
    }

    @Test
    void aceptaAltaConValoresEnElLimiteDeSusColumnas() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class))).thenReturn(empresa(1L, "B12345678"));
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(7);
        String enLimite = peticion(
                "R".repeat(200), "N".repeat(20), "D".repeat(300), "C".repeat(10),
                "M".repeat(100), "P".repeat(100), "S".repeat(100), email, "T".repeat(30));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(enLimite))
                .andExpect(status().isCreated());
    }

    @Test
    void rechazaAltaConTextosMasLargosQueSusColumnas() throws Exception {
        esperarRechazoAlCrear(peticion("R".repeat(201), "B12345678", "Calle", "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "N".repeat(21), "Calle", "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "D".repeat(301), "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "C".repeat(11), "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "M".repeat(101), "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "P".repeat(101), "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "S".repeat(101), "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España",
                "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(8), "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España", "a@b.es", "T".repeat(31)));

        verify(crearEmpresa, never()).ejecutar(any());
    }

    @Test
    void rechazaAltaConCamposObligatoriosVaciosOAusentes() throws Exception {
        esperarRechazoAlCrear(peticion(" ", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "", "Calle", "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "", "28001", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "", "Madrid", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "", "Madrid", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "", "España", "a@b.es", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "", "a@b.es", "1"));
        esperarRechazoAlCrear("""
                {"razonSocial": "Empresa SL", "direccion": "Calle Mayor 1", "codigoPostal": "28001",
                 "municipio": "Madrid", "provincia": "Madrid", "pais": "España"}
                """);

        verify(crearEmpresa, never()).ejecutar(any());
    }

    @Test
    void rechazaAltaConEmailSinFormatoValido() throws Exception {
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España", "sin-arroba", "1"));
        esperarRechazoAlCrear(peticion("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España", "a@b@c", "1"));

        verify(crearEmpresa, never()).ejecutar(any());
    }

    @Test
    void rechazaAltaConCuerpoMalFormadoOAusente() throws Exception {
        esperarRechazoAlCrear("{\"razonSocial\": ");
        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(crearEmpresa, never()).ejecutar(any());
    }

    @Test
    void nifDuplicadoAlCrearDevuelve409ConProblemDetail() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class))).thenThrow(new NifEmpresaDuplicadoException());

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Ya existe otra empresa con ese NIF"));
    }

    @Test
    void empresaInvalidaEnElDominioDevuelve400ConProblemDetail() throws Exception {
        when(crearEmpresa.ejecutar(any(NuevaEmpresaDto.class)))
                .thenThrow(new EmpresaInvalidaException("El NIF de la empresa es obligatorio"));

        mockMvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content(PETICION_VALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("El NIF de la empresa es obligatorio"));
    }

    @Test
    void obtieneEmpresaPorId() throws Exception {
        when(obtenerEmpresa.ejecutar(1L)).thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(get("/api/empresas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nif").value("B12345678"))
                .andExpect(jsonPath("$.activa").value(true));
    }

    @Test
    void empresaInexistenteDevuelve404ConProblemDetail() throws Exception {
        when(obtenerEmpresa.ejecutar(99L)).thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(get("/api/empresas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 99"));
    }

    @Test
    void idNoNumericoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/empresas/abc"))
                .andExpect(status().isBadRequest());

        verify(obtenerEmpresa, never()).ejecutar(any());
    }

    @Test
    void listaEmpresasEnElOrdenDelCasoDeUso() throws Exception {
        when(listarEmpresas.ejecutar()).thenReturn(List.of(empresa(1L, "B11111111"), empresa(2L, "B22222222")));

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nif").value("B11111111"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nif").value("B22222222"));
    }

    @Test
    void listaVaciaDevuelve200ConArrayVacio() throws Exception {
        when(listarEmpresas.ejecutar()).thenReturn(List.of());

        mockMvc.perform(get("/api/empresas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void modificaDatosFiscalesYDevuelve200() throws Exception {
        when(modificarDatosFiscalesEmpresa.ejecutar(eq(1L), any(DatosFiscalesEmpresaDto.class)))
                .thenReturn(empresa(1L, "A87654321"));

        mockMvc.perform(put("/api/empresas/1/datos-fiscales")
                        .contentType(MediaType.APPLICATION_JSON).content(DATOS_FISCALES_VALIDOS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nif").value("A87654321"));

        verify(modificarDatosFiscalesEmpresa).ejecutar(1L, new DatosFiscalesEmpresaDto(
                "Otra SA", "A87654321", "Avenida Sur 2", "41001", "Sevilla", "Sevilla", "Portugal"));
    }

    @Test
    void aceptaDatosFiscalesEnElLimiteDeSusColumnas() throws Exception {
        when(modificarDatosFiscalesEmpresa.ejecutar(eq(1L), any(DatosFiscalesEmpresaDto.class)))
                .thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(put("/api/empresas/1/datos-fiscales").contentType(MediaType.APPLICATION_JSON)
                        .content(datosFiscales("R".repeat(200), "N".repeat(20), "D".repeat(300), "C".repeat(10),
                                "M".repeat(100), "P".repeat(100), "S".repeat(100))))
                .andExpect(status().isOk());
    }

    @Test
    void rechazaDatosFiscalesInvalidosSinInvocarCasoDeUso() throws Exception {
        esperarRechazoDeDatosFiscales(datosFiscales("R".repeat(201), "B12345678", "Calle", "28001", "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "N".repeat(21), "Calle", "28001", "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "B12345678", "D".repeat(301), "28001", "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "B12345678", "Calle", "C".repeat(11), "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "B12345678", "Calle", "28001", "M".repeat(101), "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "P".repeat(101), "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", "B12345678", "Calle", "28001", "Madrid", "Madrid", "S".repeat(101)));
        esperarRechazoDeDatosFiscales(datosFiscales("", "B12345678", "Calle", "28001", "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales(datosFiscales("Empresa SL", " ", "Calle", "28001", "Madrid", "Madrid", "España"));
        esperarRechazoDeDatosFiscales("""
                {"razonSocial": "Empresa SL", "nif": "B12345678", "direccion": "Calle", "codigoPostal": "28001",
                 "municipio": "Madrid", "provincia": "Madrid"}
                """);
        esperarRechazoDeDatosFiscales("{\"nif\": ");

        verify(modificarDatosFiscalesEmpresa, never()).ejecutar(anyLong(), any());
    }

    @Test
    void modificarDatosFiscalesDeEmpresaInexistenteDevuelve404() throws Exception {
        when(modificarDatosFiscalesEmpresa.ejecutar(eq(99L), any(DatosFiscalesEmpresaDto.class)))
                .thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(put("/api/empresas/99/datos-fiscales")
                        .contentType(MediaType.APPLICATION_JSON).content(DATOS_FISCALES_VALIDOS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 99"));
    }

    @Test
    void modificarDatosFiscalesConNifDeOtraEmpresaDevuelve409() throws Exception {
        when(modificarDatosFiscalesEmpresa.ejecutar(eq(1L), any(DatosFiscalesEmpresaDto.class)))
                .thenThrow(new NifEmpresaDuplicadoException());

        mockMvc.perform(put("/api/empresas/1/datos-fiscales")
                        .contentType(MediaType.APPLICATION_JSON).content(DATOS_FISCALES_VALIDOS))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe otra empresa con ese NIF"));
    }

    @Test
    void modificaContactoYDevuelve200() throws Exception {
        when(modificarContactoEmpresa.ejecutar(eq(1L), any(ContactoEmpresaDto.class)))
                .thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(put("/api/empresas/1/contacto").contentType(MediaType.APPLICATION_JSON).content(CONTACTO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(modificarContactoEmpresa).ejecutar(1L, new ContactoEmpresaDto("nuevo@empresa.es", "600000000"));
    }

    @Test
    void modificarContactoPermiteVaciarlo() throws Exception {
        when(modificarContactoEmpresa.ejecutar(eq(1L), any(ContactoEmpresaDto.class)))
                .thenReturn(empresa(1L, "B12345678"));

        mockMvc.perform(put("/api/empresas/1/contacto").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());

        verify(modificarContactoEmpresa).ejecutar(1L, new ContactoEmpresaDto(null, null));
    }

    @Test
    void rechazaContactoInvalidoSinInvocarCasoDeUso() throws Exception {
        esperarRechazoDeContacto("{\"email\": \"sin-arroba\"}");
        esperarRechazoDeContacto("{\"email\": \"" + "a".repeat(64) + "@" + "b".repeat(63) + "."
                + "c".repeat(63) + "." + "d".repeat(8) + "\"}");
        esperarRechazoDeContacto("{\"telefono\": \"" + "T".repeat(31) + "\"}");
        esperarRechazoDeContacto("{\"email\": ");
        mockMvc.perform(put("/api/empresas/1/contacto").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(modificarContactoEmpresa, never()).ejecutar(anyLong(), any());
    }

    @Test
    void aceptaContactoEnElLimiteDeSusColumnas() throws Exception {
        when(modificarContactoEmpresa.ejecutar(eq(1L), any(ContactoEmpresaDto.class)))
                .thenReturn(empresa(1L, "B12345678"));
        String email = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(7);

        mockMvc.perform(put("/api/empresas/1/contacto").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"telefono\": \"" + "T".repeat(30) + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void modificarContactoDeEmpresaInexistenteDevuelve404() throws Exception {
        when(modificarContactoEmpresa.ejecutar(eq(99L), any(ContactoEmpresaDto.class)))
                .thenThrow(new EmpresaNoEncontradaException(99L));

        mockMvc.perform(put("/api/empresas/99/contacto").contentType(MediaType.APPLICATION_JSON).content(CONTACTO_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la empresa con id 99"));
    }
}

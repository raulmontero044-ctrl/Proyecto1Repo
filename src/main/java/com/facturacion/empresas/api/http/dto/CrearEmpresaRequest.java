package com.facturacion.empresas.api.http.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearEmpresaRequest(
        @NotBlank @Size(max = 200) String razonSocial,
        @NotBlank @Size(max = 20) String nif,
        @NotBlank @Size(max = 300) String direccion,
        @NotBlank @Size(max = 10) String codigoPostal,
        @NotBlank @Size(max = 100) String municipio,
        @NotBlank @Size(max = 100) String provincia,
        @NotBlank @Size(max = 100) String pais,
        @Email @Size(max = 200) String email,
        @Size(max = 30) String telefono) {
}

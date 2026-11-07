package com.facturacion.usuarios.api.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CrearUsuarioRequest(
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 200) String nombre,
        @NotNull String password,
        List<@NotNull @Valid AccesoEmpresaRequest> accesos) {
}

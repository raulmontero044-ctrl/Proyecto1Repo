package com.facturacion.usuarios.api.http.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ModificarUsuarioRequest(
        @NotBlank @Size(max = 200) String nombre,
        @NotBlank @Email @Size(max = 200) String email) {
}

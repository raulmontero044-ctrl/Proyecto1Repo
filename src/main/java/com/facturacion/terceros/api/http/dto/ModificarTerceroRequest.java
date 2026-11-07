package com.facturacion.terceros.api.http.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModificarTerceroRequest(
        @NotBlank @Size(max = 20) String nif,
        @NotBlank @Size(max = 200) String razonSocial,
        @Size(max = 300) String direccion,
        @Size(max = 10) String codigoPostal,
        @Size(max = 100) String municipio,
        @Size(max = 100) String provincia,
        @Size(max = 100) String pais,
        @Email @Size(max = 200) String email,
        @Size(max = 30) String telefono,
        @NotNull Boolean esCliente,
        @NotNull Boolean esProveedor) {
}

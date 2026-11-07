package com.facturacion.empresas.api.http.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ModificarContactoEmpresaRequest(
        @Email @Size(max = 200) String email,
        @Size(max = 30) String telefono) {
}

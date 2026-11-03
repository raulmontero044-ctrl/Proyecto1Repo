package com.facturacion.factura.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearFacturaRequest(
        @NotBlank String numero,
        @NotNull LocalDate fecha,
        @NotBlank String clienteNombre,
        @NotBlank String clienteNif,
        @NotNull @DecimalMin("0") BigDecimal porcentajeIva,
        @NotEmpty List<@NotNull @Valid LineaFacturaRequest> lineas) {
}

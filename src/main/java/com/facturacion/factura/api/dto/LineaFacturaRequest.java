package com.facturacion.factura.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LineaFacturaRequest(
        @NotBlank String concepto,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal cantidad,
        @NotNull @DecimalMin("0") BigDecimal precioUnitario) {
}

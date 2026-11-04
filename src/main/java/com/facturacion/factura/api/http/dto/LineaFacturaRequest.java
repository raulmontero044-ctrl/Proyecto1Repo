package com.facturacion.factura.api.http.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LineaFacturaRequest(
        @NotBlank @Size(max = 500) String concepto,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 9, fraction = 3) BigDecimal cantidad,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario) {
}

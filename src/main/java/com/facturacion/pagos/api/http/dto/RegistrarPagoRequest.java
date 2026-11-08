package com.facturacion.pagos.api.http.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarPagoRequest(
        @NotNull Long facturaId,
        @NotNull LocalDate fecha,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal importe,
        @NotNull @Pattern(regexp = "TRANSFERENCIA|TARJETA|EFECTIVO|DOMICILIACION|OTRO") String metodo,
        @Size(max = 100) String referencia) {
}

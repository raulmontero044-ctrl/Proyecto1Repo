package com.facturacion.factura.api.http.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CrearFacturaRequest(
        @NotBlank @Size(max = 50) String numero,
        @NotNull @Pattern(regexp = "EMITIDA|RECIBIDA") String tipo,
        @NotNull LocalDate fecha,
        LocalDate fechaVencimiento,
        @NotNull Long empresaId,
        @NotNull Long terceroId,
        @NotBlank @Size(max = 200) String clienteNombre,
        @NotBlank @Size(max = 20) String clienteNif,
        @NotNull @DecimalMin("0") @Digits(integer = 3, fraction = 2) BigDecimal porcentajeIva,
        @NotEmpty List<@NotNull @Valid LineaFacturaRequest> lineas) {
}

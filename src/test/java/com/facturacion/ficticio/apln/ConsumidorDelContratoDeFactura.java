package com.facturacion.ficticio.apln;

import com.facturacion.factura.api.dto.FacturaDto;
import com.facturacion.factura.api.dto.NuevaFacturaDto;

record ConsumidorDelContratoDeFactura(NuevaFacturaDto nueva, FacturaDto creada) {
}

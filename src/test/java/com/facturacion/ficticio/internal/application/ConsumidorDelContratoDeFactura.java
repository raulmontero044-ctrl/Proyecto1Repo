package com.facturacion.ficticio.internal.application;

import com.facturacion.factura.api.facade.dto.FacturaDto;
import com.facturacion.factura.api.facade.dto.NuevaFacturaDto;

record ConsumidorDelContratoDeFactura(NuevaFacturaDto nueva, FacturaDto creada) {
}

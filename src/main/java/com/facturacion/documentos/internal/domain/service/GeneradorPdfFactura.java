package com.facturacion.documentos.internal.domain.service;

import com.facturacion.documentos.internal.domain.model.FacturaImprimible;

public interface GeneradorPdfFactura {

    byte[] generar(FacturaImprimible factura);
}

package com.facturacion.auth.internal.domain.service;

import com.facturacion.auth.internal.domain.model.TokenAcceso;

public interface FirmadorTokenAcceso {

    String firmar(TokenAcceso tokenAcceso);
}

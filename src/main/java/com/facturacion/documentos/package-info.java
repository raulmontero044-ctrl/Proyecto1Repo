@org.springframework.modulith.ApplicationModule(
        displayName = "Documentos",
        allowedDependencies = {
                "factura :: facade",
                "factura :: facade-dto",
                "empresas :: facade",
                "empresas :: facade-dto",
                "terceros :: facade",
                "terceros :: facade-dto",
                "pagos :: facade",
                "pagos :: facade-dto"})
package com.facturacion.documentos;

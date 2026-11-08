@org.springframework.modulith.ApplicationModule(
        displayName = "Informes",
        allowedDependencies = {
                "factura :: facade",
                "factura :: facade-dto",
                "pagos :: facade",
                "pagos :: facade-dto"})
package com.facturacion.informes;

CREATE TABLE factura (
    id BIGINT NOT NULL AUTO_INCREMENT,
    numero VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    cliente_nombre VARCHAR(200) NOT NULL,
    cliente_nif VARCHAR(20) NOT NULL,
    porcentaje_iva DECIMAL(5, 2) NOT NULL,
    CONSTRAINT pk_factura PRIMARY KEY (id),
    CONSTRAINT uk_factura_numero UNIQUE (numero)
);

CREATE TABLE linea_factura (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factura_id BIGINT NOT NULL,
    posicion INT NOT NULL,
    concepto VARCHAR(500) NOT NULL,
    cantidad DECIMAL(12, 3) NOT NULL,
    precio_unitario DECIMAL(12, 2) NOT NULL,
    CONSTRAINT pk_linea_factura PRIMARY KEY (id),
    CONSTRAINT fk_linea_factura_factura FOREIGN KEY (factura_id) REFERENCES factura (id) ON DELETE CASCADE
);

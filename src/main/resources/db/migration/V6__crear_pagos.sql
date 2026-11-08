CREATE TABLE pago (
    id BIGINT NOT NULL AUTO_INCREMENT,
    factura_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    fecha DATE NOT NULL,
    importe DECIMAL(12, 2) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    referencia VARCHAR(100),
    CONSTRAINT pk_pago PRIMARY KEY (id)
);

CREATE INDEX ix_pago_factura ON pago (factura_id);
CREATE INDEX ix_pago_empresa_fecha ON pago (empresa_id, fecha);

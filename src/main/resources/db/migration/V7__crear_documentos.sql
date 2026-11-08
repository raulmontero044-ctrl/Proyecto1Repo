CREATE TABLE documento (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entidad_tipo VARCHAR(10) NOT NULL,
    entidad_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    tamano BIGINT NOT NULL,
    origen VARCHAR(10) NOT NULL,
    creado_en DATETIME(6) NOT NULL,
    CONSTRAINT pk_documento PRIMARY KEY (id)
);

CREATE INDEX ix_documento_entidad ON documento (entidad_tipo, entidad_id);

CREATE TABLE documento_contenido (
    documento_id BIGINT NOT NULL,
    contenido LONGBLOB NOT NULL,
    CONSTRAINT pk_documento_contenido PRIMARY KEY (documento_id),
    CONSTRAINT fk_documento_contenido_documento FOREIGN KEY (documento_id) REFERENCES documento (id) ON DELETE CASCADE
);

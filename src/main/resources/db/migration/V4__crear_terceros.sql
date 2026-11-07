CREATE TABLE tercero (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nif VARCHAR(20) NOT NULL,
    razon_social VARCHAR(200) NOT NULL,
    direccion VARCHAR(300),
    codigo_postal VARCHAR(10),
    municipio VARCHAR(100),
    provincia VARCHAR(100),
    pais VARCHAR(100),
    email VARCHAR(200),
    telefono VARCHAR(30),
    es_cliente BIT(1) NOT NULL,
    es_proveedor BIT(1) NOT NULL,
    activo BIT(1) NOT NULL,
    CONSTRAINT pk_tercero PRIMARY KEY (id),
    CONSTRAINT uk_tercero_empresa_nif UNIQUE (empresa_id, nif)
);

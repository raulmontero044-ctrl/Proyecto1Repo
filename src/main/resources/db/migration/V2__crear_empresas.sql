CREATE TABLE empresa (
    id BIGINT NOT NULL AUTO_INCREMENT,
    razon_social VARCHAR(200) NOT NULL,
    nif VARCHAR(20) NOT NULL,
    direccion VARCHAR(300) NOT NULL,
    codigo_postal VARCHAR(10) NOT NULL,
    municipio VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    email VARCHAR(200),
    telefono VARCHAR(30),
    activa BIT(1) NOT NULL,
    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT uk_empresa_nif UNIQUE (nif)
);

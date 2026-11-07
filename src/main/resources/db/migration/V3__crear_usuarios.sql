CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(200) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    activo BIT(1) NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE usuario_empresa (
    usuario_id BIGINT NOT NULL,
    empresa_id BIGINT NOT NULL,
    rol VARCHAR(20) NOT NULL,
    CONSTRAINT uk_usuario_empresa_usuario_empresa UNIQUE (usuario_id, empresa_id),
    CONSTRAINT fk_usuario_empresa_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    expira_en DATETIME(6) NOT NULL,
    revocado BIT(1) NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (hash)
);

-- Criar a sequence que dita o id para o auth
CREATE SEQUENCE IF NOT EXISTS usuarios.auth_seq START WITH 1 INCREMENT BY 1;

-- Criar a tabela de auth no schema relacionando com tb_usuario
CREATE TABLE IF NOT EXISTS usuarios.tb_auth
(
    id                 BIGINT PRIMARY KEY,
    user_id            BIGINT UNIQUE       NOT NULL,
    hash_senha         VARCHAR(255) UNIQUE NOT NULL,
    token              VARCHAR(255) UNIQUE NOT NULL,
    verificado         BOOLEAN             NOT NULL DEFAULT FALSE,
    verificacao2etapas BOOLEAN             NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_auth_usuario FOREIGN KEY (user_id)
        REFERENCES usuarios.tb_auth (id) ON DELETE CASCADE
);
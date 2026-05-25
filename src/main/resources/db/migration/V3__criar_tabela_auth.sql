-- Criar a sequence que dita o id para o auth
CREATE SEQUENCE IF NOT EXISTS usuarios.auth_seq START WITH 1 INCREMENT BY 1;

-- Criar a tabela de auth no schema relacionando com tb_usuario
CREATE TABLE IF NOT EXISTS usuarios.tb_auth
(
    id                 BIGINT PRIMARY KEY,
    user_id            BIGINT UNIQUE       NOT NULL,
    hash_senha         VARCHAR(255) UNIQUE NOT NULL,
    role               VARCHAR(50) check (role in ('USER','ADMIN')) NOT NULL DEFAULT 'USER',
    token              VARCHAR(255) UNIQUE,
    is_verificado      BOOLEAN             NOT NULL DEFAULT FALSE,
    modo_verificacao   VARCHAR(50) check (modo_verificacao in ('PENDENTE','EMAIL','SMS','TELEGRAM')),
    verificacao_duas_etapas BOOLEAN             NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_auth_usuario FOREIGN KEY (user_id)
        REFERENCES usuarios.tb_usuario (id) ON DELETE CASCADE
);
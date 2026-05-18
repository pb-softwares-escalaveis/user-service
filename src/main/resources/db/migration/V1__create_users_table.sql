-- Criando o schema
CREATE SCHEMA IF NOT EXISTS usuario;

-- Criar a sequence que dita o id para o usuario
CREATE SEQUENCE IF NOT EXISTS usuario.usuario_seq START WITH 1 INCREMENT BY 1;

-- Criar a tabela no schema novo
CREATE TABLE IF NOT EXISTS usuario.tb_users
(
    id         BIGINT PRIMARY KEY,
    name       VARCHAR(255)        NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    cpf        VARCHAR(255)        NOT NULL,
    marks      INTEGER             NOT NULL DEFAULT 3,
    status     VARCHAR(50)         NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ                  DEFAULT NOW()
);
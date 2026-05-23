-- Criando o schema
CREATE SCHEMA IF NOT EXISTS usuarios;

-- Criar a sequence que dita o id para o usuario
CREATE SEQUENCE IF NOT EXISTS usuarios.usuario_seq START WITH 1 INCREMENT BY 1;

-- Criar a tabela no schema novo
CREATE TABLE IF NOT EXISTS usuarios.tb_usuario
(
    id              BIGINT PRIMARY KEY,
    nome            VARCHAR(30)         NOT NULL,
    sobrenome       VARCHAR(100)        NOT NULL,
    username        VARCHAR(50) UNIQUE  NOT NULL,
    foto_perfil     VARCHAR(200),
    email           VARCHAR(255) UNIQUE NOT NULL,
    cpf             VARCHAR(255) UNIQUE NOT NULL,
    data_nascimento DATE                NOT NULL,
    telefone        VARCHAR(20) UNIQUE  NOT NULL,
    status          VARCHAR(50)         NOT NULL DEFAULT 'ACTIVE',
    marks           INTEGER             NOT NULL DEFAULT 3,
    reputacao       FLOAT               NOT NULL DEFAULT 5.00,
    last_login      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ                  DEFAULT NOW(),
    updated_at      TIMESTAMPTZ                  DEFAULT NOW()
);

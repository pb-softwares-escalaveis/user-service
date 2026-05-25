-- Criando o schema
CREATE SCHEMA IF NOT EXISTS usuarios;

-- Criar a sequence que dita o id para o usuario
CREATE SEQUENCE IF NOT EXISTS usuarios.usuario_seq START WITH 1 INCREMENT BY 1;

-- Criar a tabela de usuarios no schema
CREATE TABLE IF NOT EXISTS usuarios.tb_usuario
(
    id                BIGINT PRIMARY KEY,
    nome              VARCHAR(50)                                                              NOT NULL,
    sobrenome         VARCHAR(50)                                                              NOT NULL,
    username          VARCHAR(50) UNIQUE                                                       NOT NULL,
    foto_perfil       VARCHAR(200),
    email             VARCHAR(255) UNIQUE                                                      NOT NULL,
    cpf               VARCHAR(11) UNIQUE                                                       NOT NULL,
    data_nascimento   DATE                                                                     NOT NULL,
    telefone          VARCHAR(20) UNIQUE                                                       NOT NULL,
    status            VARCHAR(50) check (status in ('ATIVO', 'SUSPENSO', 'BANIDO', 'INATIVO')) NOT NULL DEFAULT 'ATIVO',
    data_ultimo_login TIMESTAMPTZ,
    data_criacao      TIMESTAMPTZ                                                                       DEFAULT NOW(),
    data_atualizacao  TIMESTAMPTZ,

    CONSTRAINT chk_idade_minima CHECK (data_nascimento <= (CURRENT_DATE - interval '18 years')),
    CONSTRAINT chk_nome_length CHECK (char_length(nome) >= 2 AND char_length(nome) <= 50),
    CONSTRAINT chk_sobrenome_length CHECK (char_length(sobrenome) >= 2 AND char_length(sobrenome) <= 50),
    CONSTRAINT chk_username_length CHECK (char_length(username) >= 4 AND char_length(username) <= 50),
    CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_telefone_format CHECK (telefone ~ '^[0-9]{10,11}$')
);

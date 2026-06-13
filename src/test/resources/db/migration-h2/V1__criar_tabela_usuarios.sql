CREATE SCHEMA IF NOT EXISTS usuarios;

CREATE TABLE IF NOT EXISTS usuarios.tb_usuario
(
    id                uuid PRIMARY KEY,
    nome              VARCHAR(50)   NOT NULL,
    sobrenome         VARCHAR(50)   NOT NULL,
    username          VARCHAR(50)   NOT NULL UNIQUE,
    foto_perfil       VARCHAR(200)  DEFAULT 'https://bucket.oleiloeiroonline.top/profile-images/default-pfp.jpg',
    email             VARCHAR(255)  NOT NULL UNIQUE,
    cpf               VARCHAR(11)   NOT NULL UNIQUE,
    data_nascimento   DATE          NOT NULL,
    telefone          VARCHAR(20)   NOT NULL UNIQUE,
    status            VARCHAR(50)   NOT NULL DEFAULT 'ATIVO',
    data_ultimo_login TIMESTAMP,
    data_criacao      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP(),
    data_atualizacao  TIMESTAMP
);

ALTER TABLE usuarios.tb_usuario ADD CONSTRAINT chk_status CHECK (status IN ('ATIVO', 'SUSPENSO', 'BANIDO', 'INATIVO'));
ALTER TABLE usuarios.tb_usuario ADD CONSTRAINT chk_idade_minima CHECK (data_nascimento <= (CURRENT_DATE - INTERVAL '18' YEAR));
ALTER TABLE usuarios.tb_usuario ADD CONSTRAINT chk_nome_length CHECK (LENGTH(nome) BETWEEN 2 AND 50);
ALTER TABLE usuarios.tb_usuario ADD CONSTRAINT chk_sobrenome_length CHECK (LENGTH(sobrenome) BETWEEN 2 AND 50);
ALTER TABLE usuarios.tb_usuario ADD CONSTRAINT chk_username_length CHECK (LENGTH(username) BETWEEN 4 AND 50);
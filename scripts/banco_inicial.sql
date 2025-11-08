-- =======================================================
-- Script de Criação do Banco de Dados - Versão para MySQL
-- =======================================================

USE banco_atm;

-- Tabela USUARIO: armazena clientes e gerentes
CREATE TABLE USUARIO (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    login VARCHAR(50) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('CLIENTE', 'GERENTE')),
    data_nascimento DATE,
    email VARCHAR(100),
    telefone VARCHAR(20),
    cep VARCHAR(8),
    endereco VARCHAR(255),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    renda DECIMAL(15, 2),
    ocupacao VARCHAR(50),
    cargo VARCHAR(50),
    token_lembrar_me VARCHAR(255) DEFAULT NULL
);

-- Tabela CONTA: contas bancárias associadas a usuários
CREATE TABLE CONTA (
    id INT PRIMARY KEY AUTO_INCREMENT,
    agencia VARCHAR(10) NOT NULL,
    numero VARCHAR(20) NOT NULL UNIQUE,
    saldo DECIMAL(15, 2) NOT NULL DEFAULT 0.0,
    id_usuario INT NOT NULL,
    CONSTRAINT fk_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIO(id)
);

-- Tabela TRANSACAO: histórico de movimentações
CREATE TABLE TRANSACAO (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    data_transacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_conta INT NOT NULL,
    CONSTRAINT fk_conta FOREIGN KEY (id_conta) REFERENCES CONTA(id)
);

-- Tabela INVESTIMENTO: registros de aplicações financeiras
CREATE TABLE INVESTIMENTO (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tipo_investimento VARCHAR(50) NOT NULL,
    valor_aplicado DECIMAL(15, 2) NOT NULL,
    data_aplicacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_conta INT NOT NULL,
    CONSTRAINT fk_investimento_conta FOREIGN KEY (id_conta) REFERENCES CONTA(id)
);

-- Índice para a coluna CPF na tabela USUARIO, para otimizar buscas
CREATE INDEX idx_usuario_cpf ON USUARIO (cpf);

-- Inserção de um gerente inicial (CPF: 00000000000 - SENHA: admin)
INSERT INTO USUARIO (nome, cpf, login, senha, tipo, cargo) 
VALUES ('Gerente Chefe', '00000000000', 'gerente', 'admin', 'GERENTE', 'Gerente Geral');
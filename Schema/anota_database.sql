DROP DATABASE anota_database;

CREATE DATABASE anota_database DEFAULT CHARACTER SET utf8mb4;
USE anota_database;

create user 'lovelace'@'localhost' identified by 'pass123';
grant select, insert, delete, update on anota_database.* to lovelace@'localhost';

CREATE TABLE usuario ( -- usuário generalizado
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    foto VARCHAR(300) DEFAULT NULL,
    
    PRIMARY KEY (email)
);

CREATE TABLE curso (
    nome_curso VARCHAR(100) NOT NULL,
    sigla_curso VARCHAR(20) NOT NULL,

    PRIMARY KEY (nome_curso),
    UNIQUE (sigla_curso)
);

CREATE TABLE turma (
    id_turma bigint NOT NULL AUTO_INCREMENT,
    nome_curso VARCHAR(100) NOT NULL,
    semestre VARCHAR(20) NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    vigencia VARCHAR(20) NOT NULL,
    nome_turma VARCHAR(20) NOT NULL DEFAULT 'Regular',
    PRIMARY KEY (id_turma),
    UNIQUE KEY uq_turma_caracteristicas
        (nome_curso, semestre, periodo, vigencia, nome_turma),
    UNIQUE KEY uq_turma_id_curso (id_turma, nome_curso),
    FOREIGN KEY (nome_curso) REFERENCES curso (nome_curso)
);

CREATE TABLE aluno ( -- usuário
    email VARCHAR(150) NOT NULL,
    RA VARCHAR(20) NOT NULL,
    data_nascimento DATE NOT NULL,
    nome_curso VARCHAR(100) NOT NULL,  -- (1,1) aluno-curso

    PRIMARY KEY (email),
    UNIQUE (RA),
    UNIQUE (RA, nome_curso),                   

    FOREIGN KEY (email) 
        REFERENCES usuario (email),
    FOREIGN KEY (nome_curso) 
        REFERENCES curso (nome_curso)
);

CREATE TABLE professor ( -- usuário
    email VARCHAR(150) NOT NULL,
    matricula VARCHAR(20) NOT NULL,

    PRIMARY KEY (email),
    UNIQUE (matricula),

    FOREIGN KEY (email)
        REFERENCES usuario (email)
);

CREATE TABLE disciplina (
    nome_disciplina VARCHAR(100) NOT NULL,
    sigla_disciplina VARCHAR(20),

    PRIMARY KEY (nome_disciplina)
);

CREATE TABLE ministra ( -- relacionamento / ação
    matricula VARCHAR(20) NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,

    PRIMARY KEY (matricula, nome_disciplina),

    FOREIGN KEY (matricula)
        REFERENCES professor (matricula),

    FOREIGN KEY (nome_disciplina)
        REFERENCES disciplina (nome_disciplina)
);

CREATE TABLE pertence ( -- aluno-turma (N:N, histórico por semestre)
    RA VARCHAR(20) NOT NULL,
    id_turma bigint NOT NULL,
    nome_curso VARCHAR(100) NOT NULL,

    PRIMARY KEY (RA, id_turma),

    FOREIGN KEY (RA, nome_curso)       
        REFERENCES aluno (RA, nome_curso),
    FOREIGN KEY (id_turma, nome_curso) 
        REFERENCES turma (id_turma, nome_curso)
);

CREATE TABLE turma_disciplina ( -- relacionamento N:N
    id_turma BIGINT NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,

    PRIMARY KEY (id_turma, nome_disciplina),

    FOREIGN KEY (id_turma)
        REFERENCES turma (id_turma),
    FOREIGN KEY (nome_disciplina)
        REFERENCES disciplina (nome_disciplina)
);

CREATE TABLE curso_disciplina ( -- relacionamento N:N
    nome_curso VARCHAR(100) NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,

    PRIMARY KEY (nome_curso, nome_disciplina),

    FOREIGN KEY (nome_curso)
        REFERENCES curso (nome_curso),
    FOREIGN KEY (nome_disciplina)
        REFERENCES disciplina (nome_disciplina)
);

CREATE TABLE `questao` ( -- QUESTÃO (cria/edita: professor 1:N | contém: disciplina 1:N)
    `questao_id`      bigint auto_increment,
    `enunciado`       varchar(100) NOT NULL,
    `tema`            varchar(100) DEFAULT NULL,
    `tipo`            enum('alt','rela','diss') NOT NULL,
    `dificuldade`     enum('Fácil','Média','Difícil') NOT NULL,
    `data_criacao`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `data_edicao`     datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `email_professor` varchar(150) NOT NULL,
    `nome_disciplina` varchar(100) NOT NULL,

    PRIMARY KEY (`questao_id`),
    UNIQUE (`enunciado`,`tipo`),

    CONSTRAINT `fk_questao_professor` FOREIGN KEY (`email_professor`)
        REFERENCES `professor` (`email`),

    CONSTRAINT `fk_questao_disciplina` FOREIGN KEY (`nome_disciplina`)
        REFERENCES `disciplina` (`nome_disciplina`)
);

CREATE TABLE `resposta` ( -- RESPOSTA (entidade fraca de questão: alternativas, itens e resposta esperada)
    `resposta_id` bigint auto_increment,
    `enunciado`   varchar(100) NOT NULL,
    `tipo`        enum('alt','rela','diss') NOT NULL,
    `texto`       varchar(100) NOT NULL, -- DISSERTATIVA: resp. esperada / ALTERNATIVA: opções / RELACIONAMENTO: conceito
    `correta`     boolean DEFAULT NULL,  -- ALTERNATIVA: verdadeiro ou falso / DISSERTATIVA e RELACIONAMENTO: null
    `conceito`    varchar(100) DEFAULT NULL, -- RELACIONAMENTO: alternativa / DISSERTATIVA e ALTERNATIVA: null

    PRIMARY KEY (`resposta_id`),

    CONSTRAINT `fk_resposta_questao` FOREIGN KEY (`enunciado`,`tipo`)
        REFERENCES `questao` (`enunciado`,`tipo`) ON DELETE CASCADE
);

CREATE TABLE `prova` ( -- PROVA (monta: professor 1:N | avalia: disciplina 1:N)
    `prova_id`        bigint auto_increment,
    `nome_prova`      varchar(100) NOT NULL,
    `dificuldade`     enum('facil','media','dificil') DEFAULT NULL,
    `valor`           decimal(5,2) NOT NULL,
    `email_professor` varchar(150) NOT NULL,
    `nome_disciplina` varchar(100) NOT NULL,

    PRIMARY KEY (`prova_id`),
    UNIQUE (`nome_prova`, `email_professor`, `nome_disciplina`),
    UNIQUE KEY `uq_prova_id_disciplina` (`prova_id`, `email_professor`, `nome_disciplina`),

    CONSTRAINT `fk_prova_professor` FOREIGN KEY (`email_professor`)
        REFERENCES `professor` (`email`),

    CONSTRAINT `fk_prova_disciplina` FOREIGN KEY (`nome_disciplina`)
        REFERENCES `disciplina` (`nome_disciplina`)
);

CREATE TABLE `prova_questao` (
    `prova_id`        bigint NOT NULL,
    `questao_id`      bigint NOT NULL,
    `email_professor` varchar(150) NOT NULL,
    `nome_disciplina` varchar(100) NOT NULL,
    `peso`            decimal(5,2) NOT NULL,

    PRIMARY KEY (`prova_id`,`email_professor`,`nome_disciplina`,`questao_id`),

    CONSTRAINT `fk_pq_prova` FOREIGN KEY (`prova_id`,`email_professor`,`nome_disciplina`)
        REFERENCES `prova` (`prova_id`,`email_professor`,`nome_disciplina`) ON DELETE CASCADE,

    CONSTRAINT `fk_pq_questao` FOREIGN KEY (`questao_id`)
        REFERENCES `questao` (`questao_id`)
);

CREATE TABLE prova_turma ( -- aplicação da prova a uma turma (qual prova, qual turma, quando)
    id_aplicacao    BIGINT       NOT NULL AUTO_INCREMENT,
    prova_id        BIGINT       NOT NULL,
    id_turma        BIGINT       NOT NULL,
    email_professor VARCHAR(150) NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,
    data            DATE         NOT NULL,
    horario         TIME         NOT NULL,

    PRIMARY KEY (id_aplicacao),
    UNIQUE (id_turma, prova_id, data, horario),
    UNIQUE (id_aplicacao, id_turma),

    CONSTRAINT fk_pt_prova FOREIGN KEY (prova_id, email_professor, nome_disciplina)
        REFERENCES prova (prova_id, email_professor, nome_disciplina),

    -- garante que a disciplina da prova faz parte da turma
    CONSTRAINT fk_pt_turma_disciplina FOREIGN KEY (id_turma, nome_disciplina)
        REFERENCES turma_disciplina (id_turma, nome_disciplina)
);

CREATE TABLE prova_aluno ( -- alunos convocados para a aplicação (turma toda ou só alguns, ex.: recuperação)
    id_aplicacao BIGINT      NOT NULL,
    id_turma     BIGINT      NOT NULL,
    RA           VARCHAR(20) NOT NULL,

    PRIMARY KEY (id_aplicacao, RA),

    CONSTRAINT fk_pa_aplicacao FOREIGN KEY (id_aplicacao, id_turma)
        REFERENCES prova_turma (id_aplicacao, id_turma),

    -- garante que o aluno pertence à turma da aplicação
    CONSTRAINT fk_pa_pertence FOREIGN KEY (RA, id_turma)
        REFERENCES pertence (RA, id_turma)
);

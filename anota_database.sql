CREATE DATABASE anota_database DEFAULT CHARACTER SET utf8mb4;
USE anota_database;

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
    id_turma INT NOT NULL AUTO_INCREMENT,
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
    id_turma INT NOT NULL,
    nome_curso VARCHAR(100) NOT NULL,

    PRIMARY KEY (RA, id_turma),

    FOREIGN KEY (RA, nome_curso)       
		REFERENCES aluno (RA, nome_curso),
    FOREIGN KEY (id_turma, nome_curso) 
		REFERENCES turma (id_turma, nome_curso)
);

CREATE TABLE turma_disciplina ( -- relacionamento N:N
    id_turma INT NOT NULL,
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
  `enunciado`       varchar(100) NOT NULL,
  `tema`            varchar(100) DEFAULT NULL,
  `tipo`            enum('alt','rela','diss') NOT NULL,
  `dificuldade`     enum('Fácil','Média','Difícil') NOT NULL,
  `data_criacao`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_edicao`     datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `email_professor` varchar(150) NOT NULL,
  `nome_disciplina` varchar(100) NOT NULL,
  
  PRIMARY KEY (`enunciado`,`tipo`),
  
  CONSTRAINT `fk_questao_professor`  FOREIGN KEY (`email_professor`)
    REFERENCES `professor` (`email`),
    
  CONSTRAINT `fk_questao_disciplina` FOREIGN KEY (`nome_disciplina`)
    REFERENCES `disciplina` (`nome_disciplina`)
);

CREATE TABLE `resposta` ( -- RESPOSTA (entidade fraca de questão: alternativas, itens e resposta esperada)
  `enunciado`   varchar(100) NOT NULL,
  `tipo`        enum('alt','rela','diss') NOT NULL,
  `texto`       varchar(100) NOT NULL, -- DISSERTATIVA: resp. esperada / ALTERNATIVA: opções / RELACIONAMENTO: conceito
  `correta`     boolean DEFAULT NULL, -- ALTERNATIVA: verdadeiro ou falso / DISSERTATIVA e RELACIONAMENTO: null
  `conceito`    varchar(100) DEFAULT NULL,   -- RELACIONAMENTO: alternativa / DISSERTATIVA e ALTERNATIVA: null
  PRIMARY KEY (`texto`,`enunciado`,`tipo`),
  
  CONSTRAINT `fk_resposta_questao` FOREIGN KEY (`enunciado`,`tipo`)
    REFERENCES `questao` (`enunciado`,`tipo`) ON DELETE CASCADE
);

CREATE TABLE `prova` ( -- PROVA (monta: professor 1:N | avalia: disciplina 1:N)
  `nome_prova`      varchar(100) NOT NULL,
  `dificuldade`     enum('facil','media','dificil') DEFAULT NULL,
  `valor`           decimal(5,2) NOT NULL,
  `email_professor` varchar(150) NOT NULL,
  `nome_disciplina` varchar(100) NOT NULL,
  
  PRIMARY KEY (`nome_prova`,`email_professor`, `nome_disciplina`),
  CONSTRAINT `fk_prova_professor`  FOREIGN KEY (`email_professor`)
    REFERENCES `professor` (`email`),
    
  CONSTRAINT `fk_prova_disciplina` FOREIGN KEY (`nome_disciplina`)
    REFERENCES `disciplina` (`nome_disciplina`)
);

CREATE TABLE `prova_questao` (
  `nome_prova`      varchar(100) NOT NULL,
  `email_professor` varchar(150) NOT NULL,
  `nome_disciplina` varchar(100) NOT NULL,
  `enunciado`       varchar(100) NOT NULL,
  `tipo`            enum('alt','rela','diss') NOT NULL,
  `peso`            decimal(5,2) NOT NULL,
  PRIMARY KEY (`nome_prova`,`email_professor`,`nome_disciplina`,`enunciado`,`tipo`),
  CONSTRAINT `fk_pq_prova` FOREIGN KEY (`nome_prova`,`email_professor`,`nome_disciplina`)
    REFERENCES `prova` (`nome_prova`,`email_professor`,`nome_disciplina`) ON DELETE CASCADE,
  CONSTRAINT `fk_pq_questao` FOREIGN KEY (`enunciado`,`tipo`)
    REFERENCES `questao` (`enunciado`,`tipo`)
);

CREATE TABLE `prova_turma` (
  `id_turma`        int NOT NULL,
  `nome_prova`      varchar(100) NOT NULL,
  `email_professor` varchar(150) NOT NULL,
  `nome_disciplina` varchar(100) NOT NULL,
  `data`            date NOT NULL,
  `horario`         time NOT NULL,
  PRIMARY KEY (`id_turma`,`nome_prova`,`email_professor`,`nome_disciplina`,`data`,`horario`),
  CONSTRAINT `fk_pt_prova` FOREIGN KEY (`nome_prova`,`email_professor`,`nome_disciplina`)
    REFERENCES `prova` (`nome_prova`,`email_professor`,`nome_disciplina`),
  CONSTRAINT `fk_pt_turma` FOREIGN KEY (`id_turma`) REFERENCES `turma` (`id_turma`)
);
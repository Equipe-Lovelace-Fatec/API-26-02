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

CREATE TABLE `resposta` (
    `resposta_id` bigint auto_increment,
    `questao_id`  bigint NOT NULL,
    `texto`       varchar(100) NOT NULL, -- DISSERTATIVA: resp. esperada / ALTERNATIVA: opções / RELACIONAMENTO: item
    `correta`     boolean DEFAULT NULL,  -- ALTERNATIVA: verdadeiro ou falso / DISSERTATIVA e RELACIONAMENTO: null
    `conceito`    varchar(100) DEFAULT NULL, -- RELACIONAMENTO: par correto / DISSERTATIVA e ALTERNATIVA: null

    PRIMARY KEY (`resposta_id`),
    UNIQUE KEY `uq_resposta_questao` (`resposta_id`, `questao_id`),

    CONSTRAINT `fk_resposta_questao` FOREIGN KEY (`questao_id`)
        REFERENCES `questao` (`questao_id`) ON DELETE CASCADE
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
    CONSTRAINT `ck_prova_valor` CHECK (`valor` >= 0 and `valor` <= 10),   -- Chack relaxado: rascunho

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
    UNIQUE KEY `uq_pq_prova_questao` (`prova_id`, `questao_id`),   -- novo
    CONSTRAINT `ck_pq_peso` CHECK (`peso` >= 0 and `peso` <= 10),   -- NOVO: check relaxado: rascunho

    CONSTRAINT `fk_pq_prova` FOREIGN KEY (`prova_id`,`email_professor`,`nome_disciplina`)
        REFERENCES `prova` (`prova_id`,`email_professor`,`nome_disciplina`) ON DELETE CASCADE,

    CONSTRAINT `fk_pq_questao` FOREIGN KEY (`questao_id`)
        REFERENCES `questao` (`questao_id`)
);


CREATE TABLE prova_turma (
    id_aplicacao    BIGINT       NOT NULL AUTO_INCREMENT,
    prova_id        BIGINT       NOT NULL,
    id_turma        BIGINT       NOT NULL,
    email_professor VARCHAR(150) NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,
    data            DATE         NOT NULL,
    horario         TIME         NOT NULL, 
    horario_fim     TIME         NOT NULL, 
    recuperacao     BOOLEAN      NOT NULL DEFAULT FALSE,  -- NOVO: TRUE = aplicação de recuperação (não entra como item da média)

    PRIMARY KEY (id_aplicacao),
    UNIQUE (id_turma, prova_id, data, horario),
    UNIQUE (id_aplicacao, id_turma),
    UNIQUE KEY uq_pt_aplicacao_prova (id_aplicacao, prova_id),
    UNIQUE KEY uq_pt_aplicacao_turma_disc (id_aplicacao, id_turma, nome_disciplina), -- NOVO
    UNIQUE KEY uq_pt_aplicacao_rec (id_aplicacao, id_turma, nome_disciplina, recuperacao), -- NOVO
    CONSTRAINT ck_pt_janela CHECK (horario_fim > horario),

    CONSTRAINT fk_pt_prova FOREIGN KEY (prova_id, email_professor, nome_disciplina)
        REFERENCES prova (prova_id, email_professor, nome_disciplina),

    CONSTRAINT fk_pt_turma_disciplina FOREIGN KEY (id_turma, nome_disciplina)
        REFERENCES turma_disciplina (id_turma, nome_disciplina)
);



CREATE TABLE prova_aluno ( -- alunos convocados para a aplicação (turma toda ou só alguns, ex.: recuperação)

    id_aplicacao BIGINT       NOT NULL,
    id_turma     BIGINT       NOT NULL,
    RA           VARCHAR(20)  NOT NULL,
    nota         DECIMAL(5,2) NULL,  -- vazio até a prova ser corrigida
    corrigida_em DATETIME     NULL,  -- NOVO: preenchido quando o professor fecha a correção; só então a nota entra na média

    PRIMARY KEY (id_aplicacao, RA),
    CONSTRAINT ck_pa_nota CHECK (nota IS NULL OR (nota >= 0 AND nota <= 10)),   -- NOVO

    CONSTRAINT fk_pa_aplicacao FOREIGN KEY (id_aplicacao, id_turma)
        REFERENCES prova_turma (id_aplicacao, id_turma),

    -- garante que o aluno pertence à turma da aplicação
    CONSTRAINT fk_pa_pertence FOREIGN KEY (RA, id_turma)
        REFERENCES pertence (RA, id_turma)
);

CREATE TABLE resposta_aluno (
    id_resposta_aluno BIGINT NOT NULL AUTO_INCREMENT,
    id_aplicacao      BIGINT NOT NULL,
    prova_id          BIGINT NOT NULL,
    RA                VARCHAR(20) NOT NULL,
    questao_id        BIGINT NOT NULL,
    resposta_id       BIGINT NULL,            -- alt: alternativa marcada | diss e rela: NULL
    texto_resposta    TEXT NULL,              -- diss: texto digitado | rela: pares (ex.: JSON)
    correta           BOOLEAN NULL,           -- NULL = ainda não corrigida
    nota              DECIMAL(5,2) NULL,      -- nota da questão; NULL = ainda não corrigida (em branco corrigida = 0)
    comentario_professor TEXT NULL,           -- NOVO: comentário do professor sobre a resposta (opcional)

    PRIMARY KEY (id_resposta_aluno),

    CONSTRAINT uq_resposta_aluno UNIQUE (id_aplicacao, RA, questao_id),
    CONSTRAINT ck_ra_nota CHECK (nota IS NULL OR (nota >= 0 AND nota <= 10)),

    CONSTRAINT fk_ra_prova_aluno
        FOREIGN KEY (id_aplicacao, RA)
        REFERENCES prova_aluno (id_aplicacao, RA),

    CONSTRAINT fk_ra_aplicacao_prova
        FOREIGN KEY (id_aplicacao, prova_id)
        REFERENCES prova_turma (id_aplicacao, prova_id),

    CONSTRAINT fk_ra_prova_questao
        FOREIGN KEY (prova_id, questao_id)
        REFERENCES prova_questao (prova_id, questao_id),

    CONSTRAINT fk_ra_resposta
        FOREIGN KEY (resposta_id, questao_id)
        REFERENCES resposta (resposta_id, questao_id)
);

-- =====================================================================
-- NOVO: COMPOSIÇÃO DA MÉDIA - pesos por prova
-- Regra: o professor define, por turma + disciplina, quais provas aplicadas formam
-- a média e quanto cada uma pesa (percentual). A nota vem de prova_aluno.nota.
-- Obs.: peso_percentual (média) NÃO é o mesmo que prova_questao.peso (valor da questão).
-- A soma dos pesos = 100 é validada pelo app (CHECK não enxerga outras linhas);
-- a view v_auditoria_pesos mostra o que estiver fora.
-- =====================================================================
CREATE TABLE item_avaliativo (
    item_id         BIGINT        NOT NULL AUTO_INCREMENT,
    id_turma        BIGINT        NOT NULL,
    nome_disciplina VARCHAR(100)  NOT NULL,
    nome            VARCHAR(100)  NOT NULL,                 -- 'Prova 1', 'Prova 2'
    peso_percentual DECIMAL(5,2)  NOT NULL,                 -- % na média (0 a 100)
    id_aplicacao    BIGINT        NOT NULL,                 -- aplicação (prova_turma) que vale na média
    recuperacao     BOOLEAN       NOT NULL DEFAULT FALSE,   -- sempre FALSE: aplicação de recuperação não vira item

    PRIMARY KEY (item_id),
    UNIQUE (id_turma, nome_disciplina, nome),
    UNIQUE (id_aplicacao),                                  -- uma aplicação só entra uma vez na composição
    UNIQUE KEY uq_ia_item_turma_disc (item_id, id_turma, nome_disciplina),

    CONSTRAINT ck_ia_peso CHECK (peso_percentual >= 0 AND peso_percentual <= 100),
    CONSTRAINT ck_ia_nao_rec CHECK (recuperacao = FALSE),

    CONSTRAINT fk_ia_turma_disciplina FOREIGN KEY (id_turma, nome_disciplina)
        REFERENCES turma_disciplina (id_turma, nome_disciplina),

    -- a aplicação precisa ser da MESMA turma e disciplina do item e NÃO pode ser recuperação
    CONSTRAINT fk_ia_aplicacao FOREIGN KEY (id_aplicacao, id_turma, nome_disciplina, recuperacao)
        REFERENCES prova_turma (id_aplicacao, id_turma, nome_disciplina, recuperacao)
);

-- NOVO: RECUPERAÇÃO - o professor escolhe, por aluno, qual item a nota da recuperação substitui
--   * a recuperação é uma aplicação (prova_turma.recuperacao = TRUE) com alunos convocados em prova_aluno
--   * 1 substituição por aluno e por aplicação; um item só pode ser substituído 1 vez por aluno
--   * o item precisa ser da mesma turma e disciplina da recuperação
--   * enquanto a recuperação não for corrigida (corrigida_em vazio), vale a nota original
CREATE TABLE recuperacao_substituicao (
    id_aplicacao    BIGINT       NOT NULL,   -- aplicação da recuperação
    id_turma        BIGINT       NOT NULL,
    nome_disciplina VARCHAR(100) NOT NULL,
    recuperacao     BOOLEAN      NOT NULL DEFAULT TRUE,   -- sempre TRUE: só aceita aplicação marcada como recuperação
    RA              VARCHAR(20)  NOT NULL,
    item_id         BIGINT       NOT NULL,   -- item (prova) cuja nota é substituída

    PRIMARY KEY (id_aplicacao, RA),
    UNIQUE (item_id, RA),
    CONSTRAINT ck_rs_rec CHECK (recuperacao = TRUE),

    CONSTRAINT fk_rs_aplicacao FOREIGN KEY (id_aplicacao, id_turma, nome_disciplina, recuperacao)
        REFERENCES prova_turma (id_aplicacao, id_turma, nome_disciplina, recuperacao),

    -- o aluno precisa ter sido convocado para a recuperação
    CONSTRAINT fk_rs_convocado FOREIGN KEY (id_aplicacao, RA)
        REFERENCES prova_aluno (id_aplicacao, RA),

    CONSTRAINT fk_rs_item FOREIGN KEY (item_id, id_turma, nome_disciplina)
        REFERENCES item_avaliativo (item_id, id_turma, nome_disciplina)
);

-- 1 linha por (item x aluno da turma): nota usada na média (NULL = ainda sem nota)
-- Só conta prova já corrigida (corrigida_em preenchido).
-- Se o aluno tem recuperação corrigida para o item, vale a nota (e o valor) da recuperação.
CREATE VIEW v_item_nota AS
SELECT i.item_id, i.id_turma, i.nome_disciplina, i.nome AS item,
       i.peso_percentual,
       CASE WHEN par.nota IS NOT NULL THEN pr.valor ELSE p.valor END AS valor_referencia,
       pe.RA,
       CASE WHEN par.nota IS NOT NULL THEN par.nota ELSE pa.nota END AS nota,
       CASE WHEN par.nota IS NOT NULL THEN 1 ELSE 0 END              AS substituida
FROM item_avaliativo i
JOIN pertence pe                 ON pe.id_turma = i.id_turma
JOIN prova_turma pt              ON pt.id_aplicacao = i.id_aplicacao
JOIN prova p                     ON p.prova_id = pt.prova_id
LEFT JOIN prova_aluno pa         ON pa.id_aplicacao = i.id_aplicacao AND pa.RA = pe.RA
                                AND pa.corrigida_em IS NOT NULL       -- só nota já corrigida entra
LEFT JOIN recuperacao_substituicao rs ON rs.item_id = i.item_id AND rs.RA = pe.RA
LEFT JOIN prova_turma ptr        ON ptr.id_aplicacao = rs.id_aplicacao
LEFT JOIN prova pr               ON pr.prova_id = ptr.prova_id
LEFT JOIN prova_aluno par        ON par.id_aplicacao = rs.id_aplicacao AND par.RA = rs.RA
                                AND par.corrigida_em IS NOT NULL;     -- recuperação já corrigida

-- MÉDIA por aluno / turma / disciplina (0 a 10)
--   media = 10 x soma( nota/valor_referencia x peso/100 )   [sem nota conta 0, aparece em itens_pendentes]
CREATE VIEW v_media_aluno AS
SELECT id_turma, nome_disciplina, RA,
       ROUND(10 * SUM(COALESCE(nota * 1.0 / valor_referencia, 0) * peso_percentual / 100.0), 2) AS media,
       SUM(CASE WHEN nota IS NULL THEN 1 ELSE 0 END) AS itens_pendentes,
       COUNT(*)                                      AS itens_total,
       SUM(substituida)                              AS itens_substituidos
FROM v_item_nota
GROUP BY id_turma, nome_disciplina, RA;

-- NOVO: tela da recuperação - por aluno convocado, o que foi escolhido e em que pé está
CREATE VIEW v_recuperacao_status AS
SELECT pt.id_turma, pt.nome_disciplina, pa.id_aplicacao, pa.RA,
       i.nome AS item_substituido, pa.nota AS nota_recuperacao, pa.corrigida_em,
       CASE WHEN rs.item_id IS NULL        THEN 'ITEM NÃO ESCOLHIDO'
            WHEN pa.corrigida_em IS NULL   THEN 'AGUARDANDO CORREÇÃO'
            ELSE 'SUBSTITUIÇÃO APLICADA' END AS situacao
FROM prova_aluno pa
JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.recuperacao = TRUE
LEFT JOIN recuperacao_substituicao rs ON rs.id_aplicacao = pa.id_aplicacao AND rs.RA = pa.RA
LEFT JOIN item_avaliativo i ON i.item_id = rs.item_id;

-- AUDITORIA 1: soma dos pesos por turma/disciplina (deve ser 100)
CREATE VIEW v_auditoria_pesos AS
SELECT id_turma, nome_disciplina,
       SUM(peso_percentual) AS soma_pesos,
       CASE WHEN SUM(peso_percentual) = 100 THEN 'OK' ELSE 'SOMA DIFERENTE DE 100' END AS situacao
FROM item_avaliativo
GROUP BY id_turma, nome_disciplina;

-- AUDITORIA 2: notas que o banco não consegue barrar sozinho (comparam tabelas diferentes)
CREATE VIEW v_auditoria_notas AS
SELECT i.id_turma, i.nome_disciplina, pa.RA, i.nome AS item, 'nota de prova acima do valor da prova' AS problema
FROM item_avaliativo i
JOIN prova_turma pt ON pt.id_aplicacao = i.id_aplicacao
JOIN prova p        ON p.prova_id = pt.prova_id
JOIN prova_aluno pa ON pa.id_aplicacao = i.id_aplicacao
WHERE pa.nota > p.valor
UNION ALL
SELECT pt.id_turma, pt.nome_disciplina, r.RA, CAST(r.questao_id AS CHAR), 'nota da questão acima do valor da questão (prova_questao.peso)'
FROM resposta_aluno r
JOIN prova_turma pt   ON pt.id_aplicacao = r.id_aplicacao
JOIN prova_questao pq ON pq.prova_id = r.prova_id AND pq.questao_id = r.questao_id
WHERE r.nota > pq.peso;

-- NOVO: situação da entrega/correção de cada aluno convocado
--   SEM ENTREGA          = nenhuma linha em resposta_aluno
--   ENTREGA INCOMPLETA   = tem linhas, mas menos que as questões da prova (app não gravou todas)
--   AGUARDANDO CORREÇÃO  = todas as linhas existem, mas falta nota em alguma
--   PRONTA PARA FECHAR   = todas as questões com nota, correção ainda não fechada (corrigida_em vazio)
--   CORRIGIDA            = correção fechada (prova_aluno.corrigida_em preenchido)
CREATE VIEW v_correcao_status AS
SELECT t.*,
       CASE WHEN t.corrigida_em IS NOT NULL                 THEN 'CORRIGIDA'
            WHEN t.linhas_gravadas = 0                      THEN 'SEM ENTREGA'
            WHEN t.linhas_gravadas < t.questoes_prova       THEN 'ENTREGA INCOMPLETA'
            WHEN t.questoes_corrigidas < t.questoes_prova   THEN 'AGUARDANDO CORREÇÃO'
            ELSE 'PRONTA PARA FECHAR' END AS situacao
FROM (
    SELECT pa.id_aplicacao, pa.id_turma, pa.RA, pa.nota AS nota_prova, pa.corrigida_em,
           (SELECT COUNT(*) FROM prova_turma pt JOIN prova_questao pq ON pq.prova_id = pt.prova_id
             WHERE pt.id_aplicacao = pa.id_aplicacao) AS questoes_prova,
           COUNT(r.id_resposta_aluno) AS linhas_gravadas,
           COUNT(r.nota)              AS questoes_corrigidas
    FROM prova_aluno pa
    LEFT JOIN resposta_aluno r ON r.id_aplicacao = pa.id_aplicacao AND r.RA = pa.RA
    GROUP BY pa.id_aplicacao, pa.id_turma, pa.RA, pa.nota, pa.corrigida_em
) t;

-- =====================================================================
-- NOVO: procedimentos de entrega e fechamento de correção
-- Rodar DEPOIS do schema, no MySQL Workbench (usa DELIMITER).
-- =====================================================================
DELIMITER //

-- Garante 1 linha em resposta_aluno por questão da prova, mesmo em branco.
-- O app chama na ENTREGA do aluno (depois de gravar as respostas marcadas).
-- Pode ser chamada várias vezes: só cria o que falta.
CREATE PROCEDURE sp_preencher_respostas_em_branco (IN p_id_aplicacao BIGINT, IN p_RA VARCHAR(20))
BEGIN
    INSERT INTO resposta_aluno (id_aplicacao, prova_id, RA, questao_id)
    SELECT pa.id_aplicacao, pt.prova_id, pa.RA, pq.questao_id
    FROM prova_aluno pa
    JOIN prova_turma pt   ON pt.id_aplicacao = pa.id_aplicacao
    JOIN prova_questao pq ON pq.prova_id = pt.prova_id
    LEFT JOIN resposta_aluno r ON r.id_aplicacao = pa.id_aplicacao
                              AND r.RA = pa.RA AND r.questao_id = pq.questao_id
    WHERE pa.id_aplicacao = p_id_aplicacao
      AND pa.RA = p_RA
      AND r.id_resposta_aluno IS NULL;
END //

-- Fecha a correção de um aluno: nota da prova = soma das notas das questões e
-- corrigida_em = agora. Só fecha se TODAS as questões da prova tiverem nota
-- (isso também exige que as linhas em branco existam).
CREATE PROCEDURE sp_fechar_correcao (IN p_id_aplicacao BIGINT, IN p_RA VARCHAR(20))
BEGIN
    DECLARE v_questoes   INT;
    DECLARE v_corrigidas INT;
    DECLARE v_soma       DECIMAL(7,2);

    SELECT COUNT(*) INTO v_questoes
    FROM prova_turma pt JOIN prova_questao pq ON pq.prova_id = pt.prova_id
    WHERE pt.id_aplicacao = p_id_aplicacao;

    SELECT COUNT(nota), COALESCE(SUM(nota), 0) INTO v_corrigidas, v_soma
    FROM resposta_aluno
    WHERE id_aplicacao = p_id_aplicacao AND RA = p_RA;

    IF v_questoes = 0 OR v_corrigidas < v_questoes THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Correção não fechada: há questões sem linha ou sem nota.';
    END IF;

    UPDATE prova_aluno
       SET nota = v_soma, corrigida_em = NOW()
     WHERE id_aplicacao = p_id_aplicacao AND RA = p_RA;
END //

DELIMITER ;

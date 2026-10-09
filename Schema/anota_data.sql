-- ====================================  INSERTS ==========================================
 
-- USUÁRIOS
INSERT INTO usuario (email, senha, foto, nome) VALUES
('allana.natasha@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Allana Natasha'),
('anderson.ilo@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Anderson Ilo'),
('amanda.emiliano@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Amanda Emiliano'),
('larissa.paula@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Larissa de Paula'),
('vitoria.souza@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Vitória Souza'),
('vitoria.vieira@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Vitória Vieira'),
('livia.dias@aluno.fatec.com', 'senha.aluno.1234', NULL, 'Lívia Dias'),
('teresinha.fatima@prof.fatec.com', 'senha.prof.1234', NULL, 'Teresinha de Fátima'),
('emanuel.mineda@prof.fatec.com', 'senha.prof.1234', NULL, 'Emanuel Mineda'),
('giuliano.bertoti@prof.fatec.com', 'senha.prof.1234', NULL, 'Giuliano Bertoti');
 
-- CURSO
INSERT INTO curso (nome_curso, sigla_curso) VALUES ('Banco de Dados', 'BD');
 
-- DISCIPLINAS
INSERT INTO disciplina (nome_disciplina, sigla_disciplina) VALUES
('Inglês I', 'Ing I'),
('Inglês II', 'Ing II'),
('Arquitetura e Organização de Computadores', 'ArqOrg'),
('Arquitetura e Modelagem de Banco de Dados', 'Arq'),
('Engenharia de Software I', 'Eng I');
 
-- TURMAS (id explícito para as FKs abaixo)
INSERT INTO turma (id_turma, nome_curso, semestre, periodo, vigencia, nome_turma) VALUES
(1, 'Banco de Dados', '1º', 'Noturno', '01/2026 a 06/2026', 'Regular'),
(2, 'Banco de Dados', '1º', 'Noturno', '01/2026 a 06/2026', 'Inglês'),
(3, 'Banco de Dados', '2º', 'Noturno', '07/2026 a 12/2026', 'Regular'),
(4, 'Banco de Dados', '2º', 'Noturno', '07/2026 a 12/2026', 'Inglês');
 
-- PROFESSORES
INSERT INTO professor (email, matricula) VALUES
('teresinha.fatima@prof.fatec.com', '86571'),
('emanuel.mineda@prof.fatec.com', '88842'),
('giuliano.bertoti@prof.fatec.com', '11209');
 
-- ALUNOS
INSERT INTO aluno (email, RA, data_nascimento, nome_curso) VALUES
('allana.natasha@aluno.fatec.com', '5615042599', '1999-11-21', 'Banco de Dados'),
('anderson.ilo@aluno.fatec.com', '7279024339', '2000-08-24', 'Banco de Dados'),
('amanda.emiliano@aluno.fatec.com', '8658188467', '2002-04-19', 'Banco de Dados'),
('larissa.paula@aluno.fatec.com', '5699154762', '1999-08-26', 'Banco de Dados'),
('vitoria.souza@aluno.fatec.com', '2727936184', '2000-03-28', 'Banco de Dados'),
('vitoria.vieira@aluno.fatec.com', '7052256714', '2002-12-13', 'Banco de Dados'),
('livia.dias@aluno.fatec.com', '2241869216', '2001-03-04', 'Banco de Dados');
 
-- QUEM MINISTRA O QUÊ
INSERT INTO ministra (matricula, nome_disciplina) VALUES
('86571', 'Inglês I'),
('86571', 'Inglês II'),
('88842', 'Arquitetura e Organização de Computadores'),
('88842', 'Arquitetura e Modelagem de Banco de Dados'),
('11209', 'Engenharia de Software I');
 
-- DISCIPLINAS DO CURSO
INSERT INTO curso_disciplina (nome_curso, nome_disciplina) VALUES
('Banco de Dados', 'Inglês I'),
('Banco de Dados', 'Inglês II'),
('Banco de Dados', 'Arquitetura e Organização de Computadores'),
('Banco de Dados', 'Arquitetura e Modelagem de Banco de Dados'),
('Banco de Dados', 'Engenharia de Software I');
 
-- DISCIPLINAS DE CADA TURMA
INSERT INTO turma_disciplina (id_turma, nome_disciplina) VALUES
(1, 'Arquitetura e Organização de Computadores'),
(2, 'Inglês I'),
(3, 'Arquitetura e Modelagem de Banco de Dados'),
(3, 'Engenharia de Software I'),
(4, 'Inglês II');
 
-- ALUNOS EM TURMAS (pertence)
-- Todos: turmas Regular (1º e 2º sem.). Só Amanda e Vitória Vieira: também as turmas de Inglês.
INSERT INTO pertence (RA, id_turma, nome_curso) VALUES
('5615042599', 1, 'Banco de Dados'),
('5615042599', 3, 'Banco de Dados'),
('7279024339', 1, 'Banco de Dados'),
('7279024339', 3, 'Banco de Dados'),
('8658188467', 1, 'Banco de Dados'),
('8658188467', 3, 'Banco de Dados'),
('8658188467', 2, 'Banco de Dados'),
('8658188467', 4, 'Banco de Dados'),
('5699154762', 1, 'Banco de Dados'),
('5699154762', 3, 'Banco de Dados'),
('2727936184', 1, 'Banco de Dados'),
('2727936184', 3, 'Banco de Dados'),
('7052256714', 1, 'Banco de Dados'),
('7052256714', 3, 'Banco de Dados'),
('7052256714', 2, 'Banco de Dados'),
('7052256714', 4, 'Banco de Dados'),
('2241869216', 1, 'Banco de Dados'),
('2241869216', 3, 'Banco de Dados');
 
-- ========== QUESTÕES ==========
INSERT INTO questao (enunciado, tema, tipo, dificuldade, email_professor, nome_disciplina) VALUES
-- Teresinha (Inglês I)
('Qual é o plural correto de "child" em inglês?', 'Plural de substantivos', 'alt', 'Fácil', 'teresinha.fatima@prof.fatec.com', 'Inglês I'),
('Escreva em inglês uma frase se apresentando com nome, idade e profissão.', 'Apresentação pessoal', 'diss', 'Média', 'teresinha.fatima@prof.fatec.com', 'Inglês I'),
-- Emanuel (Arq. e Modelagem de BD)
('Qual forma normal exige que não haja dependências parciais da chave primária?', 'Normalização', 'alt', 'Média', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados'),
('Explique a diferença entre chave primária e chave estrangeira.', 'Chaves e integridade', 'diss', 'Média', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados'),
-- Giuliano (Eng. de Software I)
('Qual metodologia ágil organiza o trabalho em sprints?', 'Metodologias ágeis', 'alt', 'Fácil', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I'),
('Cite duas diferenças entre requisitos funcionais e não funcionais.', 'Requisitos de software', 'diss', 'Média', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I');
 
-- ========== RESPOSTAS ==========
INSERT INTO resposta (enunciado, tipo, texto, correta, conceito) VALUES
-- Teresinha: alternativa
('Qual é o plural correto de "child" em inglês?', 'alt', 'children', 1, NULL),
('Qual é o plural correto de "child" em inglês?', 'alt', 'childs', 0, NULL),
('Qual é o plural correto de "child" em inglês?', 'alt', 'childrens', 0, NULL),
('Qual é o plural correto de "child" em inglês?', 'alt', 'childes', 0, NULL),
-- Teresinha: dissertativa
('Escreva em inglês uma frase se apresentando com nome, idade e profissão.', 'diss', 'My name is Ana, I am 25 years old and I am a student.', NULL, NULL),
 
-- Emanuel: alternativa
('Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 'Segunda Forma Normal (2FN)', 1, NULL),
('Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 'Primeira Forma Normal (1FN)', 0, NULL),
('Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 'Terceira Forma Normal (3FN)', 0, NULL),
('Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 'Forma Normal de Boyce-Codd (BCNF)', 0, NULL),
-- Emanuel: dissertativa
('Explique a diferença entre chave primária e chave estrangeira.', 'diss', 'A PK identifica cada registro; a FK referencia a PK de outra tabela e garante integridade.', NULL, NULL),
 
-- Giuliano: alternativa
('Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 'Scrum', 1, NULL),
('Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 'Cascata', 0, NULL),
('Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 'Modelo em V', 0, NULL),
('Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 'Espiral', 0, NULL),
-- Giuliano: dissertativa
('Cite duas diferenças entre requisitos funcionais e não funcionais.', 'diss', 'Funcionais descrevem o que o sistema faz; não funcionais definem qualidade, como desempenho.', NULL, NULL);
 
-- ========== PROVAS ==========
INSERT INTO prova (nome_prova, dificuldade, valor, email_professor, nome_disciplina) VALUES
('Prova 1', 'facil', 10.00, 'teresinha.fatima@prof.fatec.com', 'Inglês I'),
('Prova 2', 'media', 10.00, 'teresinha.fatima@prof.fatec.com', 'Inglês I'),
('Prova 1', 'media', 10.00, 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados'),
('Prova 2', 'media', 10.00, 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados'),
('Prova 1', 'facil', 10.00, 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I'),
('Prova 2', 'media', 10.00, 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I');
 
INSERT INTO prova (nome_prova, dificuldade, valor, email_professor, nome_disciplina) VALUES
('Prova 1', 'facil', 10.00, 'teresinha.fatima@prof.fatec.com', 'Inglês II');
 
-- ========== QUESTÕES DAS PROVAS ==========
INSERT INTO prova_questao (nome_prova, email_professor, nome_disciplina, enunciado, tipo, peso) VALUES
-- Teresinha
('Prova 1', 'teresinha.fatima@prof.fatec.com', 'Inglês I', 'Qual é o plural correto de "child" em inglês?', 'alt', 4.00),
('Prova 1', 'teresinha.fatima@prof.fatec.com', 'Inglês I', 'Escreva em inglês uma frase se apresentando com nome, idade e profissão.', 'diss', 6.00),
('Prova 2', 'teresinha.fatima@prof.fatec.com', 'Inglês I', 'Qual é o plural correto de "child" em inglês?', 'alt', 5.00),
('Prova 2', 'teresinha.fatima@prof.fatec.com', 'Inglês I', 'Escreva em inglês uma frase se apresentando com nome, idade e profissão.', 'diss', 5.00),
-- Emanuel
('Prova 1', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', 'Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 4.00),
('Prova 1', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', 'Explique a diferença entre chave primária e chave estrangeira.', 'diss', 6.00),
('Prova 2', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', 'Qual forma normal exige que não haja dependências parciais da chave primária?', 'alt', 5.00),
('Prova 2', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', 'Explique a diferença entre chave primária e chave estrangeira.', 'diss', 5.00),
-- Giuliano
('Prova 1', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', 'Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 4.00),
('Prova 1', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', 'Cite duas diferenças entre requisitos funcionais e não funcionais.', 'diss', 6.00),
('Prova 2', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', 'Qual metodologia ágil organiza o trabalho em sprints?', 'alt', 5.00),
('Prova 2', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', 'Cite duas diferenças entre requisitos funcionais e não funcionais.', 'diss', 5.00);
 
-- ========= PROVA aplicada TURMA =============
INSERT INTO prova_turma (id_turma, nome_prova, email_professor, nome_disciplina, data, horario) VALUES
-- Turma 2 (1º semestre, Inglês): Inglês I, da Teresinha
(2, 'Prova 1', 'teresinha.fatima@prof.fatec.com', 'Inglês I', '2026-03-18', '19:30:00'),
(2, 'Prova 2', 'teresinha.fatima@prof.fatec.com', 'Inglês I', '2026-05-20', '19:30:00'),
-- Turma 3 (2º semestre, Regular): Arquitetura e Modelagem de BD, do Emanuel
(3, 'Prova 1', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', '2026-10-14', '19:30:00'),
(3, 'Prova 2', 'emanuel.mineda@prof.fatec.com', 'Arquitetura e Modelagem de Banco de Dados', '2026-11-18', '19:30:00'),
-- Turma 3 (2º semestre, Regular): Engenharia de Software I, do Giuliano
(3, 'Prova 1', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', '2026-10-15', '19:30:00'),
(3, 'Prova 2', 'giuliano.bertoti@prof.fatec.com', 'Engenharia de Software I', '2026-11-19', '19:30:00');
 
 
 
 
 
 
 
 
 
-- ========== CONFERÊNCIA ==========

SELECT * FROM usuario;
SELECT * FROM curso;
SELECT * FROM disciplina;
SELECT * FROM turma;
SELECT * FROM professor;
SELECT * FROM aluno;

SELECT * FROM ministra;
SELECT * FROM curso_disciplina;
SELECT * FROM turma_disciplina;
SELECT * FROM pertence;
 
 
SELECT * FROM questao;
SELECT * FROM resposta;
SELECT * FROM prova;

SELECT * FROM prova_questao;
SELECT * FROM prova_turma;


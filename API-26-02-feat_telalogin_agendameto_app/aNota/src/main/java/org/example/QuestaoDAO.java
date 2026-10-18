package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class QuestaoDAO {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/anota_database?useTimezone=true&serverTimezone=UTC",
                "lovelace",
                "pass123"
        );
    }

    // INSERIR UMA NOVA QUESTÃO
    public void nova(String enunciado, String tema, String tipo,
                     String dificuldade, String emailProfessor,
                     String nomeDisciplina) {

        String insert_sql =
                "INSERT INTO questao "
                        + "(enunciado, tema, tipo, dificuldade, "
                        + "email_professor, nome_disciplina) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(insert_sql)) {

            pst.setString(1, enunciado);
            pst.setString(2, tema);
            pst.setString(3, tipo);
            pst.setString(4, dificuldade);
            pst.setString(5, emailProfessor);
            pst.setString(6, nomeDisciplina);

            pst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir nova questão!", e);
        }
    }

    // BUSCAR TODAS AS QUESTÕES
    public List<Questao> buscarQuestoes() {

        List<Questao> questoes = new ArrayList<>();

        String select_sql =
                "SELECT questao_id, enunciado, tema, tipo, dificuldade, "
                        + "data_criacao, data_edicao, email_professor, "
                        + "nome_disciplina "
                        + "FROM questao";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(select_sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Questao questao = new Questao();

                questao.setId(rs.getLong("questao_id"));
                questao.setEnunciado(rs.getString("enunciado"));
                questao.setTema(rs.getString("tema"));
                questao.setTipo(rs.getString("tipo"));
                questao.setDificuldade(rs.getString("dificuldade"));

                questao.setDataCriacao(
                        rs.getObject("data_criacao", LocalDateTime.class));

                questao.setDataEdicao(
                        rs.getObject("data_edicao", LocalDateTime.class));

                questao.setEmailProfessor(
                        rs.getString("email_professor"));

                questao.setNomeDisciplina(
                        rs.getString("nome_disciplina"));

                questoes.add(questao);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar questões!", e);
        }

        return questoes;
    }

    // DUPLICAR UMA QUESTÃO E SUAS RESPOSTAS
    public void duplicarQuestao(Questao questao) {

        String sqlQuestao =
                "INSERT INTO questao " +
                        "(enunciado, tema, tipo, dificuldade, " +
                        "email_professor, nome_disciplina) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlResposta =
                "INSERT INTO resposta " +
                        "(enunciado, tipo, texto, correta, conceito) " +
                        "VALUES (?, ?, ?, ?, ?)";

        String enunciadoOriginal = questao.getEnunciado();
        String tipo = questao.getTipo();

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {
                // 1. Buscar as respostas originais

                List<Resposta> respostas =
                        buscarRespostas(con, enunciadoOriginal, tipo);


                // 2. Criar um enunciado único para a cópia
                String enunciadoCopia = criarEnunciadoCopia(
                        con, enunciadoOriginal, tipo);

                // 3. Inserir a nova questão
                try (PreparedStatement pst = con.prepareStatement(sqlQuestao)) {

                    pst.setString(1, enunciadoCopia);
                    pst.setString(2, questao.getTema());
                    pst.setString(3, tipo);
                    pst.setString(4, questao.getDificuldade());
                    pst.setString(5, questao.getEmailProfessor());
                    pst.setString(6, questao.getNomeDisciplina());

                    pst.executeUpdate();
                }

                // 4. Copiar as respostas para a nova questão
                try (PreparedStatement pst = con.prepareStatement(sqlResposta)) {

                    for (Resposta resposta : respostas) {

                        pst.setString(1, enunciadoCopia);
                        pst.setString(2, tipo);
                        pst.setString(3, resposta.getTexto());

                        if (resposta.getCorreta() == null) {
                            pst.setNull(4, java.sql.Types.BOOLEAN);
                        } else {
                            pst.setBoolean(4, resposta.getCorreta());
                        }

                        pst.setString(5, resposta.getConceito());

                        pst.addBatch();
                    }

                    pst.executeBatch();
                }

                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao duplicar questão e respostas!", e);
        }
    }


    // CRIAR UM ENUNCIADO ÚNICO COM ATÉ 100 CARACTERES
    private String criarEnunciadoCopia(
            Connection con, String original, String tipo) throws SQLException {

        String sufixoBase = " (Cópia)";
        int limite = 100;

        String base = original;

        if (base.length() + sufixoBase.length() > limite) {
            base = base.substring(0, limite - sufixoBase.length());
        }

        String candidato = base + sufixoBase;
        int numero = 2;

        String sql =
                "SELECT COUNT(*) FROM questao " +
                        "WHERE enunciado = ? AND tipo = ?";

        while (true) {

            try (PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setString(1, candidato);
                pst.setString(2, tipo);

                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();

                    if (rs.getInt(1) == 0) {
                        return candidato;
                    }
                }
            }

            String sufixo = " (Cópia " + numero + ")";
            int tamanhoBase = limite - sufixo.length();

            String baseLimitada = original.length() > tamanhoBase
                    ? original.substring(0, tamanhoBase)
                    : original;

            candidato = baseLimitada + sufixo;
            numero++;
        }
    }


    public void atualizarQuestao(
            Questao questao,
            String enunciadoOriginal,
            String tipoOriginal,
            List<Resposta> novasRespostas) {

        String sqlExcluirRespostas =
                "DELETE FROM resposta WHERE enunciado = ? AND tipo = ?";

        String sqlAtualizarQuestao =
                "UPDATE questao SET " +
                        "enunciado = ?, " +
                        "tema = ?, " +
                        "tipo = ?, " +
                        "dificuldade = ?, " +
                        "data_edicao = CURRENT_TIMESTAMP, " +
                        "email_professor = ?, " +
                        "nome_disciplina = ? " +
                        "WHERE questao_id = ?";

        String sqlInserirResposta =
                "INSERT INTO resposta " +
                        "(enunciado, tipo, texto, correta, conceito) " +
                        "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = getConnection()) {

            con.setAutoCommit(false);

            try {
                // 1. Excluir as respostas antigas usando a chave original.
                try (PreparedStatement pst =
                             con.prepareStatement(sqlExcluirRespostas)) {

                    pst.setString(1, enunciadoOriginal);
                    pst.setString(2, tipoOriginal);
                    pst.executeUpdate();
                }

                // 2. Atualizar os dados da questão.
                try (PreparedStatement pst =
                             con.prepareStatement(sqlAtualizarQuestao)) {

                    pst.setString(1, questao.getEnunciado());
                    pst.setString(2, questao.getTema());
                    pst.setString(3, questao.getTipo());
                    pst.setString(4, questao.getDificuldade());
                    pst.setString(5, questao.getEmailProfessor());
                    pst.setString(6, questao.getNomeDisciplina());
                    pst.setLong(7, questao.getId());

                    int linhasAfetadas = pst.executeUpdate();

                    if (linhasAfetadas == 0) {
                        throw new SQLException(
                                "Questão não encontrada para atualização."
                        );
                    }
                }

                // 3. Inserir as respostas atualizadas.
                try (PreparedStatement pst =
                             con.prepareStatement(sqlInserirResposta)) {

                    for (Resposta resposta : novasRespostas) {

                        pst.setString(1, questao.getEnunciado());
                        pst.setString(2, questao.getTipo());
                        pst.setString(3, resposta.getTexto());

                        if (resposta.getCorreta() == null) {
                            pst.setNull(4, java.sql.Types.BOOLEAN);
                        } else {
                            pst.setBoolean(4, resposta.getCorreta());
                        }

                        pst.setString(5, resposta.getConceito());
                        pst.addBatch();
                    }

                    pst.executeBatch();
                }

                // 4. Confirmar todas as alterações juntas.
                con.commit();

            } catch (Exception e) {
                con.rollback();
                throw e;

            } finally {
                con.setAutoCommit(true);
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao atualizar questão e respostas!", e
            );
        }
    }



    // BUSCAR AS RESPOSTAS DE UMA QUESTÃO
    public List<Resposta> buscarRespostas(String enunciado, String tipo) {

        List<Resposta> respostas = new ArrayList<>();

        String sql =
                "SELECT resposta_id, enunciado, tipo, texto, correta, conceito " +
                        "FROM resposta " +
                        "WHERE enunciado = ? AND tipo = ? " +
                        "ORDER BY resposta_id";

        try (Connection con = getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, enunciado);
            pst.setString(2, tipo);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Resposta resposta = new Resposta();

                    resposta.setRespostaId(rs.getLong("resposta_id"));
                    resposta.setEnunciado(rs.getString("enunciado"));
                    resposta.setTipo(rs.getString("tipo"));
                    resposta.setTexto(rs.getString("texto"));

                    boolean correta = rs.getBoolean("correta");
                    resposta.setCorreta(rs.wasNull() ? null : correta);

                    resposta.setConceito(rs.getString("conceito"));

                    respostas.add(resposta);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(
                    "Erro ao buscar respostas da questão!", e);
        }

        return respostas;
    }

    // BUSCAR RESPOSTAS USANDO UMA CONEXÃO EXISTENTE
    private List<Resposta> buscarRespostas(
            Connection con, String enunciado, String tipo) throws SQLException {

        List<Resposta> respostas = new ArrayList<>();

        String sql =
                "SELECT resposta_id, enunciado, tipo, texto, correta, conceito " +
                        "FROM resposta " +
                        "WHERE enunciado = ? AND tipo = ? " +
                        "ORDER BY resposta_id";

        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, enunciado);
            pst.setString(2, tipo);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {

                    Resposta resposta = new Resposta();
                    resposta.setRespostaId(rs.getLong("resposta_id"));
                    resposta.setEnunciado(rs.getString("enunciado"));
                    resposta.setTipo(rs.getString("tipo"));
                    resposta.setTexto(rs.getString("texto"));

                    boolean correta = rs.getBoolean("correta");
                    resposta.setCorreta(rs.wasNull() ? null : correta);

                    resposta.setConceito(rs.getString("conceito"));

                    respostas.add(resposta);
                }
            }
        }

        return respostas;
    }
}

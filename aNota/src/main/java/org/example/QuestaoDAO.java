package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuestaoDAO {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/anota_database?useTimezone=true&serverTimezone=UTC",
                "lovelace",
                "pass123"
        );
    }

    public void nova(String enunciado, String tema, String tipo, String dificuldade, String emailProfessor, String nomeDisciplina) {
        Connection con = null;
        try {
            con = getConnection();
            String insert_sql = "INSERT INTO questao (enunciado, tema, tipo, dificuldade, email_professor, nome_disciplina) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(insert_sql);
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
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }
    }

    public List<Questao> buscarQuestoes() {
        List<Questao> questoes = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            String select_sql = "SELECT enunciado, tema, tipo, dificuldade, data_criacao, data_edicao, email_professor, nome_disciplina FROM questao";
            PreparedStatement pst = con.prepareStatement(select_sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Questao questao = new Questao();
                questao.setEnunciado(rs.getString("enunciado"));
                questao.setTema(rs.getString("tema"));
                questao.setTipo(rs.getString("tipo"));
                questao.setDificuldade(rs.getString("dificuldade"));
                questao.setDataCriacao(rs.getObject("data_criacao", LocalDateTime.class));
                questao.setDataEdicao(rs.getObject("data_edicao", LocalDateTime.class));
                questao.setEmailProfessor(rs.getString("email_professor"));
                questao.setNomeDisciplina(rs.getString("nome_disciplina"));

                questoes.add(questao);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar questões!", e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }

        return questoes;
    }
}
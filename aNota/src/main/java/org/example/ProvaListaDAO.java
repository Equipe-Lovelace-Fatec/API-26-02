package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProvaListaDAO {

    public List<Prova> listarPorProfessor(String emailProfessor) throws SQLException {
        String sql = "SELECT p.prova_id, p.nome_prova, p.nome_disciplina, p.email_professor, "
                + "p.dificuldade, p.valor, COUNT(pq.questao_id) AS qtd "
                + "FROM prova p "
                + "LEFT JOIN prova_questao pq ON pq.prova_id = p.prova_id "
                + "WHERE p.email_professor = ? "
                + "GROUP BY p.prova_id, p.nome_prova, p.nome_disciplina, p.email_professor, p.dificuldade, p.valor "
                + "ORDER BY p.nome_disciplina, p.nome_prova";

        List<Prova> provas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    provas.add(new Prova(
                            rs.getLong("prova_id"),
                            rs.getString("nome_prova"),
                            rs.getString("nome_disciplina"),
                            rs.getString("email_professor"),
                            rs.getString("dificuldade"),
                            rs.getBigDecimal("valor"),
                            rs.getInt("qtd")));
                }
            }
        }
        return provas;
    }

    public List<String[]> listarQuestoes(long provaId) throws SQLException {
        String sql = "SELECT q.enunciado, pq.peso "
                + "FROM prova_questao pq "
                + "JOIN questao q ON q.questao_id = pq.questao_id "
                + "WHERE pq.prova_id = ? "
                + "ORDER BY pq.questao_id";

        List<String[]> questoes = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, provaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questoes.add(new String[]{rs.getString("enunciado"), rs.getBigDecimal("peso").toPlainString()});
                }
            }
        }
        return questoes;
    }
}

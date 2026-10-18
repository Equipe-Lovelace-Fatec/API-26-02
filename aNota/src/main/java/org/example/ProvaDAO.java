package org.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProvaDAO {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/anota_database?useTimezone=true&serverTimezone=UTC", "lovelace", "pass123");
    }

    public Long nova(String nomeProva, String dificuldade, BigDecimal valor, String emailProfessor, String nomeDisciplina) {
        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        Long provaId = null;

        try {
            con = getConnection();
            String insert_sql = "insert into prova (nome_prova, dificuldade, valor, email_professor, nome_disciplina) values (?, ?, ?, ?, ?)";

            // IMPORTANTE: Adicionamos Statement.RETURN_GENERATED_KEYS para capturar o ID gerado
            pst = con.prepareStatement(insert_sql, java.sql.Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, nomeProva);

            if (dificuldade == null || dificuldade.trim().isEmpty()) {
                pst.setNull(2, java.sql.Types.VARCHAR);
            } else {
                pst.setString(2, dificuldade);
            }

            pst.setBigDecimal(3, valor);
            pst.setString(4, emailProfessor);
            pst.setString(5, nomeDisciplina);

            pst.executeUpdate();

            // Captura a chave gerada (o ID da prova)
            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                provaId = rs.getLong(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir nova prova!", e);
        } finally {
            // Fecha os recursos de forma segura
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pst != null) pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return provaId;
    }

    public List<MontagemProva> buscarProvas() {
        List<MontagemProva> provas = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            String select_sql = "select prova_id, nome_prova, dificuldade, valor, email_professor, nome_disciplina from prova";
            PreparedStatement pst = con.prepareStatement(select_sql);
            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                MontagemProva prova = new MontagemProva();
                prova.setId(rs.getLong("prova_id"));
                prova.setNomeProva(rs.getString("nome_prova"));
                prova.setDificuldade(rs.getString("dificuldade"));
                prova.setValor(rs.getBigDecimal("valor"));
                prova.setEmailProfessor(rs.getString("email_professor"));
                prova.setNomeDisciplina(rs.getString("nome_disciplina"));

                provas.add(prova);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar provas!", e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }
        return provas;
    }
    public void associarQuestao(Long provaId, Long questaoId, String emailProfessor, String nomeDisciplina, BigDecimal peso) {
        Connection con = null;
        PreparedStatement pst = null;
        try {
            con = getConnection();
            String sql = "insert into prova_questao (prova_id, questao_id, email_professor, nome_disciplina, peso) values (?, ?, ?, ?, ?)";
            pst = con.prepareStatement(sql);
            pst.setLong(1, provaId);
            pst.setLong(2, questaoId);
            pst.setString(3, emailProfessor);
            pst.setString(4, nomeDisciplina);
            pst.setBigDecimal(5, peso);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao associar questão à prova!", e);
        } finally {
            try { if (pst != null) pst.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // Metodo para associar uma turma à prova na tabela 'prova_turma' (caso vá agendar a prova)
    public void associarTurma(Long provaId, Long idTurma, String nomeProva, String emailProfessor, String nomeDisciplina, java.time.LocalDate data, java.time.LocalTime horario) {
        Connection con = null;
        try {
            con = getConnection();
            String sql = "insert into prova_turma (prova_id, id_turma, nome_prova, email_professor, nome_disciplina, data, horario) values (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setLong(1, provaId);
            pst.setLong(2, idTurma);
            pst.setString(3, nomeProva);
            pst.setString(4, emailProfessor);
            pst.setString(5, nomeDisciplina);
            pst.setObject(6, data);
            pst.setObject(7, horario);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao associar turma à prova!", e);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
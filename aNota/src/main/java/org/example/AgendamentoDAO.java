package org.example;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class AgendamentoDAO {

    public List<Turma> listarTurmasDaDisciplina(String nomeDisciplina) throws SQLException {
        String sql = "SELECT t.id_turma, t.semestre, t.periodo, t.vigencia, t.nome_turma "
                + "FROM turma t "
                + "JOIN turma_disciplina td ON td.id_turma = t.id_turma "
                + "WHERE td.nome_disciplina = ? "
                + "ORDER BY t.vigencia DESC, t.semestre, t.nome_turma";

        List<Turma> turmas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String descricao = rs.getString("nome_turma") + " - "
                            + rs.getString("semestre") + " sem. "
                            + rs.getString("periodo") + " ("
                            + rs.getString("vigencia") + ")";
                    turmas.add(new Turma(rs.getInt("id_turma"), descricao, rs.getString("periodo")));
                }
            }
        }
        return turmas;
    }

    public List<AlunoTurma> listarAlunosDaTurma(int idTurma) throws SQLException {
        String sql = "SELECT a.RA, u.nome "
                + "FROM pertence p "
                + "JOIN aluno a ON a.RA = p.RA "
                + "JOIN usuario u ON u.email = a.email "
                + "WHERE p.id_turma = ? "
                + "ORDER BY u.nome";

        List<AlunoTurma> alunos = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alunos.add(new AlunoTurma(rs.getString("RA"), rs.getString("nome")));
                }
            }
        }
        return alunos;
    }

    public void agendar(Prova prova, int idTurma, LocalDate data, LocalTime horario, LocalTime horarioFim,
                        List<String> ras) throws SQLException {
        String sqlTurma = "INSERT INTO prova_turma "
                + "(prova_id, id_turma, email_professor, nome_disciplina, data, horario, horario_fim) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlAluno = "INSERT INTO prova_aluno "
                + "(id_aplicacao, id_turma, RA) "
                + "VALUES (?, ?, ?)";

        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);
            try (PreparedStatement psTurma = con.prepareStatement(sqlTurma, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psAluno = con.prepareStatement(sqlAluno)) {

                psTurma.setLong(1, prova.getProvaId());
                psTurma.setInt(2, idTurma);
                psTurma.setString(3, prova.getEmailProfessor());
                psTurma.setString(4, prova.getNomeDisciplina());
                psTurma.setDate(5, Date.valueOf(data));
                psTurma.setTime(6, Time.valueOf(horario));
                psTurma.setTime(7, Time.valueOf(horarioFim));
                psTurma.executeUpdate();

                long idAplicacao;
                try (ResultSet chaves = psTurma.getGeneratedKeys()) {
                    if (!chaves.next()) {
                        throw new SQLException("Não foi possível obter o id_aplicacao gerado.");
                    }
                    idAplicacao = chaves.getLong(1);
                }

                for (String ra : ras) {
                    psAluno.setLong(1, idAplicacao);
                    psAluno.setInt(2, idTurma);
                    psAluno.setString(3, ra);
                    psAluno.addBatch();
                }
                psAluno.executeBatch();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<AplicacaoAgendada> listarAgendadas(String emailProfessor) throws SQLException {
        String sql = "SELECT pt.id_aplicacao, p.nome_prova, pt.nome_disciplina, "
                + "t.nome_turma, t.semestre, t.periodo, t.vigencia, "
                + "pt.data, pt.horario, pt.horario_fim, "
                + "COUNT(pa.RA) AS alunos, "
                + "(TIMESTAMP(pt.data, pt.horario) <= NOW()) AS iniciada "
                + "FROM prova_turma pt "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "JOIN turma t ON t.id_turma = pt.id_turma "
                + "LEFT JOIN prova_aluno pa ON pa.id_aplicacao = pt.id_aplicacao "
                + "WHERE pt.email_professor = ? AND TIMESTAMP(pt.data, pt.horario_fim) > NOW() "
                + "GROUP BY pt.id_aplicacao, p.nome_prova, pt.nome_disciplina, t.nome_turma, t.semestre, "
                + "t.periodo, t.vigencia, pt.data, pt.horario, pt.horario_fim "
                + "ORDER BY pt.data, pt.horario";

        List<AplicacaoAgendada> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String turma = rs.getString("nome_turma") + " - "
                            + rs.getString("semestre") + " sem. "
                            + rs.getString("periodo") + " ("
                            + rs.getString("vigencia") + ")";
                    lista.add(new AplicacaoAgendada(
                            rs.getLong("id_aplicacao"),
                            rs.getString("nome_prova"),
                            rs.getString("nome_disciplina"),
                            turma,
                            rs.getString("periodo"),
                            rs.getDate("data").toLocalDate(),
                            rs.getTime("horario").toLocalTime(),
                            rs.getTime("horario_fim").toLocalTime(),
                            rs.getInt("alunos"),
                            rs.getBoolean("iniciada")));
                }
            }
        }
        return lista;
    }

    public boolean alterarHorario(long idAplicacao, String emailProfessor, LocalDate data,
                                  LocalTime horario, LocalTime horarioFim) throws SQLException {
        String sql = "UPDATE prova_turma SET data = ?, horario = ?, horario_fim = ? "
                + "WHERE id_aplicacao = ? AND email_professor = ? "
                + "AND TIMESTAMP(data, horario) > NOW()";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(data));
            ps.setTime(2, Time.valueOf(horario));
            ps.setTime(3, Time.valueOf(horarioFim));
            ps.setLong(4, idAplicacao);
            ps.setString(5, emailProfessor);
            return ps.executeUpdate() == 1;
        }
    }

    public String impedimentoParaExcluir(long idAplicacao) throws SQLException {
        String sqlRespostas = "SELECT COUNT(*) FROM resposta_aluno WHERE id_aplicacao = ?";
        String sqlNotas = "SELECT COUNT(*) FROM prova_aluno WHERE id_aplicacao = ? AND nota IS NOT NULL";
        String sqlMedia = "SELECT COUNT(*) FROM item_avaliativo WHERE id_aplicacao = ?";

        try (Connection con = ConexaoBD.conectar()) {
            if (contar(con, sqlRespostas, idAplicacao) > 0) {
                return "Alunos já responderam esta prova.";
            }
            if (contar(con, sqlNotas, idAplicacao) > 0) {
                return "Esta prova já tem notas lançadas.";
            }
            if (contar(con, sqlMedia, idAplicacao) > 0) {
                return "Esta prova faz parte da média da turma. Tire-a da média (tela Média) antes de excluir.";
            }
        }
        return null;
    }

    public boolean excluir(long idAplicacao, String emailProfessor) throws SQLException {
        String sqlAlunos = "DELETE FROM prova_aluno WHERE id_aplicacao = ?";
        String sqlTurma = "DELETE FROM prova_turma "
                + "WHERE id_aplicacao = ? AND email_professor = ? "
                + "AND TIMESTAMP(data, horario) > NOW()";

        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);
            try (PreparedStatement psAlunos = con.prepareStatement(sqlAlunos);
                 PreparedStatement psTurma = con.prepareStatement(sqlTurma)) {
                psAlunos.setLong(1, idAplicacao);
                psAlunos.executeUpdate();

                psTurma.setLong(1, idAplicacao);
                psTurma.setString(2, emailProfessor);
                if (psTurma.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int contar(Connection con, String sql, long idAplicacao) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}

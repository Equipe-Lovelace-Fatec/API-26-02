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

    private static final int DURACAO_PROVA_MINUTOS = 100;

    public void agendar(Prova prova, int idTurma, LocalDate data, LocalTime horario,
                        List<String> ras) throws SQLException {
        String sqlTurma = "INSERT INTO prova_turma "
                + "(prova_id, id_turma, email_professor, nome_disciplina, data, horario, horario_fim) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlAluno = "INSERT INTO prova_aluno "
                + "(id_aplicacao, id_turma, RA) "
                + "VALUES (?, ?, ?)";

        LocalTime horarioFim = horario.plusMinutes(DURACAO_PROVA_MINUTOS);

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
}

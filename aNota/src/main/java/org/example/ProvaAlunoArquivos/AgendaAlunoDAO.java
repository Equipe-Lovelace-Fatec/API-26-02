package org.example.ProvaAlunoArquivos;

import org.example.ConexaoBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Consultas da tela "Minha agenda" do aluno. Só lê: esta tela não grava nada no banco.
public class AgendaAlunoDAO {

    // Mesma regra de RespostaAlunoDAO.buscarProvaEmAndamento: já começou e ainda não terminou (relógio do banco)
    private static final String JANELA_ABERTA =
            "TIMESTAMP(pt.data, pt.horario) <= NOW() AND TIMESTAMP(pt.data, pt.horario_fim) > NOW()";

    // Provas do aluno que ainda não terminaram, da mais próxima para a mais distante
    public List<ProvaAgendada> listarProvasAgendadas(String ra) throws SQLException {
        String sql = "SELECT pt.id_aplicacao, p.nome_prova, pt.nome_disciplina, up.nome AS nome_professor, "
                + "pt.data, pt.horario, pt.horario_fim, (" + JANELA_ABERTA + ") AS liberada "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "JOIN usuario up ON up.email = pt.email_professor "
                + "WHERE pa.RA = ? AND TIMESTAMP(pt.data, pt.horario_fim) > NOW() "
                + "ORDER BY pt.data, pt.horario";

        List<ProvaAgendada> provas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProvaAgendada p = new ProvaAgendada();
                    p.setIdAplicacao(rs.getLong("id_aplicacao"));
                    p.setNomeProva(rs.getString("nome_prova"));
                    p.setNomeDisciplina(rs.getString("nome_disciplina"));
                    p.setNomeProfessor(rs.getString("nome_professor"));
                    p.setData(rs.getDate("data").toLocalDate());
                    p.setHorario(rs.getTime("horario").toLocalTime());
                    p.setHorarioFim(rs.getTime("horario_fim").toLocalTime());
                    p.setLiberada(rs.getBoolean("liberada"));
                    provas.add(p);
                }
            }
        }
        return provas;
    }

    // A prova está liberada agora? Reconsulta o banco, usada ao clicar em "Iniciar prova"
    public boolean estaLiberada(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "WHERE pa.RA = ? AND pa.id_aplicacao = ? AND " + JANELA_ABERTA;
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            ps.setLong(2, idAplicacao);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

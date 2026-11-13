package org.example;

import org.example.ProvaAlunoArquivos.CorrecaoAlunoDAO;
import org.example.ProvaAlunoArquivos.CorrecaoAlunoQuestao;
import org.example.ProvaAlunoArquivos.ProvaRealizada;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Consultas das telas do aluno: provas realizadas, gabarito e correção.
public class ProvaAlunoDAO {

    private final CorrecaoAlunoDAO correcaoDAO = new CorrecaoAlunoDAO();

    // RA do aluno a partir do e-mail do login
    public String buscarRa(String emailAluno) throws SQLException {
        String sql = "SELECT RA FROM aluno WHERE email = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailAluno);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("RA") : null;
            }
        }
    }

    // Provas que o aluno realizou: aplicações para as quais ele foi convocado e cujo horário de término
    // já passou (assim o gabarito nunca aparece com a prova ainda em andamento). Mais recentes primeiro.
    public List<ProvaRealizada> listarProvasRealizadas(String ra) throws SQLException {
        String sql = "SELECT pa.id_aplicacao, pt.prova_id, p.nome_prova, pt.nome_disciplina, "
                + "up.nome AS nome_professor, pt.data, pt.horario, "
                + "CASE WHEN pa.corrigida_em IS NOT NULL THEN pa.nota END AS nota "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "JOIN usuario up ON up.email = pt.email_professor "
                + "WHERE pa.RA = ? AND TIMESTAMP(pt.data, pt.horario_fim) <= NOW() "
                + "ORDER BY pt.data DESC, pt.horario DESC";

        List<ProvaRealizada> provas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProvaRealizada p = new ProvaRealizada();
                    p.setIdAplicacao(rs.getLong("id_aplicacao"));
                    p.setProvaId(rs.getLong("prova_id"));
                    p.setNomeProva(rs.getString("nome_prova"));
                    p.setNomeDisciplina(rs.getString("nome_disciplina"));
                    p.setNomeProfessor(rs.getString("nome_professor"));
                    Date data = rs.getDate("data");
                    Time horario = rs.getTime("horario");
                    p.setData(data != null ? data.toLocalDate() : null);
                    p.setHorario(horario != null ? horario.toLocalTime() : null);
                    p.setNota(rs.getBigDecimal("nota"));
                    provas.add(p);
                }
            }
        }
        return provas;
    }

    // Questões da prova prontas para exibição. Com incluirRespostaAluno = false (gabarito) não busca
    // o que o aluno respondeu nem o comentário do professor.
    public List<CorrecaoAlunoQuestao> carregarQuestoes(String ra, ProvaRealizada prova, boolean incluirRespostaAluno)
            throws SQLException {
        List<CorrecaoAlunoDAO.QuestaoCorrecao> questoes = correcaoDAO.buscarQuestoes(prova.getProvaId());

        Map<Long, CorrecaoAlunoDAO.RespostaDoAluno> respostas = new HashMap<>();
        Map<Long, String> comentarios = new HashMap<>();
        if (incluirRespostaAluno) {
            respostas = correcaoDAO.buscarRespostasDoAluno(ra, prova.getIdAplicacao());
            comentarios = buscarComentariosDoProfessor(ra, prova.getIdAplicacao());
        }

        List<CorrecaoAlunoQuestao> lista = new ArrayList<>();
        int numero = 1;
        for (CorrecaoAlunoDAO.QuestaoCorrecao q : questoes) {
            CorrecaoAlunoDAO.RespostaDoAluno resposta = respostas.get(q.questaoId);

            CorrecaoAlunoQuestao item = new CorrecaoAlunoQuestao(
                    q.enunciado,
                    resposta != null ? resposta.texto : null,
                    String.join("\n", q.gabarito),
                    resposta != null ? resposta.correta : null);
            item.setNumero(numero++);
            item.setTipo(q.tipo);
            item.setLinhasApoio(linhasDeApoio(q));
            item.setComentarioProfessor(comentarios.get(q.questaoId));
            lista.add(item);
        }
        return lista;
    }

    // Alternativas (alt) e itens a relacionar (rela), que acompanham o enunciado
    private List<String> linhasDeApoio(CorrecaoAlunoDAO.QuestaoCorrecao q) {
        List<String> linhas = new ArrayList<>();
        if ("alt".equalsIgnoreCase(q.tipo)) {
            char letra = 'A';
            for (org.example.Resposta r : q.respostasCadastradas) {
                linhas.add(letra++ + ") " + r.getTexto());
            }
        } else if ("rela".equalsIgnoreCase(q.tipo)) {
            for (org.example.Resposta r : q.respostasCadastradas) {
                linhas.add("• " + r.getTexto());
            }
        }
        return linhas;
    }

    // Comentário do professor por questão aberta (resposta_aluno.comentario_professor)
    private Map<Long, String> buscarComentariosDoProfessor(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT questao_id, comentario_professor FROM resposta_aluno "
                + "WHERE id_aplicacao = ? AND RA = ?";
        Map<Long, String> comentarios = new HashMap<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String comentario = rs.getString("comentario_professor");
                    if (comentario != null && !comentario.isBlank()) {
                        comentarios.put(rs.getLong("questao_id"), comentario);
                    }
                }
            }
        }
        return comentarios;
    }
}
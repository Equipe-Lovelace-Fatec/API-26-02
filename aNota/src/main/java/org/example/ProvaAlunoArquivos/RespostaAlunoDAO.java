package org.example.ProvaAlunoArquivos;

import org.example.ConexaoBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;
import java.util.List;

// Grava e consulta as respostas que o aluno dá durante a prova (tabela resposta_aluno),
// que é de onde a tela de correção do professor lê.
public class RespostaAlunoDAO {

    // Prova que o aluno pode estar fazendo agora (já começou e ainda não terminou)
    public static class ProvaEmAndamento {
        public long idAplicacao;
        public long provaId;
        public String nomeProva;
        public LocalTime horarioFim;
    }

    // Resposta de uma questão, pronta para gravar
    public static class RespostaParaSalvar {
        public final long questaoId;
        public final Long respostaId;   // alternativa marcada (questões "alt"); null nas demais
        public final String texto;      // diss: texto digitado | rela: pares escolhidos | alt: null (vale resposta_id)
        public final Boolean correta;   // alt/rela: se acertou; diss e em branco: null (o professor corrige)

        public RespostaParaSalvar(long questaoId, Long respostaId, String texto, Boolean correta) {
            this.questaoId = questaoId;
            this.respostaId = respostaId;
            this.texto = texto != null && !texto.isBlank() ? texto : null;
            this.correta = correta;
        }

        // Questão respondida? As em branco não são gravadas aqui: o banco as cria na entrega.
        boolean respondida() {
            return respostaId != null || texto != null;
        }
    }

    // Aplicação em andamento para o aluno (a mais recente), ou null se não há nenhuma agora
    public ProvaEmAndamento buscarProvaEmAndamento(String ra) throws SQLException {
        String sql = "SELECT pa.id_aplicacao, pt.prova_id, p.nome_prova, pt.horario_fim "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "WHERE pa.RA = ? AND TIMESTAMP(pt.data, pt.horario) <= NOW() "
                + "AND TIMESTAMP(pt.data, pt.horario_fim) > NOW() "
                + "ORDER BY pt.data DESC, pt.horario DESC LIMIT 1";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ProvaEmAndamento p = new ProvaEmAndamento();
                p.idAplicacao = rs.getLong("id_aplicacao");
                p.provaId = rs.getLong("prova_id");
                p.nomeProva = rs.getString("nome_prova");
                Time fim = rs.getTime("horario_fim");
                p.horarioFim = fim != null ? fim.toLocalTime() : null;
                return p;
            }
        }
    }

    // O aluno já abriu e saiu/finalizou esta prova? (existe qualquer resposta gravada para ele)
    public boolean jaRealizou(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT COUNT(*) FROM resposta_aluno WHERE id_aplicacao = ? AND RA = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Entrega da prova, tudo ou nada: grava as questões respondidas e em seguida pede ao banco que crie
    // a linha em branco das demais (sp_preencher_respostas_em_branco). Como toda questão da prova passa a
    // ter linha, isso também marca a prova como realizada e ela não pode ser refeita.
    public void salvar(String ra, long idAplicacao, long provaId, List<RespostaParaSalvar> respostas) throws SQLException {
        String sql = "INSERT INTO resposta_aluno "
                + "(id_aplicacao, prova_id, RA, questao_id, resposta_id, texto_resposta, correta) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (RespostaParaSalvar r : respostas) {
                        if (!r.respondida()) {
                            continue;
                        }
                        ps.setLong(1, idAplicacao);
                        ps.setLong(2, provaId);
                        ps.setString(3, ra);
                        ps.setLong(4, r.questaoId);
                        if (r.respostaId != null) {
                            ps.setLong(5, r.respostaId);
                        } else {
                            ps.setNull(5, Types.BIGINT);
                        }
                        if (r.texto != null) {
                            ps.setString(6, r.texto);
                        } else {
                            ps.setNull(6, Types.LONGVARCHAR);
                        }
                        if (r.correta != null) {
                            ps.setBoolean(7, r.correta);
                        } else {
                            ps.setNull(7, Types.BOOLEAN);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                ProcedimentosAvaliacao.preencherRespostasEmBranco(con, idAplicacao, ra);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
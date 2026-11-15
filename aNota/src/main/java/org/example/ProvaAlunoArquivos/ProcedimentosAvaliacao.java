package org.example.ProvaAlunoArquivos;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Passos de entrega e de fechamento de correção que o banco oferece como procedimentos
// (sp_preencher_respostas_em_branco e sp_fechar_correcao).
// O usuário "lovelace" só tem SELECT/INSERT/UPDATE/DELETE: sem GRANT EXECUTE o CALL é negado.
// Nesse caso (ou se o procedimento não foi criado) executa-se o mesmo SQL do procedimento direto,
// para o sistema funcionar de qualquer jeito. Com o EXECUTE liberado, o procedimento é o usado.
public final class ProcedimentosAvaliacao {

    private static final int ER_SP_DOES_NOT_EXIST = 1305;
    private static final int ER_PROCACCESS_DENIED = 1370;

    private ProcedimentosAvaliacao() {
    }

    // Garante 1 linha em resposta_aluno por questão da prova, mesmo em branco (só cria o que falta)
    public static void preencherRespostasEmBranco(Connection con, long idAplicacao, String ra) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_preencher_respostas_em_branco(?, ?)}")) {
            cs.setLong(1, idAplicacao);
            cs.setString(2, ra);
            cs.execute();
            return;
        } catch (SQLException e) {
            if (!procedimentoIndisponivel(e)) {
                throw e;
            }
        }

        String sql = "INSERT INTO resposta_aluno (id_aplicacao, prova_id, RA, questao_id) "
                + "SELECT pa.id_aplicacao, pt.prova_id, pa.RA, pq.questao_id "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao "
                + "JOIN prova_questao pq ON pq.prova_id = pt.prova_id "
                + "LEFT JOIN resposta_aluno r ON r.id_aplicacao = pa.id_aplicacao "
                + "AND r.RA = pa.RA AND r.questao_id = pq.questao_id "
                + "WHERE pa.id_aplicacao = ? AND pa.RA = ? AND r.id_resposta_aluno IS NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            ps.executeUpdate();
        }
    }

    // Fecha a correção: nota da prova = soma das notas das questões e corrigida_em = agora.
    // Só deve ser chamado quando todas as questões da prova já têm nota.
    public static void fecharCorrecao(Connection con, long idAplicacao, String ra) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{CALL sp_fechar_correcao(?, ?)}")) {
            cs.setLong(1, idAplicacao);
            cs.setString(2, ra);
            cs.execute();
            return;
        } catch (SQLException e) {
            if (!procedimentoIndisponivel(e)) {
                throw e;
            }
        }

        String sql = "UPDATE prova_aluno pa "
                + "SET pa.nota = (SELECT COALESCE(SUM(r.nota), 0) FROM resposta_aluno r "
                + "WHERE r.id_aplicacao = pa.id_aplicacao AND r.RA = pa.RA), "
                + "pa.corrigida_em = NOW() "
                + "WHERE pa.id_aplicacao = ? AND pa.RA = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            ps.executeUpdate();
        }
    }

    // Correção que já estava fechada e voltou a ter questão sem nota: reabre (a nota sai da média)
    public static void reabrirCorrecao(Connection con, long idAplicacao, String ra) throws SQLException {
        String sql = "UPDATE prova_aluno SET nota = NULL, corrigida_em = NULL "
                + "WHERE id_aplicacao = ? AND RA = ? AND corrigida_em IS NOT NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            ps.executeUpdate();
        }
    }

    private static boolean procedimentoIndisponivel(SQLException e) {
        if (e.getErrorCode() == ER_PROCACCESS_DENIED || e.getErrorCode() == ER_SP_DOES_NOT_EXIST) {
            return true;
        }
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return msg.contains("execute command denied") || msg.contains("does not exist");
    }
}
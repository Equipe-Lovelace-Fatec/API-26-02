package org.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class DesempenhoDAO {

    public static class MediaAluno {
        public final BigDecimal media;
        public final int itensPendentes;
        public final int itensTotal;

        public MediaAluno(BigDecimal media, int itensPendentes, int itensTotal) {
            this.media = media;
            this.itensPendentes = itensPendentes;
            this.itensTotal = itensTotal;
        }
    }

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

    public List<String[]> listarVigencias(String ra) throws SQLException {
        String sql = "SELECT DISTINCT t.vigencia, t.nome_curso, t.periodo "
                + "FROM pertence p "
                + "JOIN turma t ON t.id_turma = p.id_turma "
                + "WHERE p.RA = ? "
                + "ORDER BY t.vigencia DESC";

        List<String[]> vigencias = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vigencias.add(new String[]{rs.getString("vigencia"), rs.getString("nome_curso"), rs.getString("periodo")});
                }
            }
        }
        return vigencias;
    }

    public List<String> listarDisciplinas(String ra, String vigencia) throws SQLException {
        String sql = "SELECT DISTINCT td.nome_disciplina "
                + "FROM pertence p "
                + "JOIN turma t ON t.id_turma = p.id_turma "
                + "JOIN turma_disciplina td ON td.id_turma = t.id_turma "
                + "WHERE p.RA = ? AND t.vigencia = ? "
                + "ORDER BY td.nome_disciplina";

        List<String> disciplinas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            ps.setString(2, vigencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disciplinas.add(rs.getString("nome_disciplina"));
                }
            }
        }
        return disciplinas;
    }

    public List<NotaAvaliacao> listarNotas(String ra, String vigencia, String nomeDisciplina) throws SQLException {
        String sql = "SELECT p.nome_prova, p.valor, pt.data, pt.horario_fim, pt.recuperacao, "
                + "pa.nota, pa.corrigida_em, ia.peso_percentual, "
                + "COALESCE(vin.substituida, 0) AS substituida, isub.nome AS item_substituido "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN turma t ON t.id_turma = pt.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "LEFT JOIN item_avaliativo ia ON ia.id_aplicacao = pt.id_aplicacao "
                + "LEFT JOIN v_item_nota vin ON vin.item_id = ia.item_id AND vin.RA = pa.RA "
                + "LEFT JOIN recuperacao_substituicao rs ON rs.id_aplicacao = pt.id_aplicacao AND rs.RA = pa.RA "
                + "LEFT JOIN item_avaliativo isub ON isub.item_id = rs.item_id "
                + "WHERE pa.RA = ? AND t.vigencia = ? AND pt.nome_disciplina = ? "
                + "ORDER BY pt.data, pt.horario";

        List<NotaAvaliacao> notas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            ps.setString(2, vigencia);
            ps.setString(3, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date data = rs.getDate("data");
                    Time fim = rs.getTime("horario_fim");
                    notas.add(new NotaAvaliacao(
                            rs.getString("nome_prova"),
                            data == null ? null : data.toLocalDate(),
                            fim == null ? null : fim.toLocalTime(),
                            rs.getBigDecimal("nota"),
                            rs.getBigDecimal("valor"),
                            rs.getTimestamp("corrigida_em") != null,
                            rs.getBoolean("recuperacao"),
                            rs.getBigDecimal("peso_percentual"),
                            rs.getInt("substituida") == 1,
                            rs.getString("item_substituido")));
                }
            }
        }
        return notas;
    }

    public MediaAluno buscarMedia(String ra, String vigencia, String nomeDisciplina) throws SQLException {
        String sql = "SELECT vm.media, vm.itens_pendentes, vm.itens_total "
                + "FROM v_media_aluno vm "
                + "JOIN turma t ON t.id_turma = vm.id_turma "
                + "WHERE vm.RA = ? AND t.vigencia = ? AND vm.nome_disciplina = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            ps.setString(2, vigencia);
            ps.setString(3, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new MediaAluno(rs.getBigDecimal("media"), rs.getInt("itens_pendentes"), rs.getInt("itens_total"));
            }
        }
    }
}

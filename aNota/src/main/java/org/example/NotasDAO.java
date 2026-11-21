package org.example;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Consultas da tela de Notas do professor: turmas, alunos com média e notas de cada prova.
public class NotasDAO {

    // Turma em que o professor aplicou provas, por disciplina (a média é calculada por turma + disciplina)
    public static class TurmaDisciplina {
        public long idTurma;
        public String nomeDisciplina;
        public String nomeTurma;
        public String semestre;
        public String periodo;
        public String vigencia;

        @Override
        public String toString() {
            return nomeDisciplina + "  |  Turma: " + nomeTurma + " - " + semestre + " sem. " + periodo
                    + " (" + vigencia + ")";
        }
    }

    // Aluno da turma com a média na disciplina escolhida
    public static class AlunoMedia {
        public String ra;            // null = opção "Todos os alunos" do seletor
        public String nome;
        public BigDecimal media;     // null = turma ainda sem composição de média (item_avaliativo)
        public int itensPendentes;   // provas da média ainda sem nota (contam zero na média)

        // Texto da média ao lado do nome: "7,50", "7,50 (parcial)" ou "–"
        public String getMediaExibicao() {
            if (media == null) {
                return "–";
            }
            return formatar(media) + (itensPendentes > 0 ? " (parcial)" : "");
        }

        // Texto do cartão: "Nome - RA"
        public String getNomeComRa() {
            return nome + " - " + ra;
        }

        @Override
        public String toString() {
            return ra == null ? nome : nome + " (" + ra + ")";
        }
    }

    // Uma prova que o aluno fez com este professor
    public static class NotaProva {
        public String nomeProva;
        public String nomeDisciplina;
        public LocalDate data;
        public LocalTime horario;
        public boolean recuperacao;
        public BigDecimal valor;     // valor da prova (nota máxima)
        public BigDecimal nota;      // só quando a correção foi fechada
        public String situacao;      // v_correcao_status
    }

    public static String formatar(BigDecimal valor) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(valor);
    }

    // Turmas (por disciplina) em que o professor aplicou provas, as mais recentes primeiro
    public List<TurmaDisciplina> listarTurmasDoProfessor(String emailProfessor) throws SQLException {
        String sql = "SELECT DISTINCT t.id_turma, pt.nome_disciplina, t.nome_turma, t.semestre, t.periodo, t.vigencia "
                + "FROM prova_turma pt "
                + "JOIN turma t ON t.id_turma = pt.id_turma "
                + "WHERE pt.email_professor = ? "
                + "ORDER BY t.vigencia DESC, t.semestre, t.periodo, t.nome_turma, pt.nome_disciplina";

        List<TurmaDisciplina> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TurmaDisciplina t = new TurmaDisciplina();
                    t.idTurma = rs.getLong("id_turma");
                    t.nomeDisciplina = rs.getString("nome_disciplina");
                    t.nomeTurma = rs.getString("nome_turma");
                    t.semestre = rs.getString("semestre");
                    t.periodo = rs.getString("periodo");
                    t.vigencia = rs.getString("vigencia");
                    lista.add(t);
                }
            }
        }
        return lista;
    }

    // Alunos da turma, em ordem alfabética, com a média na disciplina (view v_media_aluno)
    public List<AlunoMedia> listarAlunosDaTurma(long idTurma, String nomeDisciplina) throws SQLException {
        String sql = "SELECT a.RA, u.nome, vm.media, vm.itens_pendentes "
                + "FROM pertence pe "
                + "JOIN aluno a ON a.RA = pe.RA "
                + "JOIN usuario u ON u.email = a.email "
                + "LEFT JOIN v_media_aluno vm ON vm.id_turma = pe.id_turma "
                + "AND vm.nome_disciplina = ? AND vm.RA = pe.RA "
                + "WHERE pe.id_turma = ? "
                + "ORDER BY u.nome";

        List<AlunoMedia> alunos = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nomeDisciplina);
            ps.setLong(2, idTurma);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlunoMedia a = new AlunoMedia();
                    a.ra = rs.getString("RA");
                    a.nome = rs.getString("nome");
                    a.media = rs.getBigDecimal("media");
                    a.itensPendentes = rs.getInt("itens_pendentes");
                    alunos.add(a);
                }
            }
        }
        return alunos;
    }

    // Provas do professor que o aluno já começou a fazer, da mais recente para a mais antiga.
    // A nota só aparece depois que a correção foi fechada (corrigida_em preenchido).
    public List<NotaProva> listarNotasDoAluno(String ra, String emailProfessor) throws SQLException {
        String sql = "SELECT p.nome_prova, pt.nome_disciplina, pt.data, pt.horario, pt.recuperacao, p.valor, "
                + "CASE WHEN pa.corrigida_em IS NOT NULL THEN pa.nota END AS nota, cs.situacao "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "LEFT JOIN v_correcao_status cs ON cs.id_aplicacao = pa.id_aplicacao AND cs.RA = pa.RA "
                + "WHERE pa.RA = ? AND pt.email_professor = ? AND TIMESTAMP(pt.data, pt.horario) <= NOW() "
                + "ORDER BY pt.data DESC, pt.horario DESC";

        List<NotaProva> notas = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, ra);
            ps.setString(2, emailProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NotaProva n = new NotaProva();
                    n.nomeProva = rs.getString("nome_prova");
                    n.nomeDisciplina = rs.getString("nome_disciplina");
                    Date data = rs.getDate("data");
                    Time horario = rs.getTime("horario");
                    n.data = data != null ? data.toLocalDate() : null;
                    n.horario = horario != null ? horario.toLocalTime() : null;
                    n.recuperacao = rs.getBoolean("recuperacao");
                    n.valor = rs.getBigDecimal("valor");
                    n.nota = rs.getBigDecimal("nota");
                    n.situacao = rs.getString("situacao");
                    notas.add(n);
                }
            }
        }
        return notas;
    }
}
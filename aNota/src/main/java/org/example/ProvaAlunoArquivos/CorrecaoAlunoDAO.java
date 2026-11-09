package org.example.ProvaAlunoArquivos;

import org.example.ConexaoBD;
import org.example.Resposta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Consultas da tela de correção (Aluno.fxml): cabeçalho, questões da prova e respostas do aluno.
public class CorrecaoAlunoDAO {

    // Uma aplicação da prova a uma turma (prova_turma), para o professor escolher o que corrigir
    public static class Aplicacao {
        public long idAplicacao;
        public long provaId;
        public String nomeProva;
        public String nomeDisciplina;
        public String nomeTurma;
        public String semestre;
        public String periodo;
        public String vigencia;
        public LocalDate data;
        public LocalTime horario;

        @Override
        public String toString() {
            String dia = data != null ? data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
            String hora = horario != null ? horario.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
            return nomeProva + "  |  " + nomeDisciplina
                    + "  |  Turma: " + nomeTurma + " - " + semestre + "º sem. " + periodo + " (" + vigencia + ")"
                    + "  |  " + dia + " " + hora;
        }
    }

    // Aluno convocado para uma aplicação (prova_aluno)
    public static class AlunoConvocado {
        public String ra;
        public String nome;
        public BigDecimal nota;   // null = ainda não corrigida

        @Override
        public String toString() {
            return nome + " (" + ra + ")" + (nota != null ? "  - nota " + nota.toPlainString() : "");
        }
    }

    // Dados do aluno, da disciplina e da realização da prova
    public static class Cabecalho {
        public long provaId;
        public String nomeAluno;
        public String ra;
        public String email;
        public String nomeCurso;
        public LocalDate dataNascimento;
        public String nomeTurma;
        public String semestre;
        public String periodo;
        public String nomeDisciplina;
        public String nomeProva;
        public LocalDate data;
        public LocalTime horario;
        public BigDecimal notaAtual;   // prova_aluno.nota (null até a prova ser corrigida)
    }

    // Uma questão da prova, com o gabarito cadastrado
    public static class QuestaoCorrecao {
        public long questaoId;
        public String enunciado;
        public String tipo;          // alt | diss | rela
        public BigDecimal peso;      // valor da questão na prova
        public List<Resposta> respostasCadastradas = new ArrayList<>();
        public List<String> gabarito = new ArrayList<>(); // resposta esperada pelo professor
    }

    // O que o aluno respondeu em uma questão
    public static class RespostaDoAluno {
        public String texto;      // alt: texto da alternativa marcada | diss/rela: texto_resposta
        public Boolean correta;   // null = ainda não corrigida
    }

    // Aplicações do professor cuja prova já começou (só essas podem ser corrigidas), as mais recentes primeiro
    public List<Aplicacao> listarAplicacoesDoProfessor(String emailProfessor) throws SQLException {
        String sql = "SELECT pt.id_aplicacao, pt.prova_id, p.nome_prova, pt.nome_disciplina, "
                + "t.nome_turma, t.semestre, t.periodo, t.vigencia, pt.data, pt.horario "
                + "FROM prova_turma pt "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "JOIN turma t ON t.id_turma = pt.id_turma "
                + "WHERE pt.email_professor = ? AND TIMESTAMP(pt.data, pt.horario) <= NOW() "
                + "ORDER BY pt.data DESC, pt.horario DESC";

        List<Aplicacao> lista = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Aplicacao a = new Aplicacao();
                    a.idAplicacao = rs.getLong("id_aplicacao");
                    a.provaId = rs.getLong("prova_id");
                    a.nomeProva = rs.getString("nome_prova");
                    a.nomeDisciplina = rs.getString("nome_disciplina");
                    a.nomeTurma = rs.getString("nome_turma");
                    a.semestre = rs.getString("semestre");
                    a.periodo = rs.getString("periodo");
                    a.vigencia = rs.getString("vigencia");
                    Date data = rs.getDate("data");
                    Time horario = rs.getTime("horario");
                    a.data = data != null ? data.toLocalDate() : null;
                    a.horario = horario != null ? horario.toLocalTime() : null;
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // Alunos convocados para a aplicação, em ordem alfabética
    public List<AlunoConvocado> listarAlunosDaAplicacao(long idAplicacao) throws SQLException {
        String sql = "SELECT a.RA, u.nome, pa.nota "
                + "FROM prova_aluno pa "
                + "JOIN aluno a ON a.RA = pa.RA "
                + "JOIN usuario u ON u.email = a.email "
                + "WHERE pa.id_aplicacao = ? ORDER BY u.nome";

        List<AlunoConvocado> alunos = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlunoConvocado a = new AlunoConvocado();
                    a.ra = rs.getString("RA");
                    a.nome = rs.getString("nome");
                    a.nota = rs.getBigDecimal("nota");
                    alunos.add(a);
                }
            }
        }
        return alunos;
    }

    public Cabecalho buscarCabecalho(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT pt.prova_id, u.nome, a.RA, u.email, a.nome_curso, a.data_nascimento, "
                + "t.nome_turma, t.semestre, t.periodo, pt.nome_disciplina, pt.data, pt.horario, "
                + "p.nome_prova, pa.nota "
                + "FROM prova_aluno pa "
                + "JOIN prova_turma pt ON pt.id_aplicacao = pa.id_aplicacao AND pt.id_turma = pa.id_turma "
                + "JOIN prova p ON p.prova_id = pt.prova_id "
                + "AND p.email_professor = pt.email_professor AND p.nome_disciplina = pt.nome_disciplina "
                + "JOIN turma t ON t.id_turma = pt.id_turma "
                + "JOIN aluno a ON a.RA = pa.RA "
                + "JOIN usuario u ON u.email = a.email "
                + "WHERE pa.id_aplicacao = ? AND pa.RA = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Cabecalho c = new Cabecalho();
                c.provaId = rs.getLong("prova_id");
                c.nomeAluno = rs.getString("nome");
                c.ra = rs.getString("RA");
                c.email = rs.getString("email");
                c.nomeCurso = rs.getString("nome_curso");
                Date nasc = rs.getDate("data_nascimento");
                c.dataNascimento = nasc != null ? nasc.toLocalDate() : null;
                c.nomeTurma = rs.getString("nome_turma");
                c.semestre = rs.getString("semestre");
                c.periodo = rs.getString("periodo");
                c.nomeDisciplina = rs.getString("nome_disciplina");
                c.nomeProva = rs.getString("nome_prova");
                Date data = rs.getDate("data");
                Time horario = rs.getTime("horario");
                c.data = data != null ? data.toLocalDate() : null;
                c.horario = horario != null ? horario.toLocalTime() : null;
                c.notaAtual = rs.getBigDecimal("nota");
                return c;
            }
        }
    }

    // Questões da prova, já com as respostas cadastradas e o gabarito (busca por questao_id)
    public List<QuestaoCorrecao> buscarQuestoes(long provaId) throws SQLException {
        String sqlQuestoes = "SELECT q.questao_id, q.enunciado, q.tipo, pq.peso "
                + "FROM prova_questao pq "
                + "JOIN questao q ON q.questao_id = pq.questao_id "
                + "WHERE pq.prova_id = ? ORDER BY pq.questao_id";
        String sqlRespostas = "SELECT resposta_id, texto, correta, conceito "
                + "FROM resposta WHERE questao_id = ? ORDER BY resposta_id";

        List<QuestaoCorrecao> questoes = new ArrayList<>();
        try (Connection con = ConexaoBD.conectar()) {
            try (PreparedStatement ps = con.prepareStatement(sqlQuestoes)) {
                ps.setLong(1, provaId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        QuestaoCorrecao q = new QuestaoCorrecao();
                        q.questaoId = rs.getLong("questao_id");
                        q.enunciado = rs.getString("enunciado");
                        q.tipo = rs.getString("tipo");
                        q.peso = rs.getBigDecimal("peso");
                        questoes.add(q);
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlRespostas)) {
                for (QuestaoCorrecao q : questoes) {
                    ps.setLong(1, q.questaoId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Resposta r = new Resposta();
                            r.setRespostaId(rs.getLong("resposta_id"));
                            r.setTexto(rs.getString("texto"));
                            boolean correta = rs.getBoolean("correta");
                            r.setCorreta(rs.wasNull() ? null : correta);
                            r.setConceito(rs.getString("conceito"));
                            q.respostasCadastradas.add(r);
                        }
                    }
                    q.gabarito = montarGabarito(q);
                }
            }
        }
        return questoes;
    }

    // alt: alternativas corretas | diss: resposta esperada | rela: item -> par correto
    private List<String> montarGabarito(QuestaoCorrecao q) {
        List<String> gabarito = new ArrayList<>();
        for (Resposta r : q.respostasCadastradas) {
            if ("alt".equalsIgnoreCase(q.tipo)) {
                if (Boolean.TRUE.equals(r.getCorreta())) {
                    gabarito.add(r.getTexto());
                }
            } else if ("rela".equalsIgnoreCase(q.tipo)) {
                gabarito.add(r.getTexto() + " → " + (r.getConceito() == null ? "" : r.getConceito()));
            } else {
                gabarito.add(r.getTexto());
            }
        }
        return gabarito;
    }

    // Respostas do aluno nesta aplicação, por questao_id
    public Map<Long, RespostaDoAluno> buscarRespostasDoAluno(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT ra.questao_id, ra.texto_resposta, ra.correta, r.texto AS texto_alternativa, "
                + "r.correta AS alternativa_correta "
                + "FROM resposta_aluno ra "
                + "LEFT JOIN resposta r ON r.resposta_id = ra.resposta_id AND r.questao_id = ra.questao_id "
                + "WHERE ra.id_aplicacao = ? AND ra.RA = ?";

        Map<Long, RespostaDoAluno> respostas = new HashMap<>();
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RespostaDoAluno r = new RespostaDoAluno();
                    String alternativa = rs.getString("texto_alternativa");
                    r.texto = alternativa != null ? alternativa : rs.getString("texto_resposta");

                    boolean correta = rs.getBoolean("correta");
                    if (!rs.wasNull()) {
                        r.correta = correta;
                    } else if (alternativa != null) {
                        // alternativa marcada: dá para saber se acertou pela própria alternativa
                        r.correta = rs.getBoolean("alternativa_correta");
                    }
                    respostas.put(rs.getLong("questao_id"), r);
                }
            }
        }
        return respostas;
    }

    // Grava a nota total da prova do aluno (coluna prova_aluno.nota)
    public void salvarNota(String ra, long idAplicacao, BigDecimal nota) throws SQLException {
        String sql = "UPDATE prova_aluno SET nota = ? WHERE id_aplicacao = ? AND RA = ?";
        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, nota);
            ps.setLong(2, idAplicacao);
            ps.setString(3, ra);
            ps.executeUpdate();
        }
    }
}
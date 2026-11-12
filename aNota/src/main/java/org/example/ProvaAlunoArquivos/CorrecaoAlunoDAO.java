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
        public boolean recuperacao;   // prova_turma.recuperacao

        @Override
        public String toString() {
            String dia = data != null ? data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-";
            String hora = horario != null ? horario.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) : "";
            return nomeProva + "  |  " + nomeDisciplina
                    + "  |  Turma: " + nomeTurma + " - " + semestre + " sem. " + periodo + " (" + vigencia + ")"
                    + "  |  " + dia + " " + hora + (recuperacao ? "  |  RECUPERAÇÃO" : "");
        }
    }

    // Aluno convocado para uma aplicação (prova_aluno)
    public static class AlunoConvocado {
        public String ra;
        public String nome;
        public BigDecimal nota;   // só vale quando corrigida
        public boolean corrigida; // prova_aluno.corrigida_em preenchido
        public String situacao;   // v_correcao_status: SEM ENTREGA, AGUARDANDO CORREÇÃO, PRONTA PARA FECHAR...

        @Override
        public String toString() {
            String detalhe = corrigida && nota != null ? "nota " + nota.toPlainString()
                    : (situacao != null ? situacao.toLowerCase() : "");
            return nome + " (" + ra + ")" + (detalhe.isEmpty() ? "" : "  - " + detalhe);
        }
    }

    // Dados do aluno, da disciplina e da realização da prova
    public static class Cabecalho {
        public long provaId;
        public String nomeAluno;
        public String ra;
        public String email;
        public String nomeCurso;
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
        public BigDecimal nota;   // resposta_aluno.nota (null = questão ainda sem nota)
        public String comentario; // resposta_aluno.comentario_professor
    }

    // Nota e comentário que o professor deu a uma questão (nota null = ainda não corrigida)
    public static class NotaQuestao {
        public final long questaoId;
        public final BigDecimal nota;
        public final String comentario;

        public NotaQuestao(long questaoId, BigDecimal nota, String comentario) {
            this.questaoId = questaoId;
            this.nota = nota;
            this.comentario = comentario != null && !comentario.isBlank() ? comentario.trim() : null;
        }
    }

    // Resultado de salvar a correção de um aluno
    public static class ResultadoCorrecao {
        public boolean fechada;          // todas as questões com nota: nota da prova gravada e corrigida_em preenchido
        public int pendentes;            // questões ainda sem nota
        public BigDecimal notaProva;     // prova_aluno.nota (só quando fechada)
    }

    // Aplicações do professor cuja prova já começou (só essas podem ser corrigidas), as mais recentes primeiro
    public List<Aplicacao> listarAplicacoesDoProfessor(String emailProfessor) throws SQLException {
        String sql = "SELECT pt.id_aplicacao, pt.prova_id, p.nome_prova, pt.nome_disciplina, "
                + "t.nome_turma, t.semestre, t.periodo, t.vigencia, pt.data, pt.horario, pt.recuperacao "
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
                    a.recuperacao = rs.getBoolean("recuperacao");
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // Alunos convocados para a aplicação, em ordem alfabética
    public List<AlunoConvocado> listarAlunosDaAplicacao(long idAplicacao) throws SQLException {
        String sql = "SELECT a.RA, u.nome, pa.nota, pa.corrigida_em, cs.situacao "
                + "FROM prova_aluno pa "
                + "JOIN aluno a ON a.RA = pa.RA "
                + "JOIN usuario u ON u.email = a.email "
                + "LEFT JOIN v_correcao_status cs ON cs.id_aplicacao = pa.id_aplicacao AND cs.RA = pa.RA "
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
                    a.corrigida = rs.getTimestamp("corrigida_em") != null;
                    a.situacao = rs.getString("situacao");
                    alunos.add(a);
                }
            }
        }
        return alunos;
    }

    public Cabecalho buscarCabecalho(String ra, long idAplicacao) throws SQLException {
        String sql = "SELECT pt.prova_id, u.nome, a.RA, u.email, a.nome_curso, "
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
        String sql = "SELECT ra.questao_id, ra.texto_resposta, ra.correta, ra.nota, ra.comentario_professor, "
                + "r.texto AS texto_alternativa, r.correta AS alternativa_correta "
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
                    r.nota = rs.getBigDecimal("nota");
                    r.comentario = rs.getString("comentario_professor");
                    respostas.put(rs.getLong("questao_id"), r);
                }
            }
        }
        return respostas;
    }

    // Salva a correção de um aluno numa única transação:
    //  1. garante a linha de cada questão em resposta_aluno (aluno que não entregou fica com tudo em branco);
    //  2. grava nota e comentário de cada questão (em branco corrigida = 0; nota null = ainda não corrigida);
    //  3. se todas as questões têm nota, fecha a correção (nota da prova = soma e corrigida_em = agora);
    //     senão, se ela já estava fechada, reabre, para a nota antiga não continuar valendo na média.
    public ResultadoCorrecao salvarCorrecao(String ra, long idAplicacao, List<NotaQuestao> notas) throws SQLException {
        String sql = "UPDATE resposta_aluno SET nota = ?, comentario_professor = ? "
                + "WHERE id_aplicacao = ? AND RA = ? AND questao_id = ?";

        ResultadoCorrecao resultado = new ResultadoCorrecao();
        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);
            try {
                ProcedimentosAvaliacao.preencherRespostasEmBranco(con, idAplicacao, ra);

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (NotaQuestao n : notas) {
                        if (n.nota != null) {
                            ps.setBigDecimal(1, n.nota);
                        } else {
                            ps.setNull(1, java.sql.Types.DECIMAL);
                        }
                        if (n.comentario != null) {
                            ps.setString(2, n.comentario);
                        } else {
                            ps.setNull(2, java.sql.Types.LONGVARCHAR);
                        }
                        ps.setLong(3, idAplicacao);
                        ps.setString(4, ra);
                        ps.setLong(5, n.questaoId);
                        ps.executeUpdate();
                        if (n.nota == null) {
                            resultado.pendentes++;
                        }
                    }
                }

                if (resultado.pendentes == 0) {
                    ProcedimentosAvaliacao.fecharCorrecao(con, idAplicacao, ra);
                    resultado.fechada = true;
                    resultado.notaProva = buscarNotaDaProva(con, idAplicacao, ra);
                } else {
                    ProcedimentosAvaliacao.reabrirCorrecao(con, idAplicacao, ra);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return resultado;
    }

    private BigDecimal buscarNotaDaProva(Connection con, long idAplicacao, String ra) throws SQLException {
        String sql = "SELECT nota FROM prova_aluno WHERE id_aplicacao = ? AND RA = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idAplicacao);
            ps.setString(2, ra);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("nota") : null;
            }
        }
    }
}
package org.example.ProvaAlunoArquivos;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;

// Uma prova que o aluno realizou (uma aplicação da prova para a turma dele)
public class ProvaRealizada {

    private long idAplicacao;
    private long provaId;
    private String nomeProva;
    private String nomeDisciplina;
    private String nomeProfessor;
    private LocalDate data;
    private LocalTime horario;
    private BigDecimal nota;   // null = ainda não corrigida

    public long getIdAplicacao() { return idAplicacao; }
    public void setIdAplicacao(long idAplicacao) { this.idAplicacao = idAplicacao; }

    public long getProvaId() { return provaId; }
    public void setProvaId(long provaId) { this.provaId = provaId; }

    public String getNomeProva() { return nomeProva; }
    public void setNomeProva(String nomeProva) { this.nomeProva = nomeProva; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public String getNomeProfessor() { return nomeProfessor; }
    public void setNomeProfessor(String nomeProfessor) { this.nomeProfessor = nomeProfessor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }

    public boolean foiCorrigida() {
        return nota != null;
    }

    // Nota no formato 8,50 ou um traço quando ainda não foi corrigida
    public String getNotaExibicao() {
        if (nota == null) {
            return "–";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(nota);
    }
}
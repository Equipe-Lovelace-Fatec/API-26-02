package org.example;

import java.math.BigDecimal;

public class Prova {

    private final long provaId;
    private final String nomeProva;
    private final String nomeDisciplina;
    private final String emailProfessor;
    private final String dificuldade;
    private final BigDecimal valor;
    private final int qtdQuestoes;

    public Prova(long provaId, String nomeProva, String nomeDisciplina, String emailProfessor,
                 String dificuldade, BigDecimal valor, int qtdQuestoes) {
        this.provaId = provaId;
        this.nomeProva = nomeProva;
        this.nomeDisciplina = nomeDisciplina;
        this.emailProfessor = emailProfessor;
        this.dificuldade = dificuldade;
        this.valor = valor;
        this.qtdQuestoes = qtdQuestoes;
    }

    public long getProvaId() { return provaId; }
    public String getNomeProva() { return nomeProva; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public String getEmailProfessor() { return emailProfessor; }
    public String getDificuldade() { return dificuldade; }
    public BigDecimal getValor() { return valor; }
    public int getQtdQuestoes() { return qtdQuestoes; }

    public boolean temNota() {
        return valor != null && valor.signum() > 0;
    }

    public String getDificuldadeExibicao() {
        if (dificuldade == null) return "-";
        switch (dificuldade) {
            case "facil": return "Fácil";
            case "media": return "Média";
            case "dificil": return "Difícil";
            default: return dificuldade;
        }
    }

    public String getValorExibicao() {
        if (valor == null) return "-";
        return valor.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}

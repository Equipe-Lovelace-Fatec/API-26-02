package org.example;

public class Turma {

    private final int idTurma;
    private final String descricao;
    private final String periodo;

    public Turma(int idTurma, String descricao, String periodo) {
        this.idTurma = idTurma;
        this.descricao = descricao;
        this.periodo = periodo;
    }

    public int getIdTurma() {
        return idTurma;
    }

    public String getPeriodo() {
        return periodo;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

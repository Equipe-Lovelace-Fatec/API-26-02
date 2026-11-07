package org.example.ProvaAlunoArquivos;

public class ProvaAluno {
    private Long idAplicacao;
    private Long idTurma;
    private String ra;
    private Double nota;

    // Construtores, Getters e Setters
    public ProvaAluno() {}

    public Long getIdAplicacao() { return idAplicacao; }
    public void setIdAplicacao(Long idAplicacao) { this.idAplicacao = idAplicacao; }
    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }
    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
}
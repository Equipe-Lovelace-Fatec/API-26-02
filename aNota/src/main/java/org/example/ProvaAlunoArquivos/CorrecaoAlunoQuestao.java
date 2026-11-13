package org.example.ProvaAlunoArquivos;

import java.util.ArrayList;
import java.util.List;

// Uma questão como o aluno a vê no Gabarito e na Correção
public class CorrecaoAlunoQuestao {
    private String enunciado;
    private String respostaAluno;
    private String respostaEsperada;
    private Boolean correta;

    private int numero;
    private String tipo;                         // alt | diss | rela
    private List<String> linhasApoio = new ArrayList<>(); // alternativas / itens a relacionar
    private String comentarioProfessor;          // só questões abertas

    public CorrecaoAlunoQuestao(String enunciado, String respostaAluno, String respostaEsperada, Boolean correta) {
        this.enunciado = enunciado;
        this.respostaAluno = respostaAluno;
        this.respostaEsperada = respostaEsperada;
        this.correta = correta;
    }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getRespostaAluno() { return respostaAluno; }
    public void setRespostaAluno(String respostaAluno) { this.respostaAluno = respostaAluno; }

    public String getRespostaEsperada() { return respostaEsperada; }
    public void setRespostaEsperada(String respostaEsperada) { this.respostaEsperada = respostaEsperada; }

    public Boolean getCorreta() { return correta; }
    public void setCorreta(Boolean correta) { this.correta = correta; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public List<String> getLinhasApoio() { return linhasApoio; }
    public void setLinhasApoio(List<String> linhasApoio) { this.linhasApoio = linhasApoio; }

    public String getComentarioProfessor() { return comentarioProfessor; }
    public void setComentarioProfessor(String comentarioProfessor) { this.comentarioProfessor = comentarioProfessor; }

    public boolean ehAberta() {
        return "diss".equalsIgnoreCase(tipo);
    }
}
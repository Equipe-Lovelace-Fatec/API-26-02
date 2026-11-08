package org.example.ProvaAlunoArquivos;

public class CorrecaoAlunoQuestao {
    private String enunciado;
    private String respostaAluno;
    private String respostaEsperada;
    private Boolean correta;

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
}
package org.example.ProvaAlunoArquivos;

// Guarda a prova que o aluno escolheu na lista (SelecaoProvaAluno) para as telas de Gabarito e Correção.
public class ContextoProvaAluno {

    private static String ra;
    private static ProvaRealizada prova;

    private ContextoProvaAluno() {
    }

    public static void selecionar(String raAluno, ProvaRealizada provaEscolhida) {
        ra = raAluno;
        prova = provaEscolhida;
    }

    public static boolean temSelecao() {
        return ra != null && prova != null;
    }

    public static String getRa() {
        return ra;
    }

    public static ProvaRealizada getProva() {
        return prova;
    }

    public static void limpar() {
        ra = null;
        prova = null;
    }
}
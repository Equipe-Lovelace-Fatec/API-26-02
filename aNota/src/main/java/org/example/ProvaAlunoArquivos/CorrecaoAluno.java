package org.example.ProvaAlunoArquivos;

// Guarda qual aluno/aplicação de prova está sendo corrigida na tela Aluno.fxml.
// Quem abrir a tela (ex.: futura tela de escolha de aluno) deve chamar selecionar(...) antes.
public class CorrecaoAluno {

    private static String ra;
    private static Long idAplicacao;

    private CorrecaoAluno() {
    }

    // idAplicacao = prova_turma.id_aplicacao (a mesma prova pode ser aplicada várias vezes)
    public static void selecionar(String raAluno, long idAplicacaoProva) {
        ra = raAluno;
        idAplicacao = idAplicacaoProva;
    }

    public static boolean temSelecao() {
        return ra != null && idAplicacao != null;
    }

    public static String getRa() {
        return ra;
    }

    public static Long getIdAplicacao() {
        return idAplicacao;
    }

    public static void limpar() {
        ra = null;
        idAplicacao = null;
    }
}
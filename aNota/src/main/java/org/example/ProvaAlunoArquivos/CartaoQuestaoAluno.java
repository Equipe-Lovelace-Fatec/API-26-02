package org.example.ProvaAlunoArquivos;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// Monta o cartão de uma questão para as telas de Gabarito e de Correção do aluno
public final class CartaoQuestaoAluno {

    private static final String COR_TITULO = "#624695";
    private static final String COR_TEXTO = "#737373";

    private CartaoQuestaoAluno() {
    }

    // Gabarito: enunciado + resposta esperada pelo professor
    public static VBox paraGabarito(CorrecaoAlunoQuestao q) {
        VBox caixa = base(q);
        caixa.getChildren().add(titulo("Resposta Esperada:"));
        caixa.getChildren().add(texto(vazioSe(q.getRespostaEsperada(), "Sem resposta cadastrada")));
        fechar(caixa);
        return caixa;
    }

    // Correção: enunciado + resposta do aluno + resposta esperada + comentário (só questão aberta)
    public static VBox paraCorrecao(CorrecaoAlunoQuestao q) {
        VBox caixa = base(q);
        caixa.getChildren().add(titulo("Sua Resposta:"));
        caixa.getChildren().add(texto(vazioSe(q.getRespostaAluno(), "Não respondida")));
        caixa.getChildren().add(titulo("Resposta Esperada:"));
        caixa.getChildren().add(texto(vazioSe(q.getRespostaEsperada(), "Sem resposta cadastrada")));
        if (q.ehAberta()) {
            caixa.getChildren().add(titulo("Comentário do Professor:"));
            caixa.getChildren().add(texto(vazioSe(q.getComentarioProfessor(), "Sem comentário do professor.")));
        }
        fechar(caixa);
        return caixa;
    }

    // "Questão N" + enunciado + alternativas/itens de apoio
    private static VBox base(CorrecaoAlunoQuestao q) {
        VBox caixa = new VBox(8);
        caixa.setPadding(new Insets(15, 10, 10, 10));
        caixa.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        caixa.getChildren().add(titulo("Questão " + q.getNumero()));
        caixa.getChildren().add(titulo("Enunciado:"));
        caixa.getChildren().add(texto(q.getEnunciado()));
        for (String linha : q.getLinhasApoio()) {
            Label l = texto(linha);
            l.setFont(Font.font(14));
            caixa.getChildren().add(l);
        }
        return caixa;
    }

    private static void fechar(VBox caixa) {
        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: " + COR_TITULO + "; -fx-background-color: " + COR_TITULO + ";");
        caixa.getChildren().add(sep);
    }

    private static String vazioSe(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private static Label titulo(String t) {
        Label l = new Label(t);
        l.setWrapText(true);
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        l.setStyle("-fx-text-fill: " + COR_TITULO + ";");
        return l;
    }

    private static Label texto(String t) {
        Label l = new Label(t);
        l.setWrapText(true);
        l.setFont(Font.font(16));
        l.setStyle("-fx-text-fill: " + COR_TEXTO + ";");
        return l;
    }
}
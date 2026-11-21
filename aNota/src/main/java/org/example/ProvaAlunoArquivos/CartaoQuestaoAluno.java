package org.example.ProvaAlunoArquivos;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class CartaoQuestaoAluno {

    private static final String COR_TITULO = "#624695";
    private static final String COR_TEXTO = "#737373";

    private CartaoQuestaoAluno() {
    }

    public static VBox paraGabarito(CorrecaoAlunoQuestao q) {
        VBox caixa = base(q);
        caixa.getChildren().add(titulo("Resposta Esperada:"));
        caixa.getChildren().add(blocoTexto(vazioSe(q.getRespostaEsperada(), "Sem resposta cadastrada")));
        fechar(caixa);
        return caixa;
    }

    public static VBox paraCorrecao(CorrecaoAlunoQuestao q) {
        VBox caixa = base(q);
        caixa.getChildren().add(titulo("Sua Resposta:"));
        caixa.getChildren().add(blocoTexto(vazioSe(q.getRespostaAluno(), "Não respondida")));
        caixa.getChildren().add(titulo("Resposta Esperada:"));
        caixa.getChildren().add(blocoTexto(vazioSe(q.getRespostaEsperada(), "Sem resposta cadastrada")));
        if (q.ehAberta()) {
            caixa.getChildren().add(titulo("Comentário do Professor:"));
            caixa.getChildren().add(blocoTexto(vazioSe(q.getComentarioProfessor(), "Sem comentário do professor.")));
        }
        fechar(caixa);
        return caixa;
    }

    private static VBox base(CorrecaoAlunoQuestao q) {
        VBox caixa = new VBox(8);
        caixa.setPadding(new Insets(15, 10, 10, 10));
        caixa.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        caixa.getChildren().add(titulo("Questão " + q.getNumero()));
        caixa.getChildren().add(titulo("Enunciado:"));
        caixa.getChildren().add(blocoTexto(q.getEnunciado()));
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
        l.setMinHeight(Region.USE_PREF_SIZE);   // nunca cortar o texto com "..."
        l.setFont(Font.font(16));
        l.setStyle("-fx-text-fill: " + COR_TEXTO + ";");
        return l;
    }

    private static VBox blocoTexto(String t) {
        VBox bloco = new VBox(4);
        for (String linha : t.split("\\R", -1)) {
            bloco.getChildren().add(texto(linha.isEmpty() ? " " : linha));
        }
        return bloco;
    }
}
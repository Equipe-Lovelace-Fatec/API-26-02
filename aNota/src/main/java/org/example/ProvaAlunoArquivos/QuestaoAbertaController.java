package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.Questao;

public class QuestaoAbertaController {
    @FXML private Label EnunciadoQuestaoAberta;
    @FXML private TextArea RespostaQuestaoAberta;

    private Questao questao;

    public void setDadosQuestao(Questao q) {
        this.questao = q;
        if (EnunciadoQuestaoAberta != null) {
            EnunciadoQuestaoAberta.setWrapText(true);
            EnunciadoQuestaoAberta.setText(q.getEnunciado());
        }
    }

    // Metodo para recuperar o texto digitado pelo aluno ao finalizar
    public String getRespostaDigitada() {
        return RespostaQuestaoAberta != null ? RespostaQuestaoAberta.getText() : "";
    }

    public Long getQuestaoId() {
        return questao != null ? questao.getId() : null;
    }
}
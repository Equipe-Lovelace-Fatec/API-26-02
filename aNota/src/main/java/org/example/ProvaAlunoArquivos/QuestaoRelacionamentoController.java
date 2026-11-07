package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import org.example.Questao;

public class QuestaoRelacionamentoController {
    @FXML private Label EnunciadoQuestao;
    @FXML private Label afirmacao1, afirmacao2, afirmacao3, afirmacao4;
    @FXML private Label afirmacaoA, afirmacaoB, afirmacaoC, afirmacaoD;
    // Se você tiver ComboBoxes associados no FXML para a relação, declare-os aqui:
    // @FXML private ComboBox<String> cbRelacao1; ...

    private Questao questao;

    public void setDadosQuestao(Questao q) {
        this.questao = q;
        if (EnunciadoQuestao != null) {
            EnunciadoQuestao.setWrapText(true);
            EnunciadoQuestao.setText(q.getEnunciado());
        }
        // Aqui você preenche os labels das colunas da esquerda e direita baseados nos dados da questão/respostas
    }

    public Long getQuestaoId() {
        return questao != null ? questao.getId() : null;
    }
}
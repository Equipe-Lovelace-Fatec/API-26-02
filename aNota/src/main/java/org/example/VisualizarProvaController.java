package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class VisualizarProvaController {

    @FXML private TextField txtDisciplina;
    @FXML private TextField txtNomeProva;
    @FXML private TextField txtValor;

    @FXML private TextArea txtQuestao1, txtQuestao2, txtQuestao3, txtQuestao4;
    @FXML private TextArea txtResposta1, txtResposta2, txtResposta3, txtResposta4;

    public void setDadosProva(String nomeProva, String disciplina, String valor,
                              List<String> perguntasSelecionadas, List<String> gabaritosSelecionados) {

        if (txtNomeProva != null && nomeProva != null) txtNomeProva.setText(nomeProva);
        if (txtDisciplina != null && disciplina != null) txtDisciplina.setText(disciplina);
        if (txtValor != null && valor != null) txtValor.setText(valor);

        TextArea[] camposPerguntas = {txtQuestao1, txtQuestao2, txtQuestao3, txtQuestao4};
        TextArea[] camposRespostas = {txtResposta1, txtResposta2, txtResposta3, txtResposta4};

        for (int i = 0; i < camposPerguntas.length; i++) {
            if (i < perguntasSelecionadas.size()) {
                if (camposPerguntas[i] != null) {
                    camposPerguntas[i].setText("Pergunta " + (i + 1) + ": " + perguntasSelecionadas.get(i));
                }
                if (camposRespostas[i] != null && i < gabaritosSelecionados.size()) {
                    camposRespostas[i].setText("Gabarito: " + gabaritosSelecionados.get(i));
                }
            } else {
                if (camposPerguntas[i] != null) camposPerguntas[i].setText("");
                if (camposRespostas[i] != null) camposRespostas[i].setText("");
            }
        }
    }
}
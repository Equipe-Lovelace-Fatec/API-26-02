package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class VisualizarProvaController {

    @FXML private TextField txtDisciplina;
    @FXML private TextField txtNomeProva;
    @FXML private TextField txtValor;

    @FXML private TextArea txtQuestao1, txtQuestao2, txtQuestao3, txtQuestao4, txtQuestao5;
    @FXML private TextArea txtResposta1, txtResposta2, txtResposta3, txtResposta4, txtResposta5;

    @FXML
    public void initialize() {
        if (VisualizarProva.getNomeProva() != null || VisualizarProva.getDisciplina() != null) {
            String nome = VisualizarProva.getNomeProva() != null ? VisualizarProva.getNomeProva() : "";
            String disc = VisualizarProva.getDisciplina() != null ? VisualizarProva.getDisciplina() : "";
            String valor = VisualizarProva.getValorTotal() != null ? VisualizarProva.getValorTotal().toString() : "";

            List<String> perguntas = new ArrayList<>();
            List<String> gabaritos = new ArrayList<>();

            if (VisualizarProva.getQuestoesSelecionadas() != null) {
                for (VisualizarProva.QuestaoVisualizacao q : VisualizarProva.getQuestoesSelecionadas()) {
                    perguntas.add(q.getEnunciado()); // Apenas o enunciado limpo
                    gabaritos.add(q.getGabarito());   // O gabarito buscado do banco
                }
            }

            setDadosProva(nome, disc, valor, perguntas, gabaritos);
        }
    }

    public void setDadosProva(String nomeProva, String disciplina, String valor,
                              List<String> perguntasSelecionadas, List<String> valoresSelecionados) {

        if (txtNomeProva != null && nomeProva != null) txtNomeProva.setText(nomeProva);
        if (txtDisciplina != null && disciplina != null) txtDisciplina.setText(disciplina);
        if (txtValor != null && valor != null) txtValor.setText(valor);

        TextArea[] camposPerguntas = {txtQuestao1, txtQuestao2, txtQuestao3, txtQuestao4, txtQuestao5};
        TextArea[] camposRespostas = {txtResposta1, txtResposta2, txtResposta3, txtResposta4, txtResposta5};

        for (int i = 0; i < camposPerguntas.length; i++) {
            if (i < perguntasSelecionadas.size()) {
                if (camposPerguntas[i] != null) {
                    camposPerguntas[i].setText("Questão " + (i + 1) + ": " + perguntasSelecionadas.get(i));
                }
                if (camposRespostas[i] != null && i < valoresSelecionados.size()) {
                    camposRespostas[i].setText("Resposta: " + valoresSelecionados.get(i));
                }
            } else {
                if (camposPerguntas[i] != null) camposPerguntas[i].setText("");
                if (camposRespostas[i] != null) camposRespostas[i].setText("");
            }
        }
    }
}

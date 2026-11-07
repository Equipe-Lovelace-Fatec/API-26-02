package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.example.Questao;
import org.example.Resposta; // Supondo que exista uma classe Resposta no projeto

import java.util.List;

public class QuestaoAlternativaController {
    @FXML private Label EnunciadoQuestao1;
    @FXML private ToggleGroup grupoQuestao1;
    @FXML private RadioButton rbopcaoA;
    @FXML private RadioButton rbopcaoB;
    @FXML private RadioButton rbopcaoC;
    @FXML private RadioButton rbopcaoD;

    private Questao questao;
    private List<Resposta> respostas;

    public void setDadosQuestao(Questao q, List<Resposta> listaRespostas) {
        this.questao = q;
        this.respostas = listaRespostas;

        if (EnunciadoQuestao1 != null) {
            EnunciadoQuestao1.setWrapText(true);
            EnunciadoQuestao1.setText(q.getEnunciado());
        }

        // Atribuir textos e dados às alternativas (A, B, C, D) baseados na tabela resposta
        RadioButton[] rbs = {rbopcaoA, rbopcaoB, rbopcaoC, rbopcaoD};
        for (int i = 0; i < rbs.length && i < listaRespostas.size(); i++) {
            if (rbs[i] != null) {
                rbs[i].setText(listaRespostas.get(i).getTexto());
                // Guardar o objeto Resposta dentro do userData do RadioButton para recuperar fácil depois
                rbs[i].setUserData(listaRespostas.get(i));
            }
        }
    }

    // Retorna a Resposta selecionada pelo aluno
    public Resposta getRespostaSelecionada() {
        RadioButton[] rbs = {rbopcaoA, rbopcaoB, rbopcaoC, rbopcaoD};
        for (RadioButton rb : rbs) {
            if (rb != null && rb.isSelected() && rb.getUserData() instanceof Resposta) {
                return (Resposta) rb.getUserData();
            }
        }
        return null;
    }

    public Long getQuestaoId() {
        return questao != null ? questao.getId() : null;
    }
}
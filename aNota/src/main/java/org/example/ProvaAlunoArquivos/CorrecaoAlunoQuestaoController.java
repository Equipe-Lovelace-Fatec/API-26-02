package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CorrecaoAlunoQuestaoController {

    @FXML
    private Label label_enunciado_prova_gabarito;

    @FXML
    private Label label_resposta_aluno_gabarito;

    @FXML
    private Label label_resposta_esperada_prova_gabarito;

    /**
     * Preenche os labels com os dados vindos do banco de dados
     */
    public void setDadosGabarito(CorrecaoAlunoQuestao questaoGabarito) {
        if (label_enunciado_prova_gabarito != null) {
            label_enunciado_prova_gabarito.setWrapText(true);
            label_enunciado_prova_gabarito.setText("Enunciado: " + questaoGabarito.getEnunciado());
        }

        if (label_resposta_aluno_gabarito != null) {
            label_resposta_aluno_gabarito.setWrapText(true);
            String respAluno = questaoGabarito.getRespostaAluno();
            label_resposta_aluno_gabarito.setText("Sua Resposta: " + (respAluno != null && !respAluno.isEmpty() ? respAluno : "Não respondida"));
        }

        if (label_resposta_esperada_prova_gabarito != null) {
            label_resposta_esperada_prova_gabarito.setWrapText(true);
            label_resposta_esperada_prova_gabarito.setText("Resposta Esperada: " + questaoGabarito.getRespostaEsperada());
        }
    }
}
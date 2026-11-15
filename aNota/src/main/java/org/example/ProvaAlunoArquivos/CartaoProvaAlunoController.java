package org.example.ProvaAlunoArquivos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Consumer;

// Cartão de uma prova realizada: título, disciplina, nota e os botões Gabarito e Correção.
public class CartaoProvaAlunoController {

    @FXML private Button bt_gabarito;
    @FXML private Button bt_correcao;
    @FXML private Label lbl_disciplina;
    @FXML private Label lbl_titulo;
    @FXML private Label lbl_valor;   // nota do aluno

    private ProvaRealizada prova;
    private Consumer<ProvaRealizada> aoAbrirGabarito;
    private Consumer<ProvaRealizada> aoAbrirCorrecao;

    public void setProva(ProvaRealizada prova, Consumer<ProvaRealizada> aoAbrirGabarito,
                         Consumer<ProvaRealizada> aoAbrirCorrecao) {
        this.prova = prova;
        this.aoAbrirGabarito = aoAbrirGabarito;
        this.aoAbrirCorrecao = aoAbrirCorrecao;

        lbl_titulo.setText(prova.getNomeProva());
        lbl_disciplina.setText(prova.getNomeDisciplina());
        lbl_valor.setText(prova.getNotaExibicao());
    }

    @FXML
    void gabarito(ActionEvent event) {
        if (aoAbrirGabarito != null) {
            aoAbrirGabarito.accept(prova);
        }
    }

    @FXML
    void correcao(ActionEvent event) {
        if (aoAbrirCorrecao != null) {
            aoAbrirCorrecao.accept(prova);
        }
    }
}
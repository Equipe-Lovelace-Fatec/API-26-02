package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Consumer;

public class CartaoProvaController {

    @FXML private Label lbl_titulo;
    @FXML private Label lbl_disciplina;
    @FXML private Label lbl_dificuldade;
    @FXML private Label lbl_qtd;
    @FXML private Label lbl_valor;
    @FXML private Button bt_visualizar;
    @FXML private Button bt_agendar;

    private Prova prova;
    private Consumer<Prova> aoVisualizar;
    private Consumer<Prova> aoAgendar;

    public void setProva(Prova prova, Consumer<Prova> aoVisualizar, Consumer<Prova> aoAgendar) {
        this.prova = prova;
        this.aoVisualizar = aoVisualizar;
        this.aoAgendar = aoAgendar;

        lbl_titulo.setText(prova.getNomeProva());
        lbl_disciplina.setText(prova.getNomeDisciplina());
        lbl_dificuldade.setText(prova.getDificuldadeExibicao());
        lbl_qtd.setText(String.valueOf(prova.getQtdQuestoes()));
        lbl_valor.setText(prova.getValorExibicao());
    }

    @FXML
    void visualizar(ActionEvent event) {
        if (aoVisualizar != null) {
            aoVisualizar.accept(prova);
        }
    }

    @FXML
    void agendar(ActionEvent event) {
        if (aoAgendar != null) {
            aoAgendar.accept(prova);
        }
    }
}

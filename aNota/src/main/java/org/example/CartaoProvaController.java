package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.function.Consumer;

public class CartaoProvaController {

    @FXML
    private Label lbl_titulo;
    @FXML
    private Label lbl_disciplina;
    @FXML
    private Label lbl_dificuldade;
    @FXML
    private Label lbl_qtd;
    @FXML
    private Label lbl_valor;
    @FXML
    private Button bt_visualizar;
    @FXML
    private Button bt_agendar;
    @FXML
    private Button bt_editar;

    private Prova prova;
    private Consumer<Prova> aoVisualizar;
    private Consumer<Prova> aoAgendar;
    private Consumer<Prova> aoEditar;

    public void setProva(Prova prova, Consumer<Prova> aoVisualizar, Consumer<Prova> aoAgendar) {
        setProva(prova, aoVisualizar, aoAgendar, null);
    }

    public void setProva(Prova prova, Consumer<Prova> aoVisualizar, Consumer<Prova> aoAgendar,
                         Consumer<Prova> aoEditar) {
        this.prova = prova;
        this.aoVisualizar = aoVisualizar;
        this.aoAgendar = aoAgendar;
        this.aoEditar = aoEditar;

        lbl_titulo.setText(prova.getNomeProva());
        lbl_disciplina.setText(prova.getNomeDisciplina());
        lbl_dificuldade.setText(prova.getDificuldadeExibicao());
        lbl_qtd.setText(String.valueOf(prova.getQtdQuestoes()));
        lbl_valor.setText(prova.getValorExibicao());

        if (!prova.temNota()) {
            marcarComoRascunho();
        }
    }

    // Rascunho: sinaliza no cartão e impede o agendamento
    private void marcarComoRascunho() {
        lbl_valor.setText("RASCUNHO");
        lbl_valor.setStyle("-fx-text-alignment: center; -fx-text-fill: #b45309;"
                + " -fx-background-color: #fef3c7; -fx-background-radius: 8;"
                + " -fx-border-color: #b45309; -fx-border-radius: 8;");
        bt_agendar.setDisable(true);
        bt_agendar.setStyle(bt_agendar.getStyle() + " -fx-opacity: 0.45;");
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

    @FXML
    void editar(ActionEvent event) {
        if (aoEditar != null) {
            aoEditar.accept(prova);
        }
    }
}
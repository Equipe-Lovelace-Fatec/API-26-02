package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;

// Cartão de uma prova na lista da "Minha agenda": título, data e hora. Clicar no cartão o seleciona.
public class CartaoProvaAgendaController {

    // Borda e cantos do FXML do cartão; só o fundo muda (cinza quando selecionado)
    private static final String ESTILO_CARTAO = "-fx-border-color: #624695; -fx-border-width: 3; "
            + "-fx-background-radius: 6; -fx-border-radius: 6; -fx-background-color: ";

    @FXML private Pane pane_individual_provas1;
    @FXML private Label label_prova_de_disciplina;
    @FXML private Label label_data_dd_mm_aaaa;
    @FXML private Label label_hora_hh_mm;

    private ProvaAgendada prova;
    private Consumer<CartaoProvaAgendaController> aoSelecionar;

    public void setProva(ProvaAgendada prova, Consumer<CartaoProvaAgendaController> aoSelecionar) {
        this.prova = prova;
        this.aoSelecionar = aoSelecionar;

        label_prova_de_disciplina.setText("Prova de " + prova.getNomeDisciplina() + " — " + prova.getNomeProva());
        label_data_dd_mm_aaaa.setText(prova.getDataExibicao());
        label_hora_hh_mm.setText(prova.getHorarioExibicao());
    }

    public ProvaAgendada getProva() {
        return prova;
    }

    public void setSelecionado(boolean selecionado) {
        pane_individual_provas1.setStyle(ESTILO_CARTAO + (selecionado ? "#dadada;" : "#ffffff;"));
    }

    @FXML
    void selecionar(MouseEvent event) {
        aoSelecionar.accept(this);
    }
}

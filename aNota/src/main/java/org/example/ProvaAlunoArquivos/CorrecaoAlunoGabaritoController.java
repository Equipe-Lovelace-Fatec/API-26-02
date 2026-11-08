package org.example.ProvaAlunoArquivos;
import org.example.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class CorrecaoAlunoGabaritoController {

    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Button bt_minha_agenda;
    @FXML private Label titulo_gabarito_prova;
    @FXML private VBox vboxContainerQuestoes;


    public void inicializarGabarito(Long idAplicacao, String raAluno, String nomeProva) {
        if (titulo_gabarito_prova != null) {
            titulo_gabarito_prova.setText(nomeProva != null ? "Gabarito: " + nomeProva : "Gabarito da Avaliação");
        }

        carregarQuestoesGabaritoDoBanco(idAplicacao, raAluno);
    }

    private void carregarQuestoesGabaritoDoBanco(Long idAplicacao, String raAluno) {
        try {
            if (vboxContainerQuestoes != null) {
                vboxContainerQuestoes.getChildren().clear();
            }


        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar o gabarito: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    void ir_avaliacao(ActionEvent event) {
        try {
            App.setRoot("realizar_prova_aluno");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de avaliação: " + e.getMessage());
        }
    }

    @FXML
    void ir_correcao(ActionEvent event) {
        try {
            App.setRoot("gabarito_para_aluno");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de correção/gabarito: " + e.getMessage());
        }
    }

    @FXML
    void ir_desempenho(ActionEvent event) {

    }

    @FXML
    void ir_minha_agenda(ActionEvent event) {

    }
}
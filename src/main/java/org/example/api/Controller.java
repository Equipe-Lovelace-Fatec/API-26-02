package org.example.api;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.example.api.MontagemProvaController;

import java.io.IOException;

public class Controller {

    private static int proximaPosicao = 0;

    @FXML
    private Button btMultipla;
    @FXML
    private Button btRelac;
    @FXML
    private Button btSalvar;
    @FXML
    private Button btAberta;
    @FXML
    private TextArea txtPergunta;
    @FXML
    private TextArea txtGabarito;
    @FXML
    private TextArea txtAlternativa1;
    @FXML
    private TextArea txtAlternativa2;
    @FXML
    private TextArea txtAlternativa3;
    @FXML
    private TextArea txtAlternativa4;
    @FXML
    private CheckBox checkAlternativa1;
    @FXML
    private CheckBox checkAlternativa2;
    @FXML
    private CheckBox checkAlternativa3;
    @FXML
    private CheckBox checkAlternativa4;
    @FXML
    private TextArea txtConceito1;
    @FXML
    private TextArea txtConceito2;
    @FXML
    private TextArea txtConceito3;
    @FXML
    private TextArea txtConceito4;
    @FXML
    private ComboBox<String> comboRelacao1;
    @FXML
    private ComboBox<String> comboRelacao2;
    @FXML
    private ComboBox<String> comboRelacao3;
    @FXML
    private ComboBox<String> comboRelacao4;
    @FXML
    public void abrirMultipla(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/api/CadastroQuestoesAlt.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void abrirRelac(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/api/CadastroQuestoesRelac.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void abrirAberta(ActionEvent event) throws IOException {

        Parent root = FXMLLoader.load(
                getClass().getResource("/org/example/api/CadastroQuestoes.fxml")
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void initialize() {

        if (comboRelacao1 != null) {
            comboRelacao1.getItems().addAll(
                    "Alternativa 1",
                    "Alternativa 2",
                    "Alternativa 3",
                    "Alternativa 4"
            );
        }

        if (comboRelacao2 != null) {
            comboRelacao2.getItems().addAll(
                    "Alternativa 1",
                    "Alternativa 2",
                    "Alternativa 3",
                    "Alternativa 4"
            );
        }

        if (comboRelacao3 != null) {
            comboRelacao3.getItems().addAll(
                    "Alternativa 1",
                    "Alternativa 2",
                    "Alternativa 3",
                    "Alternativa 4"
            );
        }

        if (comboRelacao4 != null) {
            comboRelacao4.getItems().addAll(
                    "Alternativa 1",
                    "Alternativa 2",
                    "Alternativa 3",
                    "Alternativa 4"
            );
        }
    }

    @FXML
    public void salvarQuestao(ActionEvent event) {

        if (txtGabarito != null) {
            salvarQuestaoAberta();

        } else if (txtAlternativa1 != null) {
            salvarQuestaoMultipla();

        } else if (txtConceito1 != null) {
            salvarQuestaoRelacao();
        }
    }

    private void salvarQuestaoAberta() {

        String pergunta = txtPergunta.getText();
        String gabarito = txtGabarito.getText();

        adicionarQuestaoNaMontagem(pergunta, gabarito);
    }

    private void salvarQuestaoMultipla() {

        String pergunta = txtPergunta.getText();

        String alternativa1 = txtAlternativa1.getText();
        String alternativa2 = txtAlternativa2.getText();
        String alternativa3 = txtAlternativa3.getText();
        String alternativa4 = txtAlternativa4.getText();

        String gabarito = "";

        if (checkAlternativa1.isSelected()) {
            gabarito = alternativa1;
        } else if (checkAlternativa2.isSelected()) {
            gabarito = alternativa2;
        } else if (checkAlternativa3.isSelected()) {
            gabarito = alternativa3;
        } else if (checkAlternativa4.isSelected()) {
            gabarito = alternativa4;
        }

        adicionarQuestaoNaMontagem(pergunta, gabarito);
    }

    private void salvarQuestaoRelacao() {

        String pergunta = txtPergunta.getText();

        String conceito1 = txtConceito1.getText();
        String conceito2 = txtConceito2.getText();
        String conceito3 = txtConceito3.getText();
        String conceito4 = txtConceito4.getText();

        String gabarito =
                conceito1 + " → " + comboRelacao1.getValue() + "\n"
                        + conceito2 + " → " + comboRelacao2.getValue() + "\n"
                        + conceito3 + " → " + comboRelacao3.getValue() + "\n"
                        + conceito4 + " → " + comboRelacao4.getValue();

        adicionarQuestaoNaMontagem(pergunta, gabarito);
    }

    private void adicionarQuestaoNaMontagem(String pergunta, String gabarito) {

        if (proximaPosicao < 10) {

            MontagemProvaController.perguntas.set(
                    proximaPosicao,
                    pergunta
            );

            MontagemProvaController.gabaritos.set(
                    proximaPosicao,
                    gabarito
            );

            proximaPosicao++;

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Questão salva");
            alerta.setHeaderText(null);
            alerta.setContentText("Questão salva com sucesso!");
            alerta.showAndWait();
        }
    }
}
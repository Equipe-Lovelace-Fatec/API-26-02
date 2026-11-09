package org.example.ProvaAlunoArquivos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.ProvaAlunoDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Tela de Gabarito: enunciado e resposta esperada pelo professor, de cada questão da prova realizada.
public class CorrecaoAlunoGabaritoController {

    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Button bt_minha_agenda;
    @FXML private Label titulo_gabarito_prova;
    @FXML private VBox vboxContainerQuestoes;

    private final ProvaAlunoDAO dao = new ProvaAlunoDAO();

    @FXML
    private void initialize() {
        vboxContainerQuestoes.getChildren().clear();

        if (!ContextoProvaAluno.temSelecao()) {
            titulo_gabarito_prova.setText("Gabarito da Avaliação");
            vboxContainerQuestoes.getChildren().add(mensagem("Escolha uma prova em Correção para ver o gabarito."));
            return;
        }

        ProvaRealizada prova = ContextoProvaAluno.getProva();
        titulo_gabarito_prova.setText("Gabarito: " + prova.getNomeProva());

        try {
            List<CorrecaoAlunoQuestao> questoes = dao.carregarQuestoes(ContextoProvaAluno.getRa(), prova, false);
            if (questoes.isEmpty()) {
                vboxContainerQuestoes.getChildren().add(mensagem("Esta prova não possui questões."));
                return;
            }
            for (CorrecaoAlunoQuestao q : questoes) {
                vboxContainerQuestoes.getChildren().add(CartaoQuestaoAluno.paraGabarito(q));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar o gabarito: " + e.getMessage());
        }
    }

    private Label mensagem(String texto) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 18px; -fx-text-fill: #737373;");
        return l;
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
        abrir("realizar_prova_aluno", "a tela de avaliação");
    }

    @FXML
    void ir_correcao(ActionEvent event) {
        abrir("SelecaoProvaAluno", "a lista de provas");
    }

    @FXML
    void ir_desempenho(ActionEvent event) {

    }

    @FXML
    void ir_minha_agenda(ActionEvent event) {

    }

    private void abrir(String fxml, String descricao) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir " + descricao + ": " + e.getMessage());
        }
    }
}
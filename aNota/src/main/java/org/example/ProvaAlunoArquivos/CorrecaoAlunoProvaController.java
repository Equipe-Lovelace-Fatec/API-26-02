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
import java.time.format.DateTimeFormatter;
import java.util.List;

// Tela de Correção: enunciado, resposta do aluno, resposta esperada e comentário do professor (questões abertas).
public class CorrecaoAlunoProvaController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Button bt_minha_agenda;
    @FXML private Label titulo_correcao_prova;
    @FXML private Label lbl_disciplina_correcao;
    @FXML private Label lbl_professor_correcao;
    @FXML private Label lbl_data_correcao;
    @FXML private VBox vboxContainerQuestoes;

    private final ProvaAlunoDAO dao = new ProvaAlunoDAO();

    @FXML
    private void initialize() {
        vboxContainerQuestoes.getChildren().clear();

        if (!ContextoProvaAluno.temSelecao()) {
            titulo_correcao_prova.setText("Correção da Avaliação");
            lbl_disciplina_correcao.setText("Disciplina: -");
            lbl_professor_correcao.setText("Professor: -");
            lbl_data_correcao.setText("Data: -");
            vboxContainerQuestoes.getChildren().add(mensagem("Escolha uma prova em Correção para ver a correção."));
            return;
        }

        ProvaRealizada prova = ContextoProvaAluno.getProva();
        titulo_correcao_prova.setText(prova.getNomeProva());
        lbl_disciplina_correcao.setText("Disciplina: " + valor(prova.getNomeDisciplina()));
        lbl_professor_correcao.setText("Professor: " + valor(prova.getNomeProfessor()));
        lbl_data_correcao.setText("Data: " + (prova.getData() != null ? prova.getData().format(FORMATO_DATA) : "-"));

        try {
            List<CorrecaoAlunoQuestao> questoes = dao.carregarQuestoes(ContextoProvaAluno.getRa(), prova, true);
            if (questoes.isEmpty()) {
                vboxContainerQuestoes.getChildren().add(mensagem("Esta prova não possui questões."));
                return;
            }
            for (CorrecaoAlunoQuestao q : questoes) {
                vboxContainerQuestoes.getChildren().add(CartaoQuestaoAluno.paraCorrecao(q));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar a correção: " + e.getMessage());
        }
    }

    private String valor(String s) {
        return s == null || s.isBlank() ? "-" : s;
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
        abrir("desempenho_aluno", "a tela de desempenho");
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
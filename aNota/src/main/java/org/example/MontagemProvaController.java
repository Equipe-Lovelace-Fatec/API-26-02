package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MontagemProvaController {

    public static List<String> perguntas = new ArrayList<>(List.of(
            "Qual é o conceito de encapsulamento em POO?",
            "Qual a diferença entre ArrayList e Array?",
            "Qual palavra-chave em Java é usada para impedir que uma classe seja herdada ou um método seja sobrescrito?" +
                    "\nA) static      B) final      C) abstract      D) public",
            "O que faz o Wrap Text no TextArea?",
            "O que é uma classe e o que é um objeto em POO?",
            "Para que serve a palavra-chave public em Java?",
            "O que acontece se você tentar acessar um índice de Array que não existe?",
            "Associe o pilar da Orientação a Objetos à sua definição" +
                    "\nConceitos: A) Encapsulamento  B) Polimorfismo      C) Abstração\n" +
                    "\n" +
                    "Definições: 1. Capacidade de um método assumir diferentes formas de execução.\n" +
                    "\n" +
                    "2. Esconder detalhes internos e proteger o acesso direto aos dados.\n" +
                    "\n" +
                    "3. Focar apenas nos elementos essenciais de um objeto, ignorando detalhes complexos.\n",
            "Qual é a função do comando System.out.println()?"
    ));

    public static List<String> gabaritos = new ArrayList<>(List.of(
            "Esconder os detalhes internos de uma classe e restringir o acesso.",
            "Array tem tamanho fixo, ArrayList é dinâmico.",
            "B) final",
            "Quebra o texto para a linha de baixo ao atingir a borda.",
            "Classe é o molde/modelo; objeto é a instância dessa classe.",
            "Define visibilidade pública, permitindo acesso de qualquer classe.",
            "O Java lança a exceção ArrayIndexOutOfBoundsException.",
            "A2, B1, C3",
            "Imprime uma mensagem no console e pula para a próxima linha."
    ));

    // Guarda os dados preenchidos no cabeçalho da prova
    public static String nomeProvaSalvo = "";
    public static String disciplinaSalva = "";
    public static String valorProvaSalvo = "";

    @FXML private ComboBox<String> boxDisciplina;
    @FXML private Button btnAddQuestao;
    @FXML private Button btnAlunos;
    @FXML private Button btnQuestoes;
    @FXML private Button btnSalvarProva;
    @FXML private Button btnVerProva;

    @FXML private CheckBox checkbox1, checkbox2, checkbox3, checkbox4, checkbox5;
    @FXML private CheckBox checkbox6, checkbox7, checkbox8, checkbox9, checkbox10;

    @FXML private TextField txtNomeProva, txtValorProva;
    @FXML private TextField valor_questao1, valor_questao2, valor_questao3, valor_questao4, valor_questao5;
    @FXML private TextField valor_questao6, valor_questao7, valor_questao8, valor_questao9, valor_questao10;

    @FXML
    public void initialize() {
        if (boxDisciplina != null) {
            boxDisciplina.getItems().addAll("AGE002", "IMB003", "IES001", "AGR001", "ILP008", "MCA001");
        }

        CheckBox[] checkBoxes = {checkbox1, checkbox2, checkbox3, checkbox4, checkbox5,
                checkbox6, checkbox7, checkbox8, checkbox9, checkbox10};

        for (int i = 0; i < perguntas.size() && i < checkBoxes.length; i++) {
            checkBoxes[i].setText(perguntas.get(i));
        }
    }

    @FXML
    void AddQuestao(ActionEvent event) {
        // Salva as informações dos cabeçalhos nos campos estáticos
        nomeProvaSalvo = txtNomeProva.getText();
        disciplinaSalva = boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "";
        valorProvaSalvo = txtValorProva.getText();

        // Exibe o alerta de questão adicionada
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText("Questão(ões) adicionada(s) com sucesso!");
        alert.showAndWait();
    }

    @FXML
    void VerProva(ActionEvent event) {
        try {
            System.out.println(">>> 1. Iniciando clique do VerProva...");

            CheckBox[] checkBoxes = {checkbox1, checkbox2, checkbox3, checkbox4, checkbox5,
                    checkbox6, checkbox7, checkbox8, checkbox9, checkbox10};

            List<String> perguntasSelecionadas = new ArrayList<>();
            List<String> gabaritosSelecionados = new ArrayList<>();

            for (int i = 0; i < checkBoxes.length && i < perguntas.size(); i++) {
                if (checkBoxes[i] != null && checkBoxes[i].isSelected()) {
                    perguntasSelecionadas.add(perguntas.get(i));
                    if (i < gabaritos.size()) {
                        gabaritosSelecionados.add(gabaritos.get(i));
                    } else {
                        gabaritosSelecionados.add("Gabarito não cadastrado.");
                    }
                }
            }

            String nomeEnviar = !nomeProvaSalvo.isEmpty() ? nomeProvaSalvo : (txtNomeProva != null ? txtNomeProva.getText() : "");
            String disciplinaEnviar = !disciplinaSalva.isEmpty() ? disciplinaSalva : (boxDisciplina != null && boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "");
            String valorEnviar = !valorProvaSalvo.isEmpty() ? valorProvaSalvo : (txtValorProva != null ? txtValorProva.getText() : "");

            System.out.println(">>> 2. Carregando o FXML visualizacao.fxml...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/visualizacao.fxml"));
            Parent root = loader.load();

            System.out.println(">>> 3. FXML carregado com sucesso! Obtendo controller...");
            VisualizarProvaController controller = loader.getController();
            if (controller != null) {
                controller.setDadosProva(
                        nomeEnviar,
                        disciplinaEnviar,
                        valorEnviar,
                        perguntasSelecionadas,
                        gabaritosSelecionados
                );
            }

            System.out.println(">>> 4. Exibindo Stage...");
            Stage popUpStage = new Stage();
            popUpStage.setTitle("Visualização da Prova");
            popUpStage.setScene(new Scene(root));

            // Associação do dono da janela de forma segura
            if (event != null && event.getSource() instanceof Node) {
                Scene sceneAtual = ((Node) event.getSource()).getScene();
                if (sceneAtual != null && sceneAtual.getWindow() != null) {
                    popUpStage.initOwner(sceneAtual.getWindow());
                    popUpStage.initModality(Modality.WINDOW_MODAL);
                }
            }

            popUpStage.showAndWait();
            System.out.println(">>> 5. Janela exibida!");

        } catch (Throwable t) {
            System.err.println("=== ERRO ENCONTRADO AO ABRIR O POPUP ===");
            t.printStackTrace();
        }
    }

    @FXML
    void SalvarProva(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText("Prova salva com sucesso!");
        alert.showAndWait();
    }
    @FXML void irAlunos(ActionEvent event) throws IOException {
            App.setRoot("Aluno");
        }

    @FXML void irQuestao(ActionEvent event) {}
}
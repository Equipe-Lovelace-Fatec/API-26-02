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

import java.util.ArrayList;
import java.util.List;

public class MontagemProvaController {

    public static List<String> perguntas = new ArrayList<>(List.of(
            "Qual é o conceito de encapsulamento em POO?",
            "Qual a diferença entre ArrayList e Array?",
            "Para que serve o método initialize() no JavaFX?",
            "O que faz o Wrap Text no TextArea?",
            "O que é uma classe e o que é um objeto em POO?",
            "Para que serve a palavra-chave public em Java?",
            "Qual é a diferença entre os tipos int e double?",
            "O que acontece se você tentar acessar um índice de Array que não existe?",
            "Para que serve um Button no JavaFX?",
            "Qual é a função do comando System.out.println()?"
    ));

    public static List<String> gabaritos = new ArrayList<>(List.of(
            "Esconder os detalhes internos de uma classe e restringir o acesso.",
            "Array tem tamanho fixo, ArrayList é dinâmico.",
            "Executa automaticamente ao carregar o arquivo FXML.",
            "Quebra o texto para a linha de baixo ao atingir a borda.",
            "Classe é o molde/modelo; objeto é a instância dessa classe.",
            "Define visibilidade pública, permitindo acesso de qualquer classe.",
            "int armazena números inteiros; double armazena números com casas decimais.",
            "O Java lança a exceção ArrayIndexOutOfBoundsException.",
            "Representa um botão interativo na interface gráfica.",
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
        // Apenas salva as informações dos cabeçalhos nos campos estáticos sem abrir caixas de diálogo
        nomeProvaSalvo = txtNomeProva.getText();
        disciplinaSalva = boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "";
        valorProvaSalvo = txtValorProva.getText();
    }

    @FXML
    void VerProva(ActionEvent event) throws Exception {
        CheckBox[] checkBoxes = {checkbox1, checkbox2, checkbox3, checkbox4, checkbox5,
                checkbox6, checkbox7, checkbox8, checkbox9, checkbox10};

        List<String> perguntasSelecionadas = new ArrayList<>();
        List<String> gabaritosSelecionados = new ArrayList<>();

        // Filtra apenas as questões marcadas nos checkboxes
        for (int i = 0; i < checkBoxes.length && i < perguntas.size(); i++) {
            if (checkBoxes[i].isSelected()) {
                perguntasSelecionadas.add(perguntas.get(i));

                if (i < gabaritos.size()) {
                    gabaritosSelecionados.add(gabaritos.get(i));
                } else {
                    gabaritosSelecionados.add("Gabarito não cadastrado.");
                }
            }
        }

        // Usa os valores armazenados se existirem, caso contrário pega o texto atual
        String nomeEnviar = !nomeProvaSalvo.isEmpty() ? nomeProvaSalvo : txtNomeProva.getText();
        String disciplinaEnviar = !disciplinaSalva.isEmpty() ? disciplinaSalva : (boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "");
        String valorEnviar = !valorProvaSalvo.isEmpty() ? valorProvaSalvo : txtValorProva.getText();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("visualizacao.fxml"));
        Parent root = loader.load();

        VisualizarProvaController controller = loader.getController();
        controller.setDadosProva(
                nomeEnviar,
                disciplinaEnviar,
                valorEnviar,
                perguntasSelecionadas,
                gabaritosSelecionados
        );

        Stage popUpStage = new Stage();
        popUpStage.setScene(new Scene(root));

        Stage janelaPrincipal = (Stage) ((Node) event.getSource()).getScene().getWindow();
        popUpStage.initOwner(janelaPrincipal);
        popUpStage.initModality(Modality.WINDOW_MODAL);

        popUpStage.showAndWait();
    }

    @FXML void SalvarProva(ActionEvent event) {}
    @FXML void irAlunos(ActionEvent event) {}
    @FXML void irQuestao(ActionEvent event) {}
}
package org.example;

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
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class VisualizarQuestaoController {

    // Componentes da tela
    @FXML private ComboBox<String> box_filtro_dificuldade;
    @FXML private ComboBox<String> box_filtro_disciplina;
    @FXML private ComboBox<String> box_filtro_ultimo_uso;

    @FXML private MenuItem btProva_agendar;
    @FXML private MenuItem btProva_montar;
    @FXML private MenuItem btalunos_corrigir;
    @FXML private MenuItem btalunos_notas;

    @FXML private Button btnAddQuestao;
    @FXML private MenuButton btnAlunos;
    @FXML private MenuButton btnProva;
    @FXML private MenuButton btnQuestoes;
    @FXML private Button btnSalvarProva;
    @FXML private Button btnVerProva;

    @FXML private MenuItem btquestoes_consultar;
    @FXML private MenuItem btquestoes_criar;

    @FXML private CheckBox checkbox1;
    @FXML private CheckBox checkbox11;
    @FXML private CheckBox checkbox111;
    @FXML private CheckBox checkbox112;
    @FXML private CheckBox checkbox113;
    @FXML private CheckBox checkbox114;
    @FXML private CheckBox checkbox115;
    @FXML private CheckBox checkbox116;
    @FXML private CheckBox checkbox117;
    @FXML private CheckBox checkbox118;
    @FXML private CheckBox checkbox119;

    @FXML private Label txtValorProva;

    @FXML private TextField valor_questao117;
    @FXML private TextField valor_questao118;
    @FXML private TextField valor_questao119;

    // Acesso ao banco de dados
    private final QuestaoDAO questaoDAO = new QuestaoDAO();
    private List<Questao> todasAsQuestoes;
    private List<Questao> questoesCarregadas;

    // Retorna os checkboxes na mesma ordem das linhas da tela
    private CheckBox[] obterCheckBoxes() {
        return new CheckBox[]{
                checkbox1,
                checkbox11,
                checkbox111,
                checkbox112,
                checkbox113,
                checkbox114,
                checkbox115,
                checkbox116,
                checkbox117,
                checkbox118,
                checkbox119
        };
    }

    // Executado quando a tela abre
    @FXML
    public void initialize() {
        carregarQuestoes();

        box_filtro_disciplina.valueProperty().addListener(
                (observable, valorAntigo, valorNovo) -> aplicarFiltros()
        );

        box_filtro_dificuldade.valueProperty().addListener(
                (observable, valorAntigo, valorNovo) -> aplicarFiltros()
        );

        box_filtro_ultimo_uso.valueProperty().addListener(
                (observable, valorAntigo, valorNovo) -> aplicarFiltros()
        );
    }

    // Carrega as questões do banco
    private void carregarQuestoes() {
        try {
            todasAsQuestoes = questaoDAO.buscarQuestoes();

            carregarOpcoesFiltros();
            aplicarFiltros();

            System.out.println(
                    "Questões carregadas: " + todasAsQuestoes.size()
            );

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao carregar questões",
                    "Não foi possível carregar as questões: "
                            + e.getMessage()
            );
        }
    }

    // Carrega as opções dos filtros
    private void carregarOpcoesFiltros() {
        if (todasAsQuestoes == null) {
            return;
        }

        // Disciplinas dinâmicas, com opção em branco
        List<String> disciplinas = todasAsQuestoes.stream()
                .map(Questao::getNomeDisciplina)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        disciplinas.add(0, "");

        box_filtro_disciplina.setItems(
                FXCollections.observableArrayList(disciplinas)
        );

        // Dificuldades fixas, com opção em branco
        ObservableList<String> dificuldades =
                FXCollections.observableArrayList(
                        "", "Fácil", "Média", "Difícil"
                );

        box_filtro_dificuldade.setItems(dificuldades);

        // Últimos usos fixos, com opção em branco
        ObservableList<String> ultimosUsos =
                FXCollections.observableArrayList(
                        "", "6 meses", "1 ano", "2 anos", "3 anos ou mais"
                );

        box_filtro_ultimo_uso.setItems(ultimosUsos);
    }

    private void atualizarExibicaoQuestoes() {
        CheckBox[] checkBoxes = obterCheckBoxes();

        for (int i = 0; i < checkBoxes.length; i++) {
            CheckBox checkBox = checkBoxes[i];

            if (checkBox == null) {
                continue;
            }

            if (i < questoesCarregadas.size()) {
                Questao questao = questoesCarregadas.get(i);

                checkBox.setText(questao.getEnunciado());
                checkBox.setVisible(true);
                checkBox.setManaged(true);
                checkBox.setDisable(false);
            } else {
                checkBox.setText("");
                checkBox.setSelected(false);
                checkBox.setVisible(false);
                checkBox.setManaged(false);
            }
        }

        System.out.println(
                "Questões exibidas: " + questoesCarregadas.size()
        );
    }

    // Aplica os três filtros simultaneamente
    private void aplicarFiltros() {
        if (todasAsQuestoes == null) {
            return;
        }

        String disciplinaSelecionada =
                box_filtro_disciplina.getValue();

        String dificuldadeSelecionada =
                box_filtro_dificuldade.getValue();

        String ultimoUsoSelecionado =
                box_filtro_ultimo_uso.getValue();

        questoesCarregadas = todasAsQuestoes.stream()
                .filter(q ->
                        disciplinaSelecionada == null
                                || disciplinaSelecionada.isEmpty()
                                || disciplinaSelecionada.equals(
                                q.getNomeDisciplina()
                        )
                )
                .filter(q ->
                        dificuldadeSelecionada == null
                                || dificuldadeSelecionada.isEmpty()
                                || dificuldadeSelecionada.equalsIgnoreCase(
                                q.getDificuldade()
                        )
                )
                .filter(q ->
                        ultimoUsoSelecionado == null
                                || ultimoUsoSelecionado.isEmpty()
                                || ultimoUsoSelecionado.equalsIgnoreCase(
                                q.getUltimoUso()
                        )
                )
                .collect(Collectors.toList());

        atualizarExibicaoQuestoes();
    }

    // Identifica a questão da linha em que o ícone foi clicado
    private Questao obterQuestaoClicada(MouseEvent event) {
        Node componente = (Node) event.getSource();

        while (componente != null) {
            if (componente instanceof HBox) {
                HBox linha = (HBox) componente;

                for (Node filho : linha.getChildren()) {
                    if (filho instanceof CheckBox) {
                        CheckBox checkBoxDaLinha = (CheckBox) filho;
                        CheckBox[] checkBoxes = obterCheckBoxes();

                        for (int i = 0; i < checkBoxes.length; i++) {
                            if (checkBoxes[i] == checkBoxDaLinha
                                    && questoesCarregadas != null
                                    && i < questoesCarregadas.size()) {
                                return questoesCarregadas.get(i);
                            }
                        }
                    }
                }
            }

            componente = componente.getParent();
        }

        System.out.println(
                "Não foi possível identificar a linha clicada."
        );

        return null;
    }

    // Duplica uma questão
    @FXML
    void duplicarQuestao(MouseEvent event) {
        Questao questao = obterQuestaoClicada(event);

        if (questao == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Não foi possível identificar a questão."
            );
            return;
        }

        try {
            questaoDAO.duplicarQuestao(questao);

            // Atualiza a lista depois da duplicação
            carregarQuestoes();

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Sucesso",
                    "Questão duplicada com sucesso!"
            );

        } catch (Exception e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao duplicar",
                    "Não foi possível duplicar a questão: "
                            + e.getMessage()
            );
        }
    }

    // Abre o formulário correspondente para editar uma questão

// Abre o formulário correspondente para editar uma questão
    @FXML
    void edicaoQuestao(MouseEvent event) {
        Questao questao = obterQuestaoClicada(event);

        if (questao == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Não foi possível identificar a questão."
            );
            return;
        }

        String caminhoFXML;

        switch (questao.getTipo()) {
            case "diss":
                caminhoFXML = "/org/example/CadastroQuestoes.fxml";
                break;

            case "alt":
                caminhoFXML = "/org/example/CadastroQuestoesAlt.fxml";
                break;

            case "rela":
                caminhoFXML = "/org/example/CadastroQuestoesRelac.fxml";
                break;

            default:
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Tipo não reconhecido",
                        "O tipo da questão é: " + questao.getTipo()
                );
                return;
        }

        try {
            URL recursoFXML = getClass().getResource(caminhoFXML);

            if (recursoFXML == null) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Arquivo FXML não encontrado",
                        "Não encontrei o arquivo:\n" + caminhoFXML
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(recursoFXML);
            Parent root = loader.load();

            Controller controller = loader.getController();

            if (controller == null) {
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "Erro ao abrir formulário",
                        "Não foi possível obter o Controller da tela."
                );
                return;
            }

            // Preenche os campos com os dados da questão selecionada
            controller.prepararEdicao(questao);

            // Abre exatamente a tela que já recebeu os dados
            App.definirCenaAtual(new Scene(root));

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao abrir formulário",
                    "Não foi possível abrir a tela de edição.\n"
                            + e.getMessage()
            );
        }
    }
    // Exibe mensagens
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    // Métodos dos botões e menus existentes
    @FXML
    void IrProva_montar(ActionEvent event) throws IOException {
        App.setRoot("montagem");
    }

    @FXML
    void IrQuestao_criar(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoes");
    }

    @FXML
    void irAlunos(ActionEvent event) {
    }

    @FXML
    void irAlunos_corrigir(ActionEvent event) throws IOException{
        App.setRoot("Aluno");
    }

    @FXML
    void irAlunos_notas(ActionEvent event) throws IOException {

    }

    @FXML
    void irProva(ActionEvent event) {
    }

    @FXML
    void irProva_agendar(ActionEvent event) {
        try {
            App.setRoot("agendamento");
        } catch (IOException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro de navegação",
                    "Não foi possível abrir a tela de agendamento."
            );
        }
    }

    @FXML
    void irQuestao(ActionEvent event) {
    }

    @FXML
    void irQuestao_consultar(ActionEvent event) {
        try {
            App.setRoot("VisualizarQuestao");

        } catch (IOException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro de navegação",
                    "Não foi possível abrir a tela de questões."
            );
        }
    }
}
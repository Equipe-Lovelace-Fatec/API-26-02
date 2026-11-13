package org.example;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class VisualizarQuestaoController {

    private static final double ALTURA_MINIMA_PAINEL = 400; // ajuste conforme seu layout
    private static final double MARGEM_INFERIOR = 30;

    @FXML private ComboBox<String> box_filtro_dificuldade;
    @FXML private ComboBox<String> box_filtro_disciplina;
    @FXML private ComboBox<String> box_filtro_ultimo_uso;

    @FXML private MenuItem btProva_agendar;
    @FXML private MenuItem btProva_montar;
    @FXML private MenuItem btalunos_corrigir;
    @FXML private MenuItem btalunos_notas;
    @FXML private MenuItem btquestoes_consultar;
    @FXML private MenuItem btquestoes_criar;
    @FXML private MenuButton btnAlunos;
    @FXML private MenuButton btnProva;
    @FXML private MenuButton btnQuestoes;

    @FXML private Pane pane_principal;
    @FXML private Pane painel_interno;
    @FXML private ScrollPane scroll_principal;
    @FXML private VBox lista_questoes;

    private final QuestaoDAO questaoDAO = new QuestaoDAO();
    private List<Questao> todasAsQuestoes;
    private List<Questao> questoesFiltradas;

    @FXML
    public void initialize() {
        lista_questoes.setMinHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        lista_questoes.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        lista_questoes.setMaxHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        lista_questoes.setSpacing(10);
        // A altura do painel acompanha a lista: questões ilimitadas, com rolagem
        painel_interno.prefHeightProperty().bind(
                Bindings.max(ALTURA_MINIMA_PAINEL,
                        lista_questoes.heightProperty()
                                .add(lista_questoes.getLayoutY() + MARGEM_INFERIOR)));

        carregarQuestoes();

        box_filtro_disciplina.valueProperty().addListener((o, a, n) -> aplicarFiltros());
        box_filtro_dificuldade.valueProperty().addListener((o, a, n) -> aplicarFiltros());
        box_filtro_ultimo_uso.valueProperty().addListener((o, a, n) -> aplicarFiltros());
    }

    // ---------- carregamento e filtros ----------

    private void carregarQuestoes() {
        try {
            todasAsQuestoes = questaoDAO.buscarQuestoes();
            carregarOpcoesFiltros();
            aplicarFiltros();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao carregar questões",
                    "Não foi possível carregar as questões: " + e.getMessage());
        }
    }

    private void carregarOpcoesFiltros() {
        if (todasAsQuestoes == null) return;

        // Guarda a seleção atual para não perder o filtro depois de duplicar
        String discAtual = box_filtro_disciplina.getValue();
        String difAtual = box_filtro_dificuldade.getValue();
        String usoAtual = box_filtro_ultimo_uso.getValue();

        List<String> disciplinas = todasAsQuestoes.stream()
                .map(Questao::getNomeDisciplina)
                .filter(d -> d != null && !d.isBlank())
                .distinct().sorted()
                .collect(Collectors.toList());
        disciplinas.add(0, "");

        box_filtro_disciplina.setItems(FXCollections.observableArrayList(disciplinas));
        box_filtro_dificuldade.setItems(FXCollections.observableArrayList("", "Fácil", "Média", "Difícil"));
        box_filtro_ultimo_uso.setItems(FXCollections.observableArrayList(
                "", "6 meses", "1 ano", "2 anos", "3 anos ou mais"));

        box_filtro_disciplina.setValue(discAtual);
        box_filtro_dificuldade.setValue(difAtual);
        box_filtro_ultimo_uso.setValue(usoAtual);
    }

    private void aplicarFiltros() {
        if (todasAsQuestoes == null) return;

        String disc = box_filtro_disciplina.getValue();
        String dif = box_filtro_dificuldade.getValue();
        String uso = box_filtro_ultimo_uso.getValue();

        questoesFiltradas = todasAsQuestoes.stream()
                .filter(q -> disc == null || disc.isEmpty() || disc.equals(q.getNomeDisciplina()))
                .filter(q -> dif == null || dif.isEmpty() || dif.equalsIgnoreCase(q.getDificuldade()))
                .filter(q -> uso == null || uso.isEmpty() || uso.equalsIgnoreCase(q.getUltimoUso()))
                .collect(Collectors.toList());

        exibirQuestoes();
    }

    private void exibirQuestoes() {
        lista_questoes.getChildren().clear();
        for (Questao q : questoesFiltradas) {
            lista_questoes.getChildren().add(criarCartao(q));
        }
    }

    private Node criarCartao(Questao q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CartaoQuestao.fxml"));
            Node cartao = loader.load();

            // Garante que o cartão nunca seja comprimido pelo VBox
            if (cartao instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region r = (javafx.scene.layout.Region) cartao;
                r.setMinHeight(81);
                r.setPrefHeight(81);
                r.setMaxHeight(81);
            }

            CartaoQuestaoController controller = loader.getController();
            controller.setQuestao(q, ehDoProfessorLogado(q), this::editar, this::duplicar);
            return cartao;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Não foi possível carregar a questão.");
        }
    }

    // ---------- regras de propriedade ----------

    private String emailLogado() {
        Usuario u = Sessao.getUsuario();
        return u == null ? null : u.getEmail();
    }

    private boolean ehDoProfessorLogado(Questao q) {
        String email = emailLogado();
        return email != null && email.equalsIgnoreCase(q.getEmailProfessor());
    }

    // ---------- ações do cartão ----------

    private void duplicar(Questao questao) {
        String email = emailLogado();
        if (email == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atenção", "Faça login para duplicar questões.");
            return;
        }
        try {
            // A cópia vai para o banco de quem está copiando
            questaoDAO.duplicarQuestao(questao, email);
            carregarQuestoes();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    "Questão duplicada para o seu banco de questões!");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao duplicar",
                    "Não foi possível duplicar a questão: " + e.getMessage());
        }
    }

    private void editar(Questao questao) {
        // Proteção também no código, além do botão desabilitado
        if (!ehDoProfessorLogado(questao)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Edição não permitida",
                    "Você só pode editar questões criadas por você. Duplique a questão para editar a sua cópia.");
            return;
        }

        // Prova já realizada: bloqueia
        if (questaoDAO.estaEmProvaPassada(questao.getId())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Edição não permitida",
                    "Você não consegue editar essa questão, pois ela está numa prova antiga. "
                            + "Duplique-a para poder editar.");
            return;
        }

// Prova agendada para o futuro: libera, mas pede confirmação
        if (questaoDAO.estaEmProvaFutura(questao.getId())) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Questão em prova agendada");
            confirmacao.setHeaderText(null);
            confirmacao.setContentText(
                    "Você está editando uma questão de uma prova agendada. "
                            + "As alterações valerão para essa prova. Deseja continuar?");

            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        String caminhoFXML;
        switch (questao.getTipo()) {
            case "diss": caminhoFXML = "/org/example/CadastroQuestoes.fxml"; break;
            case "alt":  caminhoFXML = "/org/example/CadastroQuestoesAlt.fxml"; break;
            case "rela": caminhoFXML = "/org/example/CadastroQuestoesRelac.fxml"; break;
            default:
                mostrarAlerta(Alert.AlertType.ERROR, "Tipo não reconhecido",
                        "O tipo da questão é: " + questao.getTipo());
                return;
        }

        try {
            URL recursoFXML = getClass().getResource(caminhoFXML);
            if (recursoFXML == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Arquivo FXML não encontrado",
                        "Não encontrei o arquivo:\n" + caminhoFXML);
                return;
            }

            FXMLLoader loader = new FXMLLoader(recursoFXML);
            Parent root = loader.load();

            Controller controller = loader.getController();
            if (controller == null) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao abrir formulário",
                        "Não foi possível obter o Controller da tela.");
                return;
            }

            controller.prepararEdicao(questao);
            App.definirCenaAtual(new Scene(root));

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao abrir formulário",
                    "Não foi possível abrir a tela de edição.\n" + e.getMessage());
        }
    }

    // ---------- utilitários e navegação ----------

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void navegar(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de navegação",
                    "Não foi possível abrir a tela: " + fxml);
        }
    }

    @FXML void IrProva_montar(ActionEvent event)      { navegar("montagem"); }
    @FXML void IrQuestao_criar(ActionEvent event)     { navegar("CadastroQuestoes"); }
    @FXML void irAlunos_corrigir(ActionEvent event)   { navegar("Aluno"); }
    @FXML void irProva_agendar(ActionEvent event)     { navegar("agendamento"); }
    @FXML void irQuestao_consultar(ActionEvent event) { navegar("VisualizarQuestao"); }
    @FXML void irAlunos_notas(ActionEvent event)      { }
    @FXML void irAlunos(ActionEvent event)            { }
    @FXML void irProva(ActionEvent event)             { }
    @FXML void irQuestao(ActionEvent event)           { }
}
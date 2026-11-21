package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Tela de Notas do professor: escolhe a turma e o aluno; cada aluno aparece com a média e, ao clicar
// no nome, abre um pop-up com as notas das provas que ele fez com este professor.
public class NotasController {

    private static final String COR_TITULO = "#00596a";
    private static final String COR_TEXTO = "#737373";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---- Menus (iguais aos das outras telas do professor) ----
    @FXML private MenuButton btnQuestoes;
    @FXML private MenuButton btnProva;
    @FXML private MenuButton btnAlunos;

    // ---- Seleção ----
    @FXML private ComboBox<NotasDAO.TurmaDisciplina> combo_turma;
    @FXML private ComboBox<NotasDAO.AlunoMedia> combo_aluno;

    // ---- Lista de alunos ----
    @FXML private ScrollPane scroll_principal;
    @FXML private VBox lista_alunos;

    private final NotasDAO dao = new NotasDAO();
    private final NotasDAO.AlunoMedia todosOsAlunos = criarOpcaoTodos();
    private List<NotasDAO.AlunoMedia> alunosDaTurma = new ArrayList<>();
    private boolean carregandoSeletores = false;

    private static NotasDAO.AlunoMedia criarOpcaoTodos() {
        NotasDAO.AlunoMedia todos = new NotasDAO.AlunoMedia();
        todos.nome = "Todos os alunos";
        return todos;
    }

    @FXML
    private void initialize() {
        combo_turma.setPromptText("Selecione a turma (disciplina | turma | semestre)");
        combo_aluno.setPromptText("Selecione o aluno");
        combo_aluno.setDisable(true);

        combo_turma.valueProperty().addListener((obs, antiga, nova) -> {
            if (!carregandoSeletores) {
                aoSelecionarTurma(nova);
            }
        });
        combo_aluno.valueProperty().addListener((obs, antigo, novo) -> {
            if (!carregandoSeletores && novo != null) {
                mostrarAlunos(novo == todosOsAlunos ? alunosDaTurma : List.of(novo));
            }
        });

        mostrarMensagem("Selecione a turma para ver os alunos.");
        carregarTurmas();
    }

    // ------------------------------------------------------------------ seleção

    // Turmas em que o professor logado aplicou provas
    private void carregarTurmas() {
        String email = emailProfessor();
        if (email == null) {
            mostrarMensagem("Não foi possível identificar o professor logado.");
            return;
        }
        try {
            List<NotasDAO.TurmaDisciplina> turmas = dao.listarTurmasDoProfessor(email);
            combo_turma.getItems().setAll(turmas);
            if (turmas.isEmpty()) {
                mostrarMensagem("Você ainda não aplicou provas para nenhuma turma.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensagem("Não foi possível carregar as turmas: " + e.getMessage());
        }
    }

    private void aoSelecionarTurma(NotasDAO.TurmaDisciplina turma) {
        if (turma == null) {
            return;
        }
        try {
            alunosDaTurma = dao.listarAlunosDaTurma(turma.idTurma, turma.nomeDisciplina);

            carregandoSeletores = true;
            List<NotasDAO.AlunoMedia> opcoes = new ArrayList<>();
            opcoes.add(todosOsAlunos);
            opcoes.addAll(alunosDaTurma);
            combo_aluno.getItems().setAll(opcoes);
            combo_aluno.setValue(todosOsAlunos);
            combo_aluno.setDisable(alunosDaTurma.isEmpty());
            carregandoSeletores = false;

            mostrarAlunos(alunosDaTurma);
        } catch (SQLException e) {
            carregandoSeletores = false;
            e.printStackTrace();
            mostrarMensagem("Não foi possível carregar os alunos: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ lista

    private void mostrarAlunos(List<NotasDAO.AlunoMedia> alunos) {
        lista_alunos.getChildren().clear();
        if (alunos.isEmpty()) {
            mostrarMensagem("Nenhum aluno nesta turma.");
            return;
        }
        for (NotasDAO.AlunoMedia aluno : alunos) {
            Node cartao = criarCartao(aluno);
            if (cartao != null) {
                lista_alunos.getChildren().add(cartao);
            }
        }
    }

    private Node criarCartao(NotasDAO.AlunoMedia aluno) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("CartaoAluno.fxml"));
            Node cartao = loader.load();
            CartaoAlunoController controller = loader.getController();
            controller.setAluno(aluno, this::abrirNotasDoAluno);
            return cartao;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Não foi possível carregar o aluno " + aluno.nome);
        }
    }

    private void mostrarMensagem(String mensagem) {
        lista_alunos.getChildren().clear();
        Label l = new Label(mensagem);
        l.setWrapText(true);
        l.setPadding(new Insets(20));
        l.setStyle("-fx-font-size: 18px; -fx-text-fill: " + COR_TEXTO + ";");
        lista_alunos.getChildren().add(l);
    }

    private String emailProfessor() {
        Usuario usuario = Sessao.getUsuario();
        return usuario != null ? usuario.getEmail() : null;
    }

    // ------------------------------------------------------------------ pop-up de notas

    private void abrirNotasDoAluno(NotasDAO.AlunoMedia aluno) {
        List<NotasDAO.NotaProva> notas;
        try {
            notas = dao.listarNotasDoAluno(aluno.ra, emailProfessor());
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Erro");
            alerta.setHeaderText(null);
            alerta.setContentText("Não foi possível carregar as notas: " + e.getMessage());
            alerta.showAndWait();
            return;
        }

        VBox raiz = new VBox(16);
        raiz.setPadding(new Insets(24));
        raiz.setStyle("-fx-background-color: white;");

        Label titulo = new Label(aluno.getNomeComRa());
        titulo.setWrapText(true);
        titulo.setFont(Font.font("System", FontWeight.BOLD, 22));
        titulo.setStyle("-fx-text-fill: " + COR_TITULO + ";");
        raiz.getChildren().add(titulo);

        if (notas.isEmpty()) {
            Label vazio = new Label("Este aluno ainda não realizou nenhuma prova com você.");
            vazio.setWrapText(true);
            vazio.setFont(Font.font(16));
            vazio.setStyle("-fx-text-fill: " + COR_TEXTO + ";");
            raiz.getChildren().add(vazio);
        } else {
            ScrollPane rolagem = new ScrollPane(montarTabelaDeNotas(notas));
            rolagem.setFitToWidth(true);
            rolagem.setStyle("-fx-background-color: transparent; -fx-background: white;");
            VBox.setVgrow(rolagem, javafx.scene.layout.Priority.ALWAYS);
            raiz.getChildren().add(rolagem);
        }

        Stage popup = new Stage();
        Button fechar = new Button("FECHAR");
        fechar.setStyle("-fx-background-color: " + COR_TITULO + "; -fx-background-radius: 8; -fx-text-fill: white;"
                + " -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        fechar.setPrefSize(140, 34);
        fechar.setOnAction(e -> popup.close());
        VBox rodape = new VBox(fechar);
        rodape.setAlignment(Pos.CENTER_RIGHT);
        raiz.getChildren().add(rodape);

        double altura = Math.min(640, 200 + notas.size() * 46);
        popup.initOwner(lista_alunos.getScene().getWindow());
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("aNota - Notas do aluno");
        popup.setScene(new Scene(raiz, 900, altura));
        popup.showAndWait();
    }

    // Prova | Disciplina | Data | Nota | Situação
    private GridPane montarTabelaDeNotas(List<NotasDAO.NotaProva> notas) {
        GridPane grade = new GridPane();
        grade.setHgap(14);
        grade.setVgap(10);
        grade.setPadding(new Insets(4, 8, 4, 4));

        double[] larguras = {27, 24, 12, 15, 22};
        for (double l : larguras) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(l);
            grade.getColumnConstraints().add(cc);
        }

        String[] cabecalhos = {"Prova", "Disciplina", "Data", "Nota", "Situação"};
        for (int c = 0; c < cabecalhos.length; c++) {
            Label l = new Label(cabecalhos[c]);
            l.setFont(Font.font("System", FontWeight.BOLD, 16));
            l.setStyle("-fx-text-fill: " + COR_TITULO + ";");
            grade.add(l, c, 0);
        }
        grade.add(linhaSeparadora(), 0, 1, cabecalhos.length, 1);

        int linha = 2;
        for (NotasDAO.NotaProva n : notas) {
            String nomeProva = n.nomeProva + (n.recuperacao ? " (Recuperação)" : "");
            String data = n.data != null ? n.data.format(FORMATO_DATA) : "-";
            boolean corrigida = n.nota != null;
            String textoNota = !corrigida ? "–"
                    : NotasDAO.formatar(n.nota) + (n.valor != null ? " / " + NotasDAO.formatar(n.valor) : "");

            grade.add(celula(nomeProva, false), 0, linha);
            grade.add(celula(n.nomeDisciplina, false), 1, linha);
            grade.add(celula(data, false), 2, linha);
            grade.add(celula(textoNota, corrigida), 3, linha);
            grade.add(celula(situacaoLegivel(n.situacao), false), 4, linha);
            linha++;
            grade.add(linhaSeparadora(), 0, linha++, cabecalhos.length, 1);
        }
        return grade;
    }

    private Label celula(String texto, boolean destaque) {
        Label l = new Label(texto == null ? "-" : texto);
        l.setWrapText(true);
        l.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        l.setFont(destaque ? Font.font("System", FontWeight.BOLD, 16) : Font.font(16));
        l.setStyle("-fx-text-fill: " + (destaque ? COR_TITULO : COR_TEXTO) + ";");
        GridPane.setValignment(l, VPos.CENTER);
        return l;
    }

    private Separator linhaSeparadora() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #d9e8eb;");
        return s;
    }

    // "AGUARDANDO CORREÇÃO" -> "Aguardando correção"
    private String situacaoLegivel(String situacao) {
        if (situacao == null || situacao.isBlank()) {
            return "-";
        }
        String minuscula = situacao.toLowerCase();
        return Character.toUpperCase(minuscula.charAt(0)) + minuscula.substring(1);
    }

    // ------------------------------------------------------------------ menus

    @FXML
    private void irQuestao(ActionEvent event) {
    }

    @FXML
    private void IrQuestao_criar(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoes");
    }

    @FXML
    private void irQuestao_consultar(ActionEvent event) throws IOException {
        App.setRoot("VisualizarQuestao");
    }

    @FXML
    private void irProva(ActionEvent event) {
    }

    @FXML
    private void IrProva_montar(ActionEvent event) throws IOException {
        App.setRoot("montagem");
    }

    @FXML
    private void irProva_agendar(ActionEvent event) throws IOException {
        App.setRoot("SelecaoProva");
    }

    @FXML
    private void irProva_pesos(ActionEvent event) throws IOException {
        App.setRoot("Pesos");
    }

    @FXML
    private void irAlunos(ActionEvent event) {
    }

    @FXML
    private void irAlunos_notas(ActionEvent event) {
        // já é a tela de notas
    }

    @FXML
    private void irAlunos_corrigir(ActionEvent event) throws IOException {
        App.setRoot("Aluno");
    }
}
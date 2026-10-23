package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgendamentoController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Button bt_confirmar;
    @FXML private ChoiceBox<Turma> choice_box_turma;
    @FXML private MenuButton menu_alunos;
    @FXML private ChoiceBox<String> choice_box_disciplina;
    @FXML private ChoiceBox<String> choice_box_hora;
    @FXML private DatePicker date_picker;
    @FXML private ImageView img_fundoverde1350x1080;
    @FXML private ImageView img_menu_elaboradas;
    @FXML private ImageView img_tela_elaboradas;
    @FXML private Label nome_alunos_selecionados;
    @FXML private Label nome_data_selecionada;
    @FXML private Label nome_disciplina_selecionada;
    @FXML private Label nome_hora_selecionada;
    @FXML private Label nome_prova_selecionada;
    @FXML private Label nome_turma_selecionada;
    @FXML private Pane pane_principal;
    @FXML private Label titulo_pagina_nome_prova_selecionada;

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    private Prova prova;
    private CheckBox checkTodos;
    private final Map<String, CheckBox> checksAlunos = new LinkedHashMap<>();
    private final Map<String, String> nomesAlunos = new LinkedHashMap<>();
    private boolean atualizandoTodos = false;
    private boolean emLote = false;

    @FXML
    public void initialize() {
        prova = Sessao.getProvaParaAgendar();

        criarMenuAlunos();
        configurarDatePicker();
        if (prova == null) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "Escolha uma prova na lista para agendar.");
            javafx.application.Platform.runLater(this::voltarParaLista);
            return;
        }

        titulo_pagina_nome_prova_selecionada.setText(prova.getNomeProva());
        nome_prova_selecionada.setText(prova.getNomeProva());

        choice_box_disciplina.getItems().setAll(prova.getNomeDisciplina());
        choice_box_disciplina.setValue(prova.getNomeDisciplina());
        choice_box_disciplina.setDisable(true);
        nome_disciplina_selecionada.setText(prova.getNomeDisciplina());

        nome_turma_selecionada.setText("-");
        nome_alunos_selecionados.setText("-");
        nome_data_selecionada.setText("-");
        nome_hora_selecionada.setText("-");

        choice_box_turma.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
            nome_turma_selecionada.setText(nova == null ? "-" : nova.toString());
            carregarAlunos(nova);
            atualizarHorarios(nova);
        });

        choice_box_hora.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) ->
                nome_hora_selecionada.setText(nova == null ? "-" : nova));

        carregarTurmas();
    }

    private void carregarTurmas() {
        try {
            List<Turma> turmas = agendamentoDAO.listarTurmasDaDisciplina(prova.getNomeDisciplina());
            choice_box_turma.getItems().setAll(turmas);
            if (turmas.size() == 1) {
                choice_box_turma.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as turmas do banco de dados.");
        }
    }

    private void carregarAlunos(Turma turma) {
        checksAlunos.clear();
        nomesAlunos.clear();
        VBox lista = listaDoMenu();
        lista.getChildren().setAll(checkTodos);

        if (turma != null) {
            try {
                for (AlunoTurma a : agendamentoDAO.listarAlunosDaTurma(turma.getIdTurma())) {
                    CheckBox cb = new CheckBox(a.getNome());
                    cb.setSelected(true);
                    cb.selectedProperty().addListener((obs, antigo, novo) -> aoMarcarAluno());
                    checksAlunos.put(a.getRa(), cb);
                    nomesAlunos.put(a.getRa(), a.getNome());
                    lista.getChildren().add(cb);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os alunos da turma.");
            }
        }

        atualizandoTodos = true;
        checkTodos.setSelected(!checksAlunos.isEmpty());
        atualizandoTodos = false;
        atualizarResumoAlunos();
    }

    private void criarMenuAlunos() {
        checkTodos = new CheckBox("Todos");
        checkTodos.setStyle("-fx-font-weight: bold;");
        checkTodos.selectedProperty().addListener((obs, antigo, novo) -> {
            if (atualizandoTodos) {
                return;
            }
            emLote = true;
            for (CheckBox cb : checksAlunos.values()) {
                cb.setSelected(novo);
            }
            emLote = false;
            atualizarResumoAlunos();
        });

        VBox lista = new VBox(8, checkTodos);
        lista.setPadding(new Insets(8, 12, 8, 12));

        ScrollPane rolagem = new ScrollPane(lista);
        rolagem.setFitToWidth(true);
        rolagem.setPrefViewportHeight(220);
        rolagem.setMaxHeight(240);
        rolagem.setMinWidth(300);

        CustomMenuItem item = new CustomMenuItem(rolagem);
        item.setHideOnClick(false);

        menu_alunos.setText("Selecione os alunos");
        menu_alunos.getItems().setAll(item);
        String estilo = menu_alunos.getStyle() == null ? "" : menu_alunos.getStyle().trim();
        if (!estilo.isEmpty() && !estilo.endsWith(";")) {
            estilo += ";";
        }
        menu_alunos.setStyle(estilo + " -fx-alignment: CENTER_LEFT; -fx-font-size: 13px;");
    }

    private VBox listaDoMenu() {
        CustomMenuItem item = (CustomMenuItem) menu_alunos.getItems().get(0);
        ScrollPane rolagem = (ScrollPane) item.getContent();
        return (VBox) rolagem.getContent();
    }

    private void aoMarcarAluno() {
        if (emLote) {
            return;
        }
        boolean todos = !checksAlunos.isEmpty()
                && checksAlunos.values().stream().allMatch(CheckBox::isSelected);
        atualizandoTodos = true;
        checkTodos.setSelected(todos);
        atualizandoTodos = false;
        atualizarResumoAlunos();
    }

    private List<String> rasSelecionados() {
        List<String> ras = new ArrayList<>();
        for (Map.Entry<String, CheckBox> e : checksAlunos.entrySet()) {
            if (e.getValue().isSelected()) {
                ras.add(e.getKey());
            }
        }
        return ras;
    }

    private void atualizarResumoAlunos() {
        List<String> ras = rasSelecionados();
        String texto;
        if (checksAlunos.isEmpty() || ras.isEmpty()) {
            texto = "-";
            menu_alunos.setText("Selecione os alunos");
        } else if (ras.size() == checksAlunos.size()) {
            texto = "Todos";
            menu_alunos.setText("Todos (" + ras.size() + ")");
        } else {
            texto = ras.size() + (ras.size() == 1 ? " aluno" : " alunos");
            menu_alunos.setText(texto + " selecionado" + (ras.size() == 1 ? "" : "s"));
        }
        nome_alunos_selecionados.setText(texto);
    }

    private void configurarDatePicker() {
        date_picker.setEditable(false);
        date_picker.setPromptText("dd/mm/aaaa");
        date_picker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FORMATO_DATA.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                return (texto == null || texto.isBlank()) ? null : LocalDate.parse(texto, FORMATO_DATA);
            }
        });
        date_picker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                if (!vazio && data.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-opacity: 0.4;");
                }
            }
        });
    }

    private void atualizarHorarios(Turma turma) {
        choice_box_hora.getSelectionModel().clearSelection();
        if (turma == null) {
            choice_box_hora.getItems().clear();
            return;
        }
        List<String> horas = new ArrayList<>();
        for (LocalTime h : HorarioAula.iniciosDoPeriodo(turma.getPeriodo())) {
            horas.add(h.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        choice_box_hora.getItems().setAll(horas);
    }

    @FXML
    void escolhe_data_prova(ActionEvent event) {
        LocalDate data = date_picker.getValue();
        nome_data_selecionada.setText(data == null ? "-" : FORMATO_DATA.format(data));
    }

    @FXML
    void salva_agendamento(ActionEvent event) {
        if (prova == null) {
            return;
        }
        Turma turma = choice_box_turma.getValue();
        LocalDate data = date_picker.getValue();
        String horaTexto = choice_box_hora.getValue();
        List<String> ras = rasSelecionados();

        if (turma == null) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "Escolha a turma.");
            return;
        }
        if (ras.isEmpty()) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "Selecione pelo menos um aluno.");
            return;
        }
        if (data == null) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "Escolha a data da prova.");
            return;
        }
        if (horaTexto == null) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "Escolha o horário da prova (depende do período da turma).");
            return;
        }

        LocalTime horario = LocalTime.parse(horaTexto);
        if (!HorarioAula.iniciosDoPeriodo(turma.getPeriodo()).contains(horario)) {
            avisar(Alert.AlertType.WARNING, "Agendar prova",
                    "Esse horário não é permitido para o período da turma.");
            return;
        }
        if (data.isBefore(LocalDate.now())
                || (data.isEqual(LocalDate.now()) && horario.isBefore(LocalTime.now()))) {
            avisar(Alert.AlertType.WARNING, "Agendar prova", "A data e o horário precisam ser futuros.");
            return;
        }

        try {
            agendamentoDAO.agendar(prova, turma.getIdTurma(), data, horario, ras);
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getErrorCode() == 1062) {
                avisar(Alert.AlertType.WARNING, "Agendar prova",
                        "Esta prova já está agendada para essa turma nesse dia e horário.");
            } else if (e.getErrorCode() == 1146) {
                avisar(Alert.AlertType.ERROR, "Banco de dados",
                        "A tabela prova_aluno ainda não existe. Rode o script criar_prova_aluno.sql no MySQL.");
            } else {
                avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar o agendamento.");
            }
            return;
        }

        avisar(Alert.AlertType.INFORMATION, "Agendamento confirmado",
                prova.getNomeProva() + " agendada para " + FORMATO_DATA.format(data)
                        + " às " + horaTexto + ", para " + ras.size()
                        + (ras.size() == 1 ? " aluno." : " alunos."));
        Sessao.setProvaParaAgendar(null);
        voltarParaLista();
    }

    @FXML
    void IrQuestao_criar(ActionEvent event) {
        navegar("CadastroQuestoes");
    }

    @FXML
    void irQuestao_consultar(ActionEvent event) {
        navegar("VisualizarQuestao");
    }

    @FXML
    void IrProva_montar(ActionEvent event) {
        navegar("montagem");
    }

    @FXML
    void irProva_agendar(ActionEvent event) {
        voltarParaLista();
    }

    @FXML
    void irAlunos_notas(ActionEvent event) {
        navegar("Aluno");
    }

    @FXML
    void irAlunos_corrigir(ActionEvent event) {
        navegar("Aluno");
    }

    @FXML
    void irQuestao(ActionEvent event) {
    }

    @FXML
    void irProva(ActionEvent event) {
    }

    @FXML
    void irAlunos(ActionEvent event) {
    }

    private void navegar(String fxml) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void voltarParaLista() {
        abrir("SelecaoProva", 1350, 800);
    }

    private void abrir(String fxml, double largura, double altura) {
        try {
            App.abrirTela(fxml, "aNota", largura, altura);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void avisar(Alert.AlertType tipo, String titulo, String texto) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}

package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProvasAgendadasController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<AplicacaoAgendada> tabela_agendadas;
    @FXML private TableColumn<AplicacaoAgendada, String> col_prova;
    @FXML private TableColumn<AplicacaoAgendada, String> col_disciplina;
    @FXML private TableColumn<AplicacaoAgendada, String> col_turma;
    @FXML private TableColumn<AplicacaoAgendada, String> col_data;
    @FXML private TableColumn<AplicacaoAgendada, String> col_inicio;
    @FXML private TableColumn<AplicacaoAgendada, String> col_termino;
    @FXML private TableColumn<AplicacaoAgendada, String> col_alunos;
    @FXML private TableColumn<AplicacaoAgendada, String> col_situacao;

    @FXML private DatePicker date_picker;
    @FXML private ChoiceBox<LocalTime> choice_box_inicio;
    @FXML private ChoiceBox<LocalTime> choice_box_termino;
    @FXML private Button bt_salvar;
    @FXML private Button bt_excluir;
    @FXML private Label lbl_aviso;

    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();

    @FXML
    public void initialize() {
        configurarColunas();
        configurarCampos();

        tabela_agendadas.getSelectionModel().selectedItemProperty()
                .addListener((obs, antiga, nova) -> mostrarSelecionada(nova));

        carregarLista();
    }

    private void configurarColunas() {
        coluna(col_prova, AplicacaoAgendada::getNomeProva);
        coluna(col_disciplina, AplicacaoAgendada::getNomeDisciplina);
        coluna(col_turma, AplicacaoAgendada::getTurma);
        coluna(col_data, AplicacaoAgendada::getDataTexto);
        coluna(col_inicio, AplicacaoAgendada::getHorarioTexto);
        coluna(col_termino, AplicacaoAgendada::getHorarioFimTexto);
        coluna(col_alunos, a -> String.valueOf(a.getQuantidadeAlunos()));
        coluna(col_situacao, AplicacaoAgendada::getSituacao);
        tabela_agendadas.setPlaceholder(new Label("Nenhuma prova agendada."));
    }

    private void coluna(TableColumn<AplicacaoAgendada, String> coluna, Function<AplicacaoAgendada, String> valor) {
        coluna.setCellValueFactory(c -> new SimpleStringProperty(valor.apply(c.getValue())));
    }

    private void configurarCampos() {
        StringConverter<LocalTime> hora = new StringConverter<>() {
            @Override
            public String toString(LocalTime h) {
                return h == null ? "" : h.format(DateTimeFormatter.ofPattern("HH:mm"));
            }

            @Override
            public LocalTime fromString(String texto) {
                return (texto == null || texto.isBlank()) ? null : LocalTime.parse(texto);
            }
        };
        choice_box_inicio.setConverter(hora);
        choice_box_termino.setConverter(hora);

        date_picker.setEditable(false);
        date_picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate data) {
                return data == null ? "" : FORMATO_DATA.format(data);
            }

            @Override
            public LocalDate fromString(String texto) {
                return (texto == null || texto.isBlank()) ? null : LocalDate.parse(texto, FORMATO_DATA);
            }
        });
        date_picker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                if (!vazio && data.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-opacity: 0.4;");
                }
            }
        });

        choice_box_inicio.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            AplicacaoAgendada selecionada = tabela_agendadas.getSelectionModel().getSelectedItem();
            if (selecionada != null && novo != null) {
                atualizarTerminos(selecionada, novo);
            }
        });
    }

    private void carregarLista() {
        Usuario logado = Sessao.getUsuario();
        if (logado == null) {
            avisar(Alert.AlertType.WARNING, "Provas agendadas", "Nenhum professor logado.");
            return;
        }
        try {
            tabela_agendadas.getItems().setAll(agendamentoDAO.listarAgendadas(logado.getEmail()));
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as provas agendadas.");
        }
        mostrarSelecionada(null);
    }

    private void mostrarSelecionada(AplicacaoAgendada a) {
        boolean podeMexer = a != null && !a.isIniciada();
        date_picker.setDisable(!podeMexer);
        choice_box_inicio.setDisable(!podeMexer);
        choice_box_termino.setDisable(!podeMexer);
        bt_salvar.setDisable(!podeMexer);
        bt_excluir.setDisable(!podeMexer);

        if (a == null) {
            date_picker.setValue(null);
            choice_box_inicio.getItems().clear();
            choice_box_termino.getItems().clear();
            lbl_aviso.setText("Selecione uma prova na lista para alterar o horário ou excluir.");
            return;
        }

        date_picker.setValue(a.getData());

        List<LocalTime> inicios = new ArrayList<>(HorarioAula.iniciosDoPeriodo(a.getPeriodo()));
        if (!inicios.contains(a.getHorario())) {
            inicios.add(a.getHorario());
            inicios.sort(null);
        }
        choice_box_inicio.getItems().setAll(inicios);
        choice_box_inicio.setValue(a.getHorario());
        atualizarTerminos(a, a.getHorario());

        lbl_aviso.setText(a.isIniciada()
                ? "Esta prova já começou: não é possível alterar nem excluir."
                : a.getNomeProva() + " — " + a.getTurma());
    }

    private void atualizarTerminos(AplicacaoAgendada a, LocalTime inicio) {
        List<LocalTime> terminos = new ArrayList<>(HorarioAula.terminosApos(a.getPeriodo(), inicio));
        if (inicio.equals(a.getHorario()) && !terminos.contains(a.getHorarioFim())
                && a.getHorarioFim().isAfter(inicio)) {
            terminos.add(a.getHorarioFim());
            terminos.sort(null);
        }
        choice_box_termino.getItems().setAll(terminos);

        if (terminos.contains(a.getHorarioFim()) && inicio.equals(a.getHorario())) {
            choice_box_termino.setValue(a.getHorarioFim());
        } else if (!terminos.isEmpty()) {
            choice_box_termino.setValue(terminos.get(0));
        }
    }

    @FXML
    void salvar_alteracao(ActionEvent event) {
        AplicacaoAgendada a = tabela_agendadas.getSelectionModel().getSelectedItem();
        if (a == null) {
            return;
        }
        LocalDate data = date_picker.getValue();
        LocalTime inicio = choice_box_inicio.getValue();
        LocalTime termino = choice_box_termino.getValue();

        if (data == null || inicio == null || termino == null) {
            avisar(Alert.AlertType.WARNING, "Alterar horário", "Escolha a data, o início e o término.");
            return;
        }
        if (!termino.isAfter(inicio)) {
            avisar(Alert.AlertType.WARNING, "Alterar horário", "O término precisa ser depois do início.");
            return;
        }
        if (!LocalDateTime.of(data, inicio).isAfter(LocalDateTime.now())) {
            avisar(Alert.AlertType.WARNING, "Alterar horário", "A nova data e o novo horário precisam ser futuros.");
            return;
        }

        try {
            boolean alterou = agendamentoDAO.alterarHorario(a.getIdAplicacao(), Sessao.getUsuario().getEmail(),
                    data, inicio, termino);
            if (!alterou) {
                avisar(Alert.AlertType.WARNING, "Alterar horário",
                        "Não foi possível alterar: a prova já começou.");
            } else {
                avisar(Alert.AlertType.INFORMATION, "Horário alterado",
                        a.getNomeProva() + " agora está agendada para " + FORMATO_DATA.format(data)
                                + ", das " + choice_box_inicio.getConverter().toString(inicio)
                                + " às " + choice_box_termino.getConverter().toString(termino) + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getErrorCode() == 1062) {
                avisar(Alert.AlertType.WARNING, "Alterar horário",
                        "Esta prova já está agendada para essa turma nesse dia e horário.");
            } else {
                avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar a alteração.");
            }
        }
        carregarLista();
    }

    @FXML
    void excluir_agendamento(ActionEvent event) {
        AplicacaoAgendada a = tabela_agendadas.getSelectionModel().getSelectedItem();
        if (a == null) {
            return;
        }

        try {
            String impedimento = agendamentoDAO.impedimentoParaExcluir(a.getIdAplicacao());
            if (impedimento != null) {
                avisar(Alert.AlertType.WARNING, "Excluir agendamento", "Não é possível excluir. " + impedimento);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível verificar o agendamento.");
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION);
        confirmar.setTitle("Excluir agendamento");
        confirmar.setHeaderText(null);
        confirmar.setContentText("Excluir o agendamento de " + a.getNomeProva() + " em " + a.getDataTexto()
                + " às " + a.getHorarioTexto() + " (" + a.getQuantidadeAlunos() + " alunos)?\n"
                + "A prova continua existindo e pode ser agendada de novo.");
        Optional<ButtonType> resposta = confirmar.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) {
            return;
        }

        try {
            if (agendamentoDAO.excluir(a.getIdAplicacao(), Sessao.getUsuario().getEmail())) {
                avisar(Alert.AlertType.INFORMATION, "Excluir agendamento", "Agendamento excluído.");
            } else {
                avisar(Alert.AlertType.WARNING, "Excluir agendamento",
                        "Não foi possível excluir: a prova já começou.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getErrorCode() == 1451) {
                avisar(Alert.AlertType.WARNING, "Excluir agendamento",
                        "Não é possível excluir: este agendamento está ligado a outros registros (ex: recuperação).");
            } else {
                avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível excluir o agendamento.");
            }
        }
        carregarLista();
    }

    @FXML
    void voltar(ActionEvent event) {
        abrir("SelecaoProva", 1350, 800);
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
        abrir("SelecaoProva", 1350, 800);
    }

    @FXML
    void irProva_pesos(ActionEvent event) {
        navegar("pesos");
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

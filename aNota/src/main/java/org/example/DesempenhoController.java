package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DesempenhoController {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal MEDIA_APROVACAO = new BigDecimal("6");
    private static final double ALTURA_LINHA = 50;
    private static final double ALTURA_MAXIMA_TABELA = 500;

    @FXML private Button bt_minha_agenda;
    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Label nome_curso_periodo;
    @FXML private ChoiceBox<String> choice_box_semestre;
    @FXML private ChoiceBox<String> choice_box_disciplina;
    @FXML private TableView<NotaAvaliacao> tabela_avaliacoes;
    @FXML private TableColumn<NotaAvaliacao, String> col_avaliacao;
    @FXML private TableColumn<NotaAvaliacao, String> col_data;
    @FXML private TableColumn<NotaAvaliacao, String> col_nota;
    @FXML private TableColumn<NotaAvaliacao, String> col_observacao;
    @FXML private Label nome_media_final;
    @FXML private Label icone_situacao;
    @FXML private Label nome_situacao;

    private final DesempenhoDAO desempenhoDAO = new DesempenhoDAO();
    private final DecimalFormat formatoNota = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")));
    private final Map<String, String[]> vigenciasPorSemestre = new LinkedHashMap<>();

    private String ra;

    @FXML
    public void initialize() {
        formatoNota.setRoundingMode(RoundingMode.HALF_UP);
        configurarTabela();
        limparMedia();
        nome_curso_periodo.setText("");

        Usuario logado = Sessao.getUsuario();
        if (logado == null) {
            avisar(Alert.AlertType.WARNING, "Desempenho", "Faça login para ver suas notas.");
            return;
        }

        try {
            ra = desempenhoDAO.buscarRa(logado.getEmail());
            if (ra == null) {
                avisar(Alert.AlertType.WARNING, "Desempenho", "Aluno não encontrado para o usuário logado.");
                return;
            }
            for (String[] v : desempenhoDAO.listarVigencias(ra)) {
                vigenciasPorSemestre.putIfAbsent(semestreDaVigencia(v[0]), v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar os semestres do banco de dados.");
            return;
        }

        choice_box_semestre.getItems().setAll(vigenciasPorSemestre.keySet());
        choice_box_semestre.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> carregarDisciplinas(novo));
        choice_box_disciplina.getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> carregarNotas(nova));

        if (!choice_box_semestre.getItems().isEmpty()) {
            choice_box_semestre.getSelectionModel().selectFirst();
        }
    }

    private void configurarTabela() {
        col_avaliacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomeProva().toUpperCase()));
        col_data.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getData() == null ? "-" : FORMATO_DATA.format(c.getValue().getData())));
        col_nota.setCellValueFactory(c -> new SimpleStringProperty(formatarNota(c.getValue().getNotaDeZeroADez())));
        col_observacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSituacao()));
        ajustarAlturaTabela();
    }

    private void carregarDisciplinas(String semestre) {
        choice_box_disciplina.getItems().clear();
        tabela_avaliacoes.getItems().clear();
        ajustarAlturaTabela();
        limparMedia();

        String[] vigencia = semestre == null ? null : vigenciasPorSemestre.get(semestre);
        if (vigencia == null) {
            nome_curso_periodo.setText("");
            return;
        }
        nome_curso_periodo.setText((vigencia[1] + " " + vigencia[2]).toUpperCase());

        try {
            List<String> disciplinas = desempenhoDAO.listarDisciplinas(ra, vigencia[0]);
            choice_box_disciplina.getItems().setAll(disciplinas);
            if (!disciplinas.isEmpty()) {
                choice_box_disciplina.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as disciplinas do banco de dados.");
        }
    }

    private void carregarNotas(String disciplina) {
        tabela_avaliacoes.getItems().clear();
        limparMedia();

        String[] vigencia = vigenciasPorSemestre.get(choice_box_semestre.getValue());
        if (disciplina == null || vigencia == null) {
            ajustarAlturaTabela();
            return;
        }

        try {
            List<NotaAvaliacao> notas = desempenhoDAO.listarNotas(ra, vigencia[0], disciplina);
            tabela_avaliacoes.getItems().setAll(notas);
            mostrarMedia(desempenhoDAO.buscarMedia(ra, vigencia[0], disciplina), notas);
        } catch (SQLException e) {
            e.printStackTrace();
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as notas do banco de dados.");
        }
        ajustarAlturaTabela();
    }

    private void mostrarMedia(DesempenhoDAO.MediaAluno resultado, List<NotaAvaliacao> notas) {
        if (resultado == null || resultado.media == null || resultado.itensTotal == 0) {
            return;
        }

        BigDecimal media = resultado.media.setScale(1, RoundingMode.HALF_UP);
        nome_media_final.setText(formatarNota(media));

        boolean recuperacaoPendente = notas.stream().anyMatch(n -> n.ehRecuperacao() && !n.temNota());

        if (resultado.itensPendentes > 0) {
            mostrarSituacao("Em andamento", null, null);
        } else if (media.compareTo(MEDIA_APROVACAO) >= 0) {
            mostrarSituacao("Aprovado", "✔", "#9ac53c");
        } else if (recuperacaoPendente) {
            mostrarSituacao("Recuperação", null, null);
        } else {
            mostrarSituacao("Reprovado", "✖", "#ef5e57");
        }
    }

    private void mostrarSituacao(String texto, String icone, String cor) {
        nome_situacao.setText(texto);
        if (icone == null) {
            icone_situacao.setVisible(false);
            return;
        }
        icone_situacao.setText(icone);
        icone_situacao.setStyle("-fx-background-color: " + cor + ";");
        icone_situacao.setVisible(true);
    }

    private void limparMedia() {
        nome_media_final.setText("-");
        nome_situacao.setText("-");
        icone_situacao.setVisible(false);
    }

    private void ajustarAlturaTabela() {
        int linhas = Math.max(tabela_avaliacoes.getItems().size(), 2);
        double altura = Math.min(ALTURA_LINHA * (linhas + 1) + 4, ALTURA_MAXIMA_TABELA);
        tabela_avaliacoes.setPrefHeight(altura);
    }

    private String formatarNota(BigDecimal nota) {
        return nota == null ? "-" : formatoNota.format(nota);
    }

    private String semestreDaVigencia(String vigencia) {
        if (vigencia != null && vigencia.matches("\\d{2}/\\d{4}.*")) {
            int mes = Integer.parseInt(vigencia.substring(0, 2));
            String ano = vigencia.substring(3, 7);
            return ano + (mes >= 7 ? ".2" : ".1");
        }
        return vigencia;
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
            avisar(Alert.AlertType.ERROR, "Erro", "Não foi possível abrir " + descricao + ": " + e.getMessage());
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

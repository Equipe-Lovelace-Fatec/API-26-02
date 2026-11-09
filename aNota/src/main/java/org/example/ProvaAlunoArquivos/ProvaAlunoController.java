package org.example.ProvaAlunoArquivos;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;
import org.example.Questao;
import org.example.QuestaoDAO;
import org.example.Resposta;
import org.example.RespostaDAO;

import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ProvaAlunoController {

    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Button bt_finalizar_prova;
    @FXML private Button bt_minha_agenda;
    @FXML private Label contagem_regressiva_prova;
    @FXML private Label titulo_prova;
    @FXML private VBox vboxContainerQuestoes;

    private Timeline timeline;
    private long segundosRestantes = 0;

    private final List<Object> controllersQuestoes = new ArrayList<>();

    private Long idAplicacaoAtual;
    private String raAlunoAtual;

    /**
     * Método executado automaticamente pelo JavaFX assim que a tela abre.
     */
    @FXML
    public void initialize() {
        System.out.println("DEBUG -> Tela de prova do aluno aberta com sucesso!");

        // Configuração padrão para teste (RA123456, aplicação 1) caso não venha por parâmetro externo
        this.raAlunoAtual = "RA123456";
        this.idAplicacaoAtual = 1L;

        if (titulo_prova != null) {
            titulo_prova.setText("Avaliação Prática 1");
        }

        LocalTime horarioFimTeste = LocalTime.of(23, 59, 59);
        calcularTempoRestante(horarioFimTeste);
        iniciarCronometro();

        carregarQuestoesDaProva(1L);
    }

    public void inicializarDadosProva(Long idAplicacao, String raAluno, LocalTime horarioFim, String nomeProva) {
        this.idAplicacaoAtual = idAplicacao;
        this.raAlunoAtual = raAluno;

        if (titulo_prova != null) {
            titulo_prova.setText(nomeProva != null ? nomeProva : "Avaliação");
        }

        calcularTempoRestante(horarioFim);
        iniciarCronometro();

        carregarQuestoesDaProva(idAplicacao);
    }

    private void calcularTempoRestante(LocalTime horarioFim) {
        LocalTime agora = LocalTime.now();
        if (horarioFim != null && agora.isBefore(horarioFim)) {
            this.segundosRestantes = ChronoUnit.SECONDS.between(agora, horarioFim);
        } else {
            this.segundosRestantes = 0;
        }
    }

    private void iniciarCronometro() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (segundosRestantes > 0) {
                segundosRestantes--;
                atualizarLabelTempo();
            } else {
                timeline.stop();
                tempoEsgotado();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void atualizarLabelTempo() {
        long horas = segundosRestantes / 3600;
        long minutos = (segundosRestantes % 3600) / 60;
        long segundos = segundosRestantes % 60;

        if (contagem_regressiva_prova != null) {
            contagem_regressiva_prova.setText(String.format("%02d:%02d:%02d", horas, minutos, segundos));
        }
    }

    private void tempoEsgotado() {
        if (contagem_regressiva_prova != null) {
            contagem_regressiva_prova.setText("TEMPO ESGOTADO!");
        }
        mostrarAlerta("Atenção", "O tempo da prova acabou! Suas respostas serão enviadas automaticamente.");
        enviarProva();
    }

    private void carregarQuestoesDaProva(Long idAplicacao) {
        try {
            if (vboxContainerQuestoes != null) {
                vboxContainerQuestoes.getChildren().clear();
            }
            controllersQuestoes.clear();

            QuestaoDAO questaoDAO = new QuestaoDAO();
            List<Questao> questoes = questaoDAO.buscarQuestoesPorAplicacao(idAplicacao);

            System.out.println("DEBUG -> Questões encontradas para a aplicação " + idAplicacao + ": " + (questoes != null ? questoes.size() : 0));

            if (questoes == null || questoes.isEmpty()) {
                mostrarAlerta("Atenção", "Nenhuma questão encontrada para esta prova.");
                return;
            }

            for (Questao q : questoes) {
                FXMLLoader loader = null;
                Parent nodoQuestao = null;

                String tipo = q.getTipo() != null ? q.getTipo().toLowerCase().trim() : "";
                System.out.println("DEBUG -> Carregando questão ID: " + q.getId() + " | Tipo: " + tipo);

                if ("alt".equals(tipo)) {
                    // Caminho absoluto apontando exatamente para /org/example/
                    URL fxmlUrl = getClass().getResource("/org/example/item_questao_alternativa.fxml");
                    if (fxmlUrl == null) {
                        throw new IOException("Arquivo 'item_questao_alternativa.fxml' não encontrado em /org/example/!");
                    }

                    loader = new FXMLLoader(fxmlUrl);
                    nodoQuestao = loader.load();

                    QuestaoAlternativaController ctrl = loader.getController();
                    List<Resposta> respostas = new QuestaoDAO().buscarRespostas(q.getEnunciado(), q.getTipo());
                    ctrl.setDadosQuestao(q, respostas);
                    controllersQuestoes.add(ctrl);

                } else if ("diss".equals(tipo)) {
                    URL fxmlUrl = getClass().getResource("/org/example/item_questao_aberta.fxml");
                    if (fxmlUrl == null) {
                        throw new IOException("Arquivo 'item_questao_aberta.fxml' não encontrado em /org/example/!");
                    }

                    loader = new FXMLLoader(fxmlUrl);
                    nodoQuestao = loader.load();

                    QuestaoAbertaController ctrl = loader.getController();
                    ctrl.setDadosQuestao(q);
                    controllersQuestoes.add(ctrl);

                } else if ("rela".equals(tipo)) {
                    URL fxmlUrl = getClass().getResource("/org/example/item_questao_relacionamento.fxml");
                    if (fxmlUrl == null) {
                        throw new IOException("Arquivo 'item_questao_relacionamento.fxml' não encontrado em /org/example/!");
                    }

                    loader = new FXMLLoader(fxmlUrl);
                    nodoQuestao = loader.load();

                    QuestaoRelacionamentoController ctrl = loader.getController();
                    ctrl.setDadosQuestao(q);
                    controllersQuestoes.add(ctrl);
                }

                if (vboxContainerQuestoes != null && nodoQuestao != null) {
                    vboxContainerQuestoes.getChildren().add(nodoQuestao);
                } else {
                    System.out.println("AVISO: vboxContainerQuestoes ou nodoQuestao está nulo!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar as questões da prova: " + e.getMessage());
        }
    }

    @FXML
    void ir_finalizar_prova(ActionEvent event) {
        enviarProva();
    }

    private void enviarProva() {
        if (timeline != null) {
            timeline.stop();
        }

        mostrarAlerta("Sucesso", "Prova finalizada e enviada com sucesso!");

        try {
            App.setRoot("SelecaoProvaAluno");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            App.setRoot("realizar_prova_aluno");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de avaliação: " + e.getMessage());
        }
    }

    @FXML
    void ir_correcao(ActionEvent event) {
        try {
            App.setRoot("SelecaoProvaAluno");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de correção/gabarito: " + e.getMessage());
        }
    }

    @FXML
    void ir_desempenho(ActionEvent event) {

    }

    @FXML
    void ir_minha_agenda(ActionEvent event) {

    }
}
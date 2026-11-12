package org.example.ProvaAlunoArquivos;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.App;
import org.example.ProvaAlunoDAO;
import org.example.Questao;
import org.example.QuestaoDAO;
import org.example.Resposta;
import org.example.RespostaDAO;
import org.example.Sessao;
import org.example.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private final RespostaAlunoDAO respostaAlunoDAO = new RespostaAlunoDAO();

    private Long idAplicacaoAtual;
    private long provaIdAtual;
    private String raAlunoAtual;

    // true enquanto a prova está em andamento; ao sair/finalizar vira false e não volta mais
    private boolean provaAtiva = false;

    /**
     * Método executado automaticamente pelo JavaFX assim que a tela abre.
     * Descobre o aluno logado e a prova que está em andamento para ele; se ele já realizou
     * (ou saiu de) essa prova, não deixa refazer.
     */
    @FXML
    public void initialize() {
        atualizarLabelTempo();

        Usuario logado = Sessao.getUsuario();
        if (logado == null) {
            avisarEVoltar("Atenção", "Não foi possível identificar o aluno logado.", false);
            return;
        }

        try {
            raAlunoAtual = new ProvaAlunoDAO().buscarRa(logado.getEmail());
            if (raAlunoAtual == null) {
                avisarEVoltar("Atenção", "Não foi possível identificar o aluno logado.", false);
                return;
            }

            RespostaAlunoDAO.ProvaEmAndamento prova = respostaAlunoDAO.buscarProvaEmAndamento(raAlunoAtual);
            if (prova == null) {
                if (titulo_prova != null) {
                    titulo_prova.setText("Nenhuma prova disponível");
                }
                avisarEVoltar("Atenção", "Você não tem nenhuma prova em andamento no momento.", false);
                return;
            }

            if (respostaAlunoDAO.jaRealizou(raAlunoAtual, prova.idAplicacao)) {
                avisarEVoltar("Prova já realizada",
                        "Você já realizou esta prova e não pode refazê-la. Veja o resultado na aba Correção.", true);
                return;
            }

            inicializarDadosProva(prova.idAplicacao, prova.provaId, raAlunoAtual, prova.horarioFim, prova.nomeProva);
        } catch (SQLException e) {
            e.printStackTrace();
            avisarEVoltar("Erro", "Não foi possível carregar a prova: " + e.getMessage(), false);
        }
    }

    public void inicializarDadosProva(Long idAplicacao, long provaId, String raAluno, LocalTime horarioFim, String nomeProva) {
        this.idAplicacaoAtual = idAplicacao;
        this.provaIdAtual = provaId;
        this.raAlunoAtual = raAluno;

        if (titulo_prova != null) {
            titulo_prova.setText(nomeProva != null ? nomeProva : "Avaliação");
        }

        if (!carregarQuestoesDaProva(idAplicacao)) {
            return;
        }

        calcularTempoRestante(horarioFim);
        iniciarCronometro();

        // A partir daqui a prova está valendo: sair da tela exige confirmação e encerra a prova.
        provaAtiva = true;
        App.setGuardaNavegacao(this::confirmarSaida);
    }

    // Mostra o aviso depois que a tela já abriu; opcionalmente leva o aluno para a lista de provas realizadas.
    private void avisarEVoltar(String titulo, String mensagem, boolean irParaCorrecao) {
        Platform.runLater(() -> {
            mostrarAlerta(titulo, mensagem);
            if (irParaCorrecao) {
                try {
                    App.setRoot("SelecaoProvaAluno");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
        atualizarLabelTempo();
        timeline.play();
    }

    private void atualizarLabelTempo() {
        // Minutos cheios (sem horas): 1h30 aparece como 90:00
        long minutos = segundosRestantes / 60;
        long segundos = segundosRestantes % 60;

        if (contagem_regressiva_prova != null) {
            contagem_regressiva_prova.setText(String.format("%02d:%02d", minutos, segundos));
        }
    }

    private void tempoEsgotado() {
        if (contagem_regressiva_prova != null) {
            contagem_regressiva_prova.setText("00:00");
        }
        // showAndWait não pode rodar dentro do callback da animação do cronômetro
        Platform.runLater(() -> {
            mostrarAlerta("Atenção", "O tempo da prova acabou! Suas respostas serão enviadas automaticamente.");
            enviarProva(null);
        });
    }

    private boolean carregarQuestoesDaProva(Long idAplicacao) {
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
                return false;
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
                    List<Resposta> pares = new QuestaoDAO().buscarRespostas(q.getEnunciado(), q.getTipo());
                    ctrl.setDadosQuestao(q, pares);
                    controllersQuestoes.add(ctrl);
                }

                if (vboxContainerQuestoes != null && nodoQuestao != null) {
                    vboxContainerQuestoes.getChildren().add(nodoQuestao);
                } else {
                    System.out.println("AVISO: vboxContainerQuestoes ou nodoQuestao está nulo!");
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao carregar as questões da prova: " + e.getMessage());
            return false;
        }
    }

    @FXML
    void ir_finalizar_prova(ActionEvent event) {
        enviarProva("Prova finalizada e enviada com sucesso!");
    }

    // Grava as respostas, encerra a prova e volta para a lista. Se a gravação falhar, a prova continua aberta.
    private void enviarProva(String mensagemSucesso) {
        if (!encerrarProva()) {
            return;
        }

        if (mensagemSucesso != null) {
            mostrarAlerta("Sucesso", mensagemSucesso);
        }

        try {
            App.setRoot("SelecaoProvaAluno");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Pop-up exibido quando o aluno tenta ir para qualquer outra tela (ou fechar a janela) durante a prova.
    // Devolve true se a navegação pode seguir (prova já encerrada e salva).
    private boolean confirmarSaida() {
        ButtonType sair = new ButtonType("Sair e encerrar a prova", ButtonBar.ButtonData.OK_DONE);
        ButtonType continuar = new ButtonType("Continuar a prova", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.WARNING,
                "Se você sair desta tela, a prova será encerrada, as respostas que você já marcou serão enviadas "
                        + "e você NÃO poderá voltar para continuar.\n\nDeseja sair mesmo assim?",
                sair, continuar);
        alerta.setTitle("Sair da prova");
        alerta.setHeaderText("Atenção: sua prova será encerrada");
        alerta.setOnShown(e -> alerta.getDialogPane().lookupButton(continuar).requestFocus());

        Optional<ButtonType> escolha = alerta.showAndWait();
        if (escolha.isPresent() && escolha.get() == sair) {
            return encerrarProva();
        }
        return false;
    }

    // Salva as respostas (uma linha por questão) e trava a prova. Só devolve true se tudo foi gravado.
    private boolean encerrarProva() {
        if (!provaAtiva) {
            return true;
        }

        try {
            respostaAlunoDAO.salvar(raAlunoAtual, idAplicacaoAtual, provaIdAtual, coletarRespostas());
        } catch (SQLException e) {
            e.printStackTrace();
            Alert erro = new Alert(Alert.AlertType.ERROR);
            erro.setTitle("Erro");
            erro.setHeaderText(null);
            erro.setContentText("Não foi possível salvar suas respostas: " + e.getMessage()
                    + "\n\nA prova continua aberta. Tente finalizar novamente.");
            erro.showAndWait();
            return false;
        }

        provaAtiva = false;
        if (timeline != null) {
            timeline.stop();
        }
        App.limparGuardaNavegacao();
        return true;
    }

    // Respostas do aluno, uma por questão. alt e rela já levam "correta" (o banco guarda NULL = ainda não
    // corrigida; a dissertativa quem corrige é o professor). O que ficou em branco é completado pelo banco.
    private List<RespostaAlunoDAO.RespostaParaSalvar> coletarRespostas() {
        List<RespostaAlunoDAO.RespostaParaSalvar> respostas = new ArrayList<>();
        for (Object ctrl : controllersQuestoes) {
            if (ctrl instanceof QuestaoAlternativaController) {
                QuestaoAlternativaController alt = (QuestaoAlternativaController) ctrl;
                Resposta marcada = alt.getRespostaSelecionada();
                respostas.add(new RespostaAlunoDAO.RespostaParaSalvar(alt.getQuestaoId(),
                        marcada != null ? marcada.getRespostaId() : null,
                        null,
                        marcada != null ? marcada.getCorreta() : null));
            } else if (ctrl instanceof QuestaoAbertaController) {
                QuestaoAbertaController aberta = (QuestaoAbertaController) ctrl;
                respostas.add(new RespostaAlunoDAO.RespostaParaSalvar(aberta.getQuestaoId(), null,
                        aberta.getRespostaDigitada(), null));
            } else if (ctrl instanceof QuestaoRelacionamentoController) {
                QuestaoRelacionamentoController rela = (QuestaoRelacionamentoController) ctrl;
                respostas.add(new RespostaAlunoDAO.RespostaParaSalvar(rela.getQuestaoId(), null,
                        rela.getRespostaMontada(), rela.getCorreta()));
            }
        }
        return respostas;
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
        // já é a tela de avaliação
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
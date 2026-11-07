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
import org.example.Questao;
import org.example.Resposta;
// Importe seus DAOs correspondentes para buscar do banco:
// import org.example.ProvaDAO;
// import org.example.QuestaoDAO;
// import org.example.RespostaDAO;

import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ProvaAlunoController {
    @FXML private Button bt_avaliacao, bt_correcao, bt_desempenho, bt_finalizar_prova, bt_minha_agenda;
    @FXML private Label contagem_regressiva_prova;
    @FXML private Label titulo_prova;
    @FXML private VBox vboxContainerQuestoes;

    private Timeline timeline;
    private long segundosRestantes = 0;

    // Lista para manter referência dos controllers das questões geradas (facilita coletar respostas)
    private final List<Object> controllersQuestoes = new ArrayList<>();

    private Long idAplicacaoAtual;
    private String raAlunoAtual;

    /**
     * Método chamado ao abrir a tela para injetar a prova do aluno
     */
    public void inicializarDadosProva(Long idAplicacao, String raAluno, LocalTime horarioFim, String nomeProva) {
        this.idAplicacaoAtual = idAplicacao;
        this.raAlunoAtual = raAluno;

        if (titulo_prova != null) {
            titulo_prova.setText(nomeProva != null ? nomeProva : "Avaliação");
        }

        // 1. Configurar Cronômetro Regressivo baseado no horário de fim da tabela prova_turma
        calcularTempoRestante(horarioFim);
        iniciarCronometro();

        // 2. Carregar Questões do Banco de Dados para esta Prova
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
            vboxContainerQuestoes.getChildren().clear();
            controllersQuestoes.clear();

            // EXEMPLO DE LÓGICA DE CARREGAMENTO (ajuste conforme seus DAOs):
            // List<Questao> questoes = questaoDAO.buscarQuestaoPorAplicacao(idAplicacao);
            //
            // Para cada questão, verifique o tipo e carregue o FXML correspondente:
            // for (Questao q : questoes) {
            //     FXMLLoader loader;
            //     Parent nodoQuestao;
            //
            //     if ("alt".equalsIgnoreCase(q.getTipo())) {
            //         loader = new FXMLLoader(getClass().getResource("item_questao_alternativa.fxml"));
            //         nodoQuestao = loader.load();
            //         QuestaoAlternativaController ctrl = loader.getController();
            //         List<Resposta> resp = respostaDAO.buscarRespostasPorQuestao(q.getId());
            //         ctrl.setDadosQuestao(q, resp);
            //         controllersQuestoes.add(ctrl);
            //     } else if ("diss".equalsIgnoreCase(q.getTipo())) {
            //         loader = new FXMLLoader(getClass().getResource("item_questao_aberta.fxml"));
            //         nodoQuestao = loader.load();
            //         QuestaoAbertaController ctrl = loader.getController();
            //         ctrl.setDadosQuestao(q);
            //         controllersQuestoes.add(ctrl);
            //     }
            //     // Adiciona o componente visual da questão dentro do VBox da tela do aluno
            //     vboxContainerQuestoes.getChildren().add(nodoQuestao);
            // }

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

        // Percorrer a lista de controllers gerados para coletar as respostas do aluno
        // e salvá-las na tabela resposta_aluno via DAO:
        // for (Object ctrl : controllersQuestoes) {
        //     if (ctrl instanceof QuestaoAlternativaController) {
        //         QuestaoAlternativaController ac = (QuestaoAlternativaController) ctrl;
        //         Resposta r = ac.getRespostaSelecionada();
        //         // Salvar no banco (idAplicacaoAtual, raAlunoAtual, ac.getQuestaoId(), r.getRespostaId(), ...)
        //     } else if (ctrl instanceof QuestaoAbertaController) {
        //         QuestaoAbertaController ac = (QuestaoAbertaController) ctrl;
        //         String texto = ac.getRespostaDigitada();
        //         // Salvar no banco (idAplicacaoAtual, raAlunoAtual, ac.getQuestaoId(), texto, ...)
        //     }
        // }

        mostrarAlerta("Sucesso", "Prova finalizada e enviada com sucesso!");
        // Fechar tela ou redirecionar
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML void ir_avaliacao(ActionEvent event) {}
    @FXML void ir_correcao(ActionEvent event) {}
    @FXML void ir_desempenho(ActionEvent event) {}
    @FXML void ir_minha_agenda(ActionEvent event) {}
}
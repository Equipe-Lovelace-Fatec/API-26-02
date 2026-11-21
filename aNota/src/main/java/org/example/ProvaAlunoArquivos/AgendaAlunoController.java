package org.example.ProvaAlunoArquivos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.ProvaAlunoDAO;
import org.example.Sessao;
import org.example.Usuario;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Tela "Minha agenda" do aluno: lista as provas que ainda não terminaram e mostra o resumo da selecionada.
// Quando a prova selecionada está liberada, o botão leva para a tela de avaliação.
public class AgendaAlunoController {

    @FXML private VBox v_box_pane_interno;
    @FXML private Label label_disciplina;
    @FXML private Label label_professor_ministra_disciplina;
    @FXML private Label label_data_agendada;
    @FXML private Label label_horario;
    @FXML private Label label_tempo_de_prova;
    @FXML private Label label_status_prova;
    @FXML private Button bt_iniciar_prova;

    private final ProvaAlunoDAO alunoDAO = new ProvaAlunoDAO();
    private final AgendaAlunoDAO agendaDAO = new AgendaAlunoDAO();
    private String raAluno;
    private CartaoProvaAgendaController cartaoSelecionado;

    @FXML
    private void initialize() {
        carregarLista();
    }

    // Monta a lista de cartões e seleciona o primeiro; sem provas, mostra só a mensagem
    private void carregarLista() {
        v_box_pane_interno.getChildren().clear();
        cartaoSelecionado = null;
        limparResumo();

        List<ProvaAgendada> provas = carregarProvas();
        if (provas.isEmpty()) {
            mostrarListaVazia();
            return;
        }
        for (ProvaAgendada prova : provas) {
            adicionarCartao(prova);
        }
    }

    private List<ProvaAgendada> carregarProvas() {
        try {
            Usuario logado = Sessao.getUsuario();
            raAluno = logado == null ? null : alunoDAO.buscarRa(logado.getEmail());
            if (raAluno == null) {
                avisar("Atenção", "Não foi possível identificar o aluno logado.");
                return List.of();
            }
            return agendaDAO.listarProvasAgendadas(raAluno);
        } catch (SQLException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível carregar suas provas: " + e.getMessage());
            return List.of();
        }
    }

    // Carrega o FXML do cartão (um por prova); o primeiro cartão criado já fica selecionado
    private void adicionarCartao(ProvaAgendada prova) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("pane_individual_prova_minha_agenda.fxml"));
            Node cartao = loader.load();
            CartaoProvaAgendaController controller = loader.getController();
            controller.setProva(prova, this::selecionarCartao);
            v_box_pane_interno.getChildren().add(cartao);
            if (cartaoSelecionado == null) {
                selecionarCartao(controller);
            }
        } catch (IOException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível carregar a prova " + prova.getNomeProva() + ": " + e.getMessage());
        }
    }

    private void mostrarListaVazia() {
        Label vazio = new Label("Você não tem provas agendadas");
        vazio.setStyle("-fx-font-size: 18px; -fx-text-fill: #737373;");
        vazio.setPadding(new Insets(20));
        v_box_pane_interno.getChildren().add(vazio);
    }

    // Só um cartão selecionado por vez; o resumo e o botão acompanham a seleção
    private void selecionarCartao(CartaoProvaAgendaController cartao) {
        if (cartaoSelecionado != null) {
            cartaoSelecionado.setSelecionado(false);
        }
        cartao.setSelecionado(true);
        cartaoSelecionado = cartao;
        mostrarResumo(cartao.getProva());
    }

    private void mostrarResumo(ProvaAgendada prova) {
        definirTextoComDica(label_disciplina, prova.getNomeDisciplina());
        definirTextoComDica(label_professor_ministra_disciplina, prova.getNomeProfessor());
        label_data_agendada.setText(prova.getDataExibicao());
        label_horario.setText(prova.getHorarioExibicao());
        label_tempo_de_prova.setText(prova.getTempoDeProvaExibicao());
        label_status_prova.setText(prova.getStatusExibicao());
        mostrarBotao(prova.isLiberada());
    }

    private void limparResumo() {
        definirTextoComDica(label_disciplina, "");
        definirTextoComDica(label_professor_ministra_disciplina, "");
        label_data_agendada.setText("");
        label_horario.setText("");
        label_tempo_de_prova.setText("");
        label_status_prova.setText("");
        mostrarBotao(false);
    }

    // Texto longo é cortado com reticências dentro do painel cinza; a dica mostra o texto inteiro
    private void definirTextoComDica(Label label, String texto) {
        label.setText(texto);
        label.setTooltip(texto.isEmpty() ? null : new Tooltip(texto));
    }

    private void mostrarBotao(boolean visivel) {
        bt_iniciar_prova.setVisible(visivel);
        bt_iniciar_prova.setManaged(visivel);
    }

    // Reconsulta o banco antes de abrir a prova: a janela pode ter terminado depois que a lista foi carregada
    @FXML
    void vai_para_prova_selecionada(ActionEvent event) {
        ProvaAgendada prova = cartaoSelecionado.getProva();
        try {
            if (agendaDAO.estaLiberada(raAluno, prova.getIdAplicacao())) {
                abrirTela("realizar_prova_aluno", "avaliação");
            } else {
                avisar("Prova encerrada", "O horário desta prova já terminou.");
                carregarLista();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível verificar a prova: " + e.getMessage());
        }
    }

    private void avisar(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void abrirTela(String fxml, String nomeDaTela) {
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível abrir a tela de " + nomeDaTela + ": " + e.getMessage());
        }
    }

    // ---- Menu superior ----

    @FXML
    void vai_minha_agenda(ActionEvent event) {
        // já é a tela de agenda
    }

    @FXML
    void vai_avaliacao(ActionEvent event) {
        abrirTela("realizar_prova_aluno", "avaliação");
    }

    @FXML
    void vai_correcao(ActionEvent event) {
        abrirTela("SelecaoProvaAluno", "correção");
    }

    @FXML
    void vai_desempenho(ActionEvent event) {
        abrirTela("desempenho_aluno", "desempenho");
    }
}

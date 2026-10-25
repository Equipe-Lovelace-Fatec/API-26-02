package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SelecaoProvaController {

    private static final double ALTURA_CARTAO = 136;
    private static final double ESPACO_ENTRE_CARTOES = 49;
    private static final double TOPO_LISTA = 118;
    private static final double MARGEM_INFERIOR = 30;

    @FXML private Pane painel_interno;
    @FXML private VBox lista_provas;

    private final ProvaListaDAO provaDAO = new ProvaListaDAO();

    @FXML
    public void initialize() {
        List<Prova> provas = carregarProvas();

        lista_provas.getChildren().clear();
        for (Prova p : provas) {
            lista_provas.getChildren().add(criarCartao(p));
        }

        int n = provas.size();
        double altura = TOPO_LISTA + n * ALTURA_CARTAO
                + Math.max(0, n - 1) * ESPACO_ENTRE_CARTOES + MARGEM_INFERIOR;
        painel_interno.setPrefHeight(Math.max(altura, 684));
    }

    private List<Prova> carregarProvas() {
        Usuario logado = Sessao.getUsuario();
        if (logado == null) {
            return new ArrayList<>();
        }
        try {
            return provaDAO.listarPorProfessor(logado.getEmail());
        } catch (SQLException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível carregar as provas do banco de dados.");
            return new ArrayList<>();
        }
    }

    private Node criarCartao(Prova p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CartaoProva.fxml"));
            Node cartao = loader.load();
            CartaoProvaController controller = loader.getController();
            controller.setProva(p, this::visualizar, this::agendar);
            return cartao;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Não foi possível carregar a prova " + p.getNomeProva());
        }
    }

    private void visualizar(Prova escolhida) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("visualizacao.fxml"));
            Parent root = loader.load();
            VisualizarProvaController visualizador = loader.getController();

            List<String> perguntas = new ArrayList<>();
            List<String> respostas = new ArrayList<>();
            RespostaDAO respostaDAO = new RespostaDAO();
            for (String[] q : provaDAO.listarQuestoes(escolhida.getProvaId())) {
                perguntas.add(q[0] + "\nValor: " + q[1]);
                String gabarito = String.join(" / ", respostaDAO.buscarRespostasPorEnunciado(q[0]));
                respostas.add(gabarito.isEmpty() ? "Nenhum gabarito cadastrado." : gabarito);
            }

            visualizador.setDadosProva(escolhida.getNomeProva(), escolhida.getNomeDisciplina(),
                    escolhida.getValor() == null ? "" : escolhida.getValor().toPlainString(), perguntas, respostas);

            Stage popup = new Stage();
            popup.setTitle("Visualização da Prova");
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.showAndWait();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível abrir a tela de visualização: " + e.getMessage());
        }
    }

    private void agendar(Prova escolhida) {
        if (!escolhida.temNota()) {
            avisar("Nota não definida", "A prova \"" + escolhida.getNomeProva()
                    + "\" ainda não tem nota. Você será levado à tela de montagem para indicá-la."
                    + " Depois, volte e clique em AGENDAR novamente.");
            try {
                App.abrirTela("montagem", "aNota", 1350, 1080);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        Sessao.setProvaParaAgendar(escolhida);
        try {
            App.abrirTela("agendamento", "aNota", 1350, 800);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        navegar("SelecaoProva");
    }

    @FXML
    void irAlunos_notas(ActionEvent event) {
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

    private void avisar(String titulo, String texto) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}

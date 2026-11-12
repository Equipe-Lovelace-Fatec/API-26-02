package org.example.ProvaAlunoArquivos;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.App;
import org.example.ProvaAlunoDAO;
import org.example.Sessao;
import org.example.Usuario;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

// Tela principal da seção "Correção" do aluno: uma lista com as provas que ele já realizou.
public class SelecaoProvaAlunoController {

    private static final double TOPO_LISTA = 14;
    private static final double ALTURA_CARTAO = 136;
    private static final double ESPACO_ENTRE_CARTOES = 49;
    private static final double MARGEM_INFERIOR = 40;
    private static final double ALTURA_MINIMA = 829;

    @FXML private Button bt_avaliacao;
    @FXML private Button bt_correcao;
    @FXML private Button bt_desempenho;
    @FXML private Button bt_minha_agenda;
    @FXML private ImageView img_fundoverde1350x1080;
    @FXML private VBox lista_provas;
    @FXML private Pane painel_interno;
    @FXML private Pane pane_principal;
    @FXML private ScrollPane scroll_principal;

    private final ProvaAlunoDAO dao = new ProvaAlunoDAO();
    private String raAluno;

    @FXML
    private void initialize() {
        lista_provas.getChildren().clear();

        List<ProvaRealizada> provas = carregarProvas();
        if (provas.isEmpty()) {
            Label vazio = new Label(raAluno == null
                    ? "Não foi possível identificar o aluno logado."
                    : "Você ainda não realizou nenhuma prova.");
            vazio.setStyle("-fx-font-size: 18px; -fx-text-fill: #737373;");
            vazio.setPadding(new javafx.geometry.Insets(20));
            lista_provas.getChildren().add(vazio);
            return;
        }

        for (ProvaRealizada p : provas) {
            lista_provas.getChildren().add(criarCartao(p));
        }

        int n = provas.size();
        double altura = TOPO_LISTA + n * ALTURA_CARTAO
                + Math.max(0, n - 1) * ESPACO_ENTRE_CARTOES + MARGEM_INFERIOR;
        painel_interno.setPrefHeight(Math.max(altura, ALTURA_MINIMA));
    }

    private List<ProvaRealizada> carregarProvas() {
        Usuario logado = Sessao.getUsuario();
        if (logado == null) {
            return List.of();
        }
        try {
            raAluno = dao.buscarRa(logado.getEmail());
            if (raAluno == null) {
                return List.of();
            }
            return dao.listarProvasRealizadas(raAluno);
        } catch (SQLException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível carregar suas provas: " + e.getMessage());
            return List.of();
        }
    }

    private Node criarCartao(ProvaRealizada p) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("CartaoProvaAluno.fxml"));
            Node cartao = loader.load();
            CartaoProvaAlunoController controller = loader.getController();
            controller.setProva(p, this::abrirGabarito, this::abrirCorrecao);
            return cartao;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Não foi possível carregar a prova " + p.getNomeProva());
        }
    }

    private void abrirGabarito(ProvaRealizada prova) {
        abrirTela(prova, "gabarito_para_aluno");
    }

    private void abrirCorrecao(ProvaRealizada prova) {
        abrirTela(prova, "correcao_para_aluno");
    }

    private void abrirTela(ProvaRealizada prova, String fxml) {
        ContextoProvaAluno.selecionar(raAluno, prova);
        try {
            App.setRoot(fxml);
        } catch (IOException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível abrir a tela: " + e.getMessage());
        }
    }

    private void avisar(String titulo, String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    // ---- Menu superior ----

    @FXML
    void ir_avaliacao(ActionEvent event) {
        try {
            App.setRoot("realizar_prova_aluno");
        } catch (IOException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível abrir a tela de avaliação: " + e.getMessage());
        }
    }

    @FXML
    void ir_correcao(ActionEvent event) {
        // já é a tela de correção
    }

    @FXML
    void ir_desempenho(ActionEvent event) {
        try {
            App.setRoot("desempenho_aluno");
        } catch (IOException e) {
            e.printStackTrace();
            avisar("Erro", "Não foi possível abrir a tela de desempenho: " + e.getMessage());
        }
    }

    @FXML
    void ir_minha_agenda(ActionEvent event) {

    }
}
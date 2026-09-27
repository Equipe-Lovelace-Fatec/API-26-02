package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.function.DoubleConsumer;

public class AlunoController {

    @FXML
    private Button bt_alunos;

    @FXML
    private Button bt_menu_ant;

    @FXML
    private Button bt_prova;

    @FXML
    private Button bt_questoes;

    @FXML
    private Button bt_salva;

    @FXML
    private HBox hbox_valor_questao;

    @FXML
    private HBox hbox_valor_questao1;

    @FXML
    private HBox hbox_valor_questao11;

    @FXML
    private Circle icone_turma;

    @FXML
    private Circle img_foto_perfil;

    @FXML
    private Pane painel_aluno;

    @FXML
    private Pane painel_cabecalho;

    @FXML
    private AnchorPane painel_fundo;

    @FXML
    private Pane painel_info_prova;

    @FXML
    private Pane painel_questao_1;

    @FXML
    private Pane painel_questao_11;

    @FXML
    private Pane painel_questao_111;

    @FXML
    private Pane painel_questoes;

    @FXML
    private Pane painel_status;

    @FXML
    private ScrollPane scroll_questao;

    @FXML
    private Separator separador;

    @FXML
    private Separator separador1;

    @FXML
    private Separator separador11;

    @FXML
    private TextArea txt_area_coment;

    @FXML
    private TextArea txt_area_coment1;

    @FXML
    private TextArea txt_area_coment11;

    @FXML
    private Text txt_curso;

    @FXML
    private Text txt_enunciado;

    @FXML
    private Text txt_enunciado1;

    @FXML
    private Text txt_enunciado11;

    @FXML
    private TextField txt_field_nota1;

    @FXML
    private TextField txt_field_nota2;

    @FXML
    private TextField txt_field_nota3;

    @FXML
    private Label txt_iconeturma_1;

    @FXML
    private Text txt_materia;

    @FXML
    private Text txt_nasc;

    @FXML
    private Text txt_nome_aluno;

    @FXML
    private Label txt_nota;

    @FXML
    private Label txt_perfil;

    @FXML
    private Text txt_periodo;

    @FXML
    private Text txt_questao;

    @FXML
    private Text txt_questao1;

    @FXML
    private Text txt_questao11;

    @FXML
    private Text txt_questao_num1;

    @FXML
    private Text txt_questao_num2;

    @FXML
    private Text txt_questao_num3;

    @FXML
    private Text txt_ra;

    @FXML
    private Text txt_resp_aluno;

    @FXML
    private Text txt_resp_aluno1;

    @FXML
    private Text txt_resp_esperada;

    @FXML
    private Text txt_resp_esperada1;

    @FXML
    private Text txt_resp_esperada11;

    @FXML
    private Text txt_resp_esperada111;

    @FXML
    private Text txt_resp_esperada12;

    @FXML
    private Text txt_semestre;

    @FXML
    private Label txt_status;

    @FXML
    private Text txt_titulo_coment;

    @FXML
    private Text txt_titulo_coment1;

    @FXML
    private Text txt_titulo_coment11;

    @FXML
    private Text txt_titulo_enunciado;

    @FXML
    private Text txt_titulo_enunciado1;

    @FXML
    private Text txt_titulo_enunciado11;

    @FXML
    private Text txt_titulo_pontuacao1;

    @FXML
    private Text txt_titulo_pontuacao2;

    @FXML
    private Text txt_titulo_pontuacao3;

    @FXML
    private Text txt_titulo_resp_aluno;

    @FXML
    private Text txt_titulo_resp_aluno1;

    @FXML
    private Text txt_titulo_resp_aluno11;

    @FXML
    private Text txt_titulo_resp_esperada;

    @FXML
    private Text txt_titulo_resp_esperada1;

    @FXML
    private Text txt_titulo_resp_esperada11;

    @FXML
    private Text txt_titulo_valor;

    @FXML
    private Text txt_titulo_valor1;

    @FXML
    private Text txt_titulo_valor11;

    @FXML
    private Text txt_valor_num;

    @FXML
    private Text txt_valor_num1;

    @FXML
    private Text txt_valor_num11;

    // Aluno fixo, só para teste (sem BD, sem tela de escolha de aluno ainda).
    private final Aluno aluno = new Aluno();

    @FXML
    private void initialize() {
        txt_field_nota1.textProperty().addListener((obs, antigo, novo) ->
                validarEAtualizar(txt_field_nota1, Aluno.VALOR_MAX_NOTA1, aluno::setNota1));
        txt_field_nota2.textProperty().addListener((obs, antigo, novo) ->
                validarEAtualizar(txt_field_nota2, Aluno.VALOR_MAX_NOTA2, aluno::setNota2));
        txt_field_nota3.textProperty().addListener((obs, antigo, novo) ->
                validarEAtualizar(txt_field_nota3, Aluno.VALOR_MAX_NOTA3, aluno::setNota3));

        validarEAtualizar(txt_field_nota1, Aluno.VALOR_MAX_NOTA1, aluno::setNota1);
        validarEAtualizar(txt_field_nota2, Aluno.VALOR_MAX_NOTA2, aluno::setNota2);
        validarEAtualizar(txt_field_nota3, Aluno.VALOR_MAX_NOTA3, aluno::setNota3);
    }

    @FXML
    void salvanotas(ActionEvent event) {
        double total = aluno.getTotalNotas();

        if (txt_status != null) {
            txt_status.setText(String.format("Notas salvas. Total: %.2f", total));
        }

        System.out.println("Nota 1: " + aluno.getNota1());
        System.out.println("Nota 2: " + aluno.getNota2());
        System.out.println("Nota 3: " + aluno.getNota3());
        System.out.println("Total: " + total);

        // Quando tiver BD, aqui entra o INSERT/UPDATE.
    }

    @FXML
    void vaialunos(ActionEvent event) {
        // Já estamos na tela de alunos, não faz nada.
    }

    @FXML
    void vainota1(ActionEvent event) {
    }

    @FXML
    void vainota2(ActionEvent event) {

    }

    @FXML
    void vainota3(ActionEvent event) {
    }
    private void validarEAtualizar(TextField campo, double max, DoubleConsumer setter) {
        String texto = campo.getText().trim();
        if (texto.isEmpty() || texto.equals("-") || texto.equals(".") || texto.equals(",")) {
            return;
        }

        try {
            double valor = Double.parseDouble(texto.replace(",", "."));

            if (valor < 0 || valor > max) {
                mostrarAlerta(String.format("A nota deve estar entre 0 e %.1f.", max));
                campo.clear();
                return;
            }

            setter.accept(valor);
            atualizarTotal();

        } catch (NumberFormatException e) {
            mostrarAlerta("Digite um número válido (ex: 1.5).");
            campo.clear();
        }
    }

    private void atualizarTotal() {
        if (txt_status != null) {
            txt_status.setText(String.format("Total: %.2f", aluno.getTotalNotas()));
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Valor inválido");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    @FXML
    void vaiprova(ActionEvent event) throws IOException {
        App.setRoot("montagem");
    }

    @FXML
    void vaiquestao(ActionEvent event) {
        /*    trocar tela */
    }

    @FXML
    void vaitelaescolheralunos(ActionEvent event) {
        /*    trocar tela */
    }
}
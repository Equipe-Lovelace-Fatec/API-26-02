
package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private Questao questaoEmEdicao;
    private String enunciadoOriginal;
    private String tipoOriginal;

    private static int proximaPosicao = 0;

    private final QuestaoDAO questaoDAO = new QuestaoDAO();

    @FXML private Button btMultipla;
    @FXML private Button btRelac;
    @FXML private Button btSalvar;
    @FXML private Button btAberta;

    @FXML private TextArea txtPergunta;
    @FXML private TextArea txtGabarito;

    @FXML private TextArea txtAlternativa1;
    @FXML private TextArea txtAlternativa2;
    @FXML private TextArea txtAlternativa3;
    @FXML private TextArea txtAlternativa4;

    @FXML private CheckBox checkAlternativa1;
    @FXML private CheckBox checkAlternativa2;
    @FXML private CheckBox checkAlternativa3;
    @FXML private CheckBox checkAlternativa4;

    @FXML private TextArea txtConceito1;
    @FXML private TextArea txtConceito2;
    @FXML private TextArea txtConceito3;
    @FXML private TextArea txtConceito4;

    @FXML private ComboBox<String> comboRelacao1;
    @FXML private ComboBox<String> comboRelacao2;
    @FXML private ComboBox<String> comboRelacao3;
    @FXML private ComboBox<String> comboRelacao4;

    @FXML
    public void abrirMultipla(ActionEvent event) throws IOException {
        abrirTela(event, "/org/example/CadastroQuestoesAlt.fxml");
    }

    @FXML
    public void abrirRelac(ActionEvent event) throws IOException {
        abrirTela(event, "/org/example/CadastroQuestoesRelac.fxml");
    }

    @FXML
    public void abrirAberta(ActionEvent event) throws IOException {
        abrirTela(event, "/org/example/CadastroQuestoes.fxml");
    }

    private void abrirTela(ActionEvent event, String caminho)
            throws IOException {

        Parent root = FXMLLoader.load(
                getClass().getResource(caminho)
        );

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void initialize() {
        adicionarOpcoes(comboRelacao1);
        adicionarOpcoes(comboRelacao2);
        adicionarOpcoes(comboRelacao3);
        adicionarOpcoes(comboRelacao4);
    }

    private void adicionarOpcoes(ComboBox<String> combo) {
        if (combo != null && combo.getItems().isEmpty()) {
            combo.getItems().addAll(
                    "Alternativa 1",
                    "Alternativa 2",
                    "Alternativa 3",
                    "Alternativa 4"
            );
        }
    }

    // Prepara a tela para editar uma questão existente.
    public void prepararEdicao(Questao questao) {
        this.questaoEmEdicao = questao;
        this.enunciadoOriginal = questao.getEnunciado();
        this.tipoOriginal = questao.getTipo();

        if (txtPergunta != null) {
            txtPergunta.setText(questao.getEnunciado());
        }

        carregarRespostasDaQuestao();
    }

    private void carregarRespostasDaQuestao() {
        if (questaoEmEdicao == null) {
            return;
        }

        List<Resposta> respostas = questaoDAO.buscarRespostas(
                enunciadoOriginal,
                tipoOriginal
        );

        if ("diss".equals(tipoOriginal)) {
            if (txtGabarito != null && !respostas.isEmpty()) {
                txtGabarito.setText(respostas.get(0).getTexto());
            }

        } else if ("alt".equals(tipoOriginal)) {
            TextArea[] alternativas = {
                    txtAlternativa1,
                    txtAlternativa2,
                    txtAlternativa3,
                    txtAlternativa4
            };

            CheckBox[] checks = {
                    checkAlternativa1,
                    checkAlternativa2,
                    checkAlternativa3,
                    checkAlternativa4
            };

            for (int i = 0; i < respostas.size() && i < 4; i++) {
                Resposta resposta = respostas.get(i);

                if (alternativas[i] != null) {
                    alternativas[i].setText(resposta.getTexto());
                }

                if (checks[i] != null) {
                    checks[i].setSelected(
                            Boolean.TRUE.equals(resposta.getCorreta())
                    );
                }
            }

        } else if ("rela".equals(tipoOriginal)) {
            TextArea[] conceitos = {
                    txtConceito1,
                    txtConceito2,
                    txtConceito3,
                    txtConceito4
            };

            ComboBox<String>[] combos = new ComboBox[]{
                    comboRelacao1,
                    comboRelacao2,
                    comboRelacao3,
                    comboRelacao4
            };

            for (int i = 0; i < respostas.size() && i < 4; i++) {
                Resposta resposta = respostas.get(i);

                if (conceitos[i] != null) {
                    conceitos[i].setText(resposta.getConceito());
                }

                if (combos[i] != null) {
                    combos[i].setValue(resposta.getTexto());
                }
            }
        }
    }

    @FXML
    public void salvarQuestao(ActionEvent event) {
        String pergunta = txtPergunta == null
                ? ""
                : txtPergunta.getText().trim();

        if (pergunta.isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campo obrigatório",
                    "Digite o enunciado da questão."
            );
            return;
        }

        try {
            if (questaoEmEdicao != null) {
                salvarEdicao();
            } else {
                salvarNovaQuestaoNaMontagem();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao salvar",
                    "Não foi possível salvar a questão. Verifique os dados e a conexão com o banco."
            );
        }
    }

    private void salvarEdicao() {
        String pergunta = txtPergunta.getText().trim();

        if (pergunta.isEmpty()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Campo obrigatório",
                    "Digite o enunciado da questão."
            );
            return;
        }

        if (pergunta.length() > 100) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Enunciado muito longo",
                    "O enunciado deve ter no máximo 100 caracteres."
            );
            return;
        }

        List<Resposta> novasRespostas = new ArrayList<>();

        // Questão dissertativa
        if ("diss".equals(tipoOriginal)) {
            String gabarito = txtGabarito.getText().trim();

            if (gabarito.isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Gabarito obrigatório",
                        "Preencha o gabarito da questão."
                );
                return;
            }

            Resposta resposta = new Resposta();
            resposta.setEnunciado(pergunta);
            resposta.setTipo(tipoOriginal);
            resposta.setTexto(gabarito);
            resposta.setCorreta(null);
            resposta.setConceito(null);

            novasRespostas.add(resposta);

            // Questão de múltipla escolha
        } else if ("alt".equals(tipoOriginal)) {
            if (!validarAlternativas()) {
                return;
            }

            TextArea[] alternativas = {
                    txtAlternativa1,
                    txtAlternativa2,
                    txtAlternativa3,
                    txtAlternativa4
            };

            CheckBox[] checks = {
                    checkAlternativa1,
                    checkAlternativa2,
                    checkAlternativa3,
                    checkAlternativa4
            };

            for (int i = 0; i < alternativas.length; i++) {
                Resposta resposta = new Resposta();
                resposta.setEnunciado(pergunta);
                resposta.setTipo(tipoOriginal);
                resposta.setTexto(alternativas[i].getText().trim());
                resposta.setCorreta(checks[i].isSelected());
                resposta.setConceito(null);

                novasRespostas.add(resposta);
            }

            // Questão de relacionar colunas
        } else if ("rela".equals(tipoOriginal)) {
            if (!validarRelacoes()) {
                return;
            }

            TextArea[] conceitos = {
                    txtConceito1,
                    txtConceito2,
                    txtConceito3,
                    txtConceito4
            };

            ComboBox<String>[] combos = new ComboBox[]{
                    comboRelacao1,
                    comboRelacao2,
                    comboRelacao3,
                    comboRelacao4
            };

            for (int i = 0; i < conceitos.length; i++) {
                Resposta resposta = new Resposta();
                resposta.setEnunciado(pergunta);
                resposta.setTipo(tipoOriginal);
                resposta.setTexto(combos[i].getValue());
                resposta.setConceito(conceitos[i].getText().trim());
                resposta.setCorreta(null);

                novasRespostas.add(resposta);
            }
        }

        try {
            questaoEmEdicao.setEnunciado(pergunta);

            questaoDAO.atualizarQuestao(
                    questaoEmEdicao,
                    enunciadoOriginal,
                    tipoOriginal,
                    novasRespostas
            );

            // Atualiza a referência para os dados recém-salvos.
            enunciadoOriginal = pergunta;

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Edição salva",
                    "A questão e suas respostas foram atualizadas."
            );

        } catch (RuntimeException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao salvar",
                    "Não foi possível atualizar a questão. Verifique se o enunciado já existe e confira a conexão com o banco."
            );
        }
    }

    private void salvarNovaQuestaoNaMontagem() {
        String pergunta = txtPergunta.getText().trim();
        String gabarito;

        if (txtGabarito != null) {
            gabarito = txtGabarito.getText().trim();

        } else if (txtAlternativa1 != null) {
            if (!validarAlternativas()) {
                return;
            }

            gabarito = obterAlternativasCorretas();

        } else if (txtConceito1 != null) {
            if (!validarRelacoes()) {
                return;
            }

            gabarito = montarGabaritoRelacoes();

        } else {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Não foi possível identificar o tipo da questão."
            );
            return;
        }

        adicionarQuestaoNaMontagem(pergunta, gabarito);
    }

    private boolean validarAlternativas() {
        TextArea[] alternativas = {
                txtAlternativa1,
                txtAlternativa2,
                txtAlternativa3,
                txtAlternativa4
        };

        CheckBox[] checks = {
                checkAlternativa1,
                checkAlternativa2,
                checkAlternativa3,
                checkAlternativa4
        };

        boolean temCorreta = false;

        for (int i = 0; i < alternativas.length; i++) {
            if (alternativas[i] == null
                    || alternativas[i].getText().trim().isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Alternativa vazia",
                        "Preencha as quatro alternativas."
                );
                return false;
            }

            if (checks[i] != null && checks[i].isSelected()) {
                temCorreta = true;
            }
        }

        if (!temCorreta) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Gabarito obrigatório",
                    "Selecione pelo menos uma alternativa correta."
            );
            return false;
        }

        return true;
    }

    private String obterAlternativasCorretas() {
        List<String> corretas = new ArrayList<>();

        if (checkAlternativa1.isSelected()) {
            corretas.add(txtAlternativa1.getText().trim());
        }
        if (checkAlternativa2.isSelected()) {
            corretas.add(txtAlternativa2.getText().trim());
        }
        if (checkAlternativa3.isSelected()) {
            corretas.add(txtAlternativa3.getText().trim());
        }
        if (checkAlternativa4.isSelected()) {
            corretas.add(txtAlternativa4.getText().trim());
        }

        return String.join(", ", corretas);
    }

    private boolean validarRelacoes() {
        TextArea[] conceitos = {
                txtConceito1,
                txtConceito2,
                txtConceito3,
                txtConceito4
        };

        ComboBox<String>[] combos = new ComboBox[]{
                comboRelacao1,
                comboRelacao2,
                comboRelacao3,
                comboRelacao4
        };

        for (int i = 0; i < conceitos.length; i++) {
            if (conceitos[i] == null
                    || conceitos[i].getText().trim().isEmpty()
                    || combos[i] == null
                    || combos[i].getValue() == null) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Relação incompleta",
                        "Preencha todos os conceitos e selecione suas relações."
                );
                return false;
            }
        }

        return true;
    }

    private String montarGabaritoRelacoes() {
        return txtConceito1.getText().trim() + " → "
                + comboRelacao1.getValue() + "\n"
                + txtConceito2.getText().trim() + " → "
                + comboRelacao2.getValue() + "\n"
                + txtConceito3.getText().trim() + " → "
                + comboRelacao3.getValue() + "\n"
                + txtConceito4.getText().trim() + " → "
                + comboRelacao4.getValue();
    }

    private void adicionarQuestaoNaMontagem(
            String pergunta, String gabarito) {

        if (proximaPosicao >= 10) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Limite atingido",
                    "A montagem permite até 10 questões."
            );
            return;
        }

        // A integração com a tela de montagem permanece como estava.
        proximaPosicao++;

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Questão salva",
                "Questão adicionada à montagem."
        );
    }

    private void mostrarAlerta(
            Alert.AlertType tipo, String titulo, String mensagem) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    public void irAlunos(ActionEvent actionEvent) throws IOException {
        App.setRoot("Aluno");
    }

    public void irProva(ActionEvent actionEvent) throws IOException {
        App.setRoot("montagem");
    }
}

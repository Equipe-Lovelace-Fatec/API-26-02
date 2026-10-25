package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Controller {
    private Questao questaoEmEdicao;
    private String enunciadoOriginal;
    private String tipoOriginal;

    private final QuestaoDAO questaoDAO = new QuestaoDAO();
    private final RespostaDAO respostaDAO = new RespostaDAO();
    private final DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    @FXML
    private Button btMultipla;
    @FXML
    private Button btRelac;
    @FXML
    private Button btSalvar;
    @FXML
    private Button btAberta;

    @FXML
    private TextArea txtPergunta;
    @FXML
    private TextArea txtGabarito;

    @FXML
    private ComboBox<String> comboDisciplina;
    @FXML
    private ComboBox<String> comboTema;
    @FXML
    private ComboBox<String> comboDificuldade;

    @FXML
    private TextArea txtAlternativa1;
    @FXML
    private TextArea txtAlternativa2;
    @FXML
    private TextArea txtAlternativa3;
    @FXML
    private TextArea txtAlternativa4;

    @FXML
    private CheckBox checkAlternativa1;
    @FXML
    private CheckBox checkAlternativa2;
    @FXML
    private CheckBox checkAlternativa3;
    @FXML
    private CheckBox checkAlternativa4;

    @FXML
    private VBox painelPares;

    @FXML
    private TextArea txtTexto1;
    @FXML
    private TextArea txtTexto2;
    @FXML
    private TextArea txtTexto3;
    @FXML
    private TextArea txtTexto4;

    @FXML
    private TextArea txtConceito1;
    @FXML
    private TextArea txtConceito2;
    @FXML
    private TextArea txtConceito3;
    @FXML
    private TextArea txtConceito4;

    @FXML
    private ComboBox<String> comboAssociacao1;
    @FXML
    private ComboBox<String> comboAssociacao2;
    @FXML
    private ComboBox<String> comboAssociacao3;
    @FXML
    private ComboBox<String> comboAssociacao4;

    @FXML
    public void abrirMultipla(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoesAlt");
    }

    @FXML
    public void abrirRelac(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoesRelac");
    }

    @FXML
    public void abrirAberta(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoes");
    }

    @FXML
    public void initialize() {
        if (comboDificuldade != null) {
            comboDificuldade.getItems().setAll(
                    "Fácil",
                    "Média",
                    "Difícil"
            );
        }

        if (comboAssociacao1 != null) {
            configurarAssociacoes();
        }

        carregarDisciplinasETemas();
        configurarCheckboxesExclusivos();
    }

    private void configurarAssociacoes() {
        List<String> opcoes = List.of(
                "Conceito 1",
                "Conceito 2",
                "Conceito 3",
                "Conceito 4"
        );

        for (ComboBox<String> combo : combosAssociacao()) {
            if (combo != null) {
                combo.getItems().setAll(opcoes);
            }
        }
    }


    private void carregarDisciplinasETemas() {
        String emailProfessor = Sessao.getUsuario() != null
                ? Sessao.getUsuario().getEmail()
                : "giuliano.bertoti@prof.fatec.com";

        if (comboDisciplina != null) {
            try {
                comboDisciplina.getItems().setAll(
                        disciplinaDAO.listarPorProfessor(emailProfessor));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        if (comboTema != null) {
            try {
                List<Questao> doProfessor = questaoDAO.buscarQuestoes().stream()
                        .filter(q -> emailProfessor.equals(q.getEmailProfessor()))
                        .collect(Collectors.toList());

                comboTema.getItems().setAll(valoresDistintos(doProfessor, false));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> valoresDistintos(List<Questao> questoes, boolean disciplina) {
        return questoes.stream()
                .map(q -> disciplina ? q.getNomeDisciplina() : q.getTema())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


    private void configurarCheckboxesExclusivos() {
        CheckBox[] checks = checksAlternativas();

        for (CheckBox check : checks) {
            if (check == null) {
                return;
            }
        }

        for (CheckBox atual : checks) {
            atual.setOnAction(event -> {
                if (atual.isSelected()) {
                    for (CheckBox outro : checks) {
                        if (outro != atual) {
                            outro.setSelected(false);
                        }
                    }
                }
            });
        }
    }

    private TextArea[] alternativas() {
        return new TextArea[]{
                txtAlternativa1, txtAlternativa2,
                txtAlternativa3, txtAlternativa4
        };
    }

    private CheckBox[] checksAlternativas() {
        return new CheckBox[]{
                checkAlternativa1, checkAlternativa2,
                checkAlternativa3, checkAlternativa4
        };
    }

    private List<TextArea> textosPares() {
        return Arrays.asList(txtTexto1, txtTexto2, txtTexto3, txtTexto4);
    }

    private List<TextArea> conceitosPares() {
        return Arrays.asList(txtConceito1, txtConceito2, txtConceito3, txtConceito4);
    }

    private List<ComboBox<String>> combosAssociacao() {
        return Arrays.asList(
                comboAssociacao1, comboAssociacao2,
                comboAssociacao3, comboAssociacao4
        );
    }

    private String valorDe(ComboBox<String> combo) {
        return combo != null ? combo.getValue() : null;
    }

    public void prepararEdicao(Questao questao) {
        this.questaoEmEdicao = questao;
        this.enunciadoOriginal = questao.getEnunciado();
        this.tipoOriginal = questao.getTipo();

        if (txtPergunta != null) {
            txtPergunta.setText(questao.getEnunciado());
        }

        if (comboDificuldade != null && questao.getDificuldade() != null) {
            comboDificuldade.setValue(questao.getDificuldade());
        }

        if (comboDisciplina != null && questao.getNomeDisciplina() != null) {
            comboDisciplina.setValue(questao.getNomeDisciplina());
        }

        if (comboTema != null && questao.getTema() != null) {
            comboTema.setValue(questao.getTema());
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
            TextArea[] alternativas = alternativas();
            CheckBox[] checks = checksAlternativas();

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

            List<TextArea> textos = textosPares();
            List<TextArea> conceitos = conceitosPares();
            List<ComboBox<String>> combos = combosAssociacao();

            for (int i = 0; i < respostas.size() && i < 4; i++) {
                Resposta resposta = respostas.get(i);

                if (textos.get(i) != null) {
                    textos.get(i).setText(resposta.getTexto());
                }

                if (conceitos.get(i) != null) {
                    conceitos.get(i).setText(resposta.getConceito());
                }

                if (combos.get(i) != null) {
                    combos.get(i).setValue("Conceito " + (i + 1));
                }
            }
        }
    }

    @FXML
    public void salvarQuestao(ActionEvent event) {
        if (!validarCamposObrigatorios()) {
            return;
        }

        try {
            if (questaoEmEdicao != null) {
                salvarEdicao();
            } else {
                salvarNovaQuestao();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao salvar",
                    "Não foi possível salvar a questão. Verifique se já existe uma questão com esse enunciado e confira a conexão com o banco."
                            + detalheDoErro(e)
            );
        }
    }

    private boolean validarCamposObrigatorios() {
        if (txtPergunta == null
                || txtPergunta.getText().trim().isEmpty()) {
            mostrarErro("Preencha o enunciado da questão.");
            return false;
        }

        if (txtPergunta.getText().trim().length() > 100) {
            mostrarErro("O enunciado deve ter no máximo 100 caracteres.");
            return false;
        }

        if (comboDificuldade != null
                && comboDificuldade.getValue() == null) {
            mostrarErro("Selecione a dificuldade da questão.");
            return false;
        }

        if (comboDisciplina != null
                && comboDisciplina.getValue() == null) {
            mostrarErro("Selecione a disciplina da questão.");
            return false;
        }

        return true;
    }

    private void salvarNovaQuestao() {
        String tipo = detectarTipoNovaQuestao();

        if (tipo == null) {
            mostrarErro("Não foi possível identificar o tipo da questão.");
            return;
        }

        String pergunta = txtPergunta.getText().trim();

        List<Resposta> respostas = montarRespostas(pergunta, tipo);

        if (respostas == null) {
            return;
        }


        String emailProfessor = Sessao.getUsuario() != null
                ? Sessao.getUsuario().getEmail()
                : "giuliano.bertoti@prof.fatec.com";


        questaoDAO.nova(
                pergunta,
                valorDe(comboTema),
                tipo,
                valorDe(comboDificuldade),
                emailProfessor,
                valorDe(comboDisciplina)
        );

        try {
            for (Resposta r : respostas) {
                respostaDAO.novaResposta(
                        r.getEnunciado(),
                        r.getTipo(),
                        r.getTexto(),
                        r.getCorreta(),
                        r.getConceito()
                );
            }
        } catch (RuntimeException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Respostas não salvas",
                    "A questão foi criada, mas houve erro ao salvar as respostas. "
                            + "Abra a questão em Questões > Consultar e salve de novo para refazer as respostas."
                            + detalheDoErro(e)
            );
            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Questão salva",
                "Questão salva com sucesso!"
        );

        limparCampos();
    }

    private void limparCampos() {
        if (txtPergunta != null) txtPergunta.clear();
        if (txtGabarito != null) txtGabarito.clear();

        for (TextArea alt : alternativas()) {
            if (alt != null) alt.clear();
        }

        for (CheckBox check : checksAlternativas()) {
            if (check != null) check.setSelected(false);
        }

        for (TextArea t : textosPares()) {
            if (t != null) t.clear();
        }

        for (TextArea c : conceitosPares()) {
            if (c != null) c.clear();
        }

        for (ComboBox<String> combo : combosAssociacao()) {
            if (combo != null) combo.setValue(null);
        }

        if (comboDisciplina != null) comboDisciplina.setValue(null);
        if (comboTema != null) comboTema.setValue(null);
        if (comboDificuldade != null) comboDificuldade.setValue(null);
    }

    private String detectarTipoNovaQuestao() {
        if (txtGabarito != null) {
            return "diss";
        }

        if (txtAlternativa1 != null) {
            return "alt";
        }

        if (painelPares != null) {
            return "rela";
        }

        return null;
    }

    private void salvarEdicao() {
        String pergunta = txtPergunta.getText().trim();

        List<Resposta> novasRespostas = montarRespostas(pergunta, tipoOriginal);

        if (novasRespostas == null) {
            return;
        }

        String dificuldadeAnterior = questaoEmEdicao.getDificuldade();
        String temaAnterior = questaoEmEdicao.getTema();
        String disciplinaAnterior = questaoEmEdicao.getNomeDisciplina();

        try {
            questaoEmEdicao.setEnunciado(pergunta);

            if (valorDe(comboDificuldade) != null) {
                questaoEmEdicao.setDificuldade(valorDe(comboDificuldade));
            }

            if (valorDe(comboTema) != null) {
                questaoEmEdicao.setTema(valorDe(comboTema));
            }

            if (valorDe(comboDisciplina) != null) {
                questaoEmEdicao.setNomeDisciplina(valorDe(comboDisciplina));
            }

            questaoDAO.atualizarQuestao(
                    questaoEmEdicao,
                    enunciadoOriginal,
                    tipoOriginal,
                    novasRespostas
            );

            enunciadoOriginal = pergunta;

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Edição salva",
                    "A questão e suas respostas foram atualizadas."
            );

        } catch (RuntimeException e) {
            e.printStackTrace();

            questaoEmEdicao.setEnunciado(enunciadoOriginal);
            questaoEmEdicao.setDificuldade(dificuldadeAnterior);
            questaoEmEdicao.setTema(temaAnterior);
            questaoEmEdicao.setNomeDisciplina(disciplinaAnterior);

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao salvar",
                    "Não foi possível atualizar a questão. Verifique se o enunciado já existe e confira a conexão com o banco."
                            + detalheDoErro(e)
            );
        }
    }

    private List<Resposta> montarRespostas(String pergunta, String tipo) {
        List<Resposta> respostas = new ArrayList<>();

        if ("diss".equals(tipo)) {
            String gabarito = txtGabarito.getText().trim();

            if (gabarito.isEmpty()) {
                mostrarErro("Preencha a resposta da questão.");
                return null;

            }

            respostas.add(criarResposta(pergunta, tipo, gabarito, null, null));

        } else if ("alt".equals(tipo)) {
            if (!validarAlternativas()) {
                return null;
            }

            TextArea[] alternativas = alternativas();
            CheckBox[] checks = checksAlternativas();

            for (int i = 0; i < alternativas.length; i++) {
                String texto = alternativas[i].getText().trim();
                if (texto.isEmpty()) {
                    continue;
                }
                respostas.add(criarResposta(
                        pergunta, tipo, texto, checks[i].isSelected(), null));
            }

        } else if ("rela".equals(tipo)) {
            List<String[]> pares = coletarPares();

            if (pares == null) {
                return null;
            }

            for (String[] par : pares) {
                respostas.add(criarResposta(pergunta, tipo, par[0], null, par[1]));
            }

        } else {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Não foi possível identificar o tipo da questão."
            );
            return null;
        }

        return respostas;
    }

    private Resposta criarResposta(String enunciado, String tipo, String texto,
                                   Boolean correta, String conceito) {
        Resposta resposta = new Resposta();
        resposta.setEnunciado(enunciado);
        resposta.setTipo(tipo);
        resposta.setTexto(texto);
        resposta.setCorreta(correta);
        resposta.setConceito(conceito);
        return resposta;
    }

    private boolean validarAlternativas() {
        TextArea[] alternativas = alternativas();
        CheckBox[] checks = checksAlternativas();

        int preenchidas = 0;
        boolean temCorreta = false;

        for (int i = 0; i < alternativas.length; i++) {
            String texto = alternativas[i].getText().trim();

            if (texto.isEmpty()) {
                if (checks[i].isSelected()) {
                    mostrarErro("A alternativa " + (i + 1) + " está marcada como correta, mas está vazia.");
                    return false;
                }
                continue;
            }

            if (texto.length() > 100) {
                mostrarErro("A alternativa " + (i + 1) + " tem mais de 100 caracteres.");
                return false;
            }

            preenchidas++;
            if (checks[i].isSelected()) {
                temCorreta = true;
            }
        }

        if (preenchidas < 2) {
            mostrarErro("Preencha pelo menos duas alternativas.");
            return false;
        }

        if (!temCorreta) {
            mostrarErro("Selecione a alternativa correta.");
            return false;
        }

        return true;
    }

    private List<String[]> coletarPares() {
        List<TextArea> textos = textosPares();
        List<TextArea> conceitos = conceitosPares();
        List<ComboBox<String>> associacoes = combosAssociacao();

        Set<String> associacoesUsadas = new HashSet<>();
        List<String[]> pares = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String texto = textos.get(i).getText().trim();
            String conceito = conceitos.get(i).getText().trim();
            String associacao = associacoes.get(i).getValue();

            if (texto.isEmpty() && conceito.isEmpty() && associacao == null) {
                continue;
            }

            if (texto.isEmpty() || conceito.isEmpty() || associacao == null) {
                mostrarErro("Complete o texto, o conceito e a associação do par " + (i + 1) + ".");
                return null;
            }

            int numeroConceito;

            try {
                numeroConceito = Integer.parseInt(
                        associacao.replace("Conceito ", "").trim()
                );
            } catch (NumberFormatException e) {
                mostrarErro("Selecione uma associação válida.");
                return null;
            }

            if (numeroConceito < 1 || numeroConceito > 4) {
                mostrarErro("Selecione uma associação válida.");
                return null;
            }

            String conceitoAssociado =
                    conceitos.get(numeroConceito - 1).getText().trim();

            if (conceitoAssociado.isEmpty()) {
                mostrarErro("O Texto " + (i + 1) + " está associado a um conceito vazio.");
                return null;
            }

            if (!associacoesUsadas.add(associacao)) {
                mostrarErro("Cada conceito deve ser associado a apenas um texto.");
                return null;
            }

            pares.add(new String[]{texto, conceitoAssociado});
        }

        if (pares.size() < 2) {
            mostrarErro("Preencha pelo menos dois pares completos, com texto, conceito e associação.");
            return null;
        }

        return pares;
    }

    private void mostrarErro(String mensagem) {
        mostrarAlerta(
                Alert.AlertType.WARNING,
                "Verifique a questão",
                mensagem
        );
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private String detalheDoErro(RuntimeException e) {
        Throwable causa = e.getCause() != null ? e.getCause() : e;

        if (causa.getMessage() == null) {
            return "";
        }

        return "\n\nDetalhe técnico: " + causa.getMessage();
    }


    @FXML
    void IrProva_montar(ActionEvent event) throws IOException { App.setRoot("montagem"); }

    @FXML void IrQuestao_criar(ActionEvent event) throws IOException { App.setRoot("CadastroQuestoes"); }

    @FXML
    void irAlunos(ActionEvent event){}

    @FXML
    void irAlunos_corrigir(ActionEvent event) throws IOException { App.setRoot("Aluno");

    }

    @FXML
    void irAlunos_notas(ActionEvent event) {

    }

    @FXML
    void irProva(ActionEvent event) {

    }

    @FXML
    void irProva_agendar(ActionEvent event) throws IOException{ App.setRoot("SelecaoProva");

    }


    @FXML
    void irQuestao(ActionEvent event) {

    }

    @FXML
    void irQuestao_consultar(ActionEvent event) {
        try {
            App.setRoot("VisualizarQuestao");

        } catch (IOException e) {
            e.printStackTrace();

            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro de navegação",
                    "Não foi possível abrir a tela de questões."
            );
        }
    }
}
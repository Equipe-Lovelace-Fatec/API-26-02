package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.example.ProvaAlunoArquivos.CorrecaoAluno;
import org.example.ProvaAlunoArquivos.CorrecaoAlunoDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AlunoController {

    private static final String COR_TITULO = "#005c6d";
    private static final String COR_TEXTO = "#737373";
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_NASC = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---- Menus e navegação (inalterados) ----
    @FXML private javafx.scene.control.MenuButton btnQuestoes, btnProva, btnAlunos;
    @FXML private javafx.scene.control.MenuItem btquestoes_criar, btquestoes_consultar;
    @FXML private javafx.scene.control.MenuItem btprova_montar, btprova_agendar;
    @FXML private javafx.scene.control.MenuItem btalunos_notas, btalunos_corrigir;
    @FXML private javafx.scene.control.Button bt_menu_ant;
    @FXML private javafx.scene.control.Button bt_salva;

    // ---- Cabeçalho ----
    @FXML private Label txt_iconeturma_1;   // iniciais da disciplina
    @FXML private Text txt_materia;
    @FXML private Text txt_prova;
    @FXML private Text txt_data_prova;
    @FXML private Text txt_turma;
    @FXML private Label txt_perfil;         // iniciais do aluno
    @FXML private Text txt_nome_aluno;
    @FXML private Text txt_ra;
    @FXML private Text txt_curso;
    @FXML private Text txt_nasc;
    @FXML private Text txt_email;
    @FXML private Label txt_status;

    // ---- Seleção da prova (aplicação por turma) e do aluno ----
    @FXML private ComboBox<CorrecaoAlunoDAO.Aplicacao> combo_prova;
    @FXML private ComboBox<CorrecaoAlunoDAO.AlunoConvocado> combo_aluno;

    // ---- Questões (montadas por código, uma por questão da prova) ----
    @FXML private VBox vbox_questoes;

    private boolean carregandoSeletores = false;

    private final CorrecaoAlunoDAO dao = new CorrecaoAlunoDAO();
    private final List<CartaoQuestao> cartoes = new ArrayList<>();

    // Dados de uma questão exibida e os campos que o professor preenche
    private static class CartaoQuestao {
        CorrecaoAlunoDAO.QuestaoCorrecao questao;
        BigDecimal peso = BigDecimal.ZERO;
        TextField campoNota;
        TextArea campoComentario; // só nas questões abertas
        double notaAutomatica;    // questões objetivas

        boolean ehAberta() {
            return "diss".equalsIgnoreCase(questao.tipo);
        }

        double getNota() {
            if (!ehAberta()) {
                return notaAutomatica;
            }
            String t = campoNota.getText().trim().replace(",", ".");
            if (t.isEmpty()) {
                return 0;
            }
            try {
                return Double.parseDouble(t);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    @FXML
    private void initialize() {
        definirCabecalhoVazio();
        mostrarMensagemVazia("Selecione a prova e o aluno para corrigir.");

        combo_prova.setPromptText("Selecione a prova (nome | disciplina | turma | data)");
        combo_aluno.setPromptText("Selecione o aluno");
        combo_aluno.setDisable(true);

        combo_prova.valueProperty().addListener((obs, antiga, nova) -> {
            if (!carregandoSeletores) {
                aoSelecionarProva(nova);
            }
        });
        combo_aluno.valueProperty().addListener((obs, antigo, novo) -> {
            if (!carregandoSeletores && novo != null && combo_prova.getValue() != null) {
                CorrecaoAluno.selecionar(novo.ra, combo_prova.getValue().idAplicacao);
                carregarCorrecao();
            }
        });

        carregarProvas();
    }

    // Lista as provas aplicadas pelo professor logado (já iniciadas), mostrando a turma de cada uma
    private void carregarProvas() {
        Usuario usuario = Sessao.getUsuario();
        if (usuario == null) {
            return;
        }
        try {
            List<CorrecaoAlunoDAO.Aplicacao> aplicacoes = dao.listarAplicacoesDoProfessor(usuario.getEmail());
            carregandoSeletores = true;
            combo_prova.getItems().setAll(aplicacoes);
            carregandoSeletores = false;

            if (aplicacoes.isEmpty()) {
                limparCorrecao("Nenhuma prova aplicada para correção.");
                return;
            }

            // Se alguém abriu a tela já com uma seleção, restaura nos seletores
            if (CorrecaoAluno.temSelecao()) {
                for (CorrecaoAlunoDAO.Aplicacao a : aplicacoes) {
                    if (a.idAplicacao == CorrecaoAluno.getIdAplicacao()) {
                        carregandoSeletores = true;
                        combo_prova.setValue(a);
                        carregandoSeletores = false;
                        carregarAlunos(a, CorrecaoAluno.getRa());
                        carregarCorrecao();
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            limparCorrecao("Não foi possível carregar as provas: " + e.getMessage());
        }
    }

    private void aoSelecionarProva(CorrecaoAlunoDAO.Aplicacao aplicacao) {
        CorrecaoAluno.limpar();
        limparCorrecao("Selecione o aluno para corrigir.");
        if (aplicacao == null) {
            combo_aluno.getItems().clear();
            combo_aluno.setDisable(true);
            return;
        }
        carregarAlunos(aplicacao, null);
    }

    // Preenche o seletor de alunos da aplicação; se raParaSelecionar != null, deixa esse aluno selecionado
    private void carregarAlunos(CorrecaoAlunoDAO.Aplicacao aplicacao, String raParaSelecionar) {
        try {
            List<CorrecaoAlunoDAO.AlunoConvocado> alunos = dao.listarAlunosDaAplicacao(aplicacao.idAplicacao);
            carregandoSeletores = true;
            combo_aluno.getItems().setAll(alunos);
            combo_aluno.setDisable(alunos.isEmpty());
            for (CorrecaoAlunoDAO.AlunoConvocado a : alunos) {
                if (a.ra.equals(raParaSelecionar)) {
                    combo_aluno.setValue(a);
                }
            }
            carregandoSeletores = false;
            if (alunos.isEmpty()) {
                limparCorrecao("Nenhum aluno convocado para esta prova.");
            }
        } catch (SQLException e) {
            carregandoSeletores = false;
            e.printStackTrace();
            limparCorrecao("Não foi possível carregar os alunos: " + e.getMessage());
        }
    }

    private void limparCorrecao(String mensagem) {
        cartoes.clear();
        vbox_questoes.getChildren().clear();
        definirCabecalhoVazio();
        mostrarMensagemVazia(mensagem);
    }

    // Carrega cabeçalho e questões do aluno/prova escolhidos
    private void carregarCorrecao() {
        cartoes.clear();
        vbox_questoes.getChildren().clear();

        String ra = CorrecaoAluno.getRa();
        long idAplicacao = CorrecaoAluno.getIdAplicacao();

        try {
            CorrecaoAlunoDAO.Cabecalho cab = dao.buscarCabecalho(ra, idAplicacao);
            if (cab == null) {
                definirCabecalhoVazio();
                mostrarMensagemVazia("Não foi encontrada a prova realizada por este aluno.");
                return;
            }
            preencherCabecalho(cab);

            List<CorrecaoAlunoDAO.QuestaoCorrecao> questoes = dao.buscarQuestoes(cab.provaId);
            Map<Long, CorrecaoAlunoDAO.RespostaDoAluno> respostasAluno = dao.buscarRespostasDoAluno(ra, idAplicacao);

            if (questoes.isEmpty()) {
                mostrarMensagemVazia("Esta prova não possui questões.");
                return;
            }

            int numero = 1;
            for (CorrecaoAlunoDAO.QuestaoCorrecao q : questoes) {
                CartaoQuestao cartao = new CartaoQuestao();
                cartao.questao = q;
                cartao.peso = q.peso != null ? q.peso : BigDecimal.ZERO;
                cartoes.add(cartao);
                vbox_questoes.getChildren().add(montarCartao(numero++, cartao, respostasAluno.get(q.questaoId)));
            }
            atualizarTotal();

        } catch (SQLException | RuntimeException e) {
            e.printStackTrace();
            mostrarMensagemVazia("Não foi possível carregar a correção: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ cabeçalho

    private void preencherCabecalho(CorrecaoAlunoDAO.Cabecalho c) {
        txt_iconeturma_1.setText(iniciais(c.nomeDisciplina));
        txt_materia.setText("Matéria: " + valor(c.nomeDisciplina));
        txt_prova.setText("Prova: " + valor(c.nomeProva));

        String data = c.data != null ? c.data.format(FORMATO_DATA) : "-";
        String hora = c.horario != null ? " às " + c.horario.format(FORMATO_HORA) : "";
        txt_data_prova.setText("Realizada em: " + data + hora);

        txt_turma.setText("Turma: " + valor(c.nomeTurma) + "  |  " + valor(c.semestre)
                + "º sem.  |  " + valor(c.periodo));

        txt_perfil.setText(iniciais(c.nomeAluno));
        txt_nome_aluno.setText("Nome: " + valor(c.nomeAluno));
        txt_ra.setText("RA: " + valor(c.ra));
        txt_curso.setText("Curso: " + valor(c.nomeCurso));
        txt_nasc.setText("Data de Nascimento: "
                + (c.dataNascimento != null ? c.dataNascimento.format(FORMATO_NASC) : "-"));
        txt_email.setText("E-mail: " + valor(c.email));
    }

    private void definirCabecalhoVazio() {
        txt_iconeturma_1.setText("");
        txt_perfil.setText("");
        txt_materia.setText("Matéria: -");
        txt_prova.setText("Prova: -");
        txt_data_prova.setText("Realizada em: -");
        txt_turma.setText("Turma: -");
        txt_nome_aluno.setText("Nome: -");
        txt_ra.setText("RA: -");
        txt_curso.setText("Curso: -");
        txt_nasc.setText("Data de Nascimento: -");
        txt_email.setText("E-mail: -");
        txt_status.setText("Status:");
    }

    private String valor(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private String iniciais(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String parte : texto.trim().split("\\s+")) {
            if (parte.length() > 2 && sb.length() < 2) {   // ignora "da", "de", "do"
                sb.append(Character.toUpperCase(parte.charAt(0)));
            }
        }
        if (sb.length() == 0) {
            sb.append(Character.toUpperCase(texto.trim().charAt(0)));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ questões

    private VBox montarCartao(int numero, CartaoQuestao cartao, CorrecaoAlunoDAO.RespostaDoAluno respostaAluno) {
        CorrecaoAlunoDAO.QuestaoCorrecao q = cartao.questao;
        VBox caixa = new VBox(8);
        caixa.setPadding(new Insets(15, 10, 10, 10));
        caixa.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10;");

        caixa.getChildren().add(titulo("Questão " + numero));

        caixa.getChildren().add(titulo("Enunciado:"));
        caixa.getChildren().add(texto(q.enunciado));
        for (String linha : linhasDeApoio(q)) {          // alternativas / itens de relacionamento
            Label l = texto(linha);
            l.setStyle("-fx-text-fill: " + COR_TEXTO + "; -fx-font-size: 14px;");
            caixa.getChildren().add(l);
        }

        caixa.getChildren().add(titulo("Resposta do Aluno:"));
        caixa.getChildren().add(texto(respostaAluno == null || respostaAluno.texto == null
                || respostaAluno.texto.isBlank() ? "Não respondida" : respostaAluno.texto));

        caixa.getChildren().add(titulo("Resposta Esperada:"));
        caixa.getChildren().add(texto(q.gabarito.isEmpty()
                ? "Sem resposta cadastrada" : String.join("\n", q.gabarito)));

        if (cartao.ehAberta()) {
            caixa.getChildren().add(titulo("Comentário do Professor - Não obrigatório - Obs.: O aluno lerá este comentário"));
            TextArea comentario = new TextArea();
            comentario.setPromptText("Escreva seu comentário aqui. Não obrigatório. O aluno lerá este comentário");
            comentario.setWrapText(true);
            comentario.setPrefRowCount(3);
            comentario.setFont(Font.font(16));
            cartao.campoComentario = comentario;
            caixa.getChildren().add(comentario);
        }

        caixa.getChildren().add(montarLinhaNota(cartao, respostaAluno));

        Separator sep = new Separator();
        sep.setStyle("-fx-border-color: " + COR_TITULO + "; -fx-background-color: " + COR_TITULO + ";");
        caixa.getChildren().add(sep);
        return caixa;
    }

    // Valor da questão + pontuação (editável nas abertas; automática nas objetivas)
    private HBox montarLinhaNota(CartaoQuestao cartao, CorrecaoAlunoDAO.RespostaDoAluno respostaAluno) {
        HBox linha = new HBox(15);
        linha.setAlignment(Pos.CENTER);
        linha.setPadding(new Insets(10, 0, 5, 0));

        linha.getChildren().add(titulo("Valor da questão:"));
        linha.getChildren().add(titulo(formatar(cartao.peso.doubleValue())));
        linha.getChildren().add(titulo("Pontuação:"));

        TextField campo = new TextField();
        campo.setPrefSize(85, 45);
        campo.setFont(Font.font(16));
        cartao.campoNota = campo;

        if (cartao.ehAberta()) {
            campo.setPromptText("0,00");
            campo.textProperty().addListener((obs, antigo, novo) -> {
                validarNota(campo, cartao.peso.doubleValue());
                atualizarTotal();
            });
        } else {
            cartao.notaAutomatica = corrigirObjetiva(cartao, respostaAluno);
            campo.setText(formatar(cartao.notaAutomatica));
            campo.setEditable(false);
        }
        linha.getChildren().add(campo);
        return linha;
    }

    // Alternativas e itens a relacionar, vindos do que foi cadastrado com a questão
    private List<String> linhasDeApoio(CorrecaoAlunoDAO.QuestaoCorrecao q) {
        List<String> linhas = new ArrayList<>();
        if ("alt".equalsIgnoreCase(q.tipo)) {
            char letra = 'A';
            for (Resposta r : q.respostasCadastradas) {
                linhas.add(letra++ + ") " + r.getTexto());
            }
        } else if ("rela".equalsIgnoreCase(q.tipo)) {
            for (Resposta r : q.respostasCadastradas) {
                linhas.add("• " + r.getTexto());
            }
        }
        return linhas;
    }

    // Objetiva: vale o peso inteiro se a resposta foi marcada como correta; senão zero
    private double corrigirObjetiva(CartaoQuestao cartao, CorrecaoAlunoDAO.RespostaDoAluno respostaAluno) {
        boolean acertou = respostaAluno != null && Boolean.TRUE.equals(respostaAluno.correta);
        return acertou ? cartao.peso.doubleValue() : 0;
    }

    private void validarNota(TextField campo, double max) {
        String texto = campo.getText().trim();
        if (texto.isEmpty() || texto.equals("-") || texto.equals(".") || texto.equals(",")) {
            return;
        }
        try {
            double valor = Double.parseDouble(texto.replace(",", "."));
            if (valor < 0 || valor > max) {
                mostrarAlerta(String.format("A nota deve estar entre 0 e %.1f.", max));
                campo.clear();
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Digite um número válido (ex: 1.5).");
            campo.clear();
        }
    }

    private void atualizarTotal() {
        double total = 0;
        double maximo = 0;
        for (CartaoQuestao c : cartoes) {
            total += c.getNota();
            maximo += c.peso.doubleValue();
        }
        txt_status.setText(String.format("Total: %.2f / %.2f", total, maximo));
    }

    // ------------------------------------------------------------------ componentes auxiliares

    private Label titulo(String t) {
        Label l = new Label(t);
        l.setWrapText(true);
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        l.setStyle("-fx-text-fill: " + COR_TITULO + ";");
        return l;
    }

    private Label texto(String t) {
        Label l = new Label(t);
        l.setWrapText(true);
        l.setFont(Font.font(16));
        l.setStyle("-fx-text-fill: " + COR_TEXTO + ";");
        return l;
    }

    private void mostrarMensagemVazia(String mensagem) {
        Label l = texto(mensagem);
        l.setPadding(new Insets(20));
        vbox_questoes.getChildren().add(l);
    }

    private String formatar(double v) {
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", v);
    }

    private void mostrarAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Valor inválido");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    // ------------------------------------------------------------------ ações

    @FXML
    void salvanotas(ActionEvent event) {
        if (!CorrecaoAluno.temSelecao() || cartoes.isEmpty()) {
            return;
        }
        double total = 0;
        for (CartaoQuestao c : cartoes) {
            total += c.getNota();
        }
        try {
            dao.salvarNota(CorrecaoAluno.getRa(), CorrecaoAluno.getIdAplicacao(),
                    BigDecimal.valueOf(total).setScale(2, java.math.RoundingMode.HALF_UP));
            CorrecaoAlunoDAO.Aplicacao aplicacao = combo_prova.getValue();
            if (aplicacao != null) {
                carregarAlunos(aplicacao, CorrecaoAluno.getRa());   // atualiza "nota" ao lado do nome
            }
            txt_status.setText(String.format("Nota salva. Total: %.2f", total));
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Não foi possível salvar a nota: " + e.getMessage());
        }
    }

    @FXML
    void vaitelaescolheralunos(ActionEvent event) {

    }

    @FXML
    void irQuestao_criar(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoes");
    }

    @FXML
    void irQuestao_consultar(ActionEvent event) throws IOException {
        App.setRoot("VisualizarQuestao");
    }

    @FXML
    void irProva_montar(ActionEvent event) throws IOException {
        App.setRoot("montagem");
    }

    @FXML
    void irProva_agendar(ActionEvent event) throws IOException {
        App.setRoot("SelecaoProva");
    }

    @FXML
    void irAlunos_notas(ActionEvent event) {

    }

    @FXML
    void irAlunos_corrigir(ActionEvent event) throws IOException {
        App.setRoot("Aluno");
    }
}
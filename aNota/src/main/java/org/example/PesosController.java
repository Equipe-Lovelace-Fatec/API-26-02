package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PesosController {

    // =====================================
    // ELEMENTOS DA TELA
    // =====================================

    @FXML private MenuButton btnQuestoes;
    @FXML private MenuButton btnProva;
    @FXML private MenuButton btnAlunos;

    @FXML private ComboBox<String> cbTurma;
    @FXML private ComboBox<String> cbDisciplina;
    @FXML private ComboBox<String> cbTurno;
    @FXML private ComboBox<String> cbSemestre;

    @FXML private VBox conteinerProvas;

    @FXML private Button btnadicionarpesos;

    @FXML private TextField txtMediaFinal;


    // =====================================
    // BANCO DE DADOS
    // =====================================

    private final ProvaListaDAO provaDAO = new ProvaListaDAO();

    private List<Prova> provasProfessor = new ArrayList<>();


    // =====================================
    // TURMAS DA DISCIPLINA ESCOLHIDA
    // =====================================

    private static class TurmaInfo {
        final int id;
        final String texto;
        final String periodo;
        final String semestre;

        TurmaInfo(int id, String texto, String periodo, String semestre) {
            this.id = id;
            this.texto = texto;
            this.periodo = periodo;
            this.semestre = semestre;
        }
    }

    private List<String> disciplinasDaTurma = new ArrayList<>();

    // provas que este professor já aplicou na turma escolhida (null = nenhuma turma escolhida)
    private Set<Long> provasDaTurma = null;

    // dessas, as que foram aplicadas como recuperação
    private Set<Long> provasRecuperacao = new HashSet<>();

    // texto mostrado na caixa Turma -> turma
    private final Map<String, TurmaInfo> turmasPorTexto = new LinkedHashMap<>();


    // =====================================
    // LISTAS DAS PROVAS EXIBIDAS
    // =====================================

    private final List<Prova> provasExibidas = new ArrayList<>();

    private final List<CheckBox> selecoesProvas = new ArrayList<>();

    private final List<TextField> camposPesos = new ArrayList<>();


    // =====================================
    // INICIALIZAÇÃO
    // =====================================

    @FXML
    public void initialize() {
        cbTurma.setPromptText("Turma");
        cbDisciplina.setPromptText("Disciplina");
        cbTurno.setPromptText("Turno");
        cbSemestre.setPromptText("Semestre");

        cbDisciplina.setOnAction(event -> atualizarProvasExibidas());

        cbTurma.setOnAction(event -> aoEscolherTurma());

        // Turno e semestre são preenchidos pela turma (só mostram o valor)
        cbTurno.setMouseTransparent(true);
        cbTurno.setFocusTraversable(false);
        cbSemestre.setMouseTransparent(true);
        cbSemestre.setFocusTraversable(false);

        carregarDados();
    }


    // =====================================
    // CARREGAR DADOS DO PROFESSOR
    // =====================================

    private void carregarDados() {
        try {
            Usuario usuario = Sessao.getUsuario();

            if (usuario == null || usuario.getEmail() == null) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Atenção",
                        "Não foi possível identificar o professor logado."
                );

                return;
            }

            provasProfessor =
                    provaDAO.listarPorProfessor(usuario.getEmail());

            turmasPorTexto.clear();

            for (TurmaInfo t : carregarTurmasDoProfessor(usuario.getEmail())) {
                turmasPorTexto.put(t.texto, t);
            }

            cbTurma.getItems().setAll(turmasPorTexto.keySet());

            atualizarProvasExibidas();
        } catch (SQLException | RuntimeException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao carregar dados",
                    "Não foi possível carregar as provas e disciplinas: "
                            + e.getMessage()
            );
        }
    }


    // =====================================
    // TURMA -> DISCIPLINA, TURNO E SEMESTRE
    // =====================================

    // Turmas das disciplinas que o professor ministra
    private List<TurmaInfo> carregarTurmasDoProfessor(String emailProfessor) throws SQLException {
        // turmas das disciplinas que o professor ministra
        // + turmas onde ele já colocou prova (assim a prova sempre aparece na turma dela)
        String sql = "SELECT t.id_turma, t.nome_curso, t.semestre, t.periodo, t.nome_turma "
                + "FROM turma t "
                + "JOIN turma_disciplina td ON td.id_turma = t.id_turma "
                + "JOIN ministra m ON m.nome_disciplina = td.nome_disciplina "
                + "JOIN professor p ON p.matricula = m.matricula "
                + "WHERE p.email = ? "
                + "UNION "
                + "SELECT t.id_turma, t.nome_curso, t.semestre, t.periodo, t.nome_turma "
                + "FROM turma t "
                + "JOIN prova_turma pt ON pt.id_turma = t.id_turma "
                + "WHERE pt.email_professor = ? "
                + "ORDER BY nome_curso, semestre, periodo";

        List<TurmaInfo> turmas = new ArrayList<>();

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            ps.setString(2, emailProfessor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nomeTurma = rs.getString("nome_turma");

                    String texto = rs.getString("semestre") + " "
                            + rs.getString("periodo") + " - "
                            + rs.getString("nome_curso")
                            + ("Regular".equals(nomeTurma) ? "" : " (" + nomeTurma + ")");

                    turmas.add(new TurmaInfo(
                            rs.getInt("id_turma"),
                            texto,
                            rs.getString("periodo"),
                            rs.getString("semestre")));
                }
            }
        }

        return turmas;
    }

    // Disciplinas da turma que o professor ministra
    private List<String> carregarDisciplinasDaTurma(String emailProfessor, int idTurma) throws SQLException {
        String sql = "SELECT td.nome_disciplina "
                + "FROM turma_disciplina td "
                + "JOIN ministra m ON m.nome_disciplina = td.nome_disciplina "
                + "JOIN professor p ON p.matricula = m.matricula "
                + "WHERE p.email = ? AND td.id_turma = ? "
                + "UNION "
                + "SELECT pt.nome_disciplina FROM prova_turma pt "
                + "WHERE pt.email_professor = ? AND pt.id_turma = ? "
                + "ORDER BY nome_disciplina";

        List<String> disciplinas = new ArrayList<>();

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emailProfessor);
            ps.setInt(2, idTurma);
            ps.setString(3, emailProfessor);
            ps.setInt(4, idTurma);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    disciplinas.add(rs.getString("nome_disciplina"));
                }
            }
        }

        return disciplinas;
    }

    // Provas que ESTE professor já aplicou na turma e quais delas são recuperação
    private void carregarAplicacoesDaTurma(int idTurma, String emailProfessor) throws SQLException {
        String sql = "SELECT prova_id, MAX(recuperacao) AS rec FROM prova_turma "
                + "WHERE id_turma = ? AND email_professor = ? GROUP BY prova_id";

        Set<Long> aplicadas = new HashSet<>();
        Set<Long> recuperacoes = new HashSet<>();

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            ps.setString(2, emailProfessor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    aplicadas.add(rs.getLong("prova_id"));

                    if (rs.getInt("rec") == 1) {
                        recuperacoes.add(rs.getLong("prova_id"));
                    }
                }
            }
        }

        provasDaTurma = aplicadas;
        provasRecuperacao = recuperacoes;
    }

    // Recuperação: aplicada como recuperação na turma ou com "recuperação" no nome
    private boolean ehRecuperacao(Prova prova) {
        return provasRecuperacao.contains(prova.getProvaId())
                || (prova.getNomeProva() != null
                && prova.getNomeProva().toLowerCase().contains("recupera"));
    }

    // Escolheu a turma: preenche turno e semestre sozinhos e puxa as disciplinas dela
    private void aoEscolherTurma() {
        TurmaInfo turma = turmasPorTexto.get(cbTurma.getValue());

        disciplinasDaTurma = new ArrayList<>();
        provasDaTurma = null;
        provasRecuperacao = new HashSet<>();

        if (turma == null) {
            atualizarProvasExibidas();
            return;
        }

        // TURNO E SEMESTRE VÊM DA TURMA

        cbTurno.getItems().setAll(turma.periodo);
        cbTurno.setValue(turma.periodo);

        cbSemestre.getItems().setAll(turma.semestre);
        cbSemestre.setValue(turma.semestre);

        // DISCIPLINAS DA TURMA

        try {
            Usuario usuario = Sessao.getUsuario();

            disciplinasDaTurma = carregarDisciplinasDaTurma(usuario.getEmail(), turma.id);

            carregarAplicacoesDaTurma(turma.id, usuario.getEmail());
        } catch (SQLException | RuntimeException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao carregar disciplinas",
                    "Não foi possível carregar as disciplinas da turma: " + e.getMessage()
            );
        }

        cbDisciplina.setValue(null);
        cbDisciplina.getItems().setAll(disciplinasDaTurma);

        // SÓ UMA DISCIPLINA: JÁ FICA ESCOLHIDA

        if (disciplinasDaTurma.size() == 1) {
            cbDisciplina.setValue(disciplinasDaTurma.get(0));
        }

        atualizarProvasExibidas();
    }


    // =====================================
    // GRAVAR PESOS (item_avaliativo)
    // =====================================

    // Aplicação mais recente (não recuperação) da prova nesta turma, ou null se nunca foi agendada
    private Long buscarAplicacao(Connection con, int idTurma, long provaId, String email) throws SQLException {
        String sql = "SELECT id_aplicacao FROM prova_turma "
                + "WHERE id_turma = ? AND prova_id = ? AND email_professor = ? AND recuperacao = FALSE "
                + "ORDER BY data DESC, horario DESC LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTurma);
            ps.setLong(2, provaId);
            ps.setString(3, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id_aplicacao") : null;
            }
        }
    }

    // Peso (0 a 10) vira percentual da disciplina: peso / soma dos pesos x 100.
    // O último de cada disciplina fecha a conta para somar exatamente 100,00.
    // Devolve null se a soma dos pesos de alguma disciplina for zero.
    private List<BigDecimal> calcularPercentuais(List<Prova> provas, List<Double> pesos) {
        Map<String, Double> somas = new LinkedHashMap<>();
        Map<String, Integer> ultimo = new LinkedHashMap<>();

        for (int i = 0; i < provas.size(); i++) {
            String disciplina = provas.get(i).getNomeDisciplina();

            somas.merge(disciplina, pesos.get(i), Double::sum);
            ultimo.put(disciplina, i);
        }

        for (double soma : somas.values()) {
            if (soma <= 0) {
                return null;
            }
        }

        List<BigDecimal> percentuais = new ArrayList<>();
        Map<String, BigDecimal> acumulado = new LinkedHashMap<>();

        for (int i = 0; i < provas.size(); i++) {
            String disciplina = provas.get(i).getNomeDisciplina();

            BigDecimal percentual;

            if (ultimo.get(disciplina) == i) {
                percentual = new BigDecimal("100")
                        .subtract(acumulado.getOrDefault(disciplina, BigDecimal.ZERO));
            } else {
                percentual = BigDecimal.valueOf(pesos.get(i) * 100.0 / somas.get(disciplina))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            acumulado.merge(disciplina, percentual, BigDecimal::add);

            percentuais.add(percentual);
        }

        return percentuais;
    }

    // Grava o peso (percentual) de cada prova na turma.
    // Devolve null se deu certo, ou a mensagem do problema (nada é gravado nesse caso).
    private String gravarPesos(TurmaInfo turma, List<Prova> provas, List<BigDecimal> percentuais) throws SQLException {
        String email = Sessao.getUsuario().getEmail();

        try (Connection con = ConexaoBD.conectar()) {
            con.setAutoCommit(false);

            try {
                // APLICAÇÃO (prova_turma) DE CADA PROVA

                List<Long> aplicacoes = new ArrayList<>();

                for (Prova prova : provas) {
                    Long aplicacao = buscarAplicacao(con, turma.id, prova.getProvaId(), email);

                    if (aplicacao == null) {
                        con.rollback();

                        return "A prova \"" + prova.getNomeProva()
                                + "\" ainda não foi agendada para esta turma, "
                                + "então o peso dela não pode ser gravado.";
                    }

                    aplicacoes.add(aplicacao);
                }

                // GRAVA (OU ATUALIZA) O PESO DE CADA PROVA

                String sqlGravar = "INSERT INTO item_avaliativo "
                        + "(id_turma, nome_disciplina, nome, peso_percentual, id_aplicacao, recuperacao) "
                        + "VALUES (?, ?, ?, ?, ?, FALSE) "
                        + "ON DUPLICATE KEY UPDATE peso_percentual = VALUES(peso_percentual), "
                        + "id_aplicacao = VALUES(id_aplicacao)";

                try (PreparedStatement ps = con.prepareStatement(sqlGravar)) {
                    for (int i = 0; i < provas.size(); i++) {
                        ps.setInt(1, turma.id);
                        ps.setString(2, provas.get(i).getNomeDisciplina());
                        ps.setString(3, provas.get(i).getNomeProva());
                        ps.setBigDecimal(4, percentuais.get(i));
                        ps.setLong(5, aplicacoes.get(i));

                        ps.executeUpdate();
                    }
                }

                con.commit();

                return null;
            } catch (SQLException e) {
                con.rollback();

                throw e;
            }
        }
    }


    // =====================================
    // ATUALIZAR PROVAS NA TELA
    // =====================================

    private void atualizarProvasExibidas() {
        conteinerProvas.getChildren().clear();
        txtMediaFinal.clear();

        provasExibidas.clear();
        selecoesProvas.clear();
        camposPesos.clear();

        String disciplinaSelecionada = cbDisciplina.getValue();

        // SEM TURMA ESCOLHIDA NÃO APARECE PROVA NENHUMA

        if (provasDaTurma == null) {
            Label aviso = new Label("Escolha a turma para ver as provas.");

            aviso.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-text-fill: #006477;"
            );

            conteinerProvas.getChildren().add(aviso);

            return;
        }

        for (Prova prova : provasProfessor) {
            if (disciplinaSelecionada != null) {
                if (!disciplinaSelecionada.equals(prova.getNomeDisciplina())) {
                    continue;
                }
            } else if (!disciplinasDaTurma.contains(prova.getNomeDisciplina())) {
                // a prova aparece em toda turma que tem a disciplina dela

                continue;
            }

            // GUARDA A PROVA EXIBIDA

            provasExibidas.add(prova);

            // CRIA A LINHA

            HBox linha = new HBox(20);

            linha.setAlignment(Pos.CENTER_LEFT);

            linha.setPrefHeight(70);

            linha.setMinHeight(70);

            linha.setMaxWidth(Double.MAX_VALUE);

            linha.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #00bdc9;" +
                            "-fx-border-width: 1.8;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 8 15 8 15;"
            );

            // CHECKBOX DE SELEÇÃO

            CheckBox checkProva = new CheckBox();

            checkProva.setStyle(
                    "-fx-border-color: #006477;" +
                            "-fx-cursor: hand;"
            );

            // NOME DA PROVA

            boolean recuperacao = ehRecuperacao(prova);

            String textoNome = prova.getNomeProva()
                    + " - " + prova.getNomeDisciplina()
                    + (recuperacao ? "  (Recuperação)" : "");

            Label nomeProva = new Label(textoNome);

            nomeProva.setStyle(
                    "-fx-font-size: 20px;" +
                            "-fx-text-fill: #006477;"
            );

            // o nome ocupa o espaço que sobra, empurrando peso e botão para a ponta direita

            nomeProva.setMaxWidth(Double.MAX_VALUE);

            HBox.setHgrow(nomeProva, Priority.ALWAYS);

            // CAMPO DO PESO

            TextField campoPeso = new TextField();

            campoPeso.setPromptText("peso");

            campoPeso.setAlignment(Pos.CENTER);

            campoPeso.setPrefWidth(112);

            campoPeso.setMaxWidth(112);

            campoPeso.setDisable(true);

            campoPeso.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #ff43a5;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;" +
                            "-fx-font-size: 16px;"
            );

            // SÓ PERMITE DIGITAR O PESO SE SELECIONAR A PROVA

            checkProva.selectedProperty().addListener(
                    (observable, anterior, selecionado) -> {
                        campoPeso.setDisable(!selecionado);

                        if (!selecionado) {
                            campoPeso.clear();
                        }

                        if (selecionado && recuperacao) {
                            mostrarAlerta(
                                    Alert.AlertType.INFORMATION,
                                    "Recuperação",
                                    "Caso escolha a recuperação, ela substituirá outra prova."
                            );
                        }
                    }
            );

            // BOTÃO VISUALIZAR PROVA

            Button btnVisualizar = new Button("VISUALIZAR");

            btnVisualizar.setPrefSize(130, 34);

            btnVisualizar.setMinWidth(130);

            btnVisualizar.setStyle(
                    "-fx-background-color: #00596a;" +
                            "-fx-background-radius: 8;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-cursor: hand;"
            );

            btnVisualizar.setOnAction(event -> visualizarProva(prova));

            // GUARDA OS ELEMENTOS

            selecoesProvas.add(checkProva);

            camposPesos.add(campoPeso);

            // ADICIONA TUDO NA LINHA

            linha.getChildren().addAll(
                    checkProva,
                    nomeProva,
                    campoPeso,
                    btnVisualizar
            );

            // ADICIONA A LINHA NO QUADRO

            conteinerProvas.getChildren().add(linha);
        }

        // NENHUMA PROVA ENCONTRADA

        if (provasExibidas.isEmpty()) {
            Label mensagem = new Label(
                    "Nenhuma prova encontrada para essa turma e disciplina."
            );

            mensagem.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-text-fill: #006477;"
            );

            conteinerProvas.getChildren().add(mensagem);
        }
    }


    // =====================================
    // VISUALIZAR PROVA
    // =====================================

    private void visualizarProva(Prova escolhida) {
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

            visualizador.setDadosProva(
                    escolhida.getNomeProva(),
                    escolhida.getNomeDisciplina(),
                    escolhida.getValor() == null ? "" : escolhida.getValor().toPlainString(),
                    perguntas,
                    respostas
            );

            Stage popup = new Stage();

            popup.setTitle("Visualização da Prova");

            popup.setScene(new Scene(root));

            popup.initModality(Modality.APPLICATION_MODAL);

            popup.showAndWait();
        } catch (IOException | SQLException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro",
                    "Não foi possível abrir a tela de visualização: " + e.getMessage()
            );
        }
    }


    // =====================================
    // ADICIONAR PESOS
    // =====================================

    @FXML
    private void adicionarPesos(ActionEvent event) {
        TurmaInfo turma = turmasPorTexto.get(cbTurma.getValue());

        if (turma == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Escolha a turma para calcular a média."
            );

            return;
        }

        int quantidadeSelecionada = 0;

        // VERIFICA SE ALGUMA PROVA FOI SELECIONADA

        for (CheckBox check : selecoesProvas) {
            if (check.isSelected()) {
                quantidadeSelecionada++;
            }
        }

        if (quantidadeSelecionada == 0) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Atenção",
                    "Selecione pelo menos uma prova."
            );

            return;
        }

        // VALIDA OS PESOS DAS PROVAS SELECIONADAS

        List<Prova> provasCalculo = new ArrayList<>();
        List<Double> pesosCalculo = new ArrayList<>();

        for (int i = 0; i < selecoesProvas.size(); i++) {
            if (!selecoesProvas.get(i).isSelected()) {
                continue;
            }

            String valor = camposPesos.get(i)
                    .getText()
                    .trim()
                    .replace(",", ".");

            if (valor.isEmpty()) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Campo vazio",
                        "Preencha o peso de todas as provas selecionadas."
                );

                return;
            }

            try {
                double peso = Double.parseDouble(valor);

                if (!Double.isFinite(peso) || peso < 0) {
                    throw new NumberFormatException();
                }

                if (peso > 10) {
                    mostrarAlerta(
                            Alert.AlertType.WARNING,
                            "Peso inválido",
                            "O peso de cada prova vai de 0 a 10."
                    );

                    return;
                }

                provasCalculo.add(provasExibidas.get(i));
                pesosCalculo.add(peso);
            } catch (NumberFormatException e) {
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Peso inválido",
                        "Digite apenas números válidos e não negativos."
                );

                return;
            }
        }

        // MÉDIA FINAL = média dos valores das provas ponderada pelos pesos
        //             = soma(valor da prova x peso) / soma(pesos)

        double somaPesos = 0;
        double somaPonderada = 0;

        for (int i = 0; i < provasCalculo.size(); i++) {
            double valor = provasCalculo.get(i).getValor() == null
                    ? 0
                    : provasCalculo.get(i).getValor().doubleValue();

            somaPesos += pesosCalculo.get(i);
            somaPonderada += valor * pesosCalculo.get(i);
        }

        if (somaPesos <= 0) {
            txtMediaFinal.clear();

            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Pesos inválidos",
                    "A soma dos pesos precisa ser maior que zero."
            );

            return;
        }

        txtMediaFinal.setText(
                String.format(Locale.forLanguageTag("pt-BR"), "%.2f", somaPonderada / somaPesos)
        );

        // GRAVAÇÃO: só as provas normais (o banco não guarda recuperação como item de peso)

        List<Prova> provasGravar = new ArrayList<>();
        List<Double> pesosGravar = new ArrayList<>();
        boolean recuperacaoMarcada = false;

        for (int i = 0; i < provasCalculo.size(); i++) {
            if (ehRecuperacao(provasCalculo.get(i))) {
                recuperacaoMarcada = true;
            } else {
                provasGravar.add(provasCalculo.get(i));
                pesosGravar.add(pesosCalculo.get(i));
            }
        }

        // só recuperação marcada: não há o que gravar

        if (provasGravar.isEmpty()) {
            return;
        }

        List<BigDecimal> percentuais = calcularPercentuais(provasGravar, pesosGravar);

        if (percentuais == null) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Pesos não gravados",
                    "A soma dos pesos de cada disciplina precisa ser maior que zero."
            );

            return;
        }

        // GRAVA NO BANCO

        try {
            String problema = gravarPesos(turma, provasGravar, percentuais);

            if (problema != null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Pesos não gravados", problema);

                return;
            }
        } catch (SQLException | RuntimeException e) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao gravar os pesos",
                    "Não foi possível gravar os pesos: " + e.getMessage()
            );

            return;
        }

        mostrarAlerta(
                Alert.AlertType.INFORMATION,
                "Pesos",
                "Pesos gravados com sucesso."
                        + (recuperacaoMarcada
                        ? "\n\nA recuperação entrou na média final, mas o banco não guarda o peso dela."
                        : "")
        );
    }


    // =====================================
    // ALERTAS
    // =====================================

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String mensagem) {
        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(mensagem);

        alerta.showAndWait();
    }


    // =====================================
    // MENU QUESTÕES
    // =====================================

    @FXML
    private void irQuestao(ActionEvent event) {
    }

    @FXML
    private void IrQuestao_criar(ActionEvent event) throws IOException {
        App.setRoot("CadastroQuestoes");
    }

    @FXML
    private void irQuestao_consultar(ActionEvent event) throws IOException {
        App.setRoot("VisualizarQuestao");
    }


    // =====================================
    // MENU PROVAS
    // =====================================

    @FXML
    private void irProva(ActionEvent event) {
    }

    @FXML
    void IrProva_montar(ActionEvent event) throws IOException { App.setRoot("montagem"); }

    @FXML
    private void irProva_agendar(ActionEvent event) throws IOException {
        App.abrirTela("SelecaoProva", "aNota", 1350, 1080);
    }

    @FXML
    void irProva_pesos(ActionEvent event) throws IOException{ App.setRoot("pesos");
    }

    @FXML
    private void irProva_Media(ActionEvent event) throws IOException {
        App.setRoot("Pesos");
    }


    @FXML
    private void irAlunos(ActionEvent event) {
    }

    @FXML
    private void irAlunos_notas(ActionEvent event) {
    }

    @FXML
    private void irAlunos_corrigir(ActionEvent event) throws IOException {
        App.setRoot("Aluno");
    }

}

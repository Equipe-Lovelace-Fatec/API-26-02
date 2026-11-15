package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MontagemProvaController {

        private final QuestaoDAO questaoDAO = new QuestaoDAO();
        private final ProvaDAO montagemProvaDAO = new ProvaDAO();
        private final RespostaDAO respostaDAO = new RespostaDAO();
        private final DisciplinaDAO disciplinaDAO = new DisciplinaDAO();

        private CheckBox[] checkBoxes;
        private TextField[] camposValores;
        private List<Questao> questoesBancoGeral = new ArrayList<>();  // Questões das disciplinas do professor logado
        private List<Questao> questoesFiltradas = new ArrayList<>();   // Questões que atendem aos filtros atuais

        // Disciplinas que o professor logado ministra (tabela ministra)
        private List<String> disciplinasDoProfessor = new ArrayList<>();

        // Estruturas para reter o estado de seleção e valores por ID da Questão
        private final Map<Long, Boolean> estadosSelecionados = new HashMap<>();
        private final Map<Long, String> valoresQuestaoMap = new HashMap<>();

        // Listas temporárias para passar os dados para a tela de visualização
        public static List<String> perguntasTemporarias = new ArrayList<>();
        public static List<String> valoresTemporarios = new ArrayList<>();

        // Prova (rascunho) a ser editada: definida pela tela de provas elaboradas antes de abrir esta tela
        public static Prova provaParaEditar;
        private Prova provaEmEdicao;

        @FXML private ComboBox<String> boxDisciplina;
        @FXML private ComboBox<String> box_filtro_dificuldade;
        @FXML private ComboBox<String> box_filtro_disciplina;
        @FXML private ComboBox<String> box_filtro_ultimo_uso;
        @FXML private MenuItem btProva_agendar, btProva_montar, btalunos_corrigir, btalunos_notas;
        @FXML private Button btnAddQuestao, btnSalvarProva, btnVerProva;
        @FXML private MenuButton btnAlunos, btnProva, btnQuestoes;
        @FXML private MenuItem btquestoes_consultar, btquestoes_criar;

        @FXML private CheckBox checkbox1, checkbox11, checkbox111, checkbox112, checkbox113, checkbox114, checkbox115, checkbox116, checkbox117, checkbox118, checkbox119;
        @FXML private TextField txtNomeProva;
        @FXML private Label txtValorProva;
        @FXML private TextField valor_questao1, valor_questao11, valor_questao111, valor_questao112, valor_questao113, valor_questao114, valor_questao115, valor_questao116, valor_questao117, valor_questao118, valor_questao119;

        @FXML
        public void initialize() {
                // Consome o pedido de edição para que a próxima abertura da tela seja uma montagem nova
                provaEmEdicao = provaParaEditar;
                provaParaEditar = null;

                inicializarArraysComponentes();
                carregarDadosDoBanco();
                configurarFiltrosDinamicos();
                atualizacaoValorProva();

                if (provaEmEdicao != null) {
                        carregarProvaEmEdicao();
                }
        }

        // Preenche a tela com a prova salva: nome, disciplina e questões já selecionadas
        private void carregarProvaEmEdicao() {
                if (txtNomeProva != null) {
                        txtNomeProva.setText(provaEmEdicao.getNomeProva());
                }
                if (boxDisciplina != null && disciplinasDoProfessor.contains(provaEmEdicao.getNomeDisciplina())) {
                        boxDisciplina.setValue(provaEmEdicao.getNomeDisciplina());
                }

                Map<Long, BigDecimal> salvas;
                try {
                        salvas = buscarPesosSalvos(provaEmEdicao.getProvaId());
                } catch (SQLException e) {
                        e.printStackTrace();
                        mostrarAlerta("Erro", "Não foi possível carregar as questões da prova: " + e.getMessage());
                        return;
                }

                priorizarQuestoes(salvas.keySet());
                if (box_filtro_disciplina != null) {
                        box_filtro_disciplina.setValue(provaEmEdicao.getNomeDisciplina());
                }
                aplicarFiltros();

                for (Map.Entry<Long, BigDecimal> e : salvas.entrySet()) {
                        estadosSelecionados.put(e.getKey(), true);
                        valoresQuestaoMap.put(e.getKey(), formatarPeso(e.getValue()));
                }
                atualizarExibicaoCheckBoxes();
                recalcularValorTela();
        }

        // Questão -> peso gravado em prova_questao
        private Map<Long, BigDecimal> buscarPesosSalvos(long provaId) throws SQLException {
                Map<Long, BigDecimal> pesos = new LinkedHashMap<>();
                String sql = "SELECT questao_id, peso FROM prova_questao WHERE prova_id = ? ORDER BY questao_id";
                try (Connection con = ConexaoBD.conectar();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setLong(1, provaId);
                        try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                        pesos.put(rs.getLong("questao_id"), rs.getBigDecimal("peso"));
                                }
                        }
                }
                return pesos;
        }

        // Coloca as questões da prova no início da lista, para ficarem sempre visíveis na tela
        private void priorizarQuestoes(java.util.Set<Long> idsDaProva) {
                List<Questao> ordenadas = new ArrayList<>();
                for (Questao q : questoesBancoGeral) {
                        if (idsDaProva.contains(q.getId())) ordenadas.add(q);
                }
                for (Questao q : questoesBancoGeral) {
                        if (!idsDaProva.contains(q.getId())) ordenadas.add(q);
                }
                questoesBancoGeral = ordenadas;
        }

        // Peso 0 (rascunho) volta como campo vazio para o professor preencher
        private String formatarPeso(BigDecimal peso) {
                return (peso == null || peso.compareTo(BigDecimal.ZERO) <= 0)
                        ? "" : peso.stripTrailingZeros().toPlainString();
        }

        // Texto do campo -> peso; vazio vira zero
        private BigDecimal parsePeso(String texto) {
                String t = texto == null ? "" : texto.trim().replace(",", ".");
                return t.isEmpty() ? BigDecimal.ZERO : new BigDecimal(t);
        }

        private void inicializarArraysComponentes() {
                checkBoxes = new CheckBox[] {
                        checkbox1, checkbox11, checkbox111, checkbox112,
                        checkbox113, checkbox114, checkbox115, checkbox116,
                        checkbox117, checkbox118, checkbox119
                };

                camposValores = new TextField[] {
                        valor_questao1, valor_questao11, valor_questao111, valor_questao112,
                        valor_questao113, valor_questao114, valor_questao115, valor_questao116,
                        valor_questao117, valor_questao118, valor_questao119
                };
        }

        // Retorna o e-mail do professor logado, ou null se não houver sessão válida
        private String getEmailProfessorLogado() {
                if (Sessao.getUsuario() == null || Sessao.getUsuario().getEmail() == null) {
                        return null;
                }
                return Sessao.getUsuario().getEmail();
        }

        private void carregarDadosDoBanco() {
                try {
                        String emailProfessor = getEmailProfessorLogado();
                        if (emailProfessor == null) {
                                mostrarAlerta("Erro", "Nenhum usuário logado encontrado. Faça o login novamente.");
                                return;
                        }

                        // Disciplinas que ESTE professor ministra
                        disciplinasDoProfessor = disciplinaDAO.listarPorProfessor(emailProfessor);

                        // Apenas as questões das disciplinas do professor
                        questoesBancoGeral = questaoDAO.buscarQuestoes().stream()
                                .filter(q -> disciplinasDoProfessor.contains(q.getNomeDisciplina()))
                                .collect(Collectors.toList());

                        // Filtro de disciplina: com opção em branco para limpar o filtro
                        List<String> opcoesFiltro = new ArrayList<>(disciplinasDoProfessor);
                        opcoesFiltro.add(0, "");
                        if (box_filtro_disciplina != null) {
                                box_filtro_disciplina.setItems(FXCollections.observableArrayList(opcoesFiltro));
                        }

                        // ComboBox da prova: sem opção em branco (a prova exige disciplina por causa da FK)
                        if (boxDisciplina != null) {
                                boxDisciplina.setItems(FXCollections.observableArrayList(disciplinasDoProfessor));
                        }

                        ObservableList<String> obsDificuldades = FXCollections.observableArrayList("", "Fácil", "Média", "Difícil");
                        if (box_filtro_dificuldade != null) box_filtro_dificuldade.setItems(obsDificuldades);

                        ObservableList<String> obsUltimoUso = FXCollections.observableArrayList("", "6 meses", "1 ano", "2 anos", "3 anos ou mais");
                        if (box_filtro_ultimo_uso != null) box_filtro_ultimo_uso.setItems(obsUltimoUso);

                        aplicarFiltros();

                } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Erro", "Erro ao carregar dados: " + e.getMessage());
                }
        }

        private void configurarFiltrosDinamicos() {
                if (box_filtro_disciplina != null) {
                        box_filtro_disciplina.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
                }
                if (box_filtro_dificuldade != null) {
                        box_filtro_dificuldade.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
                }
                if (box_filtro_ultimo_uso != null) {
                        box_filtro_ultimo_uso.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
                }
        }

        private void salvarEstadoAtualNaUI() {
                if (questoesFiltradas == null) return;

                for (int i = 0; i < checkBoxes.length; i++) {
                        if (i < questoesFiltradas.size()) {
                                Questao q = questoesFiltradas.get(i);
                                estadosSelecionados.put(q.getId(), checkBoxes[i].isSelected());
                                valoresQuestaoMap.put(q.getId(), camposValores[i].getText());
                        }
                }
        }

        private void aplicarFiltros() {
                if (questoesBancoGeral == null) return;

                // Salva o estado atual dos campos visíveis antes de mudar o filtro
                salvarEstadoAtualNaUI();

                String disciplinaSelecionada = box_filtro_disciplina != null ? box_filtro_disciplina.getValue() : null;
                String dificuldadeSelecionada = box_filtro_dificuldade != null ? box_filtro_dificuldade.getValue() : null;
                String ultimoUsoSelecionado = box_filtro_ultimo_uso != null ? box_filtro_ultimo_uso.getValue() : null;

                questoesFiltradas = questoesBancoGeral.stream().filter(q -> {
                        boolean matchDisciplina = (disciplinaSelecionada == null || disciplinaSelecionada.isEmpty() || disciplinaSelecionada.equals(q.getNomeDisciplina()));
                        boolean matchDificuldade = (dificuldadeSelecionada == null || dificuldadeSelecionada.isEmpty() || dificuldadeSelecionada.equalsIgnoreCase(q.getDificuldade()));
                        boolean matchUltimoUso = (ultimoUsoSelecionado == null || ultimoUsoSelecionado.isEmpty() ||
                                (q.getUltimoUso() != null && q.getUltimoUso().equalsIgnoreCase(ultimoUsoSelecionado)));

                        return matchDisciplina && matchDificuldade && matchUltimoUso;
                }).collect(Collectors.toList());

                atualizarExibicaoCheckBoxes();
        }

        private void atualizarExibicaoCheckBoxes() {
                for (int i = 0; i < checkBoxes.length; i++) {
                        CheckBox chk = checkBoxes[i];
                        TextField txtVal = camposValores[i];

                        if (i < questoesFiltradas.size()) {
                                Questao q = questoesFiltradas.get(i);
                                chk.setText(q.getEnunciado() + " (" + q.getDificuldade() + ")");
                                chk.setVisible(true);
                                chk.setManaged(true);
                                chk.setDisable(false);

                                txtVal.setVisible(true);
                                txtVal.setManaged(true);
                                txtVal.setDisable(false);

                                chk.setSelected(estadosSelecionados.getOrDefault(q.getId(), false));
                                txtVal.setText(valoresQuestaoMap.getOrDefault(q.getId(), ""));
                        } else {
                                chk.setText("");
                                chk.setSelected(false);
                                chk.setVisible(false);
                                chk.setManaged(false);
                                chk.setDisable(true);

                                txtVal.setText("");
                                txtVal.setVisible(false);
                                txtVal.setManaged(false);
                                txtVal.setDisable(true);
                        }
                }
        }

        private void atualizacaoValorProva() {
                for (int i = 0; i < checkBoxes.length; i++) {
                        final int index = i;
                        checkBoxes[i].selectedProperty().addListener((obs, oldVal, newVal) -> {
                                if (index < questoesFiltradas.size()) {
                                        Questao q = questoesFiltradas.get(index);
                                        estadosSelecionados.put(q.getId(), newVal);
                                }
                                recalcularValorTela();
                        });

                        camposValores[i].textProperty().addListener((obs, oldVal, newVal) -> {
                                if (index < questoesFiltradas.size()) {
                                        Questao q = questoesFiltradas.get(index);
                                        valoresQuestaoMap.put(q.getId(), newVal);
                                }
                                recalcularValorTela();
                        });
                }
        }

        private void recalcularValorTela() {
                double somaTotal = 0.0;
                boolean temValorInformado = false;

                for (Map.Entry<Long, Boolean> entry : estadosSelecionados.entrySet()) {
                        if (entry.getValue()) {
                                String texto = valoresQuestaoMap.getOrDefault(entry.getKey(), "").trim();
                                if (!texto.isEmpty()) {
                                        try {
                                                double valor = Double.parseDouble(texto.replace(",", "."));
                                                if (valor > 0) {
                                                        somaTotal += valor;
                                                        temValorInformado = true;
                                                }
                                        } catch (NumberFormatException e) {
                                                // Ignora temporariamente durante a digitação
                                        }
                                }
                        }
                }

                if (temValorInformado) {
                        if (somaTotal == (int) somaTotal) {
                                txtValorProva.setText(String.valueOf((int) somaTotal));
                        } else {
                                txtValorProva.setText(String.format("%.2f", somaTotal).replace(",", "."));
                        }
                } else {
                        txtValorProva.setText("");
                }
        }

        // A tabela questao usa 'Fácil','Média','Difícil', mas prova.dificuldade usa 'facil','media','dificil'
        private String normalizarDificuldadeProva(String d) {
                if (d == null || d.isEmpty()) return null;
                switch (d) {
                        case "Fácil":   return "facil";
                        case "Média":   return "media";
                        case "Difícil": return "dificil";
                        default:        return null;
                }
        }

        @FXML
        void SalvarProva(ActionEvent event) {
                // 1. Professor logado
                String emailProfessor = getEmailProfessorLogado();
                if (emailProfessor == null) {
                        mostrarAlerta("Erro", "Nenhum usuário logado encontrado. Faça o login novamente.");
                        return;
                }

                // 2. Disciplina da prova: precisa ser uma das que o professor ministra
                String disciplinaProva = boxDisciplina != null ? boxDisciplina.getValue() : null;
                if (disciplinaProva == null || disciplinaProva.isEmpty()
                        || !disciplinasDoProfessor.contains(disciplinaProva)) {
                        mostrarAlerta("Atenção!", "Selecione uma disciplina que você ministra.");
                        return;
                }

                salvarEstadoAtualNaUI();

                List<Long> idsSelecionados = estadosSelecionados.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                // 3. Todas as questões selecionadas devem ser da disciplina da prova
                boolean questaoDeOutraDisciplina = questoesBancoGeral.stream()
                        .filter(q -> idsSelecionados.contains(q.getId()))
                        .anyMatch(q -> !disciplinaProva.equals(q.getNomeDisciplina()));
                if (questaoDeOutraDisciplina) {
                        mostrarAlerta("Atenção!", "Há questões selecionadas que não pertencem à disciplina \""
                                + disciplinaProva + "\".");
                        return;
                }

                List<String> valoresTextuais = new ArrayList<>();
                for (Long id : idsSelecionados) {
                        valoresTextuais.add(valoresQuestaoMap.getOrDefault(id, ""));
                }

                List<Integer> indicesDummy = new ArrayList<>();
                for (int i = 0; i < idsSelecionados.size(); i++) indicesDummy.add(i);

                MontagemProva prova = new MontagemProva();
                prova.setNomeProva(txtNomeProva != null ? txtNomeProva.getText() : "");
                prova.setNomeDisciplina(disciplinaProva);
                prova.setEmailProfessor(emailProfessor);

                // Rascunho: todas as questões selecionadas sem valor (valor da prova fica 0)
                boolean rascunho = MontagemProva.ehRascunho(valoresTextuais);

                if (!rascunho) {
                        String valorStr = txtValorProva.getText().trim().replace(",", ".");
                        if (!valorStr.isEmpty()) {
                                try {
                                        prova.setValor(new BigDecimal(valorStr));
                                } catch (NumberFormatException e) {
                                        prova.setValor(null);
                                }
                        }
                }

                // 4. Valida a prova (rascunho, valores parciais, valor total igual a 10, valores das questões etc.)
                String resultadoValidacao = prova.validarParaSalvar(indicesDummy, valoresTextuais);
                if (resultadoValidacao != null) {
                        mostrarAlerta("Atenção!", resultadoValidacao);
                        return;
                }

                try {
                        String dificuldadeProva = normalizarDificuldadeProva(
                                box_filtro_dificuldade != null ? box_filtro_dificuldade.getValue() : null);

                        if (provaEmEdicao != null) {
                                // Sem filtro de dificuldade escolhido, mantém a dificuldade já gravada
                                if (dificuldadeProva == null) {
                                        dificuldadeProva = provaEmEdicao.getDificuldade();
                                }
                                atualizarProvaExistente(provaEmEdicao.getProvaId(), prova.getNomeProva(), dificuldadeProva,
                                        prova.getValor(), emailProfessor, disciplinaProva, idsSelecionados);

                                mostrarAlerta("Sucesso", rascunho
                                        ? "Rascunho atualizado com sucesso!"
                                        : "Prova atualizada com sucesso!\nValor total: " + txtValorProva.getText());
                                provaEmEdicao = null;
                                App.abrirTela("SelecaoProva", "aNota", 1350, 1080);
                                return;
                        }

                        Long provaId = montagemProvaDAO.nova(
                                prova.getNomeProva(), dificuldadeProva, prova.getValor(),
                                emailProfessor, disciplinaProva);

                        for (Long questaoId : idsSelecionados) {
                                Questao q = questoesBancoGeral.stream()
                                        .filter(item -> item.getId().equals(questaoId))
                                        .findFirst().orElse(null);
                                if (q != null) {
                                        String pesoStr = valoresQuestaoMap.getOrDefault(questaoId, "").trim().replace(",", ".");
                                        BigDecimal pesoQuestao = pesoStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(pesoStr);

                                        montagemProvaDAO.associarQuestao(provaId, q.getId(), emailProfessor, disciplinaProva, pesoQuestao);
                                }
                        }

                        mostrarAlerta("Sucesso", rascunho
                                ? "Rascunho salvo com sucesso!\nDefina o valor das questões em EDITAR para poder agendá-lo."
                                : "Prova salva com sucesso!\nValor total: " + txtValorProva.getText());
                } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Erro", "Erro ao salvar a prova: " + e.getMessage());
                }
        }

        // Atualiza a prova existente (não insere outra, por causa da chave única nome/professor/disciplina)
        private void atualizarProvaExistente(long provaId, String nomeProva, String dificuldade, BigDecimal valor,
                                             String emailProfessor, String nomeDisciplina, List<Long> idsQuestoes) throws SQLException {
                try (Connection con = ConexaoBD.conectar()) {
                        con.setAutoCommit(false);
                        try {
                                try (PreparedStatement ps = con.prepareStatement("DELETE FROM prova_questao WHERE prova_id = ?")) {
                                        ps.setLong(1, provaId);
                                        ps.executeUpdate();
                                }

                                String upd = "UPDATE prova SET nome_prova = ?, dificuldade = ?, valor = ?, "
                                        + "email_professor = ?, nome_disciplina = ? WHERE prova_id = ?";
                                try (PreparedStatement ps = con.prepareStatement(upd)) {
                                        ps.setString(1, nomeProva);
                                        if (dificuldade == null || dificuldade.trim().isEmpty()) {
                                                ps.setNull(2, java.sql.Types.VARCHAR);
                                        } else {
                                                ps.setString(2, dificuldade);
                                        }
                                        ps.setBigDecimal(3, valor);
                                        ps.setString(4, emailProfessor);
                                        ps.setString(5, nomeDisciplina);
                                        ps.setLong(6, provaId);
                                        ps.executeUpdate();
                                }

                                String ins = "INSERT INTO prova_questao (prova_id, questao_id, email_professor, nome_disciplina, peso) "
                                        + "VALUES (?, ?, ?, ?, ?)";
                                try (PreparedStatement ps = con.prepareStatement(ins)) {
                                        for (Long questaoId : idsQuestoes) {
                                                ps.setLong(1, provaId);
                                                ps.setLong(2, questaoId);
                                                ps.setString(3, emailProfessor);
                                                ps.setString(4, nomeDisciplina);
                                                ps.setBigDecimal(5, parsePeso(valoresQuestaoMap.get(questaoId)));
                                                ps.executeUpdate();
                                        }
                                }
                                con.commit();
                        } catch (SQLException | RuntimeException e) {
                                con.rollback();
                                throw e;
                        }
                }
        }

        private void mostrarAlerta(String titulo, String mensagem) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(titulo);
                alert.setHeaderText(null);
                alert.setContentText(mensagem);
                alert.showAndWait();
        }

        @FXML
        void AddQuestao(ActionEvent event) {
                salvarEstadoAtualNaUI();
                perguntasTemporarias.clear();
                valoresTemporarios.clear();

                boolean selecionouAlguma = false;

                for (Map.Entry<Long, Boolean> entry : estadosSelecionados.entrySet()) {
                        if (entry.getValue()) {
                                Long qId = entry.getKey();
                                Questao q = questoesBancoGeral.stream().filter(item -> item.getId().equals(qId)).findFirst().orElse(null);
                                if (q != null) {
                                        selecionouAlguma = true;
                                        perguntasTemporarias.add(q.getEnunciado());
                                        valoresTemporarios.add(valoresQuestaoMap.getOrDefault(qId, ""));
                                }
                        }
                }

                if (!selecionouAlguma) {
                        mostrarAlerta("Atenção", "Selecione ao menos uma questão!");
                        return;
                }

                mostrarAlerta("Sucesso", "Questões adicionadas com sucesso para a prova!");
        }

        @FXML void IrProva_montar(ActionEvent event) {}
        @FXML void IrQuestao_criar(ActionEvent event) throws IOException { App.setRoot("CadastroQuestoes"); }

        @FXML void VerProva(ActionEvent event) {
                try {
                        salvarEstadoAtualNaUI();

                        perguntasTemporarias.clear();
                        valoresTemporarios.clear();

                        List<String> autoresTemporarios = new ArrayList<>();

                        for (Map.Entry<Long, Boolean> entry : estadosSelecionados.entrySet()) {
                                if (entry.getValue()) {
                                        Long qId = entry.getKey();
                                        Questao q = questoesBancoGeral.stream().filter(item -> item.getId().equals(qId)).findFirst().orElse(null);
                                        if (q != null) {
                                                perguntasTemporarias.add(q.getEnunciado());
                                                valoresTemporarios.add(valoresQuestaoMap.getOrDefault(qId, ""));

                                                String autor = q.getEmailProfessor() != null ? q.getEmailProfessor() : "Desconhecido";
                                                autoresTemporarios.add(autor);
                                        }
                                }
                        }

                        if (perguntasTemporarias.isEmpty()) {
                                mostrarAlerta("Atenção", "Nenhuma questão foi adicionada. Selecione e clique em 'Adicionar pergunta' primeiro!");
                                return;
                        }

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("visualizacao.fxml"));
                        Parent root = loader.load();

                        VisualizarProvaController visualizadorController = loader.getController();

                        String nomeP = txtNomeProva != null ? txtNomeProva.getText() : "";
                        String discP = boxDisciplina != null && boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "";
                        String valP = txtValorProva != null ? txtValorProva.getText() : "";

                        List<String> perguntasComPeso = new ArrayList<>();
                        List<String> respostasList = new ArrayList<>();

                        for (int i = 0; i < perguntasTemporarias.size(); i++) {
                                String enunciado = perguntasTemporarias.get(i);
                                String peso = (i < valoresTemporarios.size()) ? valoresTemporarios.get(i) : "";
                                if (peso.trim().isEmpty()) peso = "0";

                                String autor = (i < autoresTemporarios.size()) ? autoresTemporarios.get(i) : "Desconhecido";

                                String perguntaFormatada = enunciado + "\nValor: " + peso + "\nAutor: " + autor;
                                perguntasComPeso.add(perguntaFormatada);

                                List<String> listaRespostasBanco = respostaDAO.buscarRespostasPorEnunciado(enunciado);
                                String gabaritoFormatado = String.join(" / ", listaRespostasBanco);
                                if (gabaritoFormatado.isEmpty()) {
                                        gabaritoFormatado = "Nenhum gabarito cadastrado.";
                                }
                                respostasList.add(gabaritoFormatado);
                        }

                        visualizadorController.setDadosProva(nomeP, discP, valP, perguntasComPeso, respostasList);

                        Stage stagePopup = new Stage();
                        stagePopup.setTitle("Visualização da Prova");
                        stagePopup.setScene(new Scene(root));
                        stagePopup.initModality(Modality.APPLICATION_MODAL);
                        stagePopup.showAndWait();

                } catch (IOException e) {
                        e.printStackTrace();
                        mostrarAlerta("Erro", "Não foi possível abrir a tela de visualização: " + e.getMessage());
                }
        }

        @FXML void irAlunos(ActionEvent event) {}
        @FXML void irAlunos_corrigir(ActionEvent event) throws IOException { App.setRoot("Aluno"); }
        @FXML void irAlunos_notas(ActionEvent event) {}
        @FXML void irProva(ActionEvent event) {}
        @FXML void irProva_agendar(ActionEvent event) throws IOException { App.abrirTela("SelecaoProva", "aNota", 1350, 1080); }
        @FXML void irProva_pesos(ActionEvent event)      throws IOException{ App.setRoot("pesos");}
        @FXML void irQuestao(ActionEvent event) {}
        @FXML void irQuestao_consultar(ActionEvent event) throws IOException { App.setRoot("VisualizarQuestao"); }
}
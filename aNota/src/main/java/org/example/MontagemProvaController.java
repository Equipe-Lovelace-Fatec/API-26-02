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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MontagemProvaController {

        private final QuestaoDAO questaoDAO = new QuestaoDAO();
        private final ProvaDAO montagemProvaDAO = new ProvaDAO();

        private CheckBox[] checkBoxes;
        private TextField[] camposValores;
        private List<Questao> questoesBancoGeral; // Guarda todas as questões do banco
        private List<Questao> questoesFiltradas = new ArrayList<>();   // Guarda apenas as questões que atendem aos filtros atuais
        private final RespostaDAO respostaDAO = new RespostaDAO();

        // Estruturas para reter o estado de seleção e valores por ID da Questão
        private final Map<Long, Boolean> estadosSelecionados = new HashMap<>();
        private final Map<Long, String> valoresQuestaoMap = new HashMap<>();

        // Listas temporárias para passar os dados para a tela de visualização
        public static List<String> perguntasTemporarias = new ArrayList<>();
        public static List<String> valoresTemporarios = new ArrayList<>();

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
                inicializarArraysComponentes();
                carregarDadosDoBanco();
                configurarFiltrosDinamicos();
                atualizacaoValorProva();
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

        private void carregarDadosDoBanco() {
                try {
                        questoesBancoGeral = questaoDAO.buscarQuestoes();

                        // Preenche as disciplinas únicas e adiciona um espaço em branco no início
                        List<String> disciplinasUnicas = questoesBancoGeral.stream()
                                .map(Questao::getNomeDisciplina)
                                .distinct()
                                .collect(Collectors.toList());

                        disciplinasUnicas.add(0, ""); // Adiciona opção em branco para limpar o filtro

                        ObservableList<String> obsDisciplinas = FXCollections.observableArrayList(disciplinasUnicas);
                        if (boxDisciplina != null) boxDisciplina.setItems(obsDisciplinas);
                        if (box_filtro_disciplina != null) box_filtro_disciplina.setItems(obsDisciplinas);

                        // Preenche o filtro de dificuldades incluindo uma opção em branco no início
                        ObservableList<String> obsDificuldades = FXCollections.observableArrayList("", "Fácil", "Média", "Difícil");
                        if (box_filtro_dificuldade != null) box_filtro_dificuldade.setItems(obsDificuldades);

                        // Preenche o filtro de último uso incluindo uma opção em branco no início
                        ObservableList<String> obsUltimoUso = FXCollections.observableArrayList("", "6 meses", "1 ano", "2 anos", "3 anos ou mais");
                        if (box_filtro_ultimo_uso != null) box_filtro_ultimo_uso.setItems(obsUltimoUso);

                        // Inicialmente exibe todas as questões
                        aplicarFiltros();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private void configurarFiltrosDinamicos() {
                // Sempre que o usuário alterar os filtros, atualiza os checkboxes na tela
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

                // Filtra a lista geral com base nos valores selecionados nos ComboBoxes de filtro
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

                                // Atribui corretamente os valores salvos específicos da questão atual da linha
                                chk.setSelected(estadosSelecionados.getOrDefault(q.getId(), false));
                                txtVal.setText(valoresQuestaoMap.getOrDefault(q.getId(), ""));
                        } else {
                                // Limpa e oculta linhas excedentes para não misturar dados visuais antigos
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

                // Percorre todos os mapas globais em vez de depender apenas dos índices da tela atual
                for (Map.Entry<Long, Boolean> entry : estadosSelecionados.entrySet()) {
                        if (entry.getValue()) { // Se estiver selecionado
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

        @FXML
        void SalvarProva(ActionEvent event) {
                salvarEstadoAtualNaUI();

                List<Long> idsSelecionados = estadosSelecionados.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                List<String> valoresTextuais = new ArrayList<>();
                for (Long id : idsSelecionados) {
                        valoresTextuais.add(valoresQuestaoMap.getOrDefault(id, ""));
                }

                List<Integer> indicesDummy = new ArrayList<>();
                for (int i = 0; i < idsSelecionados.size(); i++) indicesDummy.add(i);

                MontagemProva prova = new MontagemProva();
                prova.setNomeProva(txtNomeProva != null ? txtNomeProva.getText() : "");
                prova.setNomeDisciplina(boxDisciplina != null && boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "");

                String valorStr = txtValorProva.getText().trim().replace(",", ".");
                if (!valorStr.isEmpty()) {
                        try {
                                prova.setValor(new BigDecimal(valorStr));
                        } catch (NumberFormatException e) {
                                prova.setValor(null);
                        }
                }

                // Valida a prova (exigindo valor total igual a 10 e sem rascunho)
                String resultadoValidacao = prova.validarProva(indicesDummy, valoresTextuais);
                if (resultadoValidacao != null) {
                        mostrarAlerta("Atenção!", resultadoValidacao);
                        return;
                }

                try {
                        String dificuldadeProva = box_filtro_dificuldade != null ? box_filtro_dificuldade.getValue() : null;

                        // Pega o e-mail dinamicamente da sessão do usuário logado
                        if (Sessao.getUsuario() == null || Sessao.getUsuario().getEmail() == null) {
                                mostrarAlerta("Erro", "Nenhum usuário logado encontrado. Faça o login novamente.");
                                return;
                        }
                        String emailProfessor = Sessao.getUsuario().getEmail();

                        Long provaId = montagemProvaDAO.nova(prova.getNomeProva(), dificuldadeProva, prova.getValor(), emailProfessor, prova.getNomeDisciplina());

                        for (Long questaoId : idsSelecionados) {
                                Questao q = questoesBancoGeral.stream().filter(item -> item.getId().equals(questaoId)).findFirst().orElse(null);
                                if (q != null) {
                                        String pesoStr = valoresQuestaoMap.getOrDefault(questaoId, "").trim().replace(",", ".");
                                        BigDecimal pesoQuestao = pesoStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(pesoStr);

                                        montagemProvaDAO.associarQuestao(provaId, q.getId(), emailProfessor, prova.getNomeDisciplina(), pesoQuestao);
                                }
                        }

                        mostrarAlerta("Sucesso", "Prova salva com sucesso!\nValor total: " + txtValorProva.getText());
                } catch (Exception e) {
                        e.printStackTrace();
                        mostrarAlerta("Erro", "Erro ao salvar a prova: " + e.getMessage());
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

                        // Lista para guardar os autores correspondentes às questões temporárias
                        List<String> autoresTemporarios = new ArrayList<>();

                        for (Map.Entry<Long, Boolean> entry : estadosSelecionados.entrySet()) {
                                if (entry.getValue()) {
                                        Long qId = entry.getKey();
                                        Questao q = questoesBancoGeral.stream().filter(item -> item.getId().equals(qId)).findFirst().orElse(null);
                                        if (q != null) {
                                                perguntasTemporarias.add(q.getEnunciado());
                                                valoresTemporarios.add(valoresQuestaoMap.getOrDefault(qId, ""));

                                                // Pega o autor da questão (substitua getEmailProfessor() pelo metodo real da sua classe Questao se for diferente)
                                                String autor = q.getEmailProfessor() != null ? q.getEmailProfessor() : "Desconhecido";
                                                autoresTemporarios.add(autor);
                                        }
                                }
                        }

                        if (perguntasTemporarias == null || perguntasTemporarias.isEmpty()) {
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
                                String peso = (valoresTemporarios != null && i < valoresTemporarios.size()) ? valoresTemporarios.get(i) : "";
                                if (peso.trim().isEmpty()) peso = "0";

                                String autor = (autoresTemporarios != null && i < autoresTemporarios.size()) ? autoresTemporarios.get(i) : "Desconhecido";

                                // Formata o enunciado incluindo o valor e logo abaixo o autor da questão
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
        @FXML void irQuestao(ActionEvent event) {}
        @FXML void irQuestao_consultar(ActionEvent event) throws IOException { App.setRoot("VisualizarQuestao"); }
}
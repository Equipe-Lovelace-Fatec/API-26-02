package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MontagemProvaController {

        // Instância do DAO para acessar o banco de dados
        private final QuestaoDAO questaoDAO = new QuestaoDAO();

        // Listas auxiliares para gerenciar os CheckBoxes e associá-los às questões do banco
        private List<CheckBox> listaCheckBoxes;
        private List<Questao> questoesCarregadas;

        // Guarda os dados preenchidos no cabeçalho da prova
        public static String nomeProvaSalvo = "";
        public static String disciplinaSalva = "";
        public static String valorProvaSalvo = "";

        @FXML
        private ComboBox<String> boxDisciplina;

        @FXML
        private ComboBox<String> box_filtro_dificuldade;

        @FXML
        private ComboBox<String> box_filtro_disciplina;

        @FXML
        private ComboBox<String> box_filtro_ultimo_uso;

        @FXML
        private MenuItem btProva_agendar;

        @FXML
        private MenuItem btProva_montar;

        @FXML
        private MenuItem btalunos_corrigir;

        @FXML
        private MenuItem btalunos_notas;

        @FXML
        private Button btnAddQuestao;

        @FXML
        private MenuButton btnAlunos;

        @FXML
        private MenuButton btnProva;

        @FXML
        private MenuButton btnQuestoes;

        @FXML
        private Button btnSalvarProva;

        @FXML
        private Button btnVerProva;

        @FXML
        private MenuItem btquestoes_consultar;

        @FXML
        private MenuItem btquestoes_criar;

        @FXML
        private CheckBox checkbox1;

        @FXML
        private CheckBox checkbox11;

        @FXML
        private CheckBox checkbox111;

        @FXML
        private CheckBox checkbox112;

        @FXML
        private CheckBox checkbox113;

        @FXML
        private CheckBox checkbox114;

        @FXML
        private CheckBox checkbox115;

        @FXML
        private CheckBox checkbox116;

        @FXML
        private CheckBox checkbox117;

        @FXML
        private CheckBox checkbox118;

        @FXML
        private CheckBox checkbox119;

        @FXML
        private TextField txtNomeProva;

        @FXML
        private Label txtValorProva;

        @FXML
        private TextField valor_questao1;

        @FXML
        private TextField valor_questao11;

        @FXML
        private TextField valor_questao111;

        @FXML
        private TextField valor_questao112;

        @FXML
        private TextField valor_questao113;

        @FXML
        private TextField valor_questao114;

        @FXML
        private TextField valor_questao115;

        @FXML
        private TextField valor_questao116;

        @FXML
        private TextField valor_questao117;

        @FXML
        private TextField valor_questao118;

        @FXML
        private TextField valor_questao119;

        /**
         * O método initialize é executado automaticamente pelo JavaFX assim que a tela é carregada.
         * É o local ideal para carregar dados do banco para os ComboBoxes e CheckBoxes.
         */
        @FXML
        public void initialize() {
                carregarFiltrosEDisciplinas();
                inicializarCheckBoxesComBanco();
        }

        private void carregarFiltrosEDisciplinas() {
                try {
                        // Busca todas as questões cadastradas no banco de dados
                        List<Questao> questoesBanco = questaoDAO.buscarQuestoes();

                        // Extrai as disciplinas únicas das questões para preencher os ComboBoxes de filtro
                        List<String> disciplinasUnicas = questoesBanco.stream()
                                .map(Questao::getNomeDisciplina)
                                .distinct()
                                .collect(Collectors.toList());

                        ObservableList<String> obsDisciplinas = FXCollections.observableArrayList(disciplinasUnicas);

                        if (boxDisciplina != null) {
                                boxDisciplina.setItems(obsDisciplinas);
                        }
                        if (box_filtro_disciplina != null) {
                                box_filtro_disciplina.setItems(obsDisciplinas);
                        }

                        // Preenche o filtro de dificuldades fixas baseadas na estrutura do banco
                        ObservableList<String> obsDificuldades = FXCollections.observableArrayList("Fácil", "Média", "Difícil");
                        if (box_filtro_dificuldade != null) {
                                box_filtro_dificuldade.setItems(obsDificuldades);
                        }

                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Erro ao carregar dados do banco para a tela de montagem de prova: " + e.getMessage());
                }
        }

        private void inicializarCheckBoxesComBanco() {
                // Agrupa os CheckBoxes da tela em uma lista para facilitar a manipulação sequencial
                listaCheckBoxes = List.of(
                        checkbox1, checkbox11, checkbox111, checkbox112,
                        checkbox113, checkbox114, checkbox115, checkbox116,
                        checkbox117, checkbox118, checkbox119
                );

                try {
                        // Busca as questões do banco de dados
                        questoesCarregadas = questaoDAO.buscarQuestoes();

                        // Percorre os CheckBoxes e preenche o texto com o enunciado correspondente do banco
                        for (int i = 0; i < listaCheckBoxes.size(); i++) {
                                CheckBox chk = listaCheckBoxes.get(i);

                                if (i < questoesCarregadas.size()) {
                                        Questao q = questoesCarregadas.get(i);
                                        chk.setText(q.getEnunciado());
                                        chk.setVisible(true);
                                        chk.setDisable(false);
                                        chk.setSelected(false); // Limpa seleção anterior
                                } else {
                                        // Se houver mais checkboxes na tela do que questões cadastradas, oculta os excedentes
                                        chk.setText("");
                                        chk.setVisible(false);
                                        chk.setDisable(true);
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Erro ao carregar questões para os checkboxes: " + e.getMessage());
                }
        }

        @FXML
        void AddQuestao(ActionEvent event) {
                // Lógica adicional para adicionar a questão selecionada na prova, se necessário
        }

        @FXML
        void IrProva_montar(ActionEvent event) {

        }

        @FXML
        void IrQuestao_criar(ActionEvent event) throws IOException {
                App.setRoot("CadastroQuestoes");
        }

        @FXML
        void SalvarProva(ActionEvent event) {
                // Salva os dados preenchidos no cabeçalho da prova
                nomeProvaSalvo = txtNomeProva != null ? txtNomeProva.getText() : "";
                if (boxDisciplina != null && boxDisciplina.getValue() != null) {
                        disciplinaSalva = boxDisciplina.getValue();
                }
                valorProvaSalvo = txtValorProva != null ? txtValorProva.getText() : "";

                // Captura quais questões foram marcadas pelo usuário nos CheckBoxes
                if (listaCheckBoxes != null && questoesCarregadas != null) {
                        for (int i = 0; i < listaCheckBoxes.size(); i++) {
                                if (i < questoesCarregadas.size()) {
                                        CheckBox chk = listaCheckBoxes.get(i);
                                        if (chk.isSelected()) {
                                                Questao questaoSelecionada = questoesCarregadas.get(i);
                                                System.out.println("Questão selecionada para a prova: " + questaoSelecionada.getEnunciado());
                                        }
                                }
                        }
                }
        }

        @FXML
        void VerProva(ActionEvent event) throws IOException {
                App.setRoot("visualizacao");
        }

        @FXML
        void irAlunos(ActionEvent event) {

        }

        @FXML
        void irAlunos_corrigir(ActionEvent event) throws IOException {
                App.setRoot("Aluno");
        }

        @FXML
        void irAlunos_notas(ActionEvent event) {

        }

        @FXML
        void irProva(ActionEvent event) {

        }

        @FXML
        void irProva_agendar(ActionEvent event) {

        }

        @FXML
        void irQuestao(ActionEvent event) {

        }

        @FXML
        void irQuestao_consultar(ActionEvent event) {

        }
}
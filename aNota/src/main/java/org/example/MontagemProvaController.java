package org.example;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MontagemProvaController {

        private final QuestaoDAO questaoDAO = new QuestaoDAO();
        private final ProvaDAO montagemProvaDAO = new ProvaDAO();

        private CheckBox[] checkBoxes;
        private TextField[] camposValores;
        private List<Questao> questoesCarregadas;

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
                carregarFiltrosEDisciplinas();
                inicializarCheckBoxesComBanco();
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

        private void carregarFiltrosEDisciplinas() {
                try {
                        List<Questao> questoesBanco = questaoDAO.buscarQuestoes();
                        List<String> disciplinasUnicas = questoesBanco.stream()
                                .map(Questao::getNomeDisciplina)
                                .distinct()
                                .collect(Collectors.toList());

                        ObservableList<String> obsDisciplinas = FXCollections.observableArrayList(disciplinasUnicas);
                        if (boxDisciplina != null) boxDisciplina.setItems(obsDisciplinas);
                        if (box_filtro_disciplina != null) box_filtro_disciplina.setItems(obsDisciplinas);

                        ObservableList<String> obsDificuldades = FXCollections.observableArrayList("fácil", "média", "difícil");
                        if (box_filtro_dificuldade != null) box_filtro_dificuldade.setItems(obsDificuldades);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private void inicializarCheckBoxesComBanco() {
                try {
                        questoesCarregadas = questaoDAO.buscarQuestoes();
                        for (int i = 0; i < checkBoxes.length; i++) {
                                CheckBox chk = checkBoxes[i];
                                TextField txtVal = camposValores[i];

                                if (i < questoesCarregadas.size()) {
                                        Questao q = questoesCarregadas.get(i);
                                        chk.setText(q.getEnunciado());
                                        chk.setVisible(true);
                                        chk.setDisable(false);
                                        chk.setSelected(false);
                                        txtVal.setVisible(true);
                                        txtVal.setDisable(false);
                                } else {
                                        chk.setText("");
                                        chk.setVisible(false);
                                        chk.setDisable(true);
                                        txtVal.setText("");
                                        txtVal.setVisible(false);
                                        txtVal.setDisable(true);
                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private void atualizacaoValorProva() {
                for (int i = 0; i < checkBoxes.length; i++) {
                        checkBoxes[i].selectedProperty().addListener((obs, oldVal, newVal) -> recalcularValorTela());
                        camposValores[i].textProperty().addListener((obs, oldVal, newVal) -> recalcularValorTela());
                }
        }

        private void recalcularValorTela() {
                double somaTotal = 0.0;
                boolean temValorInformado = false;

                for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                                String texto = camposValores[i].getText().trim();
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
                List<Integer> indicesSelecionados = new ArrayList<>();
                List<String> valoresTextuais = new ArrayList<>();

                for (int i = 0; i < checkBoxes.length; i++) {
                        if (checkBoxes[i].isSelected()) {
                                indicesSelecionados.add(i);
                        }
                        valoresTextuais.add(camposValores[i].getText());
                }

                MontagemProva prova = new MontagemProva();
                prova.setNomeProva(txtNomeProva != null ? txtNomeProva.getText() : "");
                prova.setNomeDisciplina(boxDisciplina != null && boxDisciplina.getValue() != null ? boxDisciplina.getValue() : "");

                String valorStr = txtValorProva.getText().trim().replace(",", ".");
                if (!valorStr.isEmpty()) {
                        prova.setValor(new BigDecimal(valorStr));
                }

                String resultadoValidacao = prova.validarProva(indicesSelecionados, valoresTextuais);

                if ("RASCUNHO".equals(resultadoValidacao)) {
                        mostrarAlerta("Sucesso", "Prova salva como rascunho (sem valores definidos).");
                        return;
                } else if (resultadoValidacao != null) {
                        mostrarAlerta("Atenção!", resultadoValidacao);
                        return;
                }

                try {
                        String dificuldade = null;
                        String emailProfessor = "teresinha.fatima@prof.fatec.com";

                        // 1. Salva a prova e recupera o ID gerado automaticamente pelo banco
                        Long provaId = montagemProvaDAO.nova(prova.getNomeProva(), dificuldade, prova.getValor(), emailProfessor, prova.getNomeDisciplina());

                        // 2. Percorre as questões marcadas e as associa na tabela 'prova_questao'
                        for (int index : indicesSelecionados) {
                                Questao q = questoesCarregadas.get(index);

                                // Pega o peso/valor específico preenchido para esta questão
                                String pesoStr = camposValores[index].getText().trim().replace(",", ".");
                                BigDecimal pesoQuestao = pesoStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(pesoStr);

                                // Chama o DAO para gravar na tabela N para N
                                montagemProvaDAO.associarQuestao(provaId, q.getId(), emailProfessor, prova.getNomeDisciplina(), pesoQuestao);
                        }

                        mostrarAlerta("Sucesso", "Prova e questões salvas com sucesso!\nValor total: " + txtValorProva.getText());
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

        @FXML void AddQuestao(ActionEvent event) {}
        @FXML void IrProva_montar(ActionEvent event) {}
        @FXML void IrQuestao_criar(ActionEvent event) throws IOException { App.setRoot("CadastroQuestoes"); }
        @FXML void VerProva(ActionEvent event) throws IOException { App.setRoot("visualizacao"); }
        @FXML void irAlunos(ActionEvent event) {}
        @FXML void irAlunos_corrigir(ActionEvent event) throws IOException { App.setRoot("Aluno"); }
        @FXML void irAlunos_notas(ActionEvent event) {}
        @FXML void irProva(ActionEvent event) {}
        @FXML void irProva_agendar(ActionEvent event) {}
        @FXML void irQuestao(ActionEvent event) {}
        @FXML void irQuestao_consultar(ActionEvent event) {}
}
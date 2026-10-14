package org.example;

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
import java.util.ArrayList;
import java.util.List;


public class MontagemProvaController {
        public static List<String> perguntas = new ArrayList<>(List.of(
                "Qual é o conceito de encapsulamento em POO?",
                "Qual a diferença entre ArrayList e Array?",
                "Qual palavra-chave em Java é usada para impedir que uma classe seja herdada ou um método seja sobrescrito?" +
                        "\nA) static      B) final      C) abstract      D) public",
                "O que faz o Wrap Text no TextArea?",
                "O que é uma classe e o que é um objeto em POO?",
                "Para que serve a palavra-chave public em Java?",
                "O que acontece se você tentar acessar um índice de Array que não existe?",
                "Associe o pilar da Orientação a Objetos à sua definição" +
                        "\nConceitos: A) Encapsulamento  B) Polimorfismo      C) Abstração\n" +
                        "\n" +
                        "Definições: 1. Capacidade de um método assumir diferentes formas de execução.\n" +
                        "\n" +
                        "2. Esconder detalhes internos e proteger o acesso direto aos dados.\n" +
                        "\n" +
                        "3. Focar apenas nos elementos essenciais de um objeto, ignorando detalhes complexos.\n",
                "Qual é a função do comando System.out.println()?"
        ));

        public static List<String> gabaritos = new ArrayList<>(List.of(
                "Esconder os detalhes internos de uma classe e restringir o acesso.",
                "Array tem tamanho fixo, ArrayList é dinâmico.",
                "B) final",
                "Quebra o texto para a linha de baixo ao atingir a borda.",
                "Classe é o molde/modelo; objeto é a instância dessa classe.",
                "Define visibilidade pública, permitindo acesso de qualquer classe.",
                "O Java lança a exceção ArrayIndexOutOfBoundsException.",
                "A2, B1, C3",
                "Imprime uma mensagem no console e pula para a próxima linha."
        ));

        // Guarda os dados preenchidos no cabeçalho da prova
        public static String nomeProvaSalvo = "";
        public static String disciplinaSalva = "";
        public static String valorProvaSalvo = "";

        @FXML
        private ComboBox<?> boxDisciplina;

        @FXML
        private ComboBox<?> box_filtro_dificuldade;

        @FXML
        private ComboBox<?> box_filtro_disciplina;

        @FXML
        private ComboBox<?> box_filtro_ultimo_uso;

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

        @FXML
        void AddQuestao(ActionEvent event) {

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

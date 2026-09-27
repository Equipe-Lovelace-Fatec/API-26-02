package org.example.api;

import java.util.ArrayList;
import java.util.List;

public class MontagemProvaController {

    public static List<String> perguntas = new ArrayList<>(List.of(
            "Qual é o conceito de encapsulamento em POO?",
            "Qual a diferença entre ArrayList e Array?",
            "Para que serve o método initialize() no JavaFX?",
            "O que faz o Wrap Text no TextArea?",
            "O que é uma classe e o que é um objeto em POO?",
            "Para que serve a palavra-chave public em Java?",
            "Qual é a diferença entre os tipos int e double?",
            "O que acontece se você tentar acessar um índice de Array que não existe?",
            "Para que serve um Button no JavaFX?",
            "Qual é a função do comando System.out.println()?"
    ));

    public static List<String> gabaritos = new ArrayList<>(List.of(
            "Esconder os detalhes internos de uma classe e restringir o acesso.",
            "Array tem tamanho fixo, ArrayList é dinâmico.",
            "Executa automaticamente ao carregar o arquivo FXML.",
            "Quebra o texto para a linha de baixo ao atingir a borda.",
            "Classe é o molde/modelo; objeto é a instância dessa classe.",
            "Define visibilidade pública, permitindo acesso de qualquer classe.",
            "int armazena números inteiros; double armazena números com casas decimais.",
            "O Java lança a exceção ArrayIndexOutOfBoundsException.",
            "Representa um botão interativo na interface gráfica.",
            "Imprime uma mensagem no console e pula para a próxima linha."
    ));
}
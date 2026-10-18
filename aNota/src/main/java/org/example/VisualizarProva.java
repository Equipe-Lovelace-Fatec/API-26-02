package org.example;

import java.math.BigDecimal;
import java.util.List;

public class VisualizarProva {
    private static String nomeProva;
    private static String disciplina;
    private static BigDecimal valorTotal;
    private static List<QuestaoVisualizacao> questoesSelecionadas;

    public static class QuestaoVisualizacao {
        private String enunciado;
        private String gabarito;

        public QuestaoVisualizacao(String enunciado, String gabarito) {
            this.enunciado = enunciado;
            this.gabarito = gabarito;
        }

        public String getEnunciado() { return enunciado; }
        public String getGabarito() { return gabarito; }
    }

    public static String getNomeProva() { return nomeProva; }
    public static void setNomeProva(String nomeProva) { VisualizarProva.nomeProva = nomeProva; }

    public static String getDisciplina() { return disciplina; }
    public static void setDisciplina(String disciplina) { VisualizarProva.disciplina = disciplina; }

    public static BigDecimal getValorTotal() { return valorTotal; }
    public static void setValorTotal(BigDecimal valorTotal) { VisualizarProva.valorTotal = valorTotal; }

    public static List<QuestaoVisualizacao> getQuestoesSelecionadas() { return questoesSelecionadas; }
    public static void setQuestoesSelecionadas(List<QuestaoVisualizacao> questoes) { VisualizarProva.questoesSelecionadas = questoes; }
}
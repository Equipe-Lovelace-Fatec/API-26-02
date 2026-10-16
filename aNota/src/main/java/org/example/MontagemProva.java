package org.example;

import java.math.BigDecimal;
import java.util.List;

public class MontagemProva {

    private Long id;
    private String nomeProva;
    private String dificuldade;
    private BigDecimal valor;
    private String emailProfessor;
    private String nomeDisciplina;
    private List<Questao> questoes;

    public MontagemProva() {
    }

    public MontagemProva(String nomeProva, String dificuldade, BigDecimal valor, String emailProfessor, String nomeDisciplina, List<Questao> questoes) {
        this.nomeProva = nomeProva;
        this.dificuldade = dificuldade;
        this.valor = valor;
        this.emailProfessor = emailProfessor;
        this.nomeDisciplina = nomeDisciplina;
        this.questoes = questoes;
    }

    /**
     * Regra de negócio: Valida se os dados da prova estão corretos antes de salvar.
     * Retorna uma mensagem de erro/aviso se houver inconsistência, ou null se estiver tudo OK.
     */
    public String validarProva(List<Integer> indicesSelecionados, List<String> valoresDigitados) {
        if (indicesSelecionados.isEmpty()) {
            return "Selecione ao menos uma questão para a prova.";
        }

        boolean provaTemValor = this.valor != null && this.valor.compareTo(BigDecimal.ZERO) > 0;

        for (int index : indicesSelecionados) {
            String textoValor = valoresDigitados.get(index).trim();
            boolean questaoTemValor = !textoValor.isEmpty();

            if (provaTemValor && !questaoTemValor) {
                return "Há questões selecionadas sem valor definido.";
            }
            if (!provaTemValor && questaoTemValor) {
                return "A prova está sem valor total calculado.";
            }
        }

        if (!provaTemValor) {
            return "RASCUNHO"; // Sinaliza que é um rascunho sem valores
        }

        return null; // Tudo válido!
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeProva() { return nomeProva; }
    public void setNomeProva(String nomeProva) { this.nomeProva = nomeProva; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getEmailProfessor() { return emailProfessor; }
    public void setEmailProfessor(String emailProfessor) { this.emailProfessor = emailProfessor; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public List<Questao> getQuestoes() { return questoes; }
    public void setQuestoes(List<Questao> questoes) { this.questoes = questoes; }
}
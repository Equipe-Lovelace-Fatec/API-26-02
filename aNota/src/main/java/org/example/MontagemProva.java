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

    // ---- Rascunho: prova montada sem valor em nenhuma questão (no banco, valor da prova = 0) ----

    // Valor vazio ou zero conta como "sem valor"
    public static boolean valorEmBranco(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return true;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", ".")).compareTo(BigDecimal.ZERO) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Todas as questões estão sem valor
    public static boolean ehRascunho(List<String> valoresDigitados) {
        return valoresDigitados != null && !valoresDigitados.isEmpty()
                && valoresDigitados.stream().allMatch(MontagemProva::valorEmBranco);
    }

    // Algumas questões têm valor e outras não
    public static boolean temValoresParciais(List<String> valoresDigitados) {
        if (valoresDigitados == null) {
            return false;
        }
        long semValor = valoresDigitados.stream().filter(MontagemProva::valorEmBranco).count();
        return semValor > 0 && semValor < valoresDigitados.size();
    }

    // Valida o salvamento como rascunho ou como prova completa; null = tudo certo
    public String validarParaSalvar(List<Integer> indicesSelecionados, List<String> valoresDigitados) {
        if (indicesSelecionados.isEmpty()) {
            return "Selecione ao menos uma questão para a prova.";
        }
        if (temValoresParciais(valoresDigitados)) {
            return "Informe o valor de todas as questões ou deixe todas sem valor "
                    + "para salvar a prova como rascunho.";
        }
        if (ehRascunho(valoresDigitados)) {
            this.valor = BigDecimal.ZERO; // coluna prova.valor é NOT NULL
            return null;
        }
        return validarProva(indicesSelecionados, valoresDigitados);
    }

    public String validarProva(List<Integer> indicesSelecionados, List<String> valoresDigitados) {
        if (indicesSelecionados.isEmpty()) {
            return "Selecione ao menos uma questão para a prova.";
        }

        // Valida se a prova tem valor informado
        if (this.valor == null || this.valor.compareTo(BigDecimal.ZERO) <= 0) {
            return "A prova precisa ter um valor total definido.";
        }

        // Valida se o valor total da prova é exatamente igual a 10
        if (this.valor.compareTo(new BigDecimal("10")) != 0) {
            return "O valor total da prova deve ser 10.";
        }

        // Valida cada questão selecionada
        for (int index : indicesSelecionados) {
            if (index < 0 || index >= valoresDigitados.size()) {
                return "Erro nos índices das questões selecionadas.";
            }

            String textoValor = valoresDigitados.get(index).trim();
            if (textoValor.isEmpty()) {
                return "Há questões selecionadas sem valor definido.";
            }

            try {
                BigDecimal questaoVal = new BigDecimal(textoValor.replace(",", "."));
                if (questaoVal.compareTo(BigDecimal.ZERO) <= 0) {
                    return "Todas as questões selecionadas devem ter um valor maior que zero.";
                }
            } catch (NumberFormatException e) {
                return "Formato de valor inválido em uma das questões.";
            }
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
package org.example.ProvaAlunoArquivos;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.example.Questao;
import org.example.Resposta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Questão de relacionamento: à esquerda os itens (A, B, C, D), à direita os conceitos numerados (1 a 4,
// em ordem embaralhada) e, entre eles, uma caixa onde o aluno escolhe o número que corresponde a cada item.
public class QuestaoRelacionamentoController {

    private static final int MAX_PARES = 4;   // o layout tem 4 linhas

    @FXML private Label EnunciadoQuestao;
    @FXML private Label afirmacao1, afirmacao2, afirmacao3, afirmacao4;
    @FXML private Label afirmacaoA, afirmacaoB, afirmacaoC, afirmacaoD;
    @FXML private ComboBox<String> cbRelacaoA, cbRelacaoB, cbRelacaoC, cbRelacaoD;

    private Questao questao;
    private final List<Resposta> itens = new ArrayList<>();      // texto = item, conceito = par correto
    private final List<String> conceitos = new ArrayList<>();    // coluna da direita (embaralhada)

    public void setDadosQuestao(Questao q) {
        setDadosQuestao(q, new ArrayList<>());
    }

    public void setDadosQuestao(Questao q, List<Resposta> pares) {
        setDadosQuestao(q, pares, q.getId() != null ? q.getId() : 0L);
    }

    public void setDadosQuestao(Questao q, List<Resposta> pares, long semente) {
        this.questao = q;
        if (EnunciadoQuestao != null) {
            EnunciadoQuestao.setWrapText(true);
            EnunciadoQuestao.setText(q.getEnunciado());
        }

        itens.clear();
        conceitos.clear();
        for (int i = 0; i < pares.size() && i < MAX_PARES; i++) {
            itens.add(pares.get(i));
            conceitos.add(pares.get(i).getConceito() == null ? "" : pares.get(i).getConceito());
        }
        // Mesma ordem toda vez que a questão é aberta (semente = id da questão)
        // Mesma ordem para o mesmo aluno, diferente entre alunos
        Collections.shuffle(conceitos, new Random(semente));

        Label[] esquerda = {afirmacaoA, afirmacaoB, afirmacaoC, afirmacaoD};
        Label[] direita = {afirmacao1, afirmacao2, afirmacao3, afirmacao4};
        ComboBox<String>[] combos = combos();
        String[] letras = {"A", "B", "C", "D"};

        for (int i = 0; i < MAX_PARES; i++) {
            boolean usada = i < itens.size();
            esquerda[i].setVisible(usada);
            direita[i].setVisible(usada);
            combos[i].setVisible(usada);
            if (!usada) {
                continue;
            }
            esquerda[i].setWrapText(true);
            esquerda[i].setText(letras[i] + ") " + itens.get(i).getTexto());
            direita[i].setWrapText(true);
            direita[i].setText((i + 1) + ") " + conceitos.get(i));

            combos[i].getItems().clear();
            for (int n = 1; n <= itens.size(); n++) {
                combos[i].getItems().add(String.valueOf(n));
            }
            combos[i].setValue(null);
        }
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String>[] combos() {
        return new ComboBox[]{cbRelacaoA, cbRelacaoB, cbRelacaoC, cbRelacaoD};
    }

    // Pares escolhidos, um por linha ("item → conceito", o mesmo formato do gabarito); null se nada foi escolhido
    public String getRespostaMontada() {
        ComboBox<String>[] combos = combos();
        List<String> linhas = new ArrayList<>();
        for (int i = 0; i < itens.size(); i++) {
            String conceito = conceitoEscolhido(combos[i]);
            if (conceito != null) {
                linhas.add(itens.get(i).getTexto() + " → " + conceito);
            }
        }
        return linhas.isEmpty() ? null : String.join("\n", linhas);
    }

    // true se todos os itens foram ligados ao conceito certo; false se errou ou deixou algum sem ligar;
    // null se o aluno não respondeu nada
    public Boolean getCorreta() {
        ComboBox<String>[] combos = combos();
        boolean respondeuAlgo = false;
        boolean tudoCerto = !itens.isEmpty();
        for (int i = 0; i < itens.size(); i++) {
            String escolhido = conceitoEscolhido(combos[i]);
            if (escolhido == null) {
                tudoCerto = false;
                continue;
            }
            respondeuAlgo = true;
            String esperado = itens.get(i).getConceito() == null ? "" : itens.get(i).getConceito();
            if (!esperado.trim().equalsIgnoreCase(escolhido.trim())) {
                tudoCerto = false;
            }
        }
        return respondeuAlgo ? tudoCerto : null;
    }

    private String conceitoEscolhido(ComboBox<String> combo) {
        if (combo == null || combo.getValue() == null) {
            return null;
        }
        try {
            int posicao = Integer.parseInt(combo.getValue()) - 1;
            return posicao >= 0 && posicao < conceitos.size() ? conceitos.get(posicao) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getQuestaoId() {
        return questao != null ? questao.getId() : null;
    }
}
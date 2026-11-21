package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class CartaoQuestaoController {

    @FXML private Text questao1;
    @FXML private Button btnEdicao1;
    @FXML private Button btnDuplicar1;

    private Questao questao;
    private Consumer<Questao> aoEditar;
    private Consumer<Questao> aoDuplicar;

    private String rotuloTipo(String tipo) {
        if (tipo == null) return "?";
        switch (tipo) {
            case "diss": return "Aberta";
            case "alt":  return "Múltipla escolha";
            case "rela": return "Relacionamento";
            default:     return tipo;
        }
    }

    public void setQuestao(Questao questao, boolean podeEditar,
                           Consumer<Questao> aoEditar,
                           Consumer<Questao> aoDuplicar) {
        this.questao = questao;
        this.aoEditar = aoEditar;
        this.aoDuplicar = aoDuplicar;

        questao1.setText(questao.getEnunciado() + " [" + rotuloTipo(questao.getTipo()) + "]");

        // Botão continua clicável: o aviso aparece ao clicar.
        // Só deixa o ícone mais apagado para indicar que não é sua.
        if (!podeEditar) {
            btnEdicao1.setOpacity(0.4);
            Tooltip.install(btnEdicao1,
                    new Tooltip("Questão de outro professor. Duplique para editar."));
        }
    }



    public Questao getQuestao() {
        return questao;
    }

    @FXML
    void editaQuestao(ActionEvent event) {
        if (aoEditar != null) aoEditar.accept(questao);
    }

    @FXML
    void copiaQuestao(ActionEvent event) {
        if (aoDuplicar != null) aoDuplicar.accept(questao);
    }

}
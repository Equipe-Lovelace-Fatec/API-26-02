package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.util.function.Consumer;

// Cartão de um aluno na tela de Notas: "Nome - RA" e a média. Ao clicar, abre as notas das provas.
public class CartaoAlunoController {

    @FXML private Label lbl_nome;
    @FXML private Label lbl_media;

    private NotasDAO.AlunoMedia aluno;
    private Consumer<NotasDAO.AlunoMedia> acaoClique;

    public void setAluno(NotasDAO.AlunoMedia aluno, Consumer<NotasDAO.AlunoMedia> acaoClique) {
        this.aluno = aluno;
        this.acaoClique = acaoClique;

        lbl_nome.setText(aluno.getNomeComRa());
        lbl_media.setText(aluno.getMediaExibicao());
    }

    @FXML
    void clicarNoCartao(MouseEvent event) {
        if (acaoClique != null) {
            acaoClique.accept(aluno);
        }
    }
}
package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class LoginController {

    @FXML private Button bt_acessar_logar;
    @FXML private Button bt_esqueceu_senha;
    @FXML private Button bt_ver_senha;
    @FXML private CheckBox checkbox_lembre;
    @FXML private ImageView img_login_fundo;
    @FXML private ImageView img_ver_senha;
    @FXML private TextField txt_email;
    @FXML private PasswordField txt_senha;
    @FXML private TextField txt_senha_visivel;
    @FXML private Label lbl_erro;

    private final Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        txt_senha_visivel.setVisible(false);
        txt_senha_visivel.setManaged(false);
        txt_senha_visivel.textProperty().bindBidirectional(txt_senha.textProperty());
        txt_senha_visivel.setOnAction(this::digitar_senha);

        String emailSalvo = prefs.get("email", "");
        if (!emailSalvo.isEmpty()) {
            txt_email.setText(emailSalvo);
            checkbox_lembre.setSelected(true);
        }
    }

    @FXML
    void checkbox_lembre(ActionEvent event) {
    }

    @FXML
    void digitar_email(ActionEvent event) {
        txt_senha.requestFocus();
    }

    @FXML
    void digitar_senha(ActionEvent event) {
        logar(event);
    }

    @FXML
    void ver_senha(ActionEvent event) {
        boolean mostrar = !txt_senha_visivel.isVisible();
        txt_senha_visivel.setVisible(mostrar);
        txt_senha_visivel.setManaged(mostrar);
        txt_senha.setVisible(!mostrar);
        txt_senha.setManaged(!mostrar);
    }

    @FXML
    void trocar_senha(ActionEvent event) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Recuperar senha");
        alerta.setHeaderText(null);
        alerta.setContentText("Funcionalidade em desenvolvimento.");
        alerta.showAndWait();
    }

    @FXML
    void logar(ActionEvent event) {
        String email = txt_email.getText().trim();
        String senha = txt_senha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            lbl_erro.setText("Preencha e-mail e senha.");
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioDAO.autenticar(email, senha);
        } catch (SQLException e) {
            e.printStackTrace();
            lbl_erro.setText("Não foi possível conectar ao banco de dados.");
            return;
        }

        if (usuario == null) {
            lbl_erro.setText("E-mail ou senha inválidos.");
            return;
        }

        if (usuario.getPerfil() != Usuario.Perfil.PROFESSOR) {
            lbl_erro.setText("Acesso permitido apenas para professores.");
            return;
        }

        Sessao.setUsuario(usuario);

        if (checkbox_lembre.isSelected()) {
            prefs.put("email", email);
        } else {
            prefs.remove("email");
        }

        abrirTelaPrincipal();
    }

    private void abrirTelaPrincipal() {
        try {
            App.abrirTela("CadastroQuestoes", "aNota", 1350, 1500);
        } catch (IOException e) {
            e.printStackTrace();
            lbl_erro.setText("Erro ao abrir a tela principal.");
        }
    }
}
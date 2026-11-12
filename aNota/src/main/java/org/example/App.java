package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.BooleanSupplier;


public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    // Guarda de navegação: enquanto definida, qualquer troca de tela (e o fechamento da janela) só
    // acontece se ela devolver true. Usada pela tela de prova para avisar o aluno antes de sair.
    private static BooleanSupplier guardaNavegacao;

    public static void setGuardaNavegacao(BooleanSupplier guarda) {
        guardaNavegacao = guarda;
    }

    public static void limparGuardaNavegacao() {
        guardaNavegacao = null;
    }

    private static boolean navegacaoLiberada() {
        return guardaNavegacao == null || guardaNavegacao.getAsBoolean();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login"), 1350, 753);
        stage.setTitle("aNota - Login");
        stage.setOnCloseRequest(e -> {
            if (!navegacaoLiberada()) {
                e.consume();
            }
        });
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        if (!navegacaoLiberada()) {
            return;
        }
        scene.setRoot(loadFXML(fxml));
    }

    static void definirCenaAtual(Scene novaCena) {
        if (!navegacaoLiberada()) {
            return;
        }
        scene = novaCena;
        stage.setScene(scene);
    }

    static void abrirTela(String fxml) throws IOException {
        if (!navegacaoLiberada()) {
            return;
        }
        scene.setRoot(loadFXML(fxml));
        stage.setScene(scene);
    }

    static void abrirTela(String fxml, String titulo, double largura, double altura) throws IOException {
        if (!navegacaoLiberada()) {
            return;
        }
        scene = new Scene(loadFXML(fxml), largura, altura);
        stage.setTitle(titulo);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
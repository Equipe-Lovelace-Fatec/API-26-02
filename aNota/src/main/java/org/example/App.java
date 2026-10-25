package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        scene = new Scene(loadFXML("login"), 1350, 753);
        stage.setTitle("aNota - Login");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    static void definirCenaAtual(Scene novaCena) {
        scene = novaCena;
        stage.setScene(scene);
    }

    static void abrirTela(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
        stage.setScene(scene);
    }

    static void abrirTela(String fxml, String titulo, double largura, double altura) throws IOException {
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

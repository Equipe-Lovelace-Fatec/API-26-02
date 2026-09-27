module org.example.api {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.api to javafx.fxml;
    exports org.example.api;
}
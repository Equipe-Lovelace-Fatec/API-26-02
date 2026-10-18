package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/anota_database"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000";
    private static final String USUARIO = "lovelace";
    private static final String SENHA = "pass123";

    private ConexaoBD() {
    }

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
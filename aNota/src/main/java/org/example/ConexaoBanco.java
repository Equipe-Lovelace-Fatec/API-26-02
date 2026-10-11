package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    private static final String URL = "jdbc:mysql://localhost:3306/anota_database?useTimezone=true&serverTimezone=UTC";
    private static final String USUARIO = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String SENHA = System.getenv("DB_PASS") != null ? System.getenv("DB_PASS") : "sua_senha_local";

    public static Connection conectar() {
        try {
            Connection conexao = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("Ligação ao MySQL realizada com sucesso!");
            return conexao;
        } catch (SQLException e) {
            System.out.println("Erro ao ligar ao MySQL: " + e.getMessage());
            return null;
        }
    }
}


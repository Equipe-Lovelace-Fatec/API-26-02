package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    public List<String> listarTodas() {
        List<String> lista = new ArrayList<>();
        String sql = "SELECT nome_disciplina FROM disciplina ORDER BY nome_disciplina";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getString("nome_disciplina"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar disciplinas!", e);
        }
        return lista;
    }
}

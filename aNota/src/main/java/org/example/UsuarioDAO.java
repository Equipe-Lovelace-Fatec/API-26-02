package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public Usuario autenticar(String email, String senhaDigitada) throws SQLException {
        String sql = "SELECT u.nome, u.email, u.senha, "
                + "CASE WHEN p.email IS NOT NULL THEN 'PROFESSOR' "
                + "WHEN a.email IS NOT NULL THEN 'ALUNO' "
                + "ELSE 'SEM_PERFIL' END AS perfil "
                + "FROM usuario u "
                + "LEFT JOIN professor p ON p.email = u.email "
                + "LEFT JOIN aluno a ON a.email = u.email "
                + "WHERE u.email = ?";

        try (Connection con = ConexaoBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                if (!senhaConfere(senhaDigitada, rs.getString("senha"))) {
                    return null;
                }
                return new Usuario(
                        rs.getString("nome"),
                        rs.getString("email"),
                        Usuario.Perfil.valueOf(rs.getString("perfil")));
            }
        }
    }

    private boolean senhaConfere(String digitada, String armazenada) {
        return armazenada != null && armazenada.equals(digitada);
    }
}
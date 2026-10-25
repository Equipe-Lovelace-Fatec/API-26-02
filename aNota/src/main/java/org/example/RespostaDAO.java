package org.example;

import org.example.Resposta;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RespostaDAO {

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/anota_database?useTimezone=true&serverTimezone=UTC",
                "lovelace",
                "pass123"
        );
    }

    public void novaResposta(String enunciado, String tipo, String texto, Boolean correta, String conceito) {
        Connection con = null;
        try {
            con = getConnection();
            String insert_sql = "INSERT INTO resposta (enunciado, tipo, texto, correta, conceito) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pst = con.prepareStatement(insert_sql);
            pst.setString(1, enunciado);
            pst.setString(2, tipo);
            pst.setString(3, texto);

            // Como o campo 'correta' pode ser nulo no banco (ex: dissertativas e relacionamento), tratamos adequadamente
            if (correta != null) {
                pst.setBoolean(4, correta);
            } else {
                pst.setNull(4, java.sql.Types.BOOLEAN);
            }

            pst.setString(5, conceito);

            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao inserir nova resposta!", e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }
    }

    public List<Resposta> buscarRespostasPorQuestao(String enunciadoQuestao, String tipoQuestao) {
        List<Resposta> respostas = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            String select_sql = "SELECT resposta_id, enunciado, tipo, texto, correta, conceito FROM resposta WHERE enunciado = ? AND tipo = ?";

            PreparedStatement pst = con.prepareStatement(select_sql);
            pst.setString(1, enunciadoQuestao);
            pst.setString(2, tipoQuestao);

            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Resposta resposta = new Resposta();
                resposta.setRespostaId(rs.getLong("resposta_id"));
                resposta.setEnunciado(rs.getString("enunciado"));
                resposta.setTipo(rs.getString("tipo"));
                resposta.setTexto(rs.getString("texto"));

                // Tratamento seguro para campos booleanos que podem vir null do banco
                boolean valorCorreta = rs.getBoolean("correta");
                if (rs.wasNull()) {
                    resposta.setCorreta(null);
                } else {
                    resposta.setCorreta(valorCorreta);
                }

                resposta.setConceito(rs.getString("conceito"));

                respostas.add(resposta);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar respostas!", e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }
        return respostas;
    }
    public List<String> buscarRespostasPorEnunciado(String enunciadoQuestao) {
        List<String> respostas = new ArrayList<>();
        Connection con = null;
        try {
            con = getConnection();
            String sql = "SELECT texto, tipo, correta FROM resposta WHERE enunciado = ?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, enunciadoQuestao);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String tipo = rs.getString("tipo");
                String texto = rs.getString("texto");
                boolean correta = rs.getBoolean("correta");

                if ("diss".equalsIgnoreCase(tipo)) {
                    // Se for dissertativa, adiciona o texto direto
                    respostas.add(texto);
                } else if ("alt".equalsIgnoreCase(tipo) && correta) {
                    // Se for alternativa, adiciona APENAS se correta for verdadeira
                    respostas.add(texto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar gabarito da resposta!", e);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return respostas;
    }
}
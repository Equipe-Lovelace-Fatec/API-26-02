package org.example;

public class Sessao {

    private static Usuario usuario;
    private static Prova provaParaAgendar;

    private Sessao() {
    }

    public static void setUsuario(Usuario u) {
        usuario = u;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setProvaParaAgendar(Prova p) {
        provaParaAgendar = p;
    }

    public static Prova getProvaParaAgendar() {
        return provaParaAgendar;
    }

    public static void encerrar() {
        usuario = null;
        provaParaAgendar = null;
    }
}

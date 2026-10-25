package org.example;

public class Usuario {

    public enum Perfil { PROFESSOR, ALUNO, SEM_PERFIL }

    private final String nome;
    private final String email;
    private final Perfil perfil;

    public Usuario(String nome, String email, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Perfil getPerfil() {
        return perfil;
    }
}
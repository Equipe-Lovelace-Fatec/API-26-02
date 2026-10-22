package org.example;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Questao {
    private Long id;
    private String enunciado;
    private String tema;
    private String tipo;
    private String dificuldade;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataEdicao;
    private LocalDateTime dataUltimoUso; // Armazena a data em que a questão foi usada pela última vez
    private String emailProfessor;
    private String nomeDisciplina;

    // Método que retorna a categoria amigável baseada no tempo passado desde o último uso
    public String getUltimoUso() {
        if (dataUltimoUso == null) {
            return "3 anos ou mais"; // Ou "Nunca usada", ajuste conforme sua preferência
        }

        long meses = ChronoUnit.MONTHS.between(dataUltimoUso, LocalDateTime.now());

        if (meses <= 6) {
            return "6 meses";
        } else if (meses <= 12) {
            return "1 ano";
        } else if (meses <= 24) {
            return "2 anos";
        } else {
            return "3 anos ou mais";
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataEdicao() {
        return dataEdicao;
    }

    public void setDataEdicao(LocalDateTime dataEdicao) {
        this.dataEdicao = dataEdicao;
    }

    public LocalDateTime getDataUltimoUso() {
        return dataUltimoUso;
    }

    public void setDataUltimoUso(LocalDateTime dataUltimoUso) {
        this.dataUltimoUso = dataUltimoUso;
    }

    public String getEmailProfessor() {
        return emailProfessor;
    }

    public void setEmailProfessor(String emailProfessor) {
        this.emailProfessor = emailProfessor;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }
}
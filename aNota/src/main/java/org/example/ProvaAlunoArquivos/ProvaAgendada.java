package org.example.ProvaAlunoArquivos;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Uma prova agendada para o aluno (uma aplicação que ainda não terminou)
public class ProvaAgendada {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private long idAplicacao;
    private String nomeProva;
    private String nomeDisciplina;
    private String nomeProfessor;
    private LocalDate data;
    private LocalTime horario;
    private LocalTime horarioFim;
    private boolean liberada;   // calculada pelo banco com NOW(): já começou e ainda não terminou

    public long getIdAplicacao() { return idAplicacao; }
    public void setIdAplicacao(long idAplicacao) { this.idAplicacao = idAplicacao; }

    public String getNomeProva() { return nomeProva; }
    public void setNomeProva(String nomeProva) { this.nomeProva = nomeProva; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    public String getNomeProfessor() { return nomeProfessor; }
    public void setNomeProfessor(String nomeProfessor) { this.nomeProfessor = nomeProfessor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public LocalTime getHorarioFim() { return horarioFim; }
    public void setHorarioFim(LocalTime horarioFim) { this.horarioFim = horarioFim; }

    public boolean isLiberada() { return liberada; }
    public void setLiberada(boolean liberada) { this.liberada = liberada; }

    // Data no formato dd/MM/yyyy
    public String getDataExibicao() {
        return data.format(FORMATO_DATA);
    }

    // Horário de início no formato HH:mm
    public String getHorarioExibicao() {
        return horario.format(FORMATO_HORA);
    }

    // Duração da prova: "50 min", "1h40min" ou "2h"
    public String getTempoDeProvaExibicao() {
        long minutos = Duration.between(horario, horarioFim).toMinutes();
        long horas = minutos / 60;
        long resto = minutos % 60;
        if (horas == 0) {
            return resto + " min";
        }
        return resto == 0 ? horas + "h" : horas + "h" + resto + "min";
    }

    public String getStatusExibicao() {
        return liberada ? "Liberada" : "Aguardando início";
    }
}

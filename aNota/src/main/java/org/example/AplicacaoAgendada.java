package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AplicacaoAgendada {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final long idAplicacao;
    private final String nomeProva;
    private final String nomeDisciplina;
    private final String turma;
    private final String periodo;
    private final LocalDate data;
    private final LocalTime horario;
    private final LocalTime horarioFim;
    private final int quantidadeAlunos;
    private final boolean iniciada;

    public AplicacaoAgendada(long idAplicacao, String nomeProva, String nomeDisciplina, String turma, String periodo,
                             LocalDate data, LocalTime horario, LocalTime horarioFim,
                             int quantidadeAlunos, boolean iniciada) {
        this.idAplicacao = idAplicacao;
        this.nomeProva = nomeProva;
        this.nomeDisciplina = nomeDisciplina;
        this.turma = turma;
        this.periodo = periodo;
        this.data = data;
        this.horario = horario;
        this.horarioFim = horarioFim;
        this.quantidadeAlunos = quantidadeAlunos;
        this.iniciada = iniciada;
    }

    public long getIdAplicacao() { return idAplicacao; }
    public String getNomeProva() { return nomeProva; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public String getTurma() { return turma; }
    public String getPeriodo() { return periodo; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public LocalTime getHorarioFim() { return horarioFim; }
    public int getQuantidadeAlunos() { return quantidadeAlunos; }
    public boolean isIniciada() { return iniciada; }

    public String getDataTexto() { return FORMATO_DATA.format(data); }
    public String getHorarioTexto() { return FORMATO_HORA.format(horario); }
    public String getHorarioFimTexto() { return FORMATO_HORA.format(horarioFim); }
    public String getSituacao() { return iniciada ? "Em andamento" : "Agendada"; }
}

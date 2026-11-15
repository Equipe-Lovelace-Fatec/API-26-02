package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class NotaAvaliacao {

    private final String nomeProva;
    private final LocalDate data;
    private final LocalTime horarioFim;
    private final BigDecimal nota;
    private final BigDecimal valor;
    private final boolean corrigida;
    private final boolean recuperacao;
    private final BigDecimal pesoPercentual;
    private final boolean substituida;
    private final String itemSubstituido;

    public NotaAvaliacao(String nomeProva, LocalDate data, LocalTime horarioFim, BigDecimal nota, BigDecimal valor,
                         boolean corrigida, boolean recuperacao, BigDecimal pesoPercentual,
                         boolean substituida, String itemSubstituido) {
        this.nomeProva = nomeProva;
        this.data = data;
        this.horarioFim = horarioFim;
        this.nota = nota;
        this.valor = valor;
        this.corrigida = corrigida;
        this.recuperacao = recuperacao;
        this.pesoPercentual = pesoPercentual;
        this.substituida = substituida;
        this.itemSubstituido = itemSubstituido;
    }

    public String getNomeProva() {
        return nomeProva;
    }

    public LocalDate getData() {
        return data;
    }

    public boolean temNota() {
        return corrigida && nota != null;
    }

    public boolean ehRecuperacao() {
        return recuperacao;
    }

    public boolean jaAconteceu() {
        if (data == null) {
            return false;
        }
        LocalTime fim = horarioFim == null ? LocalTime.MAX : horarioFim;
        return !LocalDateTime.of(data, fim).isAfter(LocalDateTime.now());
    }

    public BigDecimal getNotaDeZeroADez() {
        if (!temNota()) {
            return null;
        }
        if (valor == null || valor.signum() <= 0 || valor.compareTo(BigDecimal.TEN) == 0) {
            return nota;
        }
        return nota.multiply(BigDecimal.TEN).divide(valor, 2, RoundingMode.HALF_UP);
    }

    public String getSituacao() {
        String andamento;
        if (temNota()) {
            andamento = "Corrigida";
        } else {
            andamento = jaAconteceu() ? "Aguardando correção" : "Agendada";
        }

        if (recuperacao) {
            return itemSubstituido == null
                    ? "Recuperação · " + andamento
                    : "Recuperação de " + itemSubstituido + " · " + andamento;
        }
        if (pesoPercentual == null) {
            return "Fora da média · " + andamento;
        }
        String peso = "Peso " + pesoPercentual.stripTrailingZeros().toPlainString() + "%";
        if (substituida) {
            return peso + " · Substituída pela recuperação";
        }
        return peso + " · " + andamento;
    }
}

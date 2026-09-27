package org.example;

public class Aluno {

    public static final double VALOR_MAX_NOTA1 = 4.0;
    public static final double VALOR_MAX_NOTA2 = 3.0;
    public static final double VALOR_MAX_NOTA3 = 3.0;

    private double nota1;
    private double nota2;
    private double nota3;

    public double getNota1() {
        return nota1;
    }

    public void setNota1(double nota1) {
        this.nota1 = validarNota(nota1, VALOR_MAX_NOTA1);
    }

    public double getNota2() {
        return nota2;
    }

    public void setNota2(double nota2) {
        this.nota2 = validarNota(nota2, VALOR_MAX_NOTA2);
    }

    public double getNota3() {
        return nota3;
    }

    public void setNota3(double nota3) {
        this.nota3 = validarNota(nota3, VALOR_MAX_NOTA3);
    }

    // Valida a nota contra o valor máximo específico dela (4, 3 ou 3).
    private double validarNota(double nota, double max) {
        if (nota < 0 || nota > max) {
            throw new IllegalArgumentException(
                    String.format("A nota deve estar entre 0 e %.1f.", max));
        }
        return nota;
    }

    // Soma as 3 notas (máximo 10, já que 4 + 3 + 3 = 10).
    public double getTotalNotas() {
        return nota1 + nota2 + nota3;
    }
}
package org.example;

import java.time.LocalTime;
import java.util.List;

public final class HorarioAula {

    private static final List<LocalTime> NOTURNO = List.of(
            LocalTime.of(18, 45),
            LocalTime.of(19, 35),
            LocalTime.of(20, 25),
            LocalTime.of(21, 25),
            LocalTime.of(22, 15)
    );

    private static final List<LocalTime> DIURNO = List.of(
            LocalTime.of(7, 10),
            LocalTime.of(7, 55),
            LocalTime.of(9, 15),
            LocalTime.of(10, 0),
            LocalTime.of(10, 55),
            LocalTime.of(11, 40)
    );

    private HorarioAula() {
    }

    public static boolean ehNoturno(String periodo) {
        return periodo != null && periodo.toLowerCase().startsWith("noturn");
    }

    public static List<LocalTime> iniciosDoPeriodo(String periodo) {
        return ehNoturno(periodo) ? NOTURNO : DIURNO;
    }
}

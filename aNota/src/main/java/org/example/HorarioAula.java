package org.example;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final List<LocalTime> NOTURNO_FIM = List.of(
            LocalTime.of(19, 35),
            LocalTime.of(20, 25),
            LocalTime.of(21, 15),
            LocalTime.of(22, 15),
            LocalTime.of(23, 5)
    );

    private static final List<LocalTime> DIURNO_FIM = List.of(
            LocalTime.of(7, 55),
            LocalTime.of(8, 40),
            LocalTime.of(10, 0),
            LocalTime.of(10, 45),
            LocalTime.of(11, 40),
            LocalTime.of(12, 25)
    );

    private HorarioAula() {
    }

    public static boolean ehNoturno(String periodo) {
        return periodo != null && periodo.toLowerCase().startsWith("noturn");
    }

    public static List<LocalTime> iniciosDoPeriodo(String periodo) {
        return ehNoturno(periodo) ? NOTURNO : DIURNO;
    }

    public static List<LocalTime> terminosApos(String periodo, LocalTime inicio) {
        List<LocalTime> fins = ehNoturno(periodo) ? NOTURNO_FIM : DIURNO_FIM;
        return fins.stream().filter(fim -> fim.isAfter(inicio)).collect(Collectors.toList());
    }
}

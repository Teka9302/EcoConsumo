package com.upn.ecoconsumo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public final class FechaUtils {

    private static final DateTimeFormatter FORMATO_VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private FechaUtils() {}

    public static String hoy() {
        return LocalDate.now().toString();
    }

    public static LocalDate parse(String iso) {
        try {
            return LocalDate.parse(iso);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /** Convierte yyyy-MM-dd a dd/MM/yyyy para mostrar en pantalla. */
    public static String formatear(String iso) {
        LocalDate fecha = parse(iso);
        return fecha == null ? "" : fecha.format(FORMATO_VISTA);
    }

    /** Días desde hoy hasta la fecha (negativo si ya pasó). */
    public static long diasHasta(String iso) {
        LocalDate fecha = parse(iso);
        if (fecha == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), fecha);
    }

    public static String textoVencimiento(long dias) {
        if (dias < 0) return dias == -1 ? "Venció ayer" : "Venció hace " + (-dias) + " días";
        if (dias == 0) return "Vence hoy";
        if (dias == 1) return "Vence mañana";
        return "Vence en " + dias + " días";
    }
}

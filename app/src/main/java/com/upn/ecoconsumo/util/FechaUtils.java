package com.upn.ecoconsumo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FechaUtils {

    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final DateTimeFormatter FORMATO_VISTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private FechaUtils() {}

    public static String hoy() {
        return LocalDate.now().toString();
    }

    public static LocalDate parse(String iso) {
        if (iso == null) return null;
        try {
            return LocalDate.parse(iso);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Convierte yyyy-MM-dd a dd/MM/yyyy para mostrar en pantalla. */
    public static String formatear(String iso) {
        LocalDate fecha = parse(iso);
        return fecha == null ? "" : fecha.format(FORMATO_VISTA);
    }

    public static String formatearFechaHora(long millis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()).format(FORMATO_FECHA_HORA);
    }

    /** Días desde hoy hasta la fecha (negativo si ya pasó). */
    public static long diasHasta(String iso) {
        return diasEntre(LocalDate.now(), iso);
    }

    public static long diasEntre(LocalDate desde, String iso) {
        LocalDate fecha = parse(iso);
        if (fecha == null) return 0;
        return ChronoUnit.DAYS.between(desde, fecha);
    }

    public static String textoVencimiento(long dias) {
        if (dias < 0) return dias == -1 ? "Venció ayer" : "Venció hace " + (-dias) + " días";
        if (dias == 0) return "Vence hoy";
        if (dias == 1) return "Vence mañana";
        return "Vence en " + dias + " días";
    }

    /** Primer día del periodo de estadísticas: el mes actual y los (meses - 1) anteriores. */
    public static String inicioPeriodo(int meses) {
        return inicioPeriodo(YearMonth.now(), meses);
    }

    static String inicioPeriodo(YearMonth actual, int meses) {
        return actual.minusMonths(meses - 1L).atDay(1).toString();
    }

    /** Meses del periodo en formato yyyy-MM, del más antiguo al actual. */
    public static List<String> mesesDelPeriodo(int meses) {
        return mesesDelPeriodo(YearMonth.now(), meses);
    }

    static List<String> mesesDelPeriodo(YearMonth actual, int meses) {
        List<String> lista = new ArrayList<>();
        for (int i = meses - 1; i >= 0; i--) {
            lista.add(actual.minusMonths(i).toString());
        }
        return lista;
    }

    /** "2026-10" -> "oct." */
    public static String nombreMesCorto(String anioMes) {
        try {
            return YearMonth.parse(anioMes).getMonth().getDisplayName(TextStyle.SHORT, ES);
        } catch (DateTimeParseException e) {
            return anioMes;
        }
    }
}

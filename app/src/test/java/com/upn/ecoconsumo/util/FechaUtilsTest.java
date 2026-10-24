package com.upn.ecoconsumo.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

public class FechaUtilsTest {

    @Test
    public void formatearYParsear() {
        assertEquals("05/03/2026", FechaUtils.formatear("2026-03-05"));
        assertEquals("", FechaUtils.formatear("no-es-fecha"));
        assertNull(FechaUtils.parse(null));
    }

    @Test
    public void diasEntreFechas() {
        LocalDate hoy = LocalDate.of(2026, 10, 24);
        assertEquals(0, FechaUtils.diasEntre(hoy, "2026-10-24"));
        assertEquals(8, FechaUtils.diasEntre(hoy, "2026-11-01"));
        assertEquals(-2, FechaUtils.diasEntre(hoy, "2026-10-22"));
    }

    @Test
    public void textoDeVencimiento() {
        assertEquals("Vence hoy", FechaUtils.textoVencimiento(0));
        assertEquals("Vence mañana", FechaUtils.textoVencimiento(1));
        assertEquals("Vence en 5 días", FechaUtils.textoVencimiento(5));
        assertEquals("Venció ayer", FechaUtils.textoVencimiento(-1));
        assertEquals("Venció hace 3 días", FechaUtils.textoVencimiento(-3));
    }

    @Test
    public void periodoDeEstadisticasCruzaElAnio() {
        YearMonth enero = YearMonth.of(2026, 1);
        assertEquals("2025-11-01", FechaUtils.inicioPeriodo(enero, 3));
        assertEquals("2026-01-01", FechaUtils.inicioPeriodo(enero, 1));
        assertEquals(Arrays.asList("2025-11", "2025-12", "2026-01"), FechaUtils.mesesDelPeriodo(enero, 3));
    }
}

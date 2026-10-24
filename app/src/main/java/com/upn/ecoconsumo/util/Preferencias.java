package com.upn.ecoconsumo.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

/** Lectura de la configuración definida en la pantalla de Ajustes (res/xml/preferencias.xml). */
public final class Preferencias {

    public static final String KEY_NOTIFICACIONES = "notificaciones_activas";
    public static final String KEY_DIAS_AVISO = "dias_aviso";
    public static final String KEY_HORA_AVISO = "hora_aviso";
    public static final String KEY_TEMA = "tema";
    public static final String KEY_EXPORTAR_INVENTARIO = "exportar_inventario";
    public static final String KEY_EXPORTAR_HISTORIAL = "exportar_historial";

    public static final String TEMA_SISTEMA = "sistema";
    public static final String TEMA_CLARO = "claro";
    public static final String TEMA_OSCURO = "oscuro";

    private Preferencias() {}

    private static SharedPreferences prefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean notificacionesActivas(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICACIONES, true);
    }

    /** Días de anticipación para considerar un producto "por caducar". */
    public static int diasAviso(Context context) {
        return entero(prefs(context).getString(KEY_DIAS_AVISO, "3"), 3);
    }

    /** Hora del día (0-23) en que se envía la revisión diaria de vencimientos. */
    public static int horaAviso(Context context) {
        return entero(prefs(context).getString(KEY_HORA_AVISO, "9"), 9);
    }

    public static void aplicarTema(Context context) {
        aplicarTema(prefs(context).getString(KEY_TEMA, TEMA_SISTEMA));
    }

    public static void aplicarTema(String tema) {
        int modo;
        if (TEMA_CLARO.equals(tema)) {
            modo = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (TEMA_OSCURO.equals(tema)) {
            modo = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            modo = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(modo);
    }

    private static int entero(String valor, int porDefecto) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException | NullPointerException e) {
            return porDefecto;
        }
    }
}

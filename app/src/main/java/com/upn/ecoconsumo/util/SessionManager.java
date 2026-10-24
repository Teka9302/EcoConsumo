package com.upn.ecoconsumo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.upn.ecoconsumo.data.model.SesionUsuario;

/** Mantiene la sesión activa del usuario en el dispositivo (HU0008, HU0012). */
public class SessionManager {

    private static final String PREFS = "sesion_ecoconsumo";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_HOGAR = "id_hogar";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void iniciar(SesionUsuario sesion) {
        prefs.edit()
                .putInt(KEY_ID, sesion.idUsuario)
                .putString(KEY_NOMBRE, sesion.nombre)
                .putString(KEY_CORREO, sesion.correo)
                .putInt(KEY_HOGAR, sesion.idHogar)
                .apply();
    }

    public boolean haySesion() {
        return getIdUsuario() != -1 && getIdHogar() != -1;
    }

    public int getIdUsuario() {
        return prefs.getInt(KEY_ID, -1);
    }

    public int getIdHogar() {
        return prefs.getInt(KEY_HOGAR, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    public void setIdHogar(int idHogar) {
        prefs.edit().putInt(KEY_HOGAR, idHogar).apply();
    }

    public void setNombre(String nombre) {
        prefs.edit().putString(KEY_NOMBRE, nombre).apply();
    }

    public void cerrar() {
        prefs.edit().clear().apply();
    }
}

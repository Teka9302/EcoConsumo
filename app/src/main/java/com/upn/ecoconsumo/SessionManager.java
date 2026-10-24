package com.upn.ecoconsumo;

import android.content.Context;
import android.content.SharedPreferences;

/** Mantiene la sesión activa del usuario en el dispositivo (HU0008, HU0012). */
public class SessionManager {

    private static final String PREFS = "sesion_ecoconsumo";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_CORREO = "correo";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void iniciar(Usuario usuario) {
        prefs.edit()
                .putInt(KEY_ID, usuario.getIdUsuario())
                .putString(KEY_NOMBRE, usuario.getNombre())
                .putString(KEY_CORREO, usuario.getCorreo())
                .apply();
    }

    public boolean haySesion() {
        return getIdUsuario() != -1;
    }

    public int getIdUsuario() {
        return prefs.getInt(KEY_ID, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    public void cerrar() {
        prefs.edit().clear().apply();
    }
}

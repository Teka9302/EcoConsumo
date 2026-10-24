package com.upn.ecoconsumo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class UsuarioDAO {

    private final DBHelper dbHelper;

    public UsuarioDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public boolean existeCorreo(String correo) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM usuario WHERE correo = ? COLLATE NOCASE",
                new String[]{correo})) {
            return c.moveToFirst();
        }
    }

    /** Registra el usuario guardando solo el hash de la contraseña (HU0007). */
    public long registrar(String nombre, String correo, String contrasena) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("correo", correo.toLowerCase());
        values.put("contrasena", SeguridadUtils.hashContrasena(contrasena));
        return dbHelper.getWritableDatabase().insert("usuario", null, values);
    }

    /** Devuelve el usuario si las credenciales son válidas, o null (HU0008). */
    public Usuario autenticar(String correo, String contrasena) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT id_usuario, nombre, correo, contrasena FROM usuario WHERE correo = ? COLLATE NOCASE",
                new String[]{correo})) {
            if (!c.moveToFirst()) return null;
            if (!SeguridadUtils.verificarContrasena(contrasena, c.getString(3))) return null;
            return new Usuario(c.getInt(0), c.getString(1), c.getString(2));
        }
    }
}

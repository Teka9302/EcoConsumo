package com.upn.ecoconsumo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private final DBHelper dbHelper;

    public CategoriaDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    /** Categorías predefinidas más las creadas por el usuario (HU0011). */
    public List<String> listar(int idUsuario) {
        List<String> lista = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT nombre FROM categoria WHERE id_usuario IS NULL OR id_usuario = ?" +
                        " ORDER BY id_usuario IS NOT NULL, id_categoria",
                new String[]{String.valueOf(idUsuario)})) {
            while (c.moveToNext()) {
                lista.add(c.getString(0));
            }
        }
        return lista;
    }

    public boolean existe(int idUsuario, String nombre) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT 1 FROM categoria WHERE (id_usuario IS NULL OR id_usuario = ?)" +
                        " AND nombre = ? COLLATE NOCASE",
                new String[]{String.valueOf(idUsuario), nombre})) {
            return c.moveToFirst();
        }
    }

    public long insertar(int idUsuario, String nombre) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("id_usuario", idUsuario);
        return dbHelper.getWritableDatabase().insert("categoria", null, values);
    }
}

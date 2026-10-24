package com.upn.ecoconsumo;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoConsumo.db";
    private static final int DATABASE_VERSION = 2;

    // Categorías predefinidas (HU0011)
    public static final String[] CATEGORIAS_BASE = {
            "Lácteos", "Frutas y Verduras", "Granos", "Panadería", "Carnes", "Bebidas", "Otros"
    };

    private static DBHelper instancia;

    public static synchronized DBHelper getInstance(Context context) {
        if (instancia == null) {
            instancia = new DBHelper(context.getApplicationContext());
        }
        return instancia;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_USUARIO = "CREATE TABLE usuario (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "correo TEXT NOT NULL UNIQUE, " +
                "contrasena TEXT NOT NULL);";

        // id_usuario NULL = categoría predefinida, visible para todos
        String CREATE_TABLE_CATEGORIA = "CREATE TABLE categoria (" +
                "id_categoria INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "id_usuario INTEGER, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE);";

        // Fechas en formato ISO (yyyy-MM-dd) para poder ordenarlas y compararlas como texto
        String CREATE_TABLE_PRODUCTO = "CREATE TABLE producto (" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre_producto TEXT NOT NULL, " +
                "categoria TEXT NOT NULL, " +
                "cantidad INTEGER NOT NULL DEFAULT 0, " +
                "stock_minimo INTEGER NOT NULL DEFAULT 1, " +
                "fecha_compra TEXT NOT NULL, " +
                "fecha_vencimiento TEXT NOT NULL, " +
                "id_usuario INTEGER NOT NULL, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE);";

        String CREATE_TABLE_LISTA_COMPRA = "CREATE TABLE lista_compra (" +
                "id_item INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre_producto TEXT NOT NULL, " +
                "categoria TEXT, " +
                "cantidad INTEGER NOT NULL DEFAULT 1, " +
                "comprado INTEGER NOT NULL DEFAULT 0, " +
                "id_producto INTEGER, " +
                "id_usuario INTEGER NOT NULL, " +
                "FOREIGN KEY(id_producto) REFERENCES producto(id_producto) ON DELETE SET NULL, " +
                "FOREIGN KEY(id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE);";

        db.execSQL(CREATE_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_CATEGORIA);
        db.execSQL(CREATE_TABLE_PRODUCTO);
        db.execSQL(CREATE_TABLE_LISTA_COMPRA);

        for (String nombre : CATEGORIAS_BASE) {
            ContentValues values = new ContentValues();
            values.put("nombre", nombre);
            db.insert("categoria", null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS lista_compra");
        db.execSQL("DROP TABLE IF EXISTS producto");
        db.execSQL("DROP TABLE IF EXISTS categoria");
        db.execSQL("DROP TABLE IF EXISTS usuario");
        onCreate(db);
    }
}

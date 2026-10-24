package com.upn.ecoconsumo.data;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.upn.ecoconsumo.data.dao.CategoriaDao;
import com.upn.ecoconsumo.data.dao.CompraDao;
import com.upn.ecoconsumo.data.dao.HogarDao;
import com.upn.ecoconsumo.data.dao.MovimientoDao;
import com.upn.ecoconsumo.data.dao.ProductoDao;
import com.upn.ecoconsumo.data.dao.UsuarioDao;
import com.upn.ecoconsumo.data.entity.Categoria;
import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.entity.ItemCompra;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.entity.Usuario;

/** Base de datos local SQLite gestionada con Room. */
@Database(entities = {Usuario.class, Hogar.class, MiembroHogar.class, Categoria.class,
        Producto.class, ItemCompra.class, Movimiento.class},
        version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE = "EcoConsumo.db";

    // Categorías predefinidas (HU0011)
    public static final String[] CATEGORIAS_BASE = {
            "Lácteos", "Frutas y Verduras", "Granos", "Panadería", "Carnes", "Bebidas", "Otros"
    };

    private static volatile AppDatabase instancia;

    public abstract UsuarioDao usuarioDao();
    public abstract HogarDao hogarDao();
    public abstract CategoriaDao categoriaDao();
    public abstract ProductoDao productoDao();
    public abstract CompraDao compraDao();
    public abstract MovimientoDao movimientoDao();

    public static AppDatabase getInstance(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NOMBRE)
                            // La versión 2 (SQLiteOpenHelper) no tenía datos reales que conservar
                            .fallbackToDestructiveMigration()
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    // Room no activa las claves foráneas por defecto (CASCADE / SET NULL)
                                    db.execSQL("PRAGMA foreign_keys = ON");
                                    insertarCategoriasBase(db);
                                }
                            })
                            .build();
                }
            }
        }
        return instancia;
    }

    /** Se ejecuta al abrir la base; solo inserta si aún no existen (también tras una migración destructiva). */
    private static void insertarCategoriasBase(SupportSQLiteDatabase db) {
        try (Cursor c = db.query("SELECT COUNT(*) FROM categoria WHERE id_hogar IS NULL")) {
            if (c.moveToFirst() && c.getInt(0) > 0) return;
        }
        for (String nombre : CATEGORIAS_BASE) {
            db.execSQL("INSERT INTO categoria (nombre, id_hogar) VALUES (?, NULL)", new Object[]{nombre});
        }
    }
}

package com.upn.ecoconsumo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public static final int FILTRO_TODOS = 0;
    public static final int FILTRO_POR_CADUCAR = 1;
    public static final int FILTRO_EN_STOCK = 2;

    private final DBHelper dbHelper;

    public ProductoDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
    }

    public long insertar(Producto p) {
        return dbHelper.getWritableDatabase().insert("producto", null, toValues(p));
    }

    public int actualizar(Producto p) {
        return dbHelper.getWritableDatabase().update("producto", toValues(p),
                "id_producto = ?", new String[]{String.valueOf(p.getIdProducto())});
    }

    public int eliminar(int idProducto) {
        return dbHelper.getWritableDatabase().delete("producto",
                "id_producto = ?", new String[]{String.valueOf(idProducto)});
    }

    public void sumarCantidad(int idProducto, int unidades) {
        dbHelper.getWritableDatabase().execSQL(
                "UPDATE producto SET cantidad = MAX(cantidad + ?, 0) WHERE id_producto = ?",
                new Object[]{unidades, idProducto});
    }

    public Producto obtener(int idProducto) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM producto WHERE id_producto = ?",
                new String[]{String.valueOf(idProducto)})) {
            return c.moveToFirst() ? fromCursor(c) : null;
        }
    }

    /**
     * Lista los productos del usuario ordenados por fecha de vencimiento (HU0004),
     * filtrando por nombre (HU0010), categoría (HU0004) y estado de caducidad (HU0009).
     */
    public List<Producto> listar(int idUsuario, String texto, String categoria, int filtro) {
        StringBuilder sql = new StringBuilder("SELECT * FROM producto WHERE id_usuario = ?");
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(idUsuario));

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND nombre_producto LIKE ?");
            args.add("%" + texto.trim() + "%");
        }
        if (categoria != null) {
            sql.append(" AND categoria = ?");
            args.add(categoria);
        }
        if (filtro == FILTRO_POR_CADUCAR) {
            sql.append(" AND fecha_vencimiento <= date('now', 'localtime', '+")
                    .append(Producto.DIAS_POR_CADUCAR).append(" days')");
        } else if (filtro == FILTRO_EN_STOCK) {
            sql.append(" AND cantidad > 0 AND fecha_vencimiento >= date('now', 'localtime')");
        }
        sql.append(" ORDER BY fecha_vencimiento ASC, nombre_producto COLLATE NOCASE ASC");

        return consultar(sql.toString(), args.toArray(new String[0]));
    }

    /** Productos no vencidos que vencen dentro de los próximos días (HU0003). */
    public List<Producto> listarPorVencer(int idUsuario) {
        return consultar("SELECT * FROM producto WHERE id_usuario = ? AND cantidad > 0" +
                        " AND fecha_vencimiento BETWEEN date('now', 'localtime')" +
                        " AND date('now', 'localtime', '+" + Producto.DIAS_POR_CADUCAR + " days')" +
                        " ORDER BY fecha_vencimiento ASC",
                new String[]{String.valueOf(idUsuario)});
    }

    /** Productos agotados o por debajo del stock mínimo (HU0006). */
    public List<Producto> listarStockBajo(int idUsuario) {
        return consultar("SELECT * FROM producto WHERE id_usuario = ? AND cantidad <= stock_minimo" +
                        " ORDER BY nombre_producto COLLATE NOCASE",
                new String[]{String.valueOf(idUsuario)});
    }

    public int contar(int idUsuario) {
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM producto WHERE id_usuario = ?",
                new String[]{String.valueOf(idUsuario)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    private List<Producto> consultar(String sql, String[] args) {
        List<Producto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                lista.add(fromCursor(c));
            }
        }
        return lista;
    }

    private ContentValues toValues(Producto p) {
        ContentValues values = new ContentValues();
        values.put("nombre_producto", p.getNombreProducto());
        values.put("categoria", p.getCategoria());
        values.put("cantidad", p.getCantidad());
        values.put("stock_minimo", p.getStockMinimo());
        values.put("fecha_compra", p.getFechaCompra());
        values.put("fecha_vencimiento", p.getFechaVencimiento());
        values.put("id_usuario", p.getIdUsuario());
        return values;
    }

    private Producto fromCursor(Cursor c) {
        return new Producto(
                c.getInt(c.getColumnIndexOrThrow("id_producto")),
                c.getString(c.getColumnIndexOrThrow("nombre_producto")),
                c.getString(c.getColumnIndexOrThrow("categoria")),
                c.getInt(c.getColumnIndexOrThrow("cantidad")),
                c.getInt(c.getColumnIndexOrThrow("stock_minimo")),
                c.getString(c.getColumnIndexOrThrow("fecha_compra")),
                c.getString(c.getColumnIndexOrThrow("fecha_vencimiento")),
                c.getInt(c.getColumnIndexOrThrow("id_usuario")));
    }
}

package com.upn.ecoconsumo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    private final DBHelper dbHelper;
    private final ProductoDAO productoDAO;

    public CompraDAO(Context context) {
        dbHelper = DBHelper.getInstance(context);
        productoDAO = new ProductoDAO(context);
    }

    /**
     * Agrega a la lista de compras los productos agotados o con stock bajo que aún no
     * estén pendientes (HU0006). Devuelve cuántos ítems nuevos se agregaron.
     */
    public int generarSugerencias(int idUsuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int agregados = 0;
        db.beginTransaction();
        try {
            for (Producto p : productoDAO.listarStockBajo(idUsuario)) {
                if (tienePendiente(db, p.getIdProducto())) continue;
                ContentValues values = new ContentValues();
                values.put("nombre_producto", p.getNombreProducto());
                values.put("categoria", p.getCategoria());
                // Sugiere reponer hasta el doble del stock mínimo
                values.put("cantidad", Math.max(1, p.getStockMinimo() * 2 - p.getCantidad()));
                values.put("id_producto", p.getIdProducto());
                values.put("id_usuario", idUsuario);
                db.insert("lista_compra", null, values);
                agregados++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return agregados;
    }

    public List<ItemCompra> listar(int idUsuario) {
        List<ItemCompra> lista = new ArrayList<>();
        try (Cursor c = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM lista_compra WHERE id_usuario = ?" +
                        " ORDER BY comprado ASC, nombre_producto COLLATE NOCASE",
                new String[]{String.valueOf(idUsuario)})) {
            int colProducto = c.getColumnIndexOrThrow("id_producto");
            while (c.moveToNext()) {
                lista.add(new ItemCompra(
                        c.getInt(c.getColumnIndexOrThrow("id_item")),
                        c.getString(c.getColumnIndexOrThrow("nombre_producto")),
                        c.getString(c.getColumnIndexOrThrow("categoria")),
                        c.getInt(c.getColumnIndexOrThrow("cantidad")),
                        c.getInt(c.getColumnIndexOrThrow("comprado")) == 1,
                        c.isNull(colProducto) ? null : c.getInt(colProducto)));
            }
        }
        return lista;
    }

    public void marcarComprado(int idItem, boolean comprado) {
        ContentValues values = new ContentValues();
        values.put("comprado", comprado ? 1 : 0);
        dbHelper.getWritableDatabase().update("lista_compra", values,
                "id_item = ?", new String[]{String.valueOf(idItem)});
    }

    /**
     * Suma a la despensa las cantidades de los artículos marcados como obtenidos
     * y los quita de la lista. Devuelve cuántos artículos se procesaron.
     */
    public int finalizarCompra(int idUsuario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int procesados = 0;
        db.beginTransaction();
        try {
            for (ItemCompra item : listar(idUsuario)) {
                if (!item.isComprado()) continue;
                if (item.getIdProducto() != null) {
                    productoDAO.sumarCantidad(item.getIdProducto(), item.getCantidad());
                }
                db.delete("lista_compra", "id_item = ?", new String[]{String.valueOf(item.getIdItem())});
                procesados++;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return procesados;
    }

    private boolean tienePendiente(SQLiteDatabase db, int idProducto) {
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM lista_compra WHERE id_producto = ? AND comprado = 0",
                new String[]{String.valueOf(idProducto)})) {
            return c.moveToFirst();
        }
    }
}

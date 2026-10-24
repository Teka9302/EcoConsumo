package com.upn.ecoconsumo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.Tareas;
import com.upn.ecoconsumo.data.dao.CompraDao;
import com.upn.ecoconsumo.data.dao.ProductoDao;
import com.upn.ecoconsumo.data.entity.ItemCompra;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;

import java.util.List;

/** HU0006: lista de compras del hogar. */
public class CompraRepository {

    private final AppDatabase db;
    private final CompraDao compraDao;
    private final ProductoDao productoDao;

    public CompraRepository(Context context) {
        db = AppDatabase.getInstance(context);
        compraDao = db.compraDao();
        productoDao = db.productoDao();
    }

    public LiveData<List<ItemCompra>> observar(int idHogar) {
        return compraDao.observar(idHogar);
    }

    /** Agrega los productos agotados o con stock bajo que aún no estén pendientes. Devuelve cuántos agregó. */
    public void generarSugerencias(int idHogar, Tareas.Callback<Integer> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            int agregados = 0;
            for (Producto p : productoDao.listarStockBajo(idHogar)) {
                if (compraDao.contarPendientes(p.idProducto) > 0) continue;
                ItemCompra item = new ItemCompra();
                item.nombreProducto = p.nombreProducto;
                item.categoria = p.categoria;
                // Sugiere reponer hasta el doble del stock mínimo
                item.cantidad = Math.max(1, p.stockMinimo * 2 - p.cantidad);
                item.idProducto = p.idProducto;
                item.idHogar = idHogar;
                compraDao.insertar(item);
                agregados++;
            }
            return agregados;
        }), callback);
    }

    public void agregarManual(int idHogar, String nombre, int cantidad) {
        Tareas.ejecutar(() -> {
            ItemCompra item = new ItemCompra();
            item.nombreProducto = nombre;
            item.cantidad = cantidad;
            item.idHogar = idHogar;
            compraDao.insertar(item);
        });
    }

    public void marcarComprado(int idItem, boolean comprado) {
        Tareas.ejecutar(() -> compraDao.marcarComprado(idItem, comprado));
    }

    public void eliminar(int idItem) {
        Tareas.ejecutar(() -> compraDao.eliminar(idItem));
    }

    /**
     * Suma a la despensa los artículos marcados como obtenidos, los registra como compra
     * en el historial y los quita de la lista. Devuelve cuántos procesó.
     */
    public void finalizarCompra(int idHogar, int idUsuario, Tareas.Callback<Integer> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            List<ItemCompra> comprados = compraDao.listarComprados(idHogar);
            for (ItemCompra item : comprados) {
                Producto producto = item.idProducto == null ? null : productoDao.obtener(item.idProducto);
                if (producto != null) {
                    productoDao.sumarCantidad(producto.idProducto, item.cantidad);
                    db.movimientoDao().insertar(Movimiento.crear(producto, Movimiento.TIPO_COMPRA,
                            item.cantidad, null, idUsuario));
                }
                compraDao.eliminar(item.idItem);
            }
            return comprados.size();
        }), callback);
    }
}

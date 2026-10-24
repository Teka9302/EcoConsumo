package com.upn.ecoconsumo.data.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.Tareas;
import com.upn.ecoconsumo.data.dao.MovimientoDao;
import com.upn.ecoconsumo.data.dao.ProductoDao;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.util.CsvExporter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ProductoRepository {

    /** Qué ocurrió con el producto que se elimina (HU0005). */
    public enum AccionEliminar { CONSUMIDO, DESECHADO, REGISTRO_ERRONEO }

    private final AppDatabase db;
    private final ProductoDao productoDao;
    private final MovimientoDao movimientoDao;

    public ProductoRepository(Context context) {
        db = AppDatabase.getInstance(context);
        productoDao = db.productoDao();
        movimientoDao = db.movimientoDao();
    }

    public LiveData<List<Producto>> observarFiltrados(int idHogar, String texto, String categoria,
                                                      int filtro, int dias) {
        return productoDao.observarFiltrados(idHogar, texto, categoria, filtro, dias);
    }

    public LiveData<Producto> observar(int idProducto) {
        return productoDao.observar(idProducto);
    }

    public LiveData<Integer> observarTotal(int idHogar) {
        return productoDao.observarTotal(idHogar);
    }

    public LiveData<Integer> observarTotalPorVencer(int idHogar, int dias) {
        return productoDao.observarTotalPorVencer(idHogar, dias);
    }

    public void obtener(int idProducto, Tareas.Callback<Producto> callback) {
        Tareas.ejecutar(() -> productoDao.obtener(idProducto), callback);
    }

    /** Inserta o actualiza. Un producto nuevo con stock queda registrado como compra en el historial. */
    public void guardar(Producto producto, int idUsuario, Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            if (producto.idProducto == 0) {
                producto.idProducto = (int) productoDao.insertar(producto);
                if (producto.cantidad > 0) {
                    movimientoDao.insertar(Movimiento.crear(producto, Movimiento.TIPO_COMPRA,
                            producto.cantidad, null, idUsuario));
                }
            } else {
                productoDao.actualizar(producto);
            }
            return true;
        }), callback);
    }

    public void registrarConsumo(Producto producto, int cantidad, int idUsuario) {
        registrarSalida(producto, Movimiento.TIPO_CONSUMO, cantidad, null, idUsuario);
    }

    public void registrarDesecho(Producto producto, int cantidad, String motivo, int idUsuario) {
        registrarSalida(producto, Movimiento.TIPO_DESECHO, cantidad, motivo, idUsuario);
    }

    private void registrarSalida(Producto producto, String tipo, int cantidad, String motivo, int idUsuario) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            productoDao.sumarCantidad(producto.idProducto, -cantidad);
            movimientoDao.insertar(Movimiento.crear(producto, tipo, cantidad, motivo, idUsuario));
        }));
    }

    /** Elimina el producto registrando en el historial lo que quedaba, salvo que fuera un error de registro. */
    public void eliminar(Producto producto, AccionEliminar accion, String motivo, int idUsuario,
                         Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> db.runInTransaction(() -> {
            if (accion != AccionEliminar.REGISTRO_ERRONEO && producto.cantidad > 0) {
                String tipo = accion == AccionEliminar.CONSUMIDO ? Movimiento.TIPO_CONSUMO : Movimiento.TIPO_DESECHO;
                movimientoDao.insertar(Movimiento.crear(producto, tipo, producto.cantidad,
                        accion == AccionEliminar.DESECHADO ? motivo : null, idUsuario));
            }
            productoDao.eliminar(producto);
            return true;
        }), callback);
    }

    /** Solo desde un hilo secundario (VencimientoWorker). */
    public List<Producto> listarPorVencer(int idHogar, int dias) {
        return productoDao.listarPorVencer(idHogar, dias);
    }

    public void exportarInventario(ContentResolver resolver, Uri destino, int idHogar, int diasAviso,
                                   Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> {
            try (OutputStream out = resolver.openOutputStream(destino)) {
                if (out == null) return false;
                CsvExporter.exportarInventario(out, productoDao.listarTodos(idHogar), diasAviso);
                return true;
            } catch (IOException e) {
                return false;
            }
        }, callback);
    }

    public void exportarHistorial(ContentResolver resolver, Uri destino, int idHogar,
                                  Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> {
            try (OutputStream out = resolver.openOutputStream(destino)) {
                if (out == null) return false;
                CsvExporter.exportarHistorial(out, movimientoDao.listarTodos(idHogar));
                return true;
            } catch (IOException e) {
                return false;
            }
        }, callback);
    }
}

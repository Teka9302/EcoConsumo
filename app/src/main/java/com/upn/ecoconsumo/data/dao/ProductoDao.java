package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.upn.ecoconsumo.data.entity.Producto;

import java.util.List;

@Dao
public interface ProductoDao {

    /** Valores del parámetro "filtro" de {@link #observarFiltrados}. */
    int FILTRO_TODOS = 0;
    int FILTRO_POR_CADUCAR = 1;
    int FILTRO_EN_STOCK = 2;

    @Insert
    long insertar(Producto producto);

    @Update
    int actualizar(Producto producto);

    @Delete
    int eliminar(Producto producto);

    @Query("SELECT * FROM producto WHERE id_producto = :idProducto")
    Producto obtener(int idProducto);

    @Query("SELECT * FROM producto WHERE id_producto = :idProducto")
    LiveData<Producto> observar(int idProducto);

    /**
     * Productos del hogar ordenados por vencimiento (HU0004), filtrados por nombre (HU0010),
     * categoría (HU0004) y estado de caducidad (HU0009).
     */
    @Query("SELECT * FROM producto WHERE id_hogar = :idHogar" +
            " AND (:texto = '' OR nombre_producto LIKE '%' || :texto || '%')" +
            " AND (:categoria IS NULL OR categoria = :categoria)" +
            " AND (:filtro = 0" +
            "   OR (:filtro = 1 AND fecha_vencimiento <= date('now', 'localtime', '+' || :dias || ' days'))" +
            "   OR (:filtro = 2 AND cantidad > 0 AND fecha_vencimiento >= date('now', 'localtime')))" +
            " ORDER BY fecha_vencimiento ASC, nombre_producto COLLATE NOCASE ASC")
    LiveData<List<Producto>> observarFiltrados(int idHogar, String texto, String categoria, int filtro, int dias);

    /** Productos con stock que vencen entre hoy y los próximos días (HU0003). */
    @Query("SELECT * FROM producto WHERE id_hogar = :idHogar AND cantidad > 0" +
            " AND fecha_vencimiento BETWEEN date('now', 'localtime')" +
            " AND date('now', 'localtime', '+' || :dias || ' days')" +
            " ORDER BY fecha_vencimiento ASC")
    List<Producto> listarPorVencer(int idHogar, int dias);

    @Query("SELECT COUNT(*) FROM producto WHERE id_hogar = :idHogar AND cantidad > 0" +
            " AND fecha_vencimiento BETWEEN date('now', 'localtime')" +
            " AND date('now', 'localtime', '+' || :dias || ' days')")
    LiveData<Integer> observarTotalPorVencer(int idHogar, int dias);

    @Query("SELECT COUNT(*) FROM producto WHERE id_hogar = :idHogar")
    LiveData<Integer> observarTotal(int idHogar);

    /** Agotados o por debajo del stock mínimo (HU0006). */
    @Query("SELECT * FROM producto WHERE id_hogar = :idHogar AND cantidad <= stock_minimo" +
            " ORDER BY nombre_producto COLLATE NOCASE")
    List<Producto> listarStockBajo(int idHogar);

    @Query("SELECT * FROM producto WHERE id_hogar = :idHogar ORDER BY nombre_producto COLLATE NOCASE")
    List<Producto> listarTodos(int idHogar);

    @Query("UPDATE producto SET cantidad = MAX(cantidad + :unidades, 0) WHERE id_producto = :idProducto")
    void sumarCantidad(int idProducto, int unidades);
}

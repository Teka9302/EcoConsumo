package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.model.MovimientoConUsuario;
import com.upn.ecoconsumo.data.model.TotalMensual;
import com.upn.ecoconsumo.data.model.TotalPorClave;

import java.util.List;

@Dao
public interface MovimientoDao {

    @Insert
    long insertar(Movimiento movimiento);

    @Query("SELECT m.*, u.nombre AS nombre_usuario FROM movimiento m" +
            " LEFT JOIN usuario u ON u.id_usuario = m.id_usuario" +
            " WHERE m.id_hogar = :idHogar ORDER BY m.fecha_hora DESC LIMIT 500")
    LiveData<List<MovimientoConUsuario>> observarHistorial(int idHogar);

    @Query("SELECT * FROM movimiento WHERE id_hogar = :idHogar ORDER BY fecha_hora DESC")
    List<Movimiento> listarTodos(int idHogar);

    // --- Estadísticas (desde = primer día del periodo, yyyy-MM-dd) ---

    @Query("SELECT tipo AS clave, SUM(cantidad) AS total FROM movimiento" +
            " WHERE id_hogar = :idHogar AND fecha >= :desde GROUP BY tipo")
    LiveData<List<TotalPorClave>> totalesPorTipo(int idHogar, String desde);

    @Query("SELECT substr(fecha, 1, 7) AS mes, tipo, SUM(cantidad) AS total FROM movimiento" +
            " WHERE id_hogar = :idHogar AND fecha >= :desde AND tipo != 'COMPRA'" +
            " GROUP BY mes, tipo ORDER BY mes")
    LiveData<List<TotalMensual>> totalesMensuales(int idHogar, String desde);

    @Query("SELECT categoria AS clave, SUM(cantidad) AS total FROM movimiento" +
            " WHERE id_hogar = :idHogar AND fecha >= :desde AND tipo = 'DESECHO'" +
            " GROUP BY categoria ORDER BY total DESC")
    LiveData<List<TotalPorClave>> desechoPorCategoria(int idHogar, String desde);

    @Query("SELECT COALESCE(motivo, 'OTRO') AS clave, SUM(cantidad) AS total FROM movimiento" +
            " WHERE id_hogar = :idHogar AND fecha >= :desde AND tipo = 'DESECHO'" +
            " GROUP BY clave ORDER BY total DESC")
    LiveData<List<TotalPorClave>> desechoPorMotivo(int idHogar, String desde);

    @Query("SELECT nombre_producto AS clave, SUM(cantidad) AS total FROM movimiento" +
            " WHERE id_hogar = :idHogar AND fecha >= :desde AND tipo = 'DESECHO'" +
            " GROUP BY nombre_producto COLLATE NOCASE ORDER BY total DESC LIMIT 5")
    LiveData<List<TotalPorClave>> masDesechados(int idHogar, String desde);
}

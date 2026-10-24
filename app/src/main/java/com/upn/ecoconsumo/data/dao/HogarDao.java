package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.model.MiembroDetalle;

import java.util.List;

@Dao
public interface HogarDao {

    @Insert
    long insertar(Hogar hogar);

    @Query("SELECT * FROM hogar WHERE id_hogar = :idHogar")
    LiveData<Hogar> observar(int idHogar);

    @Query("SELECT * FROM hogar WHERE codigo_invitacion = :codigo LIMIT 1")
    Hogar buscarPorCodigo(String codigo);

    @Query("UPDATE hogar SET nombre = :nombre WHERE id_hogar = :idHogar")
    void renombrar(int idHogar, String nombre);

    @Query("UPDATE hogar SET codigo_invitacion = :codigo WHERE id_hogar = :idHogar")
    void cambiarCodigo(int idHogar, String codigo);

    @Query("DELETE FROM hogar WHERE id_hogar = :idHogar")
    void eliminar(int idHogar);

    // --- Miembros ---

    /** Como id_usuario es la clave, reemplazar equivale a cambiar de hogar. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardarMiembro(MiembroHogar miembro);

    @Query("SELECT * FROM miembro_hogar WHERE id_usuario = :idUsuario")
    MiembroHogar obtenerMembresia(int idUsuario);

    @Query("SELECT * FROM miembro_hogar WHERE id_usuario = :idUsuario")
    LiveData<MiembroHogar> observarMembresia(int idUsuario);

    @Query("SELECT COUNT(*) FROM miembro_hogar WHERE id_hogar = :idHogar")
    int contarMiembros(int idHogar);

    @Query("SELECT * FROM miembro_hogar WHERE id_hogar = :idHogar AND id_usuario != :excluir" +
            " ORDER BY fecha_union LIMIT 1")
    MiembroHogar miembroMasAntiguo(int idHogar, int excluir);

    @Query("UPDATE miembro_hogar SET rol = :rol WHERE id_usuario = :idUsuario")
    void cambiarRol(int idUsuario, String rol);

    @Query("SELECT u.id_usuario, u.nombre, u.correo, m.rol, m.fecha_union FROM miembro_hogar m" +
            " INNER JOIN usuario u ON u.id_usuario = m.id_usuario" +
            " WHERE m.id_hogar = :idHogar ORDER BY m.rol = 'ADMIN' DESC, m.fecha_union")
    LiveData<List<MiembroDetalle>> observarMiembros(int idHogar);

    // --- Traslado de datos al unirse a otro hogar ---

    @Query("UPDATE producto SET id_hogar = :destino WHERE id_hogar = :origen")
    void moverProductos(int origen, int destino);

    @Query("UPDATE categoria SET id_hogar = :destino WHERE id_hogar = :origen")
    void moverCategorias(int origen, int destino);

    @Query("UPDATE lista_compra SET id_hogar = :destino WHERE id_hogar = :origen")
    void moverListaCompra(int origen, int destino);

    @Query("UPDATE movimiento SET id_hogar = :destino WHERE id_hogar = :origen")
    void moverMovimientos(int origen, int destino);
}

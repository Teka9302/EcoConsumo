package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.upn.ecoconsumo.data.entity.ItemCompra;

import java.util.List;

@Dao
public interface CompraDao {

    @Insert
    long insertar(ItemCompra item);

    @Query("SELECT * FROM lista_compra WHERE id_hogar = :idHogar" +
            " ORDER BY comprado ASC, nombre_producto COLLATE NOCASE")
    LiveData<List<ItemCompra>> observar(int idHogar);

    @Query("SELECT * FROM lista_compra WHERE id_hogar = :idHogar AND comprado = 1")
    List<ItemCompra> listarComprados(int idHogar);

    @Query("SELECT COUNT(*) FROM lista_compra WHERE id_producto = :idProducto AND comprado = 0")
    int contarPendientes(int idProducto);

    @Query("UPDATE lista_compra SET comprado = :comprado WHERE id_item = :idItem")
    void marcarComprado(int idItem, boolean comprado);

    @Query("DELETE FROM lista_compra WHERE id_item = :idItem")
    void eliminar(int idItem);
}

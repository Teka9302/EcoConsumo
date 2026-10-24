package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.upn.ecoconsumo.data.entity.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    long insertar(Categoria categoria);

    /** Predefinidas primero y luego las del hogar, sin nombres repetidos (HU0011). */
    @Query("SELECT nombre FROM categoria WHERE id_hogar IS NULL OR id_hogar = :idHogar" +
            " GROUP BY nombre COLLATE NOCASE" +
            " ORDER BY MIN(id_hogar IS NOT NULL), MIN(id_categoria)")
    LiveData<List<String>> observarNombres(int idHogar);

    @Query("SELECT COUNT(*) FROM categoria WHERE (id_hogar IS NULL OR id_hogar = :idHogar)" +
            " AND nombre = :nombre COLLATE NOCASE")
    int contarPorNombre(int idHogar, String nombre);
}

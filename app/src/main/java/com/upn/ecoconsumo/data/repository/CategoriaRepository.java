package com.upn.ecoconsumo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.Tareas;
import com.upn.ecoconsumo.data.dao.CategoriaDao;
import com.upn.ecoconsumo.data.entity.Categoria;

import java.util.List;

public class CategoriaRepository {

    private final CategoriaDao categoriaDao;

    public CategoriaRepository(Context context) {
        categoriaDao = AppDatabase.getInstance(context).categoriaDao();
    }

    public LiveData<List<String>> observarNombres(int idHogar) {
        return categoriaDao.observarNombres(idHogar);
    }

    /** Devuelve false si ya existe una categoría con ese nombre. */
    public void agregar(int idHogar, String nombre, Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> {
            if (categoriaDao.contarPorNombre(idHogar, nombre) > 0) return false;
            Categoria categoria = new Categoria();
            categoria.nombre = nombre;
            categoria.idHogar = idHogar;
            categoriaDao.insertar(categoria);
            return true;
        }, callback);
    }
}

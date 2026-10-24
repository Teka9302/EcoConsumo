package com.upn.ecoconsumo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.dao.MovimientoDao;
import com.upn.ecoconsumo.data.model.MovimientoConUsuario;
import com.upn.ecoconsumo.data.model.TotalMensual;
import com.upn.ecoconsumo.data.model.TotalPorClave;

import java.util.List;

public class EstadisticasRepository {

    private final MovimientoDao movimientoDao;

    public EstadisticasRepository(Context context) {
        movimientoDao = AppDatabase.getInstance(context).movimientoDao();
    }

    public LiveData<List<MovimientoConUsuario>> observarHistorial(int idHogar) {
        return movimientoDao.observarHistorial(idHogar);
    }

    public LiveData<List<TotalPorClave>> totalesPorTipo(int idHogar, String desde) {
        return movimientoDao.totalesPorTipo(idHogar, desde);
    }

    public LiveData<List<TotalMensual>> totalesMensuales(int idHogar, String desde) {
        return movimientoDao.totalesMensuales(idHogar, desde);
    }

    public LiveData<List<TotalPorClave>> desechoPorCategoria(int idHogar, String desde) {
        return movimientoDao.desechoPorCategoria(idHogar, desde);
    }

    public LiveData<List<TotalPorClave>> desechoPorMotivo(int idHogar, String desde) {
        return movimientoDao.desechoPorMotivo(idHogar, desde);
    }

    public LiveData<List<TotalPorClave>> masDesechados(int idHogar, String desde) {
        return movimientoDao.masDesechados(idHogar, desde);
    }
}

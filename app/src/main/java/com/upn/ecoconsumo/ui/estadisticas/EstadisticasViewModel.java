package com.upn.ecoconsumo.ui.estadisticas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.model.TotalMensual;
import com.upn.ecoconsumo.data.model.TotalPorClave;
import com.upn.ecoconsumo.data.repository.EstadisticasRepository;
import com.upn.ecoconsumo.util.FechaUtils;
import com.upn.ecoconsumo.util.LiveDataUtil;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;

/** Estadísticas de consumo y desperdicio del hogar para el periodo elegido (1, 3 o 6 meses). */
public class EstadisticasViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> meses = new MutableLiveData<>(3);
    private final LiveData<List<TotalPorClave>> totales;
    private final LiveData<List<TotalMensual>> mensuales;
    private final LiveData<List<TotalPorClave>> porCategoria;
    private final LiveData<List<TotalPorClave>> porMotivo;
    private final LiveData<List<TotalPorClave>> masDesechados;

    public EstadisticasViewModel(@NonNull Application app) {
        super(app);
        EstadisticasRepository repo = new EstadisticasRepository(app);
        int idHogar = new SessionManager(app).getIdHogar();

        totales = LiveDataUtil.switchMap(meses, m -> repo.totalesPorTipo(idHogar, FechaUtils.inicioPeriodo(m)));
        mensuales = LiveDataUtil.switchMap(meses, m -> repo.totalesMensuales(idHogar, FechaUtils.inicioPeriodo(m)));
        porCategoria = LiveDataUtil.switchMap(meses, m -> repo.desechoPorCategoria(idHogar, FechaUtils.inicioPeriodo(m)));
        porMotivo = LiveDataUtil.switchMap(meses, m -> repo.desechoPorMotivo(idHogar, FechaUtils.inicioPeriodo(m)));
        masDesechados = LiveDataUtil.switchMap(meses, m -> repo.masDesechados(idHogar, FechaUtils.inicioPeriodo(m)));
    }

    public int getMeses() {
        Integer valor = meses.getValue();
        return valor == null ? 3 : valor;
    }

    public void setMeses(int valor) {
        if (valor != getMeses()) meses.setValue(valor);
    }

    public LiveData<List<TotalPorClave>> getTotales() { return totales; }
    public LiveData<List<TotalMensual>> getMensuales() { return mensuales; }
    public LiveData<List<TotalPorClave>> getPorCategoria() { return porCategoria; }
    public LiveData<List<TotalPorClave>> getPorMotivo() { return porMotivo; }
    public LiveData<List<TotalPorClave>> getMasDesechados() { return masDesechados; }
}

package com.upn.ecoconsumo.ui.estadisticas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.model.MovimientoConUsuario;
import com.upn.ecoconsumo.data.repository.EstadisticasRepository;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/** Historial de movimientos del hogar con filtro por tipo. */
public class HistorialViewModel extends AndroidViewModel {

    private final LiveData<List<MovimientoConUsuario>> historial;
    /** null = todos los tipos */
    private final MutableLiveData<String> tipo = new MutableLiveData<>(null);
    private final MediatorLiveData<List<MovimientoConUsuario>> filtrado = new MediatorLiveData<>();

    public HistorialViewModel(@NonNull Application app) {
        super(app);
        historial = new EstadisticasRepository(app).observarHistorial(new SessionManager(app).getIdHogar());
        filtrado.addSource(historial, lista -> filtrar());
        filtrado.addSource(tipo, t -> filtrar());
    }

    public LiveData<List<MovimientoConUsuario>> getMovimientos() { return filtrado; }

    public void setTipo(String nuevo) {
        tipo.setValue(nuevo);
    }

    private void filtrar() {
        List<MovimientoConUsuario> todos = historial.getValue();
        if (todos == null) return;
        String t = tipo.getValue();
        if (t == null) {
            filtrado.setValue(todos);
            return;
        }
        List<MovimientoConUsuario> resultado = new ArrayList<>();
        for (MovimientoConUsuario m : todos) {
            if (t.equals(m.movimiento.tipo)) resultado.add(m);
        }
        filtrado.setValue(resultado);
    }
}

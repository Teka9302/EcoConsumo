package com.upn.ecoconsumo.ui.compras;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.entity.ItemCompra;
import com.upn.ecoconsumo.data.repository.CompraRepository;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;

/** HU0006: lista de compras generada a partir del stock bajo o agotado. */
public class ComprasViewModel extends AndroidViewModel {

    private final CompraRepository repo;
    private final int idHogar;
    private final int idUsuario;
    private final LiveData<List<ItemCompra>> items;

    private final MutableLiveData<Evento<Integer>> sugerenciasAgregadas = new MutableLiveData<>();
    private final MutableLiveData<Evento<Integer>> compraFinalizada = new MutableLiveData<>();

    public ComprasViewModel(@NonNull Application app) {
        super(app);
        repo = new CompraRepository(app);
        SessionManager session = new SessionManager(app);
        idHogar = session.getIdHogar();
        idUsuario = session.getIdUsuario();
        items = repo.observar(idHogar);
    }

    public LiveData<List<ItemCompra>> getItems() { return items; }
    public LiveData<Evento<Integer>> getSugerenciasAgregadas() { return sugerenciasAgregadas; }
    public LiveData<Evento<Integer>> getCompraFinalizada() { return compraFinalizada; }

    /** @param avisar true si el usuario lo pidió con el botón y espera un mensaje */
    public void generarSugerencias(boolean avisar) {
        repo.generarSugerencias(idHogar, agregados -> {
            if (avisar) sugerenciasAgregadas.setValue(new Evento<>(agregados));
        });
    }

    public void marcarComprado(ItemCompra item, boolean comprado) {
        repo.marcarComprado(item.idItem, comprado);
    }

    public void agregarManual(String nombre, int cantidad) {
        repo.agregarManual(idHogar, nombre, cantidad);
    }

    public void eliminar(ItemCompra item) {
        repo.eliminar(item.idItem);
    }

    public boolean hayComprados() {
        List<ItemCompra> lista = items.getValue();
        if (lista == null) return false;
        for (ItemCompra item : lista) {
            if (item.comprado) return true;
        }
        return false;
    }

    public void finalizarCompra() {
        repo.finalizarCompra(idHogar, idUsuario, procesados -> compraFinalizada.setValue(new Evento<>(procesados)));
    }
}

package com.upn.ecoconsumo.ui.cuenta;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.model.MiembroDetalle;
import com.upn.ecoconsumo.data.repository.HogarRepository;
import com.upn.ecoconsumo.data.repository.HogarRepository.ResultadoUnion;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;

/** Despensa compartida: miembros, roles y código de invitación. */
public class HogarViewModel extends AndroidViewModel {

    private final HogarRepository repo;
    private final SessionManager session;
    private final int idHogar;
    private final int idUsuario;
    private final LiveData<Hogar> hogar;
    private final LiveData<MiembroHogar> membresia;
    private final LiveData<List<MiembroDetalle>> miembros;

    private final MutableLiveData<Evento<ResultadoUnion>> resultadoUnion = new MutableLiveData<>();
    /** true si cambió de hogar; false si no pudo salir por ser el único miembro. */
    private final MutableLiveData<Evento<Boolean>> resultadoSalida = new MutableLiveData<>();
    private final MutableLiveData<Evento<Boolean>> resultadoExpulsion = new MutableLiveData<>();

    public HogarViewModel(@NonNull Application app) {
        super(app);
        repo = new HogarRepository(app);
        session = new SessionManager(app);
        idHogar = session.getIdHogar();
        idUsuario = session.getIdUsuario();
        hogar = repo.observarHogar(idHogar);
        membresia = repo.observarMembresia(idUsuario);
        miembros = repo.observarMiembros(idHogar);
    }

    public int getIdUsuario() { return idUsuario; }
    public LiveData<Hogar> getHogar() { return hogar; }
    public LiveData<MiembroHogar> getMembresia() { return membresia; }
    public LiveData<List<MiembroDetalle>> getMiembros() { return miembros; }
    public LiveData<Evento<ResultadoUnion>> getResultadoUnion() { return resultadoUnion; }
    public LiveData<Evento<Boolean>> getResultadoSalida() { return resultadoSalida; }
    public LiveData<Evento<Boolean>> getResultadoExpulsion() { return resultadoExpulsion; }

    public boolean esAdmin() {
        MiembroHogar m = membresia.getValue();
        return m != null && m.esAdmin();
    }

    public void renombrar(String nombre) {
        if (esAdmin()) repo.renombrar(idHogar, nombre);
    }

    public void regenerarCodigo() {
        if (esAdmin()) repo.regenerarCodigo(idHogar);
    }

    public void unirse(String codigo) {
        repo.unirse(idUsuario, codigo, resultado -> {
            if (resultado.estado == HogarRepository.EstadoUnion.OK) session.setIdHogar(resultado.idHogar);
            resultadoUnion.setValue(new Evento<>(resultado));
        });
    }

    public void salir() {
        repo.salir(idUsuario, nuevoHogar -> {
            if (nuevoHogar != null) session.setIdHogar(nuevoHogar);
            resultadoSalida.setValue(new Evento<>(nuevoHogar != null));
        });
    }

    public void expulsar(MiembroDetalle miembro) {
        if (!esAdmin() || miembro.idUsuario == idUsuario) return;
        repo.expulsar(miembro.idUsuario, ok -> resultadoExpulsion.setValue(new Evento<>(ok)));
    }
}

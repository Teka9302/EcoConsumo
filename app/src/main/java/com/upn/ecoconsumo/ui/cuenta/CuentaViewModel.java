package com.upn.ecoconsumo.ui.cuenta;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.entity.Usuario;
import com.upn.ecoconsumo.data.model.MiembroDetalle;
import com.upn.ecoconsumo.data.repository.HogarRepository;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.data.repository.UsuarioRepository;
import com.upn.ecoconsumo.notificaciones.NotificacionHelper;
import com.upn.ecoconsumo.notificaciones.VencimientoWorker;
import com.upn.ecoconsumo.util.Preferencias;
import com.upn.ecoconsumo.util.SessionManager;

import java.util.List;

public class CuentaViewModel extends AndroidViewModel {

    private final SessionManager session;
    private final int diasAviso;
    private final LiveData<Usuario> usuario;
    private final LiveData<Hogar> hogar;
    private final LiveData<MiembroHogar> membresia;
    private final LiveData<List<MiembroDetalle>> miembros;
    private final LiveData<Integer> totalProductos;
    private final LiveData<Integer> totalPorVencer;

    public CuentaViewModel(@NonNull Application app) {
        super(app);
        session = new SessionManager(app);
        int idHogar = session.getIdHogar();
        int idUsuario = session.getIdUsuario();
        diasAviso = Preferencias.diasAviso(app);

        HogarRepository hogarRepo = new HogarRepository(app);
        ProductoRepository productoRepo = new ProductoRepository(app);
        usuario = new UsuarioRepository(app).observar(idUsuario);
        hogar = hogarRepo.observarHogar(idHogar);
        membresia = hogarRepo.observarMembresia(idUsuario);
        miembros = hogarRepo.observarMiembros(idHogar);
        totalProductos = productoRepo.observarTotal(idHogar);
        totalPorVencer = productoRepo.observarTotalPorVencer(idHogar, diasAviso);
    }

    public int getDiasAviso() { return diasAviso; }
    public LiveData<Usuario> getUsuario() { return usuario; }
    public LiveData<Hogar> getHogar() { return hogar; }
    public LiveData<MiembroHogar> getMembresia() { return membresia; }
    public LiveData<List<MiembroDetalle>> getMiembros() { return miembros; }
    public LiveData<Integer> getTotalProductos() { return totalProductos; }
    public LiveData<Integer> getTotalPorVencer() { return totalPorVencer; }

    /** HU0012: limpia la sesión, las alertas pendientes y la revisión programada. */
    public void cerrarSesion() {
        session.cerrar();
        VencimientoWorker.cancelar(getApplication());
        NotificacionHelper.cancelarTodas(getApplication());
    }
}

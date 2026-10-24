package com.upn.ecoconsumo.ui.despensa;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.data.repository.ProductoRepository.AccionEliminar;
import com.upn.ecoconsumo.data.repository.UsuarioRepository;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.LiveDataUtil;
import com.upn.ecoconsumo.util.Preferencias;
import com.upn.ecoconsumo.util.SessionManager;

/** Detalle del producto con consumo, desecho, edición y eliminación (HU0003, HU0005). */
public class DetalleViewModel extends AndroidViewModel {

    private final ProductoRepository productoRepo;
    private final int idHogar;
    private final int idUsuario;

    private final MutableLiveData<Integer> idProducto = new MutableLiveData<>();
    private final LiveData<Producto> producto;
    private final LiveData<String> registradoPor;
    private final MutableLiveData<Evento<Boolean>> eliminado = new MutableLiveData<>();

    public DetalleViewModel(@NonNull Application app) {
        super(app);
        productoRepo = new ProductoRepository(app);
        UsuarioRepository usuarioRepo = new UsuarioRepository(app);
        SessionManager session = new SessionManager(app);
        idHogar = session.getIdHogar();
        idUsuario = session.getIdUsuario();

        producto = LiveDataUtil.switchMap(idProducto, productoRepo::observar);
        registradoPor = LiveDataUtil.<Producto, String>switchMap(producto, p -> {
            if (p == null || p.idUsuarioRegistro == null) return new MutableLiveData<String>(null);
            return usuarioRepo.observarNombre(p.idUsuarioRegistro);
        });
    }

    public void setIdProducto(int id) {
        if (idProducto.getValue() == null) idProducto.setValue(id);
    }

    public LiveData<Producto> getProducto() { return producto; }
    public LiveData<String> getRegistradoPor() { return registradoPor; }
    public LiveData<Evento<Boolean>> getEliminado() { return eliminado; }

    public int getDiasAviso() {
        return Preferencias.diasAviso(getApplication());
    }

    /** Un producto de otro hogar se trata como inexistente. */
    public boolean perteneceAlHogar(Producto p) {
        return p != null && p.idHogar == idHogar;
    }

    public void consumir(Producto p, int cantidad) {
        productoRepo.registrarConsumo(p, cantidad, idUsuario);
    }

    public void desechar(Producto p, int cantidad, String motivo) {
        productoRepo.registrarDesecho(p, cantidad, motivo, idUsuario);
    }

    public void eliminar(Producto p, AccionEliminar accion, String motivo) {
        productoRepo.eliminar(p, accion, motivo, idUsuario, ok -> eliminado.setValue(new Evento<>(true)));
    }
}

package com.upn.ecoconsumo.ui.cuenta;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.entity.Usuario;
import com.upn.ecoconsumo.data.repository.UsuarioRepository;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.SessionManager;

/** Edición del nombre y cambio de contraseña. */
public class PerfilViewModel extends AndroidViewModel {

    private final UsuarioRepository repo;
    private final SessionManager session;
    private final int idUsuario;
    private final LiveData<Usuario> usuario;
    private final MutableLiveData<Evento<Boolean>> contrasenaCambiada = new MutableLiveData<>();

    public PerfilViewModel(@NonNull Application app) {
        super(app);
        repo = new UsuarioRepository(app);
        session = new SessionManager(app);
        idUsuario = session.getIdUsuario();
        usuario = repo.observar(idUsuario);
    }

    public LiveData<Usuario> getUsuario() { return usuario; }

    /** true si se cambió; false si la contraseña actual era incorrecta. */
    public LiveData<Evento<Boolean>> getContrasenaCambiada() { return contrasenaCambiada; }

    public void guardarNombre(String nombre) {
        repo.actualizarNombre(idUsuario, nombre);
        session.setNombre(nombre);
    }

    public void cambiarContrasena(String actual, String nueva) {
        repo.cambiarContrasena(idUsuario, actual, nueva, ok -> contrasenaCambiada.setValue(new Evento<>(ok)));
    }
}

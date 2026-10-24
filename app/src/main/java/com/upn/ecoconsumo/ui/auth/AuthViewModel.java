package com.upn.ecoconsumo.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.upn.ecoconsumo.data.repository.UsuarioRepository;
import com.upn.ecoconsumo.data.repository.UsuarioRepository.ResultadoRegistro;
import com.upn.ecoconsumo.util.Evento;
import com.upn.ecoconsumo.util.SessionManager;

/** Inicio de sesión (HU0008) y registro (HU0007). */
public class AuthViewModel extends AndroidViewModel {

    private final UsuarioRepository repo;
    private final SessionManager session;

    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>(false);
    private final MutableLiveData<Evento<Boolean>> resultadoLogin = new MutableLiveData<>();
    private final MutableLiveData<Evento<ResultadoRegistro>> resultadoRegistro = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new UsuarioRepository(app);
        session = new SessionManager(app);
    }

    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<Evento<Boolean>> getResultadoLogin() { return resultadoLogin; }
    public LiveData<Evento<ResultadoRegistro>> getResultadoRegistro() { return resultadoRegistro; }

    public boolean haySesion() {
        return session.haySesion();
    }

    public void iniciarSesion(String correo, String contrasena) {
        cargando.setValue(true);
        repo.autenticar(correo, contrasena, sesion -> {
            cargando.setValue(false);
            if (sesion != null) session.iniciar(sesion);
            resultadoLogin.setValue(new Evento<>(sesion != null));
        });
    }

    public void registrar(String nombre, String correo, String contrasena) {
        cargando.setValue(true);
        repo.registrar(nombre, correo, contrasena, resultado -> {
            cargando.setValue(false);
            resultadoRegistro.setValue(new Evento<>(resultado));
        });
    }
}

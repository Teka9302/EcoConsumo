package com.upn.ecoconsumo.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.upn.ecoconsumo.data.AppDatabase;
import com.upn.ecoconsumo.data.Tareas;
import com.upn.ecoconsumo.data.dao.UsuarioDao;
import com.upn.ecoconsumo.data.entity.MiembroHogar;
import com.upn.ecoconsumo.data.entity.Usuario;
import com.upn.ecoconsumo.data.model.SesionUsuario;
import com.upn.ecoconsumo.util.SeguridadUtils;

import java.util.Locale;

public class UsuarioRepository {

    public enum ResultadoRegistro { OK, CORREO_EXISTE }

    private final AppDatabase db;
    private final UsuarioDao usuarioDao;

    public UsuarioRepository(Context context) {
        db = AppDatabase.getInstance(context);
        usuarioDao = db.usuarioDao();
    }

    /** Crea la cuenta y un hogar propio donde el usuario es administrador (HU0007). */
    public void registrar(String nombre, String correo, String contrasena,
                         Tareas.Callback<ResultadoRegistro> callback) {
        Tareas.ejecutar(() -> {
            String hash = SeguridadUtils.hashContrasena(contrasena);
            return db.runInTransaction(() -> {
                if (usuarioDao.buscarPorCorreo(correo) != null) return ResultadoRegistro.CORREO_EXISTE;
                Usuario usuario = new Usuario();
                usuario.nombre = nombre;
                usuario.correo = correo.toLowerCase(Locale.ROOT);
                usuario.contrasena = hash;
                int idUsuario = (int) usuarioDao.insertar(usuario);
                HogarRepository.crearHogarPersonal(db, idUsuario, nombre);
                return ResultadoRegistro.OK;
            });
        }, callback);
    }

    /** Devuelve los datos de sesión si las credenciales son válidas, o null (HU0008). */
    public void autenticar(String correo, String contrasena, Tareas.Callback<SesionUsuario> callback) {
        Tareas.ejecutar(() -> {
            Usuario usuario = usuarioDao.buscarPorCorreo(correo);
            if (usuario == null || !SeguridadUtils.verificarContrasena(contrasena, usuario.contrasena)) {
                return null;
            }
            MiembroHogar membresia = db.hogarDao().obtenerMembresia(usuario.idUsuario);
            int idHogar;
            if (membresia != null) {
                idHogar = membresia.idHogar;
            } else {
                // Cuenta sin hogar (no debería ocurrir): se le crea uno propio
                Integer nuevo = db.runInTransaction(
                        () -> HogarRepository.crearHogarPersonal(db, usuario.idUsuario, usuario.nombre));
                idHogar = nuevo;
            }
            return new SesionUsuario(usuario.idUsuario, usuario.nombre, usuario.correo, idHogar);
        }, callback);
    }

    public LiveData<Usuario> observar(int idUsuario) {
        return usuarioDao.observar(idUsuario);
    }

    public LiveData<String> observarNombre(int idUsuario) {
        return usuarioDao.observarNombre(idUsuario);
    }

    public void actualizarNombre(int idUsuario, String nombre) {
        Tareas.ejecutar(() -> usuarioDao.actualizarNombre(idUsuario, nombre));
    }

    /** Devuelve false si la contraseña actual no es correcta. */
    public void cambiarContrasena(int idUsuario, String actual, String nueva, Tareas.Callback<Boolean> callback) {
        Tareas.ejecutar(() -> {
            Usuario usuario = usuarioDao.obtener(idUsuario);
            if (usuario == null || !SeguridadUtils.verificarContrasena(actual, usuario.contrasena)) {
                return false;
            }
            usuarioDao.actualizarContrasena(idUsuario, SeguridadUtils.hashContrasena(nueva));
            return true;
        }, callback);
    }
}

package com.upn.ecoconsumo.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.upn.ecoconsumo.data.entity.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    long insertar(Usuario usuario);

    @Query("SELECT * FROM usuario WHERE correo = :correo COLLATE NOCASE LIMIT 1")
    Usuario buscarPorCorreo(String correo);

    @Query("SELECT * FROM usuario WHERE id_usuario = :idUsuario")
    Usuario obtener(int idUsuario);

    @Query("SELECT * FROM usuario WHERE id_usuario = :idUsuario")
    LiveData<Usuario> observar(int idUsuario);

    @Query("SELECT nombre FROM usuario WHERE id_usuario = :idUsuario")
    LiveData<String> observarNombre(int idUsuario);

    @Query("UPDATE usuario SET nombre = :nombre WHERE id_usuario = :idUsuario")
    void actualizarNombre(int idUsuario, String nombre);

    @Query("UPDATE usuario SET contrasena = :hash WHERE id_usuario = :idUsuario")
    void actualizarContrasena(int idUsuario, String hash);
}

package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuario", indices = {@Index(value = "correo", unique = true)})
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    @NonNull
    public String nombre = "";

    @NonNull
    public String correo = "";

    /** Hash PBKDF2 con sal, nunca la contraseña en texto plano. */
    @NonNull
    public String contrasena = "";
}

package com.upn.ecoconsumo.data.model;

import androidx.room.ColumnInfo;

import com.upn.ecoconsumo.data.entity.MiembroHogar;

public class MiembroDetalle {
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    public String nombre;

    public String correo;

    public String rol;

    @ColumnInfo(name = "fecha_union")
    public long fechaUnion;

    public boolean esAdmin() {
        return MiembroHogar.ROL_ADMIN.equals(rol);
    }
}

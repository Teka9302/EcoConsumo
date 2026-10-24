package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Pertenencia de un usuario a un hogar. Cada usuario pertenece a un solo hogar a la vez. */
@Entity(tableName = "miembro_hogar",
        foreignKeys = {
                @ForeignKey(entity = Usuario.class, parentColumns = "id_usuario",
                        childColumns = "id_usuario", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Hogar.class, parentColumns = "id_hogar",
                        childColumns = "id_hogar", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("id_hogar")})
public class MiembroHogar {

    public static final String ROL_ADMIN = "ADMIN";
    public static final String ROL_MIEMBRO = "MIEMBRO";

    @PrimaryKey
    @ColumnInfo(name = "id_usuario")
    public int idUsuario;

    @ColumnInfo(name = "id_hogar")
    public int idHogar;

    @NonNull
    public String rol = ROL_MIEMBRO;

    @ColumnInfo(name = "fecha_union")
    public long fechaUnion;

    public boolean esAdmin() {
        return ROL_ADMIN.equals(rol);
    }
}

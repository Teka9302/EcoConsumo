package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Despensa compartida por los miembros de una familia. */
@Entity(tableName = "hogar", indices = {@Index(value = "codigo_invitacion", unique = true)})
public class Hogar {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_hogar")
    public int idHogar;

    @NonNull
    public String nombre = "";

    @NonNull
    @ColumnInfo(name = "codigo_invitacion")
    public String codigoInvitacion = "";
}

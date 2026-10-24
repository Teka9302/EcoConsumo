package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categoria",
        foreignKeys = @ForeignKey(entity = Hogar.class, parentColumns = "id_hogar",
                childColumns = "id_hogar", onDelete = ForeignKey.CASCADE),
        indices = {@Index("id_hogar")})
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_categoria")
    public int idCategoria;

    @NonNull
    public String nombre = "";

    /** null = categoría predefinida, visible para todos los hogares (HU0011). */
    @ColumnInfo(name = "id_hogar")
    public Integer idHogar;
}

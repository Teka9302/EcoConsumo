package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "lista_compra",
        foreignKeys = {
                @ForeignKey(entity = Producto.class, parentColumns = "id_producto",
                        childColumns = "id_producto", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Hogar.class, parentColumns = "id_hogar",
                        childColumns = "id_hogar", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("id_producto"), @Index("id_hogar")})
public class ItemCompra {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_item")
    public int idItem;

    @NonNull
    @ColumnInfo(name = "nombre_producto")
    public String nombreProducto = "";

    public String categoria;

    public int cantidad;

    public boolean comprado;

    /** null = artículo agregado a mano, sin producto asociado en la despensa. */
    @ColumnInfo(name = "id_producto")
    public Integer idProducto;

    @ColumnInfo(name = "id_hogar")
    public int idHogar;
}

package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.upn.ecoconsumo.util.FechaUtils;

@Entity(tableName = "producto",
        foreignKeys = {
                @ForeignKey(entity = Hogar.class, parentColumns = "id_hogar",
                        childColumns = "id_hogar", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Usuario.class, parentColumns = "id_usuario",
                        childColumns = "id_usuario_registro", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("id_hogar"), @Index("id_usuario_registro")})
public class Producto {

    public static final int ESTADO_VIGENTE = 0;
    public static final int ESTADO_POR_CADUCAR = 1;
    public static final int ESTADO_VENCIDO = 2;

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_producto")
    public int idProducto;

    @NonNull
    @ColumnInfo(name = "nombre_producto")
    public String nombreProducto = "";

    @NonNull
    public String categoria = "";

    public int cantidad;

    @ColumnInfo(name = "stock_minimo")
    public int stockMinimo;

    /** Fechas en formato ISO (yyyy-MM-dd) para poder ordenarlas y compararlas como texto. */
    @NonNull
    @ColumnInfo(name = "fecha_compra")
    public String fechaCompra = "";

    @NonNull
    @ColumnInfo(name = "fecha_vencimiento")
    public String fechaVencimiento = "";

    @ColumnInfo(name = "id_hogar")
    public int idHogar;

    @ColumnInfo(name = "id_usuario_registro")
    public Integer idUsuarioRegistro;

    public long diasParaVencer() {
        return FechaUtils.diasHasta(fechaVencimiento);
    }

    /** @param diasAviso días de anticipación configurados en Ajustes */
    public int estado(int diasAviso) {
        long dias = diasParaVencer();
        if (dias < 0) return ESTADO_VENCIDO;
        if (dias <= diasAviso) return ESTADO_POR_CADUCAR;
        return ESTADO_VIGENTE;
    }

    public boolean isStockBajo() {
        return cantidad <= stockMinimo;
    }
}

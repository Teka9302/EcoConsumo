package com.upn.ecoconsumo.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

/** Historial de compras, consumos y desechos; base de las estadísticas. */
@Entity(tableName = "movimiento",
        foreignKeys = {
                @ForeignKey(entity = Hogar.class, parentColumns = "id_hogar",
                        childColumns = "id_hogar", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Producto.class, parentColumns = "id_producto",
                        childColumns = "id_producto", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Usuario.class, parentColumns = "id_usuario",
                        childColumns = "id_usuario", onDelete = ForeignKey.SET_NULL)
        },
        indices = {@Index("id_hogar"), @Index("id_producto"), @Index("id_usuario"), @Index("fecha")})
public class Movimiento {

    public static final String TIPO_COMPRA = "COMPRA";
    public static final String TIPO_CONSUMO = "CONSUMO";
    public static final String TIPO_DESECHO = "DESECHO";

    public static final String MOTIVO_VENCIDO = "VENCIDO";
    public static final String MOTIVO_MAL_ESTADO = "MAL_ESTADO";
    public static final String MOTIVO_SOBRANTE = "SOBRANTE";
    public static final String MOTIVO_OTRO = "OTRO";

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_movimiento")
    public int idMovimiento;

    @ColumnInfo(name = "id_hogar")
    public int idHogar;

    @ColumnInfo(name = "id_producto")
    public Integer idProducto;

    @ColumnInfo(name = "id_usuario")
    public Integer idUsuario;

    /** Se copian nombre y categoría para conservar el historial aunque se elimine el producto. */
    @NonNull
    @ColumnInfo(name = "nombre_producto")
    public String nombreProducto = "";

    @NonNull
    public String categoria = "";

    @NonNull
    public String tipo = "";

    public int cantidad;

    /** Solo para desechos. */
    public String motivo;

    /** yyyy-MM-dd */
    @NonNull
    public String fecha = "";

    @ColumnInfo(name = "fecha_hora")
    public long fechaHora;

    public static Movimiento crear(Producto producto, String tipo, int cantidad, String motivo, Integer idUsuario) {
        Movimiento m = new Movimiento();
        m.idHogar = producto.idHogar;
        m.idProducto = producto.idProducto;
        m.idUsuario = idUsuario;
        m.nombreProducto = producto.nombreProducto;
        m.categoria = producto.categoria;
        m.tipo = tipo;
        m.cantidad = cantidad;
        m.motivo = motivo;
        m.fecha = LocalDate.now().toString();
        m.fechaHora = System.currentTimeMillis();
        return m;
    }
}

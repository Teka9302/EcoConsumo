package com.upn.ecoconsumo.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

import com.upn.ecoconsumo.data.entity.Movimiento;

public class MovimientoConUsuario {
    @Embedded
    public Movimiento movimiento;

    @ColumnInfo(name = "nombre_usuario")
    public String nombreUsuario;
}

package com.upn.ecoconsumo.ui;

import android.content.res.ColorStateList;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;

/** Textos y colores compartidos para estados de caducidad, tipos de movimiento y motivos de desecho. */
public final class EstadoUi {

    /** Motivos de desecho en el orden en que se muestran (ver R.array.motivos_desecho). */
    public static final String[] MOTIVOS = {
            Movimiento.MOTIVO_VENCIDO, Movimiento.MOTIVO_MAL_ESTADO,
            Movimiento.MOTIVO_SOBRANTE, Movimiento.MOTIVO_OTRO
    };

    private EstadoUi() {}

    /** Pinta la etiqueta de estado (Vigente / Por caducar / Vencido) con su color. */
    public static void aplicarEstado(TextView tv, Producto producto, int diasAviso) {
        int texto, color;
        switch (producto.estado(diasAviso)) {
            case Producto.ESTADO_VENCIDO:
                texto = R.string.estado_vencido;
                color = R.color.estado_vencido;
                break;
            case Producto.ESTADO_POR_CADUCAR:
                texto = R.string.estado_por_caducar;
                color = R.color.estado_por_caducar;
                break;
            default:
                texto = R.string.estado_vigente;
                color = R.color.estado_vigente;
        }
        tv.setText(texto);
        tv.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(tv.getContext(), color)));
    }

    @StringRes
    public static int textoTipo(String tipo) {
        if (Movimiento.TIPO_CONSUMO.equals(tipo)) return R.string.tipo_consumo;
        if (Movimiento.TIPO_DESECHO.equals(tipo)) return R.string.tipo_desecho;
        return R.string.tipo_compra;
    }

    @ColorRes
    public static int colorTipo(String tipo) {
        if (Movimiento.TIPO_CONSUMO.equals(tipo)) return R.color.estado_vigente;
        if (Movimiento.TIPO_DESECHO.equals(tipo)) return R.color.estado_vencido;
        return R.color.tipo_compra;
    }

    @StringRes
    public static int textoMotivo(String motivo) {
        if (Movimiento.MOTIVO_VENCIDO.equals(motivo)) return R.string.motivo_vencido;
        if (Movimiento.MOTIVO_MAL_ESTADO.equals(motivo)) return R.string.motivo_mal_estado;
        if (Movimiento.MOTIVO_SOBRANTE.equals(motivo)) return R.string.motivo_sobrante;
        return R.string.motivo_otro;
    }
}

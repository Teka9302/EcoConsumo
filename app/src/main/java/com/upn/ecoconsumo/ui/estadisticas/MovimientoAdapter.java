package com.upn.ecoconsumo.ui.estadisticas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.model.MovimientoConUsuario;
import com.upn.ecoconsumo.ui.EstadoUi;
import com.upn.ecoconsumo.util.FechaUtils;

public class MovimientoAdapter extends ListAdapter<MovimientoConUsuario, MovimientoAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<MovimientoConUsuario> DIFF =
            new DiffUtil.ItemCallback<MovimientoConUsuario>() {
                @Override
                public boolean areItemsTheSame(@NonNull MovimientoConUsuario a, @NonNull MovimientoConUsuario b) {
                    return a.movimiento.idMovimiento == b.movimiento.idMovimiento;
                }

                @Override
                public boolean areContentsTheSame(@NonNull MovimientoConUsuario a, @NonNull MovimientoConUsuario b) {
                    // Los movimientos no se editan
                    return true;
                }
            };

    public MovimientoAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MovimientoConUsuario item = getItem(position);
        Movimiento m = item.movimiento;
        Context ctx = h.itemView.getContext();
        int color = ContextCompat.getColor(ctx, EstadoUi.colorTipo(m.tipo));

        h.indicador.setBackgroundColor(color);
        h.tvTipo.setText(ctx.getString(R.string.movimiento_tipo_cantidad,
                ctx.getString(EstadoUi.textoTipo(m.tipo)), m.cantidad));
        h.tvTipo.setTextColor(color);
        h.tvProducto.setText(m.nombreProducto);

        StringBuilder detalle = new StringBuilder(FechaUtils.formatearFechaHora(m.fechaHora));
        if (item.nombreUsuario != null) detalle.append(" · ").append(item.nombreUsuario);
        if (Movimiento.TIPO_DESECHO.equals(m.tipo)) {
            detalle.append(" · ").append(ctx.getString(EstadoUi.textoMotivo(m.motivo)));
        }
        h.tvDetalle.setText(detalle);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View indicador;
        final TextView tvTipo, tvProducto, tvDetalle;

        ViewHolder(View v) {
            super(v);
            indicador = v.findViewById(R.id.indicador);
            tvTipo = v.findViewById(R.id.tvTipo);
            tvProducto = v.findViewById(R.id.tvProducto);
            tvDetalle = v.findViewById(R.id.tvDetalle);
        }
    }
}

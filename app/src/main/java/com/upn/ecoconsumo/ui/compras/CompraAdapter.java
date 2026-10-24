package com.upn.ecoconsumo.ui.compras;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.ItemCompra;

import java.util.Objects;

public class CompraAdapter extends ListAdapter<ItemCompra, CompraAdapter.ViewHolder> {

    public interface Listener {
        void onMarcado(ItemCompra item, boolean comprado);
        void onMantenerPulsado(ItemCompra item);
    }

    private static final DiffUtil.ItemCallback<ItemCompra> DIFF = new DiffUtil.ItemCallback<ItemCompra>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemCompra a, @NonNull ItemCompra b) {
            return a.idItem == b.idItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ItemCompra a, @NonNull ItemCompra b) {
            return a.comprado == b.comprado
                    && a.cantidad == b.cantidad
                    && a.nombreProducto.equals(b.nombreProducto)
                    && Objects.equals(a.categoria, b.categoria);
        }
    };

    private final Listener listener;

    public CompraAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compra, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ItemCompra item = getItem(position);
        Context ctx = h.itemView.getContext();
        h.tvNombre.setText(item.nombreProducto);
        String detalle = ctx.getString(R.string.cantidad_sugerida, item.cantidad);
        if (item.categoria != null) detalle = item.categoria + " · " + detalle;
        h.tvDetalle.setText(detalle);
        pintarTachado(h, item.comprado);

        h.cbComprado.setOnCheckedChangeListener(null);
        h.cbComprado.setChecked(item.comprado);
        h.cbComprado.setOnCheckedChangeListener((b, checked) -> {
            pintarTachado(h, checked);
            listener.onMarcado(item, checked);
        });
        h.itemView.setOnClickListener(v -> h.cbComprado.toggle());
        h.itemView.setOnLongClickListener(v -> {
            listener.onMantenerPulsado(item);
            return true;
        });
    }

    private static void pintarTachado(ViewHolder h, boolean comprado) {
        int flags = h.tvNombre.getPaintFlags();
        h.tvNombre.setPaintFlags(comprado ? flags | Paint.STRIKE_THRU_TEXT_FLAG : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
        h.itemView.setAlpha(comprado ? 0.6f : 1f);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbComprado;
        final TextView tvNombre, tvDetalle;

        ViewHolder(View v) {
            super(v);
            cbComprado = v.findViewById(R.id.cbComprado);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvDetalle = v.findViewById(R.id.tvDetalle);
        }
    }
}

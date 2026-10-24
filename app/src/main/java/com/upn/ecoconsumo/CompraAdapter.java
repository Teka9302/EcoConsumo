package com.upn.ecoconsumo;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CompraAdapter extends RecyclerView.Adapter<CompraAdapter.ViewHolder> {

    public interface OnItemMarcadoListener {
        void onItemMarcado(ItemCompra item, boolean comprado);
    }

    private final List<ItemCompra> items = new ArrayList<>();
    private final OnItemMarcadoListener listener;

    public CompraAdapter(OnItemMarcadoListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ItemCompra> nuevos) {
        items.clear();
        items.addAll(nuevos);
        notifyDataSetChanged();
    }

    public boolean hayComprados() {
        for (ItemCompra item : items) {
            if (item.isComprado()) return true;
        }
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compra, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        ItemCompra item = items.get(position);
        Context ctx = h.itemView.getContext();
        h.tvNombre.setText(item.getNombreProducto());
        String detalle = ctx.getString(R.string.cantidad_sugerida, item.getCantidad());
        if (item.getCategoria() != null) detalle = item.getCategoria() + " · " + detalle;
        h.tvDetalle.setText(detalle);
        pintarTachado(h, item.isComprado());

        h.cbComprado.setOnCheckedChangeListener(null);
        h.cbComprado.setChecked(item.isComprado());
        h.cbComprado.setOnCheckedChangeListener((b, checked) -> {
            item.setComprado(checked);
            pintarTachado(h, checked);
            listener.onItemMarcado(item, checked);
        });
        h.itemView.setOnClickListener(v -> h.cbComprado.toggle());
    }

    private static void pintarTachado(ViewHolder h, boolean comprado) {
        int flags = h.tvNombre.getPaintFlags();
        h.tvNombre.setPaintFlags(comprado ? flags | Paint.STRIKE_THRU_TEXT_FLAG : flags & ~Paint.STRIKE_THRU_TEXT_FLAG);
        h.itemView.setAlpha(comprado ? 0.6f : 1f);
    }

    @Override
    public int getItemCount() {
        return items.size();
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

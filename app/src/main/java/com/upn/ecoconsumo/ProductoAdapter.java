package com.upn.ecoconsumo;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ViewHolder> {

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    private final List<Producto> productos = new ArrayList<>();
    private final OnProductoClickListener listener;

    public ProductoAdapter(OnProductoClickListener listener) {
        this.listener = listener;
    }

    public void setProductos(List<Producto> nuevos) {
        productos.clear();
        productos.addAll(nuevos);
        notifyDataSetChanged();
    }

    /** Pinta la etiqueta de estado (Vigente / Por caducar / Vencido) con su color. */
    public static void aplicarEstado(TextView tv, Producto p) {
        Context ctx = tv.getContext();
        int texto, color;
        switch (p.getEstado()) {
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
        tv.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, color)));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = productos.get(position);
        Context ctx = h.itemView.getContext();
        h.tvNombre.setText(p.getNombreProducto());
        h.tvCategoria.setText(p.getCategoria());
        h.tvCantidad.setText(ctx.getString(R.string.cantidad_unidades, p.getCantidad()));
        h.tvStockBajo.setVisibility(p.isStockBajo() ? View.VISIBLE : View.GONE);
        h.tvFecha.setText(FechaUtils.textoVencimiento(p.getDiasParaVencer()));
        aplicarEstado(h.tvEstado, p);
        h.itemView.setOnClickListener(v -> listener.onProductoClick(p));
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvCategoria, tvCantidad, tvStockBajo, tvEstado, tvFecha;

        ViewHolder(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvCategoria = v.findViewById(R.id.tvCategoria);
            tvCantidad = v.findViewById(R.id.tvCantidad);
            tvStockBajo = v.findViewById(R.id.tvStockBajo);
            tvEstado = v.findViewById(R.id.tvEstado);
            tvFecha = v.findViewById(R.id.tvFecha);
        }
    }
}

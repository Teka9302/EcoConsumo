package com.upn.ecoconsumo.ui.despensa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.ui.EstadoUi;
import com.upn.ecoconsumo.util.FechaUtils;

public class ProductoAdapter extends ListAdapter<Producto, ProductoAdapter.ViewHolder> {

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    private static final DiffUtil.ItemCallback<Producto> DIFF = new DiffUtil.ItemCallback<Producto>() {
        @Override
        public boolean areItemsTheSame(@NonNull Producto a, @NonNull Producto b) {
            return a.idProducto == b.idProducto;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Producto a, @NonNull Producto b) {
            return a.nombreProducto.equals(b.nombreProducto)
                    && a.categoria.equals(b.categoria)
                    && a.cantidad == b.cantidad
                    && a.stockMinimo == b.stockMinimo
                    && a.fechaVencimiento.equals(b.fechaVencimiento);
        }
    };

    private final OnProductoClickListener listener;
    private int diasAviso;

    public ProductoAdapter(OnProductoClickListener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void setDiasAviso(int diasAviso) {
        if (this.diasAviso != diasAviso) {
            this.diasAviso = diasAviso;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Producto p = getItem(position);
        Context ctx = h.itemView.getContext();
        h.tvNombre.setText(p.nombreProducto);
        h.tvCategoria.setText(p.categoria);
        h.tvCantidad.setText(ctx.getString(R.string.cantidad_unidades, p.cantidad));
        h.tvStockBajo.setVisibility(p.isStockBajo() ? View.VISIBLE : View.GONE);
        h.tvFecha.setText(FechaUtils.textoVencimiento(p.diasParaVencer()));
        EstadoUi.aplicarEstado(h.tvEstado, p, diasAviso);
        h.itemView.setOnClickListener(v -> listener.onProductoClick(p));
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

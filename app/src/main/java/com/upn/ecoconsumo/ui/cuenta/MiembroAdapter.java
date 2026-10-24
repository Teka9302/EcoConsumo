package com.upn.ecoconsumo.ui.cuenta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.model.MiembroDetalle;

import java.util.Objects;

public class MiembroAdapter extends ListAdapter<MiembroDetalle, MiembroAdapter.ViewHolder> {

    public interface OnQuitarListener {
        void onQuitar(MiembroDetalle miembro);
    }

    private static final DiffUtil.ItemCallback<MiembroDetalle> DIFF = new DiffUtil.ItemCallback<MiembroDetalle>() {
        @Override
        public boolean areItemsTheSame(@NonNull MiembroDetalle a, @NonNull MiembroDetalle b) {
            return a.idUsuario == b.idUsuario;
        }

        @Override
        public boolean areContentsTheSame(@NonNull MiembroDetalle a, @NonNull MiembroDetalle b) {
            return Objects.equals(a.nombre, b.nombre) && Objects.equals(a.rol, b.rol);
        }
    };

    private final int idUsuarioActual;
    private final OnQuitarListener listener;
    private boolean puedeGestionar;

    public MiembroAdapter(int idUsuarioActual, OnQuitarListener listener) {
        super(DIFF);
        this.idUsuarioActual = idUsuarioActual;
        this.listener = listener;
    }

    /** Solo el administrador ve el botón para quitar miembros. */
    public void setPuedeGestionar(boolean puede) {
        if (puedeGestionar != puede) {
            puedeGestionar = puede;
            notifyItemRangeChanged(0, getItemCount());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_miembro, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MiembroDetalle m = getItem(position);
        boolean soyYo = m.idUsuario == idUsuarioActual;
        String nombre = soyYo ? h.itemView.getContext().getString(R.string.miembro_yo, m.nombre) : m.nombre;
        h.tvNombre.setText(nombre);
        h.tvCorreo.setText(m.correo);
        h.tvRol.setText(m.esAdmin() ? R.string.rol_admin : R.string.rol_miembro);
        h.btnQuitar.setVisibility(puedeGestionar && !soyYo ? View.VISIBLE : View.GONE);
        h.btnQuitar.setOnClickListener(v -> listener.onQuitar(m));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvNombre, tvCorreo, tvRol;
        final ImageButton btnQuitar;

        ViewHolder(View v) {
            super(v);
            tvNombre = v.findViewById(R.id.tvNombre);
            tvCorreo = v.findViewById(R.id.tvCorreo);
            tvRol = v.findViewById(R.id.tvRol);
            btnQuitar = v.findViewById(R.id.btnQuitar);
        }
    }
}

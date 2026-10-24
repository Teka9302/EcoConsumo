package com.upn.ecoconsumo.ui.cuenta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.ui.Dialogos;
import com.upn.ecoconsumo.ui.auth.LoginActivity;

/** Pantalla de Cuenta: perfil, hogar compartido, ajustes y cierre de sesión (HU0012). */
public class CuentaFragment extends Fragment {

    private CuentaViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CuentaViewModel.class);

        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvCorreo = view.findViewById(R.id.tvCorreo);
        TextView tvHogar = view.findViewById(R.id.tvHogar);
        TextView tvRol = view.findViewById(R.id.tvRol);
        TextView tvMiembros = view.findViewById(R.id.tvMiembros);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        TextView tvPorVencer = view.findViewById(R.id.tvPorVencer);

        viewModel.getUsuario().observe(getViewLifecycleOwner(), u -> {
            if (u == null) return;
            tvNombre.setText(u.nombre);
            tvCorreo.setText(u.correo);
        });
        viewModel.getHogar().observe(getViewLifecycleOwner(), h -> {
            if (h != null) tvHogar.setText(h.nombre);
        });
        viewModel.getMembresia().observe(getViewLifecycleOwner(), m -> {
            if (m != null) tvRol.setText(m.esAdmin() ? R.string.rol_admin : R.string.rol_miembro);
        });
        viewModel.getMiembros().observe(getViewLifecycleOwner(), lista ->
                tvMiembros.setText(getResources().getQuantityString(R.plurals.cantidad_miembros, lista.size(), lista.size())));
        viewModel.getTotalProductos().observe(getViewLifecycleOwner(), total ->
                tvTotal.setText(getString(R.string.total_productos, total)));
        viewModel.getTotalPorVencer().observe(getViewLifecycleOwner(), total ->
                tvPorVencer.setText(getString(R.string.productos_por_vencer, viewModel.getDiasAviso(), total)));

        view.findViewById(R.id.cardHogar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HogarActivity.class)));
        view.findViewById(R.id.opcionPerfil).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PerfilActivity.class)));
        view.findViewById(R.id.opcionAjustes).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AjustesActivity.class)));
        view.findViewById(R.id.btnCerrarSesion).setOnClickListener(v ->
                Dialogos.confirmar(requireContext(), R.string.btn_cerrar_sesion,
                        getString(R.string.confirmar_cerrar_sesion), R.string.btn_cerrar_sesion, this::cerrarSesion));
    }

    private void cerrarSesion() {
        viewModel.cerrarSesion();
        startActivity(new Intent(requireContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}

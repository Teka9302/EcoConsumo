package com.upn.ecoconsumo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

/** Pantalla de Cuenta con cierre de sesión (HU0012). */
public class CuentaFragment extends Fragment {

    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cuenta, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        ProductoDAO productoDAO = new ProductoDAO(requireContext());
        int idUsuario = session.getIdUsuario();

        ((TextView) view.findViewById(R.id.tvNombre)).setText(session.getNombre());
        ((TextView) view.findViewById(R.id.tvCorreo)).setText(session.getCorreo());
        ((TextView) view.findViewById(R.id.tvTotal)).setText(
                getString(R.string.total_productos, productoDAO.contar(idUsuario)));
        ((TextView) view.findViewById(R.id.tvPorVencer)).setText(getString(R.string.productos_por_vencer,
                Producto.DIAS_POR_CADUCAR, productoDAO.listarPorVencer(idUsuario).size()));

        view.findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> confirmarCierre());
    }

    private void confirmarCierre() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_cerrar_sesion)
                .setMessage(R.string.confirmar_cerrar_sesion)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.btn_cerrar_sesion, (d, w) -> cerrarSesion())
                .show();
    }

    private void cerrarSesion() {
        session.cerrar();
        WorkManager.getInstance(requireContext()).cancelUniqueWork(VencimientoWorker.NOMBRE);
        NotificationManagerCompat.from(requireContext()).cancelAll();

        startActivity(new Intent(requireContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}

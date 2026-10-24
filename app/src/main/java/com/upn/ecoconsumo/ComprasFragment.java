package com.upn.ecoconsumo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/** HU0006: lista de compras generada a partir del stock bajo o agotado. */
public class ComprasFragment extends Fragment {

    private CompraDAO compraDAO;
    private int idUsuario;
    private CompraAdapter adapter;
    private TextView tvVacio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compras, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        compraDAO = new CompraDAO(requireContext());
        idUsuario = new SessionManager(requireContext()).getIdUsuario();
        tvVacio = view.findViewById(R.id.tvVacio);

        RecyclerView rv = view.findViewById(R.id.rvCompras);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CompraAdapter((item, comprado) -> compraDAO.marcarComprado(item.getIdItem(), comprado));
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnGenerar).setOnClickListener(v -> {
            int agregados = compraDAO.generarSugerencias(idUsuario);
            Toast.makeText(requireContext(), agregados > 0
                    ? getString(R.string.msg_items_agregados, agregados)
                    : getString(R.string.msg_lista_al_dia), Toast.LENGTH_SHORT).show();
            cargar();
        });
        view.findViewById(R.id.btnFinalizar).setOnClickListener(v -> finalizarCompra());
    }

    @Override
    public void onResume() {
        super.onResume();
        compraDAO.generarSugerencias(idUsuario);
        cargar();
    }

    private void cargar() {
        List<ItemCompra> items = compraDAO.listar(idUsuario);
        adapter.setItems(items);
        tvVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void finalizarCompra() {
        if (!adapter.hayComprados()) {
            Toast.makeText(requireContext(), R.string.msg_sin_obtenidos, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.btn_finalizar_compra)
                .setMessage(R.string.confirmar_finalizar_msg)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.aceptar, (d, w) -> {
                    int procesados = compraDAO.finalizarCompra(idUsuario);
                    Toast.makeText(requireContext(), getString(R.string.msg_compra_guardada, procesados),
                            Toast.LENGTH_SHORT).show();
                    cargar();
                })
                .show();
    }
}

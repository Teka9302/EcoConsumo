package com.upn.ecoconsumo.ui.compras;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.ItemCompra;
import com.upn.ecoconsumo.ui.Dialogos;

/** HU0006: lista de compras generada a partir del stock bajo o agotado. */
public class ComprasFragment extends Fragment {

    private ComprasViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compras, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ComprasViewModel.class);
        TextView tvVacio = view.findViewById(R.id.tvVacio);

        RecyclerView rv = view.findViewById(R.id.rvCompras);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        CompraAdapter adapter = new CompraAdapter(new CompraAdapter.Listener() {
            @Override
            public void onMarcado(ItemCompra item, boolean comprado) {
                viewModel.marcarComprado(item, comprado);
            }

            @Override
            public void onMantenerPulsado(ItemCompra item) {
                Dialogos.confirmar(requireContext(), R.string.quitar_de_lista,
                        getString(R.string.confirmar_quitar_item, item.nombreProducto),
                        R.string.btn_eliminar, () -> viewModel.eliminar(item));
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnGenerar).setOnClickListener(v -> viewModel.generarSugerencias(true));
        view.findViewById(R.id.btnFinalizar).setOnClickListener(v -> finalizarCompra());
        view.findViewById(R.id.fabAgregarItem).setOnClickListener(v ->
                Dialogos.pedirTexto(requireContext(), R.string.agregar_articulo, R.string.hint_articulo,
                        null, false, nombre -> viewModel.agregarManual(nombre, 1)));

        viewModel.getItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            tvVacio.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
        viewModel.getSugerenciasAgregadas().observe(getViewLifecycleOwner(), evento -> {
            Integer agregados = evento.consumir();
            if (agregados == null) return;
            Toast.makeText(requireContext(), agregados > 0
                    ? getString(R.string.msg_items_agregados, agregados)
                    : getString(R.string.msg_lista_al_dia), Toast.LENGTH_SHORT).show();
        });
        viewModel.getCompraFinalizada().observe(getViewLifecycleOwner(), evento -> {
            Integer procesados = evento.consumir();
            if (procesados != null) {
                Toast.makeText(requireContext(), getString(R.string.msg_compra_guardada, procesados),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Detecta automáticamente los productos que se agotaron desde la última vez
        viewModel.generarSugerencias(false);
    }

    private void finalizarCompra() {
        if (!viewModel.hayComprados()) {
            Toast.makeText(requireContext(), R.string.msg_sin_obtenidos, Toast.LENGTH_SHORT).show();
            return;
        }
        Dialogos.confirmar(requireContext(), R.string.btn_finalizar_compra,
                getString(R.string.confirmar_finalizar_msg), R.string.aceptar, viewModel::finalizarCompra);
    }
}

package com.upn.ecoconsumo.ui.despensa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.dao.ProductoDao;
import com.upn.ecoconsumo.data.entity.Producto;

import java.util.ArrayList;
import java.util.List;

/** Vista principal de la despensa: HU0004, HU0009 y HU0010. */
public class DespensaFragment extends Fragment {

    private DespensaViewModel viewModel;
    private ProductoAdapter adapter;
    private Spinner spCategoria;
    private TextView tvVacio;
    private final List<String> categorias = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_despensa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DespensaViewModel.class);
        tvVacio = view.findViewById(R.id.tvVacio);
        spCategoria = view.findViewById(R.id.spCategoria);

        RecyclerView rv = view.findViewById(R.id.rvProductos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductoAdapter(p -> startActivity(new Intent(requireContext(), DetalleProductoActivity.class)
                .putExtra(DetalleProductoActivity.EXTRA_ID, p.idProducto)));
        adapter.setDiasAviso(viewModel.getFiltro().diasAviso);
        rv.setAdapter(adapter);

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setTexto(newText);
                return true;
            }
        });

        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viewModel.setCategoria(position == 0 ? null : categorias.get(position - 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.chipTodos : checkedIds.get(0);
            if (id == R.id.chipPorCaducar) {
                viewModel.setEstado(ProductoDao.FILTRO_POR_CADUCAR);
            } else if (id == R.id.chipEnStock) {
                viewModel.setEstado(ProductoDao.FILTRO_EN_STOCK);
            } else {
                viewModel.setEstado(ProductoDao.FILTRO_TODOS);
            }
        });

        view.findViewById(R.id.fabAgregar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProductoFormActivity.class)));

        viewModel.getCategorias().observe(getViewLifecycleOwner(), this::mostrarCategorias);
        viewModel.getProductos().observe(getViewLifecycleOwner(), this::mostrarProductos);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refrescarDiasAviso();
        adapter.setDiasAviso(viewModel.getFiltro().diasAviso);
    }

    private void mostrarCategorias(List<String> nombres) {
        categorias.clear();
        categorias.addAll(nombres);
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.todas_categorias));
        opciones.addAll(categorias);

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opciones);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(spAdapter);

        String seleccionada = viewModel.getFiltro().categoria;
        int indice = seleccionada == null ? -1 : categorias.indexOf(seleccionada);
        spCategoria.setSelection(indice + 1, false);
    }

    private void mostrarProductos(List<Producto> lista) {
        adapter.submitList(lista);
        if (!lista.isEmpty()) {
            tvVacio.setVisibility(View.GONE);
            return;
        }
        DespensaViewModel.Filtro f = viewModel.getFiltro();
        if (!f.texto.isEmpty()) {
            tvVacio.setText(getString(R.string.producto_no_existe, f.texto));
        } else if (!f.esVacio()) {
            tvVacio.setText(R.string.sin_resultados_filtro);
        } else {
            tvVacio.setText(R.string.despensa_vacia);
        }
        tvVacio.setVisibility(View.VISIBLE);
    }
}

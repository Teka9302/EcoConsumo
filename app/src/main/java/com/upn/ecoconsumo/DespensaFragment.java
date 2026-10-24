package com.upn.ecoconsumo;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

/** Vista principal de la despensa: HU0004, HU0009 y HU0010. */
public class DespensaFragment extends Fragment {

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private int idUsuario;

    private ProductoAdapter adapter;
    private Spinner spCategoria;
    private TextView tvVacio;

    private final List<String> categorias = new ArrayList<>();
    private String textoBusqueda = "";
    private String categoriaSeleccionada = null;
    private int filtro = ProductoDAO.FILTRO_TODOS;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_despensa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        productoDAO = new ProductoDAO(requireContext());
        categoriaDAO = new CategoriaDAO(requireContext());
        idUsuario = new SessionManager(requireContext()).getIdUsuario();

        tvVacio = view.findViewById(R.id.tvVacio);
        spCategoria = view.findViewById(R.id.spCategoria);

        RecyclerView rv = view.findViewById(R.id.rvProductos);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductoAdapter(p -> startActivity(new Intent(requireContext(), DetalleProductoActivity.class)
                .putExtra(DetalleProductoActivity.EXTRA_ID, p.getIdProducto())));
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
                textoBusqueda = newText;
                cargarProductos();
                return true;
            }
        });

        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                categoriaSeleccionada = position == 0 ? null : categorias.get(position - 1);
                cargarProductos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        ChipGroup chipGroup = view.findViewById(R.id.chipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int id = checkedIds.isEmpty() ? R.id.chipTodos : checkedIds.get(0);
            if (id == R.id.chipPorCaducar) {
                filtro = ProductoDAO.FILTRO_POR_CADUCAR;
            } else if (id == R.id.chipEnStock) {
                filtro = ProductoDAO.FILTRO_EN_STOCK;
            } else {
                filtro = ProductoDAO.FILTRO_TODOS;
            }
            cargarProductos();
        });

        view.findViewById(R.id.fabAgregar).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProductoFormActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Se recarga al volver del formulario o del detalle para ver los cambios al instante
        cargarCategorias();
        cargarProductos();
    }

    private void cargarCategorias() {
        categorias.clear();
        categorias.addAll(categoriaDAO.listar(idUsuario));
        List<String> opciones = new ArrayList<>();
        opciones.add(getString(R.string.todas_categorias));
        opciones.addAll(categorias);

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opciones);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(spAdapter);

        int indice = categoriaSeleccionada == null ? -1 : categorias.indexOf(categoriaSeleccionada);
        if (indice < 0) categoriaSeleccionada = null;
        spCategoria.setSelection(indice + 1, false);
    }

    private void cargarProductos() {
        List<Producto> lista = productoDAO.listar(idUsuario, textoBusqueda, categoriaSeleccionada, filtro);
        adapter.setProductos(lista);

        if (!lista.isEmpty()) {
            tvVacio.setVisibility(View.GONE);
            return;
        }
        String busqueda = textoBusqueda == null ? "" : textoBusqueda.trim();
        if (!busqueda.isEmpty()) {
            tvVacio.setText(getString(R.string.producto_no_existe, busqueda));
        } else if (filtro != ProductoDAO.FILTRO_TODOS || categoriaSeleccionada != null) {
            tvVacio.setText(R.string.sin_resultados_filtro);
        } else {
            tvVacio.setText(R.string.despensa_vacia);
        }
        tvVacio.setVisibility(View.VISIBLE);
    }
}

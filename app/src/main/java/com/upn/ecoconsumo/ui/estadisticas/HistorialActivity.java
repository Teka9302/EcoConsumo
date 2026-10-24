package com.upn.ecoconsumo.ui.estadisticas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Movimiento;

/** Historial completo de compras, consumos y desechos del hogar. */
public class HistorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HistorialViewModel viewModel = new ViewModelProvider(this).get(HistorialViewModel.class);
        TextView tvVacio = findViewById(R.id.tvVacio);

        RecyclerView rv = findViewById(R.id.rvHistorial);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        MovimientoAdapter adapter = new MovimientoAdapter();
        rv.setAdapter(adapter);

        ChipGroup chipTipo = findViewById(R.id.chipTipo);
        chipTipo.setOnCheckedStateChangeListener((group, ids) -> {
            int id = ids.isEmpty() ? R.id.chipTodos : ids.get(0);
            if (id == R.id.chipConsumos) viewModel.setTipo(Movimiento.TIPO_CONSUMO);
            else if (id == R.id.chipDesechos) viewModel.setTipo(Movimiento.TIPO_DESECHO);
            else if (id == R.id.chipCompras) viewModel.setTipo(Movimiento.TIPO_COMPRA);
            else viewModel.setTipo(null);
        });

        viewModel.getMovimientos().observe(this, lista -> {
            adapter.submitList(lista);
            tvVacio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

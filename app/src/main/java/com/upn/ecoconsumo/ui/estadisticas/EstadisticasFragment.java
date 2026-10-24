package com.upn.ecoconsumo.ui.estadisticas;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.model.TotalMensual;
import com.upn.ecoconsumo.data.model.TotalPorClave;
import com.upn.ecoconsumo.ui.EstadoUi;
import com.upn.ecoconsumo.util.FechaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Indicadores, gráfico mensual y rankings de desperdicio del hogar. */
public class EstadisticasFragment extends Fragment {

    private EstadisticasViewModel viewModel;
    private View contenedorDatos;
    private TextView tvSinDatos, tvConsejo;
    private TextView kpiComprado, kpiConsumido, kpiDesechado, kpiAprovechado;
    private BarChartView grafico;
    private LinearLayout listaCategorias, listaMotivos, listaMasDesechados;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(EstadisticasViewModel.class);

        contenedorDatos = view.findViewById(R.id.contenedorDatos);
        tvSinDatos = view.findViewById(R.id.tvSinDatos);
        tvConsejo = view.findViewById(R.id.tvConsejo);
        grafico = view.findViewById(R.id.graficoMensual);
        listaCategorias = view.findViewById(R.id.listaCategorias);
        listaMotivos = view.findViewById(R.id.listaMotivos);
        listaMasDesechados = view.findViewById(R.id.listaMasDesechados);
        kpiComprado = configurarKpi(view, R.id.kpiComprado, R.string.kpi_comprado);
        kpiConsumido = configurarKpi(view, R.id.kpiConsumido, R.string.kpi_consumido);
        kpiDesechado = configurarKpi(view, R.id.kpiDesechado, R.string.kpi_desechado);
        kpiAprovechado = configurarKpi(view, R.id.kpiAprovechado, R.string.kpi_aprovechado);

        ChipGroup chipPeriodo = view.findViewById(R.id.chipPeriodo);
        chipPeriodo.check(viewModel.getMeses() == 1 ? R.id.chipMes
                : viewModel.getMeses() == 6 ? R.id.chipSeisMeses : R.id.chipTresMeses);
        chipPeriodo.setOnCheckedStateChangeListener((group, ids) -> {
            int id = ids.isEmpty() ? R.id.chipTresMeses : ids.get(0);
            viewModel.setMeses(id == R.id.chipMes ? 1 : id == R.id.chipSeisMeses ? 6 : 3);
        });

        view.findViewById(R.id.btnHistorial).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HistorialActivity.class)));

        viewModel.getTotales().observe(getViewLifecycleOwner(), this::mostrarTotales);
        viewModel.getMensuales().observe(getViewLifecycleOwner(), this::mostrarGrafico);
        viewModel.getPorCategoria().observe(getViewLifecycleOwner(), datos ->
                llenarBarras(listaCategorias, datos, nombre -> nombre));
        viewModel.getPorMotivo().observe(getViewLifecycleOwner(), datos -> {
            llenarBarras(listaMotivos, datos, motivo -> getString(EstadoUi.textoMotivo(motivo)));
            mostrarConsejo(datos);
        });
        viewModel.getMasDesechados().observe(getViewLifecycleOwner(), datos ->
                llenarBarras(listaMasDesechados, datos, nombre -> nombre));
    }

    private TextView configurarKpi(View raiz, int idInclude, int etiqueta) {
        View kpi = raiz.findViewById(idInclude);
        ((TextView) kpi.findViewById(R.id.tvKpiEtiqueta)).setText(etiqueta);
        return kpi.findViewById(R.id.tvKpiValor);
    }

    private void mostrarTotales(List<TotalPorClave> totales) {
        int comprado = 0, consumido = 0, desechado = 0;
        for (TotalPorClave t : totales) {
            if (Movimiento.TIPO_COMPRA.equals(t.clave)) comprado = t.total;
            else if (Movimiento.TIPO_CONSUMO.equals(t.clave)) consumido = t.total;
            else if (Movimiento.TIPO_DESECHO.equals(t.clave)) desechado = t.total;
        }
        boolean hayDatos = !totales.isEmpty();
        tvSinDatos.setVisibility(hayDatos ? View.GONE : View.VISIBLE);
        contenedorDatos.setVisibility(hayDatos ? View.VISIBLE : View.GONE);

        kpiComprado.setText(String.valueOf(comprado));
        kpiConsumido.setText(String.valueOf(consumido));
        kpiDesechado.setText(String.valueOf(desechado));
        int salidas = consumido + desechado;
        kpiAprovechado.setText(salidas == 0 ? "—" : (consumido * 100 / salidas) + "%");
    }

    private void mostrarGrafico(List<TotalMensual> datos) {
        List<String> meses = FechaUtils.mesesDelPeriodo(viewModel.getMeses());
        int[] consumido = new int[meses.size()];
        int[] desechado = new int[meses.size()];
        for (TotalMensual t : datos) {
            int i = meses.indexOf(t.mes);
            if (i < 0) continue;
            if (Movimiento.TIPO_CONSUMO.equals(t.tipo)) consumido[i] = t.total;
            else if (Movimiento.TIPO_DESECHO.equals(t.tipo)) desechado[i] = t.total;
        }
        List<String> etiquetas = new ArrayList<>();
        for (String mes : meses) etiquetas.add(FechaUtils.nombreMesCorto(mes));
        grafico.setDatos(etiquetas, consumido, desechado);
    }

    /** Filas con barra de progreso proporcional al mayor valor. */
    private void llenarBarras(LinearLayout contenedor, List<TotalPorClave> datos, Function<String, String> etiqueta) {
        contenedor.removeAllViews();
        if (datos.isEmpty()) {
            TextView vacio = new TextView(requireContext());
            vacio.setText(R.string.sin_desechos_periodo);
            contenedor.addView(vacio);
            return;
        }
        int maximo = 1;
        for (TotalPorClave d : datos) maximo = Math.max(maximo, d.total);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int color = ContextCompat.getColor(requireContext(), R.color.estado_vencido);
        for (TotalPorClave d : datos) {
            View fila = inflater.inflate(R.layout.item_barra, contenedor, false);
            ((TextView) fila.findViewById(R.id.tvEtiqueta)).setText(etiqueta.apply(d.clave));
            ((TextView) fila.findViewById(R.id.tvValor)).setText(getString(R.string.cantidad_unidades, d.total));
            LinearProgressIndicator progreso = fila.findViewById(R.id.progreso);
            progreso.setMax(maximo);
            progreso.setProgress(d.total);
            progreso.setIndicatorColor(color);
            contenedor.addView(fila);
        }
    }

    /** Recomendación según el principal motivo de desperdicio. */
    private void mostrarConsejo(List<TotalPorClave> motivos) {
        if (motivos.isEmpty()) {
            tvConsejo.setText(R.string.consejo_sin_desperdicio);
            return;
        }
        String principal = motivos.get(0).clave;
        if (Movimiento.MOTIVO_VENCIDO.equals(principal)) {
            tvConsejo.setText(R.string.consejo_vencido);
        } else if (Movimiento.MOTIVO_SOBRANTE.equals(principal)) {
            tvConsejo.setText(R.string.consejo_sobrante);
        } else if (Movimiento.MOTIVO_MAL_ESTADO.equals(principal)) {
            tvConsejo.setText(R.string.consejo_mal_estado);
        } else {
            tvConsejo.setText(R.string.consejo_general);
        }
    }
}

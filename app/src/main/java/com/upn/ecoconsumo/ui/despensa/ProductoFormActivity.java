package com.upn.ecoconsumo.ui.despensa;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.notificaciones.VencimientoWorker;
import com.upn.ecoconsumo.ui.Dialogos;
import com.upn.ecoconsumo.util.FechaUtils;
import com.upn.ecoconsumo.util.ProductoValidator;
import com.upn.ecoconsumo.util.ProductoValidator.Campo;
import com.upn.ecoconsumo.util.ProductoValidator.ErrorValidacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Registro y edición de productos: HU0001, HU0002, HU0005 y HU0011. */
public class ProductoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_producto";

    private ProductoFormViewModel viewModel;
    private TextInputLayout tilNombre, tilCantidad, tilStockMinimo, tilFechaCompra, tilFechaVencimiento;
    private TextInputEditText etNombre, etCantidad, etStockMinimo, etFechaCompra, etFechaVencimiento;
    private Spinner spCategoria;
    private final List<String> categorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int idProducto = getIntent().getIntExtra(EXTRA_ID, -1);
        setTitle(idProducto == -1 ? R.string.titulo_nuevo_producto : R.string.titulo_editar_producto);

        tilNombre = findViewById(R.id.tilNombre);
        tilCantidad = findViewById(R.id.tilCantidad);
        tilStockMinimo = findViewById(R.id.tilStockMinimo);
        tilFechaCompra = findViewById(R.id.tilFechaCompra);
        tilFechaVencimiento = findViewById(R.id.tilFechaVencimiento);
        etNombre = findViewById(R.id.etNombre);
        etCantidad = findViewById(R.id.etCantidad);
        etStockMinimo = findViewById(R.id.etStockMinimo);
        etFechaCompra = findViewById(R.id.etFechaCompra);
        etFechaVencimiento = findViewById(R.id.etFechaVencimiento);
        spCategoria = findViewById(R.id.spCategoria);

        viewModel = new ViewModelProvider(this).get(ProductoFormViewModel.class);
        viewModel.inicializar(idProducto);
        mostrarFechas();

        spCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                viewModel.setCategoriaSeleccionada(categorias.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etFechaCompra.setOnClickListener(v -> elegirFecha(true));
        etFechaVencimiento.setOnClickListener(v -> elegirFecha(false));
        findViewById(R.id.btnNuevaCategoria).setOnClickListener(v ->
                Dialogos.pedirTexto(this, R.string.nueva_categoria, R.string.hint_nueva_categoria,
                        null, false, viewModel::agregarCategoria));
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());

        viewModel.getCategorias().observe(this, this::mostrarCategorias);
        viewModel.getProductoCargado().observe(this, evento -> {
            Producto p = evento.consumir();
            if (p == null) return;
            etNombre.setText(p.nombreProducto);
            etCantidad.setText(String.valueOf(p.cantidad));
            etStockMinimo.setText(String.valueOf(p.stockMinimo));
            mostrarFechas();
            mostrarCategorias(viewModel.getCategorias().getValue());
        });
        viewModel.getMensaje().observe(this, evento -> {
            Integer texto = evento.consumir();
            if (texto != null) Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
        });
        viewModel.getGuardado().observe(this, evento -> {
            Boolean ok = evento.consumir();
            if (ok == null) return;
            if (ok) VencimientoWorker.revisarAhora(this);
            finish();
        });
    }

    private void mostrarCategorias(List<String> nombres) {
        if (nombres == null) return;
        categorias.clear();
        categorias.addAll(nombres);
        String seleccionada = viewModel.getCategoriaSeleccionada();
        // Una categoría que ya no existe se conserva para no perderla al editar
        if (seleccionada != null && !categorias.contains(seleccionada)) categorias.add(seleccionada);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);
        if (seleccionada != null) spCategoria.setSelection(categorias.indexOf(seleccionada));
    }

    private void mostrarFechas() {
        etFechaCompra.setText(FechaUtils.formatear(viewModel.getFechaCompra()));
        etFechaVencimiento.setText(FechaUtils.formatear(viewModel.getFechaVencimiento()));
    }

    private void elegirFecha(boolean esCompra) {
        LocalDate inicial = FechaUtils.parse(esCompra ? viewModel.getFechaCompra() : viewModel.getFechaVencimiento());
        if (inicial == null) inicial = LocalDate.now();

        DatePickerDialog dialog = new DatePickerDialog(this, (picker, anio, mes, dia) -> {
            String iso = LocalDate.of(anio, mes + 1, dia).toString();
            if (esCompra) {
                viewModel.setFechaCompra(iso);
                tilFechaCompra.setError(null);
            } else {
                viewModel.setFechaVencimiento(iso);
                tilFechaVencimiento.setError(null);
            }
            mostrarFechas();
        }, inicial.getYear(), inicial.getMonthValue() - 1, inicial.getDayOfMonth());

        if (esCompra) dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void guardar() {
        String nombre = texto(etNombre);
        String cantidad = texto(etCantidad);
        String stockMinimo = texto(etStockMinimo);

        // HU0002: validación de fechas incorrectas o anteriores
        Map<Campo, ErrorValidacion> errores = ProductoValidator.validar(nombre, cantidad, stockMinimo,
                viewModel.getFechaCompra(), viewModel.getFechaVencimiento(),
                viewModel.getVencimientoOriginal(), LocalDate.now());

        mostrarError(tilNombre, errores.get(Campo.NOMBRE));
        mostrarError(tilCantidad, errores.get(Campo.CANTIDAD));
        mostrarError(tilStockMinimo, errores.get(Campo.STOCK_MINIMO));
        mostrarError(tilFechaCompra, errores.get(Campo.FECHA_COMPRA));
        mostrarError(tilFechaVencimiento, errores.get(Campo.FECHA_VENCIMIENTO));

        String categoria = (String) spCategoria.getSelectedItem();
        if (!errores.isEmpty() || categoria == null) return;

        viewModel.guardar(nombre, categoria,
                ProductoValidator.parseCantidad(cantidad), ProductoValidator.parseCantidad(stockMinimo));
    }

    private void mostrarError(TextInputLayout til, ErrorValidacion error) {
        if (error == null) {
            til.setError(null);
            return;
        }
        int texto;
        switch (error) {
            case NUMERO_INVALIDO: texto = R.string.err_cantidad; break;
            case COMPRA_FUTURA: texto = R.string.err_fecha_compra_futura; break;
            case VENCE_ANTES_DE_COMPRA: texto = R.string.err_vencimiento_antes_compra; break;
            case VENCIMIENTO_PASADO: texto = R.string.err_vencimiento_pasado; break;
            default: texto = R.string.err_campo_obligatorio;
        }
        til.setError(getString(texto));
    }

    private static String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

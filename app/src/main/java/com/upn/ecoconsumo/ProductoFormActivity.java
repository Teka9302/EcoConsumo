package com.upn.ecoconsumo;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.List;

/** Registro y edición de productos: HU0001, HU0002, HU0005 y HU0011. */
public class ProductoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_producto";

    private ProductoDAO productoDAO;
    private CategoriaDAO categoriaDAO;
    private int idUsuario;
    private Producto productoOriginal; // null = nuevo producto

    private TextInputLayout tilNombre, tilCantidad, tilStockMinimo, tilFechaCompra, tilFechaVencimiento;
    private TextInputEditText etNombre, etCantidad, etStockMinimo, etFechaCompra, etFechaVencimiento;
    private Spinner spCategoria;
    private List<String> categorias;

    // Fechas en formato ISO yyyy-MM-dd
    private String fechaCompra;
    private String fechaVencimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_form);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productoDAO = new ProductoDAO(this);
        categoriaDAO = new CategoriaDAO(this);
        idUsuario = new SessionManager(this).getIdUsuario();

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

        cargarCategorias(null);

        int idProducto = getIntent().getIntExtra(EXTRA_ID, -1);
        if (idProducto != -1) {
            productoOriginal = productoDAO.obtener(idProducto);
        }
        if (productoOriginal != null) {
            setTitle(R.string.titulo_editar_producto);
            etNombre.setText(productoOriginal.getNombreProducto());
            etCantidad.setText(String.valueOf(productoOriginal.getCantidad()));
            etStockMinimo.setText(String.valueOf(productoOriginal.getStockMinimo()));
            seleccionarCategoria(productoOriginal.getCategoria());
            fechaCompra = productoOriginal.getFechaCompra();
            fechaVencimiento = productoOriginal.getFechaVencimiento();
        } else {
            setTitle(R.string.titulo_nuevo_producto);
            fechaCompra = FechaUtils.hoy();
        }
        etFechaCompra.setText(FechaUtils.formatear(fechaCompra));
        etFechaVencimiento.setText(FechaUtils.formatear(fechaVencimiento));

        etFechaCompra.setOnClickListener(v -> elegirFecha(true));
        etFechaVencimiento.setOnClickListener(v -> elegirFecha(false));
        findViewById(R.id.btnNuevaCategoria).setOnClickListener(v -> dialogoNuevaCategoria());
        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardar());
    }

    private void cargarCategorias(String seleccion) {
        categorias = categoriaDAO.listar(idUsuario);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categorias);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapter);
        if (seleccion != null) seleccionarCategoria(seleccion);
    }

    private void seleccionarCategoria(String categoria) {
        int indice = categorias.indexOf(categoria);
        if (indice < 0) {
            // Categoría que ya no está en la lista: se agrega para no perderla
            categorias.add(categoria);
            ((ArrayAdapter<?>) spCategoria.getAdapter()).notifyDataSetChanged();
            indice = categorias.size() - 1;
        }
        spCategoria.setSelection(indice);
    }

    private void dialogoNuevaCategoria() {
        EditText input = new EditText(this);
        input.setHint(R.string.hint_nueva_categoria);
        input.setSingleLine(true);
        FrameLayout contenedor = new FrameLayout(this);
        int margen = (int) (20 * getResources().getDisplayMetrics().density);
        contenedor.setPadding(margen, margen / 2, margen, 0);
        contenedor.addView(input);

        new AlertDialog.Builder(this)
                .setTitle(R.string.nueva_categoria)
                .setView(contenedor)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.btn_guardar, (d, w) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, R.string.err_campo_obligatorio, Toast.LENGTH_SHORT).show();
                    } else if (categoriaDAO.existe(idUsuario, nombre)) {
                        Toast.makeText(this, R.string.err_categoria_existe, Toast.LENGTH_SHORT).show();
                    } else {
                        categoriaDAO.insertar(idUsuario, nombre);
                        cargarCategorias(nombre);
                    }
                })
                .show();
    }

    private void elegirFecha(boolean esCompra) {
        LocalDate inicial = FechaUtils.parse(esCompra ? fechaCompra : fechaVencimiento);
        if (inicial == null) inicial = LocalDate.now();

        DatePickerDialog dialog = new DatePickerDialog(this, (picker, anio, mes, dia) -> {
            String iso = LocalDate.of(anio, mes + 1, dia).toString();
            if (esCompra) {
                fechaCompra = iso;
                etFechaCompra.setText(FechaUtils.formatear(iso));
                tilFechaCompra.setError(null);
            } else {
                fechaVencimiento = iso;
                etFechaVencimiento.setText(FechaUtils.formatear(iso));
                tilFechaVencimiento.setError(null);
            }
        }, inicial.getYear(), inicial.getMonthValue() - 1, inicial.getDayOfMonth());

        if (esCompra) {
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        dialog.show();
    }

    private void guardar() {
        tilNombre.setError(null);
        tilCantidad.setError(null);
        tilStockMinimo.setError(null);
        tilFechaCompra.setError(null);
        tilFechaVencimiento.setError(null);

        boolean valido = true;
        String nombre = etNombre.getText() == null ? "" : etNombre.getText().toString().trim();
        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        }

        Integer cantidad = leerEntero(etCantidad);
        if (cantidad == null) {
            tilCantidad.setError(getString(R.string.err_cantidad));
            valido = false;
        }
        Integer stockMinimo = leerEntero(etStockMinimo);
        if (stockMinimo == null) {
            tilStockMinimo.setError(getString(R.string.err_cantidad));
            valido = false;
        }

        // HU0002: validación de fechas incorrectas o anteriores
        LocalDate hoy = LocalDate.now();
        LocalDate compra = FechaUtils.parse(fechaCompra);
        LocalDate vencimiento = FechaUtils.parse(fechaVencimiento);
        if (compra == null) {
            tilFechaCompra.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        } else if (compra.isAfter(hoy)) {
            tilFechaCompra.setError(getString(R.string.err_fecha_compra_futura));
            valido = false;
        }
        boolean vencimientoCambiado = productoOriginal == null
                || !productoOriginal.getFechaVencimiento().equals(fechaVencimiento);
        if (vencimiento == null) {
            tilFechaVencimiento.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        } else if (compra != null && vencimiento.isBefore(compra)) {
            tilFechaVencimiento.setError(getString(R.string.err_vencimiento_antes_compra));
            valido = false;
        } else if (vencimientoCambiado && vencimiento.isBefore(hoy)) {
            tilFechaVencimiento.setError(getString(R.string.err_vencimiento_pasado));
            valido = false;
        }

        String categoria = (String) spCategoria.getSelectedItem();
        if (categoria == null) valido = false;
        if (!valido) return;

        if (productoOriginal == null) {
            productoDAO.insertar(new Producto(0, nombre, categoria, cantidad, stockMinimo,
                    fechaCompra, fechaVencimiento, idUsuario));
            Toast.makeText(this, R.string.msg_producto_guardado, Toast.LENGTH_SHORT).show();
        } else {
            productoDAO.actualizar(new Producto(productoOriginal.getIdProducto(), nombre, categoria,
                    cantidad, stockMinimo, fechaCompra, fechaVencimiento, idUsuario));
            Toast.makeText(this, R.string.msg_producto_actualizado, Toast.LENGTH_SHORT).show();
        }
        VencimientoWorker.revisarAhora(this);
        finish();
    }

    private static Integer leerEntero(TextInputEditText et) {
        try {
            int valor = Integer.parseInt(et.getText() == null ? "" : et.getText().toString().trim());
            return valor >= 0 ? valor : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

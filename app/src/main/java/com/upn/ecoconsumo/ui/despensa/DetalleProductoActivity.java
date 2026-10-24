package com.upn.ecoconsumo.ui.despensa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Movimiento;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.repository.ProductoRepository.AccionEliminar;
import com.upn.ecoconsumo.notificaciones.NotificacionHelper;
import com.upn.ecoconsumo.ui.EstadoUi;
import com.upn.ecoconsumo.ui.auth.LoginActivity;
import com.upn.ecoconsumo.util.FechaUtils;
import com.upn.ecoconsumo.util.SessionManager;

/** Detalle del producto (destino de la notificación, HU0003) con consumo, desecho, edición y eliminación (HU0005). */
public class DetalleProductoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_producto";

    private DetalleViewModel viewModel;
    private Producto producto;
    private boolean eliminando;
    private boolean cerrando;

    private TextView tvNombre, tvCategoria, tvEstado, tvCantidad, tvStockBajo,
            tvFechaCompra, tvFechaVencimiento, tvRegistradoPor;
    private Button btnConsumir, btnDesechar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).haySesion()) {
            // Se puede llegar aquí desde una notificación después de cerrar sesión
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        setContentView(R.layout.activity_detalle_producto);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvNombre = findViewById(R.id.tvNombre);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvEstado = findViewById(R.id.tvEstado);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvStockBajo = findViewById(R.id.tvStockBajo);
        tvFechaCompra = findViewById(R.id.tvFechaCompra);
        tvFechaVencimiento = findViewById(R.id.tvFechaVencimiento);
        tvRegistradoPor = findViewById(R.id.tvRegistradoPor);
        btnConsumir = findViewById(R.id.btnConsumir);
        btnDesechar = findViewById(R.id.btnDesechar);

        viewModel = new ViewModelProvider(this).get(DetalleViewModel.class);
        viewModel.setIdProducto(getIntent().getIntExtra(EXTRA_ID, -1));

        btnConsumir.setOnClickListener(v -> dialogoSalida(false));
        btnDesechar.setOnClickListener(v -> dialogoSalida(true));
        findViewById(R.id.btnEditar).setOnClickListener(v ->
                startActivity(new Intent(this, ProductoFormActivity.class)
                        .putExtra(ProductoFormActivity.EXTRA_ID, producto.idProducto)));
        findViewById(R.id.btnEliminar).setOnClickListener(v -> dialogoEliminar());

        viewModel.getProducto().observe(this, this::mostrarProducto);
        viewModel.getRegistradoPor().observe(this, nombre -> {
            tvRegistradoPor.setVisibility(nombre == null ? View.GONE : View.VISIBLE);
            if (nombre != null) tvRegistradoPor.setText(getString(R.string.label_registrado_por, nombre));
        });
        viewModel.getEliminado().observe(this, evento -> {
            if (evento.consumir() != null) cerrar();
        });
    }

    /** Puede llamarse desde el evento de eliminación o al recibir el producto nulo; actúa una vez. */
    private void cerrar() {
        if (cerrando) return;
        cerrando = true;
        Toast.makeText(this, eliminando ? R.string.msg_producto_eliminado : R.string.msg_producto_no_encontrado,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void mostrarProducto(Producto p) {
        if (!viewModel.perteneceAlHogar(p)) {
            cerrar();
            return;
        }
        producto = p;
        tvNombre.setText(p.nombreProducto);
        tvCategoria.setText(p.categoria);
        EstadoUi.aplicarEstado(tvEstado, p, viewModel.getDiasAviso());
        tvEstado.append(" · " + FechaUtils.textoVencimiento(p.diasParaVencer()));
        tvCantidad.setText(getString(R.string.label_cantidad, p.cantidad, p.stockMinimo));
        tvStockBajo.setVisibility(p.isStockBajo() ? View.VISIBLE : View.GONE);
        tvFechaCompra.setText(getString(R.string.label_fecha_compra, FechaUtils.formatear(p.fechaCompra)));
        tvFechaVencimiento.setText(getString(R.string.label_fecha_vencimiento, FechaUtils.formatear(p.fechaVencimiento)));
        btnConsumir.setEnabled(p.cantidad > 0);
        btnDesechar.setEnabled(p.cantidad > 0);
    }

    /** Diálogo para registrar un consumo o un desecho de N unidades. */
    private void dialogoSalida(boolean esDesecho) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_movimiento, null);
        NumberPicker npCantidad = vista.findViewById(R.id.npCantidad);
        Spinner spMotivo = vista.findViewById(R.id.spMotivo);
        npCantidad.setMinValue(1);
        npCantidad.setMaxValue(Math.max(1, producto.cantidad));

        if (esDesecho) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.motivos_desecho, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spMotivo.setAdapter(adapter);
            // Si ya venció, se propone ese motivo
            spMotivo.setSelection(producto.estado(viewModel.getDiasAviso()) == Producto.ESTADO_VENCIDO ? 0 : 1);
        } else {
            vista.findViewById(R.id.grupoMotivo).setVisibility(View.GONE);
        }

        new AlertDialog.Builder(this)
                .setTitle(esDesecho ? R.string.btn_desechar : R.string.btn_consumir)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.aceptar, (d, w) -> {
                    int cantidad = npCantidad.getValue();
                    if (esDesecho) {
                        viewModel.desechar(producto, cantidad, EstadoUi.MOTIVOS[spMotivo.getSelectedItemPosition()]);
                    } else {
                        viewModel.consumir(producto, cantidad);
                    }
                })
                .show();
    }

    /** HU0005: confirmación previa indicando qué pasó con el producto, para el historial. */
    private void dialogoEliminar() {
        if (producto.cantidad == 0) {
            confirmarEliminar(AccionEliminar.REGISTRO_ERRONEO);
            return;
        }
        String[] opciones = {
                getString(R.string.eliminar_consumido),
                getString(R.string.eliminar_desechado),
                getString(R.string.eliminar_error)
        };
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirmar_eliminar_pregunta, producto.nombreProducto))
                .setItems(opciones, (d, cual) -> {
                    AccionEliminar accion = cual == 0 ? AccionEliminar.CONSUMIDO
                            : cual == 1 ? AccionEliminar.DESECHADO : AccionEliminar.REGISTRO_ERRONEO;
                    confirmarEliminar(accion);
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void confirmarEliminar(AccionEliminar accion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(getString(R.string.confirmar_eliminar_msg, producto.nombreProducto))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (d, w) -> {
                    eliminando = true;
                    String motivo = producto.estado(viewModel.getDiasAviso()) == Producto.ESTADO_VENCIDO
                            ? Movimiento.MOTIVO_VENCIDO : Movimiento.MOTIVO_MAL_ESTADO;
                    NotificacionHelper.cancelar(this, producto.idProducto);
                    viewModel.eliminar(producto, accion, motivo);
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package com.upn.ecoconsumo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/** Detalle del producto (destino de la notificación, HU0003) con edición y eliminación (HU0005). */
public class DetalleProductoActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_producto";

    private ProductoDAO productoDAO;
    private int idUsuario;
    private int idProducto;
    private Producto producto;

    private TextView tvNombre, tvCategoria, tvEstado, tvCantidad, tvStockBajo, tvFechaCompra, tvFechaVencimiento;
    private Button btnConsumir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager session = new SessionManager(this);
        if (!session.haySesion()) {
            // Se puede llegar aquí desde una notificación después de cerrar sesión
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        setContentView(R.layout.activity_detalle_producto);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productoDAO = new ProductoDAO(this);
        idUsuario = session.getIdUsuario();
        idProducto = getIntent().getIntExtra(EXTRA_ID, -1);

        tvNombre = findViewById(R.id.tvNombre);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvEstado = findViewById(R.id.tvEstado);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvStockBajo = findViewById(R.id.tvStockBajo);
        tvFechaCompra = findViewById(R.id.tvFechaCompra);
        tvFechaVencimiento = findViewById(R.id.tvFechaVencimiento);
        btnConsumir = findViewById(R.id.btnConsumir);

        btnConsumir.setOnClickListener(v -> {
            productoDAO.sumarCantidad(idProducto, -1);
            mostrarProducto();
        });
        findViewById(R.id.btnEditar).setOnClickListener(v ->
                startActivity(new Intent(this, ProductoFormActivity.class)
                        .putExtra(ProductoFormActivity.EXTRA_ID, idProducto)));
        findViewById(R.id.btnEliminar).setOnClickListener(v -> confirmarEliminar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productoDAO != null) mostrarProducto();
    }

    private void mostrarProducto() {
        producto = productoDAO.obtener(idProducto);
        if (producto == null || producto.getIdUsuario() != idUsuario) {
            Toast.makeText(this, R.string.msg_producto_no_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvNombre.setText(producto.getNombreProducto());
        tvCategoria.setText(producto.getCategoria());
        ProductoAdapter.aplicarEstado(tvEstado, producto);
        tvEstado.append(" · " + FechaUtils.textoVencimiento(producto.getDiasParaVencer()));
        tvCantidad.setText(getString(R.string.label_cantidad, producto.getCantidad(), producto.getStockMinimo()));
        tvStockBajo.setVisibility(producto.isStockBajo() ? View.VISIBLE : View.GONE);
        tvFechaCompra.setText(getString(R.string.label_fecha_compra, FechaUtils.formatear(producto.getFechaCompra())));
        tvFechaVencimiento.setText(getString(R.string.label_fecha_vencimiento,
                FechaUtils.formatear(producto.getFechaVencimiento())));
        btnConsumir.setEnabled(producto.getCantidad() > 0);
    }

    private void confirmarEliminar() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(getString(R.string.confirmar_eliminar_msg, producto.getNombreProducto()))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.btn_eliminar, (d, w) -> {
                    productoDAO.eliminar(idProducto);
                    NotificacionHelper.cancelar(this, idProducto);
                    Toast.makeText(this, R.string.msg_producto_eliminado, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

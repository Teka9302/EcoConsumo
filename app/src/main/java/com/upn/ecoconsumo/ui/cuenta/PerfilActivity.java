package com.upn.ecoconsumo.ui.cuenta;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.util.SeguridadUtils;

/** Editar perfil y cambiar contraseña. */
public class PerfilActivity extends AppCompatActivity {

    private PerfilViewModel viewModel;
    private TextInputLayout tilNombre, tilActual, tilNueva, tilConfirmar;
    private TextInputEditText etNombre, etCorreo, etActual, etNueva, etConfirmar;
    private boolean nombreCargado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = new ViewModelProvider(this).get(PerfilViewModel.class);
        nombreCargado = savedInstanceState != null;

        tilNombre = findViewById(R.id.tilNombre);
        tilActual = findViewById(R.id.tilActual);
        tilNueva = findViewById(R.id.tilNueva);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etActual = findViewById(R.id.etActual);
        etNueva = findViewById(R.id.etNueva);
        etConfirmar = findViewById(R.id.etConfirmar);

        viewModel.getUsuario().observe(this, u -> {
            if (u == null) return;
            etCorreo.setText(u.correo);
            // Solo la primera vez, para no pisar lo que el usuario está escribiendo
            if (!nombreCargado) {
                etNombre.setText(u.nombre);
                nombreCargado = true;
            }
        });
        viewModel.getContrasenaCambiada().observe(this, evento -> {
            Boolean ok = evento.consumir();
            if (ok == null) return;
            if (ok) {
                etActual.setText("");
                etNueva.setText("");
                etConfirmar.setText("");
                Toast.makeText(this, R.string.msg_contrasena_cambiada, Toast.LENGTH_SHORT).show();
            } else {
                tilActual.setError(getString(R.string.err_contrasena_actual));
            }
        });

        findViewById(R.id.btnGuardarNombre).setOnClickListener(v -> guardarNombre());
        findViewById(R.id.btnCambiarContrasena).setOnClickListener(v -> cambiarContrasena());
    }

    private void guardarNombre() {
        String nombre = texto(etNombre);
        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.err_campo_obligatorio));
            return;
        }
        tilNombre.setError(null);
        viewModel.guardarNombre(nombre);
        Toast.makeText(this, R.string.msg_perfil_actualizado, Toast.LENGTH_SHORT).show();
    }

    private void cambiarContrasena() {
        String actual = texto(etActual);
        String nueva = texto(etNueva);
        String confirmar = texto(etConfirmar);
        tilActual.setError(actual.isEmpty() ? getString(R.string.err_campo_obligatorio) : null);
        tilNueva.setError(SeguridadUtils.esContrasenaSegura(nueva) ? null : getString(R.string.err_contrasena_debil));
        tilConfirmar.setError(nueva.equals(confirmar) ? null : getString(R.string.err_contrasenas_distintas));
        if (tilActual.getError() == null && tilNueva.getError() == null && tilConfirmar.getError() == null) {
            viewModel.cambiarContrasena(actual, nueva);
        }
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

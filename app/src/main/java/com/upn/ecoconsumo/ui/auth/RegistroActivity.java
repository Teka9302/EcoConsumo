package com.upn.ecoconsumo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.repository.UsuarioRepository.ResultadoRegistro;
import com.upn.ecoconsumo.util.SeguridadUtils;

import java.util.Locale;

/** HU0007: registro de usuarios. */
public class RegistroActivity extends AppCompatActivity {

    public static final String EXTRA_CORREO = "correo";

    private AuthViewModel viewModel;
    private TextInputLayout tilNombre, tilCorreo, tilContrasena, tilConfirmar;
    private TextInputEditText etNombre, etCorreo, etContrasena, etConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmar = findViewById(R.id.etConfirmar);
        View btnRegistrar = findViewById(R.id.btnRegistrar);

        btnRegistrar.setOnClickListener(v -> registrar());
        viewModel.getCargando().observe(this, cargando -> btnRegistrar.setEnabled(!cargando));
        viewModel.getResultadoRegistro().observe(this, evento -> {
            ResultadoRegistro resultado = evento.consumir();
            if (resultado == null) return;
            if (resultado == ResultadoRegistro.CORREO_EXISTE) {
                tilCorreo.setError(getString(R.string.err_correo_registrado));
                etCorreo.requestFocus();
            } else {
                // Regresa al inicio de sesión con el correo ya escrito
                setResult(RESULT_OK, new Intent().putExtra(EXTRA_CORREO,
                        LoginActivity.texto(etCorreo).toLowerCase(Locale.ROOT)));
                finish();
            }
        });
    }

    private void registrar() {
        String nombre = LoginActivity.texto(etNombre);
        String correo = LoginActivity.texto(etCorreo);
        String contrasena = LoginActivity.texto(etContrasena);
        String confirmar = LoginActivity.texto(etConfirmar);

        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        tilConfirmar.setError(null);

        View primerError = null;
        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.err_campo_obligatorio));
            primerError = etNombre;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError(getString(R.string.err_campo_obligatorio));
            if (primerError == null) primerError = etCorreo;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.err_correo_invalido));
            if (primerError == null) primerError = etCorreo;
        }
        if (!SeguridadUtils.esContrasenaSegura(contrasena)) {
            tilContrasena.setError(getString(R.string.err_contrasena_debil));
            if (primerError == null) primerError = etContrasena;
        }
        if (!contrasena.equals(confirmar)) {
            tilConfirmar.setError(getString(R.string.err_contrasenas_distintas));
            if (primerError == null) primerError = etConfirmar;
        }

        if (primerError != null) {
            // Lleva el foco (y el scroll) al primer campo con error para que siempre sea visible
            primerError.requestFocus();
            return;
        }
        viewModel.registrar(nombre, correo, contrasena);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package com.upn.ecoconsumo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** HU0007: registro de usuarios. */
public class RegistroActivity extends AppCompatActivity {

    public static final String EXTRA_CORREO = "correo";

    private TextInputLayout tilNombre, tilCorreo, tilContrasena, tilConfirmar;
    private TextInputEditText etNombre, etCorreo, etContrasena, etConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tilNombre = findViewById(R.id.tilNombre);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etNombre = findViewById(R.id.etNombre);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);
        etConfirmar = findViewById(R.id.etConfirmar);

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrar());
    }

    private void registrar() {
        String nombre = texto(etNombre);
        String correo = texto(etCorreo);
        String contrasena = texto(etContrasena);
        String confirmar = texto(etConfirmar);

        tilNombre.setError(null);
        tilCorreo.setError(null);
        tilContrasena.setError(null);
        tilConfirmar.setError(null);

        boolean valido = true;
        if (nombre.isEmpty()) {
            tilNombre.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        }
        if (correo.isEmpty()) {
            tilCorreo.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.err_correo_invalido));
            valido = false;
        }
        if (!esContrasenaSegura(contrasena)) {
            tilContrasena.setError(getString(R.string.err_contrasena_debil));
            valido = false;
        }
        if (!contrasena.equals(confirmar)) {
            tilConfirmar.setError(getString(R.string.err_contrasenas_distintas));
            valido = false;
        }
        if (!valido) return;

        UsuarioDAO usuarioDAO = new UsuarioDAO(this);
        if (usuarioDAO.existeCorreo(correo)) {
            tilCorreo.setError(getString(R.string.err_correo_registrado));
            return;
        }
        usuarioDAO.registrar(nombre, correo, contrasena);

        // Regresa al inicio de sesión con el correo ya escrito
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_CORREO, correo.toLowerCase()));
        finish();
    }

    private static boolean esContrasenaSegura(String contrasena) {
        return contrasena.length() >= 6
                && contrasena.matches(".*[A-Za-z].*")
                && contrasena.matches(".*\\d.*");
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

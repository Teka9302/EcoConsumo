package com.upn.ecoconsumo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** HU0008: inicio de sesión. */
public class LoginActivity extends AppCompatActivity {

    private SessionManager session;
    private TextInputLayout tilCorreo, tilContrasena;
    private TextInputEditText etCorreo, etContrasena;

    private final ActivityResultLauncher<Intent> registroLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    etCorreo.setText(result.getData().getStringExtra(RegistroActivity.EXTRA_CORREO));
                    etContrasena.setText("");
                    etContrasena.requestFocus();
                    Toast.makeText(this, R.string.msg_registro_ok, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.haySesion()) {
            irAlInicio();
            return;
        }
        setContentView(R.layout.activity_login);

        tilCorreo = findViewById(R.id.tilCorreo);
        tilContrasena = findViewById(R.id.tilContrasena);
        etCorreo = findViewById(R.id.etCorreo);
        etContrasena = findViewById(R.id.etContrasena);

        findViewById(R.id.btnIngresar).setOnClickListener(v -> iniciarSesion());
        findViewById(R.id.tvIrRegistro).setOnClickListener(v ->
                registroLauncher.launch(new Intent(this, RegistroActivity.class)));
    }

    private void iniciarSesion() {
        String correo = texto(etCorreo);
        String contrasena = texto(etContrasena);
        tilCorreo.setError(null);
        tilContrasena.setError(null);

        boolean valido = true;
        if (correo.isEmpty()) {
            tilCorreo.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError(getString(R.string.err_correo_invalido));
            valido = false;
        }
        if (contrasena.isEmpty()) {
            tilContrasena.setError(getString(R.string.err_campo_obligatorio));
            valido = false;
        }
        if (!valido) return;

        Usuario usuario = new UsuarioDAO(this).autenticar(correo, contrasena);
        if (usuario == null) {
            tilContrasena.setError(getString(R.string.err_credenciales));
            return;
        }
        session.iniciar(usuario);
        irAlInicio();
    }

    private void irAlInicio() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private static String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}

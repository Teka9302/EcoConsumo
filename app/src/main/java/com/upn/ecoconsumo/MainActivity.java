package com.upn.ecoconsumo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> permisoNotificaciones = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) VencimientoWorker.revisarAhora(this);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).haySesion()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_compras) {
                mostrar(new ComprasFragment(), R.string.nav_compras);
            } else if (id == R.id.nav_cuenta) {
                mostrar(new CuentaFragment(), R.string.nav_cuenta);
            } else {
                mostrar(new DespensaFragment(), R.string.nav_despensa);
            }
            return true;
        });
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_despensa);
        } else {
            setTitle(bottomNav.getMenu().findItem(bottomNav.getSelectedItemId()).getTitle());
        }

        // HU0003: canal de notificaciones y revisión diaria de vencimientos
        NotificacionHelper.crearCanal(this);
        VencimientoWorker.programar(this);
        solicitarPermisoNotificaciones();
    }

    private void mostrar(Fragment fragment, int titulo) {
        setTitle(titulo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contenedor, fragment)
                .commit();
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }
}

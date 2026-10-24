package com.upn.ecoconsumo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.upn.ecoconsumo.notificaciones.NotificacionHelper;
import com.upn.ecoconsumo.notificaciones.VencimientoWorker;
import com.upn.ecoconsumo.ui.auth.LoginActivity;
import com.upn.ecoconsumo.ui.compras.ComprasFragment;
import com.upn.ecoconsumo.ui.cuenta.CuentaFragment;
import com.upn.ecoconsumo.ui.despensa.DespensaFragment;
import com.upn.ecoconsumo.ui.estadisticas.EstadisticasFragment;
import com.upn.ecoconsumo.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<String> permisoNotificaciones = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) VencimientoWorker.revisarAhora(this);
            });

    /** Reinicia la pantalla principal, por ejemplo al cambiar de hogar. */
    public static Intent reiniciar(Context context) {
        return new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

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
            } else if (id == R.id.nav_estadisticas) {
                mostrar(new EstadisticasFragment(), R.string.nav_estadisticas);
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

        // HU0003: revisión diaria de vencimientos a la hora configurada
        VencimientoWorker.programar(this, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificacionHelper.tienePermiso(this)) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void mostrar(Fragment fragment, int titulo) {
        setTitle(titulo);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.contenedor, fragment)
                .commit();
    }
}

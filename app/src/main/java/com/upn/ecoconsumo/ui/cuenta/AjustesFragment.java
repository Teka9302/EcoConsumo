package com.upn.ecoconsumo.ui.cuenta;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.notificaciones.VencimientoWorker;
import com.upn.ecoconsumo.util.FechaUtils;
import com.upn.ecoconsumo.util.Preferencias;
import com.upn.ecoconsumo.util.SessionManager;

/** Notificaciones, días de aviso, tema y exportación a CSV. */
public class AjustesFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final ActivityResultLauncher<String> exportarInventario = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> exportar(uri, true));

    private final ActivityResultLauncher<String> exportarHistorial = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> exportar(uri, false));

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferencias, rootKey);

        Preference inventario = findPreference(Preferencias.KEY_EXPORTAR_INVENTARIO);
        if (inventario != null) {
            inventario.setOnPreferenceClickListener(p -> {
                exportarInventario.launch("inventario_" + FechaUtils.hoy() + ".csv");
                return true;
            });
        }
        Preference historial = findPreference(Preferencias.KEY_EXPORTAR_HISTORIAL);
        if (historial != null) {
            historial.setOnPreferenceClickListener(p -> {
                exportarHistorial.launch("historial_" + FechaUtils.hoy() + ".csv");
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /** Se ejecuta después de guardar el valor, así que ya se puede leer la nueva configuración. */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, @Nullable String key) {
        if (Preferencias.KEY_TEMA.equals(key)) {
            Preferencias.aplicarTema(requireContext());
        } else if (Preferencias.KEY_NOTIFICACIONES.equals(key) || Preferencias.KEY_HORA_AVISO.equals(key)) {
            VencimientoWorker.programar(requireContext(), true);
        } else if (Preferencias.KEY_DIAS_AVISO.equals(key)) {
            VencimientoWorker.revisarAhora(requireContext());
        }
    }

    private void exportar(@Nullable Uri uri, boolean inventario) {
        if (uri == null) return; // El usuario canceló
        Context contexto = requireContext().getApplicationContext();
        ProductoRepository repo = new ProductoRepository(contexto);
        int idHogar = new SessionManager(contexto).getIdHogar();
        if (inventario) {
            repo.exportarInventario(contexto.getContentResolver(), uri, idHogar,
                    Preferencias.diasAviso(contexto), ok -> avisarExportacion(contexto, ok));
        } else {
            repo.exportarHistorial(contexto.getContentResolver(), uri, idHogar,
                    ok -> avisarExportacion(contexto, ok));
        }
    }

    private static void avisarExportacion(Context contexto, boolean ok) {
        Toast.makeText(contexto, ok ? R.string.msg_exportado : R.string.err_exportar, Toast.LENGTH_SHORT).show();
    }
}

package com.upn.ecoconsumo.ui.cuenta;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.ecoconsumo.MainActivity;
import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Hogar;
import com.upn.ecoconsumo.data.repository.HogarRepository.ResultadoUnion;
import com.upn.ecoconsumo.ui.Dialogos;

/** Gestión del hogar compartido: código de invitación, miembros y roles. */
public class HogarActivity extends AppCompatActivity {

    private HogarViewModel viewModel;
    private Hogar hogar;
    private int totalMiembros;

    private TextView tvNombreHogar, tvCodigo;
    private View btnRenombrar, btnRegenerar, btnSalir;
    private MiembroAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hogar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = new ViewModelProvider(this).get(HogarViewModel.class);

        tvNombreHogar = findViewById(R.id.tvNombreHogar);
        tvCodigo = findViewById(R.id.tvCodigo);
        btnRenombrar = findViewById(R.id.btnRenombrar);
        btnRegenerar = findViewById(R.id.btnRegenerar);
        btnSalir = findViewById(R.id.btnSalir);

        RecyclerView rv = findViewById(R.id.rvMiembros);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MiembroAdapter(viewModel.getIdUsuario(), miembro ->
                Dialogos.confirmar(this, R.string.quitar_miembro,
                        getString(R.string.confirmar_quitar_miembro, miembro.nombre),
                        R.string.btn_eliminar, () -> viewModel.expulsar(miembro)));
        rv.setAdapter(adapter);

        btnRenombrar.setOnClickListener(v -> Dialogos.pedirTexto(this, R.string.renombrar_hogar,
                R.string.hint_nombre_hogar, hogar == null ? null : hogar.nombre, false, viewModel::renombrar));
        btnRegenerar.setOnClickListener(v -> Dialogos.confirmar(this, R.string.regenerar_codigo,
                getString(R.string.confirmar_regenerar_codigo), R.string.aceptar, viewModel::regenerarCodigo));
        findViewById(R.id.btnCopiar).setOnClickListener(v -> copiarCodigo());
        findViewById(R.id.btnUnirse).setOnClickListener(v -> Dialogos.pedirTexto(this, R.string.unirse_hogar,
                R.string.hint_codigo, null, true, viewModel::unirse));
        btnSalir.setOnClickListener(v -> Dialogos.confirmar(this, R.string.salir_hogar,
                getString(R.string.confirmar_salir_hogar), R.string.salir_hogar, viewModel::salir));

        observar();
    }

    private void observar() {
        viewModel.getHogar().observe(this, h -> {
            if (h == null) return;
            hogar = h;
            tvNombreHogar.setText(h.nombre);
            tvCodigo.setText(h.codigoInvitacion);
        });
        viewModel.getMembresia().observe(this, m -> {
            boolean admin = m != null && m.esAdmin();
            btnRenombrar.setVisibility(admin ? View.VISIBLE : View.GONE);
            btnRegenerar.setVisibility(admin ? View.VISIBLE : View.GONE);
            adapter.setPuedeGestionar(admin);
        });
        viewModel.getMiembros().observe(this, lista -> {
            adapter.submitList(lista);
            totalMiembros = lista.size();
            // Con un solo miembro no hay de qué hogar salir
            btnSalir.setVisibility(totalMiembros > 1 ? View.VISIBLE : View.GONE);
        });

        viewModel.getResultadoUnion().observe(this, evento -> {
            ResultadoUnion r = evento.consumir();
            if (r == null) return;
            switch (r.estado) {
                case OK:
                    Toast.makeText(this, R.string.msg_unido_hogar, Toast.LENGTH_LONG).show();
                    startActivity(MainActivity.reiniciar(this));
                    break;
                case MISMO_HOGAR:
                    Toast.makeText(this, R.string.err_mismo_hogar, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, R.string.err_codigo_invalido, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getResultadoSalida().observe(this, evento -> {
            Boolean ok = evento.consumir();
            if (ok == null) return;
            if (ok) {
                Toast.makeText(this, R.string.msg_salio_hogar, Toast.LENGTH_LONG).show();
                startActivity(MainActivity.reiniciar(this));
            } else {
                Toast.makeText(this, R.string.err_unico_miembro, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.getResultadoExpulsion().observe(this, evento -> {
            Boolean ok = evento.consumir();
            if (ok != null && ok) Toast.makeText(this, R.string.msg_miembro_quitado, Toast.LENGTH_SHORT).show();
        });
    }

    private void copiarCodigo() {
        if (hogar == null) return;
        ClipboardManager clipboard = getSystemService(ClipboardManager.class);
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.codigo_invitacion), hogar.codigoInvitacion));
        Toast.makeText(this, R.string.msg_codigo_copiado, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package com.upn.ecoconsumo.ui;

import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.upn.ecoconsumo.R;

public final class Dialogos {

    public interface AlAceptarTexto {
        void alAceptar(String texto);
    }

    private Dialogos() {}

    /** Diálogo con un campo de texto; no permite aceptar si está vacío. */
    public static void pedirTexto(Context context, @StringRes int titulo, @StringRes int hint,
                                  String valorInicial, boolean mayusculas, AlAceptarTexto alAceptar) {
        View vista = LayoutInflater.from(context).inflate(R.layout.dialog_texto, null);
        TextInputLayout til = vista.findViewById(R.id.tilTexto);
        TextInputEditText et = vista.findViewById(R.id.etTexto);
        til.setHint(hint);
        if (mayusculas) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        }
        if (valorInicial != null) {
            et.setText(valorInicial);
            et.setSelection(valorInicial.length());
        }

        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.aceptar, (d, w) -> {
                    String texto = et.getText() == null ? "" : et.getText().toString().trim();
                    if (texto.isEmpty()) {
                        Toast.makeText(context, R.string.err_campo_obligatorio, Toast.LENGTH_SHORT).show();
                    } else {
                        alAceptar.alAceptar(texto);
                    }
                })
                .show();
    }

    public static void confirmar(Context context, @StringRes int titulo, CharSequence mensaje,
                                 @StringRes int textoAceptar, Runnable alAceptar) {
        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(textoAceptar, (d, w) -> alAceptar.run())
                .show();
    }
}

package com.upn.ecoconsumo;

import android.app.Application;

import com.upn.ecoconsumo.notificaciones.NotificacionHelper;
import com.upn.ecoconsumo.util.Preferencias;

public class EcoConsumoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Preferencias.aplicarTema(this);
        NotificacionHelper.crearCanal(this);
    }
}

package com.upn.ecoconsumo;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

/** Revisa en segundo plano los productos próximos a vencer y envía las alertas (HU0003). */
public class VencimientoWorker extends Worker {

    public static final String NOMBRE = "revision_vencimientos";

    public VencimientoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionManager session = new SessionManager(context);
        if (!session.haySesion()) return Result.success();

        for (Producto producto : new ProductoDAO(context).listarPorVencer(session.getIdUsuario())) {
            NotificacionHelper.notificarPorVencer(context, producto);
        }
        return Result.success();
    }

    /** Programa la revisión una vez al día; la primera ejecución es inmediata. */
    public static void programar(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                VencimientoWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOMBRE, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    /** Revisión puntual, por ejemplo tras registrar o editar un producto. */
    public static void revisarAhora(Context context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(VencimientoWorker.class));
    }
}

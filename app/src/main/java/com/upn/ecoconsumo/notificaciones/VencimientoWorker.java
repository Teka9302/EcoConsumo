package com.upn.ecoconsumo.notificaciones;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.data.repository.ProductoRepository;
import com.upn.ecoconsumo.util.Preferencias;
import com.upn.ecoconsumo.util.SessionManager;

import java.time.Duration;
import java.time.LocalDateTime;
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
        if (!session.haySesion() || !Preferencias.notificacionesActivas(context)) return Result.success();

        int dias = Preferencias.diasAviso(context);
        for (Producto producto : new ProductoRepository(context).listarPorVencer(session.getIdHogar(), dias)) {
            NotificacionHelper.notificarPorVencer(context, producto);
        }
        return Result.success();
    }

    /**
     * Programa la revisión diaria a la hora configurada en Ajustes.
     * @param reprogramar true cuando cambia la hora: reinicia el calendario
     */
    public static void programar(Context context, boolean reprogramar) {
        if (!Preferencias.notificacionesActivas(context)) {
            cancelar(context);
            return;
        }
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(VencimientoWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(minutosHastaLaHora(Preferencias.horaAviso(context)), TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NOMBRE,
                reprogramar ? ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE : ExistingPeriodicWorkPolicy.KEEP,
                request);
    }

    public static void cancelar(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(NOMBRE);
    }

    /** Revisión puntual, por ejemplo tras registrar o editar un producto. */
    public static void revisarAhora(Context context) {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequest.from(VencimientoWorker.class));
    }

    private static long minutosHastaLaHora(int hora) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime proxima = ahora.toLocalDate().atTime(hora, 0);
        if (!proxima.isAfter(ahora)) proxima = proxima.plusDays(1);
        return Duration.between(ahora, proxima).toMinutes();
    }
}

package com.upn.ecoconsumo.data;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Ejecuta operaciones de base de datos fuera del hilo principal y entrega el resultado en él. */
public final class Tareas {

    public interface Callback<T> {
        void alTerminar(T resultado);
    }

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    private static final Handler HILO_PRINCIPAL = new Handler(Looper.getMainLooper());

    private Tareas() {}

    public static void ejecutar(Runnable tarea) {
        EXECUTOR.execute(tarea);
    }

    public static <T> void ejecutar(Callable<T> tarea, Callback<T> callback) {
        EXECUTOR.execute(() -> {
            final T resultado = llamar(tarea);
            if (callback != null) HILO_PRINCIPAL.post(() -> callback.alTerminar(resultado));
        });
    }

    private static <T> T llamar(Callable<T> tarea) {
        try {
            return tarea.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

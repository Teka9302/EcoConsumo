package com.upn.ecoconsumo.notificaciones;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.upn.ecoconsumo.R;
import com.upn.ecoconsumo.data.entity.Producto;
import com.upn.ecoconsumo.ui.despensa.DetalleProductoActivity;
import com.upn.ecoconsumo.util.FechaUtils;

/** HU0003: notificaciones de productos próximos a vencer. */
public final class NotificacionHelper {

    public static final String CANAL_VENCIMIENTOS = "vencimientos";

    private NotificacionHelper() {}

    public static void crearCanal(Context context) {
        NotificationChannel canal = new NotificationChannel(CANAL_VENCIMIENTOS,
                context.getString(R.string.canal_vencimientos), NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(context.getString(R.string.canal_vencimientos_desc));
        context.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    public static boolean tienePermiso(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public static void notificarPorVencer(Context context, Producto producto) {
        if (!tienePermiso(context)) return;

        // Al tocar la alerta se abre el detalle del producto, con la despensa detrás
        Intent intent = new Intent(context, DetalleProductoActivity.class)
                .putExtra(DetalleProductoActivity.EXTRA_ID, producto.idProducto);
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intent)
                .getPendingIntent(producto.idProducto,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String texto = FechaUtils.textoVencimiento(producto.diasParaVencer()) + " · "
                + context.getString(R.string.cantidad_unidades, producto.cantidad);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_VENCIMIENTOS)
                .setSmallIcon(R.drawable.ic_notificacion)
                .setContentTitle(context.getString(R.string.notif_titulo, producto.nombreProducto))
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(producto.idProducto, builder.build());
    }

    public static void cancelar(Context context, int idProducto) {
        NotificationManagerCompat.from(context).cancel(idProducto);
    }

    public static void cancelarTodas(Context context) {
        NotificationManagerCompat.from(context).cancelAll();
    }
}

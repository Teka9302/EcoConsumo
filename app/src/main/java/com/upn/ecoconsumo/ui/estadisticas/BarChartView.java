package com.upn.ecoconsumo.ui.estadisticas;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.upn.ecoconsumo.R;

import java.util.ArrayList;
import java.util.List;

/** Gráfico de barras agrupadas (consumido vs desechado por mes) dibujado sin librerías externas. */
public class BarChartView extends View {

    private final Paint paintSerieA = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintSerieB = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintEtiqueta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintEje = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float densidad;

    private List<String> etiquetas = new ArrayList<>();
    private int[] serieA = new int[0];
    private int[] serieB = new int[0];

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        densidad = getResources().getDisplayMetrics().density;
        float escalaTexto = getResources().getDisplayMetrics().scaledDensity;

        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.textColorSecondary});
        int colorTexto = a.getColor(0, Color.GRAY);
        a.recycle();

        paintSerieA.setColor(ContextCompat.getColor(context, R.color.estado_vigente));
        paintSerieB.setColor(ContextCompat.getColor(context, R.color.estado_vencido));
        paintEtiqueta.setColor(colorTexto);
        paintEtiqueta.setTextSize(12 * escalaTexto);
        paintEtiqueta.setTextAlign(Paint.Align.CENTER);
        paintValor.setColor(colorTexto);
        paintValor.setTextSize(10 * escalaTexto);
        paintValor.setTextAlign(Paint.Align.CENTER);
        paintEje.setColor(colorTexto);
        paintEje.setStrokeWidth(densidad);
    }

    public void setDatos(List<String> etiquetas, int[] serieA, int[] serieB) {
        this.etiquetas = etiquetas;
        this.serieA = serieA;
        this.serieB = serieB;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int alto = resolveSize((int) (200 * densidad), heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), alto);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = etiquetas.size();
        float margen = 8 * densidad;
        float izquierda = getPaddingLeft() + margen;
        float derecha = getWidth() - getPaddingRight() - margen;
        float arriba = getPaddingTop() + paintValor.getTextSize() + margen;
        float abajo = getHeight() - getPaddingBottom() - paintEtiqueta.getTextSize() * 1.8f;

        canvas.drawLine(izquierda, abajo, derecha, abajo, paintEje);
        if (n == 0) return;

        int maximo = 1;
        for (int i = 0; i < n; i++) {
            maximo = Math.max(maximo, Math.max(valor(serieA, i), valor(serieB, i)));
        }

        float anchoGrupo = (derecha - izquierda) / n;
        float anchoBarra = Math.min(anchoGrupo * 0.3f, 28 * densidad);
        float separacion = 4 * densidad;
        float alturaUtil = abajo - arriba;
        float yEtiqueta = getHeight() - getPaddingBottom() - paintEtiqueta.getTextSize() * 0.4f;

        for (int i = 0; i < n; i++) {
            float centro = izquierda + anchoGrupo * (i + 0.5f);
            dibujarBarra(canvas, centro - separacion / 2 - anchoBarra, anchoBarra, valor(serieA, i),
                    maximo, abajo, alturaUtil, paintSerieA);
            dibujarBarra(canvas, centro + separacion / 2, anchoBarra, valor(serieB, i),
                    maximo, abajo, alturaUtil, paintSerieB);
            canvas.drawText(etiquetas.get(i), centro, yEtiqueta, paintEtiqueta);
        }
    }

    private void dibujarBarra(Canvas canvas, float x, float ancho, int valor, int maximo,
                              float base, float alturaUtil, Paint paint) {
        if (valor <= 0) return;
        float alto = alturaUtil * valor / maximo;
        rect.set(x, base - alto, x + ancho, base);
        canvas.drawRoundRect(rect, 4 * densidad, 4 * densidad, paint);
        canvas.drawText(String.valueOf(valor), x + ancho / 2, base - alto - 4 * densidad, paintValor);
    }

    private static int valor(int[] serie, int i) {
        return i < serie.length ? serie[i] : 0;
    }
}

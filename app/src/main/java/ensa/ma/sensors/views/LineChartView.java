package ensa.ma.sensors.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Vue graphique personnalisée qui trace une courbe en temps réel.
 * Affiche jusqu'à maxPoints valeurs, avec grille, axes et zone dégradée.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class LineChartView extends View {

    private final List<Float> values = new ArrayList<>();
    private final int maxPoints = 80;

    private final Paint axisPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint areaPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LineChartView(Context context) {
        super(context);

        axisPaint.setColor(Color.parseColor("#455A64"));
        axisPaint.setStrokeWidth(4f);

        linePaint.setColor(Color.parseColor("#2196F3"));
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setAlpha(80);

        textPaint.setColor(Color.parseColor("#263238"));
        textPaint.setTextSize(32f);

        areaPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Ajoute une valeur à la courbe et déclenche un redessin.
     *
     * @param value valeur reçue du capteur
     */
    public void addValue(float value) {
        if (Math.abs(value) > 1_000_000f) return;   // filtre les valeurs aberrantes

        if (values.size() >= maxPoints) {
            values.remove(0);
        }
        values.add(value);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        int pad = 60;

        // ── Grille horizontale ──────────────────────────────────────────────
        for (int i = 0; i <= 4; i++) {
            float y = pad + i * (h - 2f * pad) / 4f;
            canvas.drawLine(pad, y, w - pad, y, gridPaint);
        }

        // ── Axes ────────────────────────────────────────────────────────────
        canvas.drawLine(pad, h - pad, w - pad, h - pad, axisPaint); // X
        canvas.drawLine(pad, pad,     pad,     h - pad, axisPaint); // Y

        // ── Message d'attente ───────────────────────────────────────────────
        if (values.size() < 2) {
            canvas.drawText("En attente des données...", pad + 20, h / 2f, textPaint);
            return;
        }

        // ── Calcul min / max ────────────────────────────────────────────────
        float min = Float.MAX_VALUE;
        float max = -Float.MAX_VALUE;
        for (float v : values) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (max == min) max = min + 1f;

        // ── Construction de la courbe ────────────────────────────────────────
        Path linePath = new Path();
        Path areaPath = new Path();

        float stepX  = (w - 2f * pad) / (maxPoints - 1);
        float scaleY = (h - 2f * pad) / (max - min);

        for (int i = 0; i < values.size(); i++) {
            float x = pad + i * stepX;
            float y = h - pad - (values.get(i) - min) * scaleY;

            if (i == 0) {
                linePath.moveTo(x, y);
                areaPath.moveTo(x, h - pad);
                areaPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                areaPath.lineTo(x, y);
            }

            if (i == values.size() - 1) {
                areaPath.lineTo(x, h - pad);
                areaPath.close();
            }
        }

        // ── Dégradé de remplissage ───────────────────────────────────────────
        areaPaint.setShader(new LinearGradient(
                0, pad, 0, h - pad,
                Color.argb(90, 33, 150, 243),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawPath(areaPath, areaPaint);

        // ── Courbe principale ────────────────────────────────────────────────
        canvas.drawPath(linePath, linePaint);

        // ── Étiquette Min / Max ──────────────────────────────────────────────
        canvas.drawText(
                String.format("Min: %.2f  |  Max: %.2f", min, max),
                pad + 10, pad - 10, textPaint);
    }
}

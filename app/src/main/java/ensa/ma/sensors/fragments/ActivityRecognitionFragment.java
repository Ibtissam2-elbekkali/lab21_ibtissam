package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

/**
 * Reconnaissance simple d'activité basée sur l'accéléromètre.
 *
 * Principe :
 *  1. Un filtre passe-bas (constante ALPHA) estime la composante gravitationnelle.
 *  2. La gravité est retirée du signal pour obtenir l'accélération linéaire.
 *  3. Une fenêtre glissante de WINDOW_SIZE mesures calcule la moyenne et l'écart-type.
 *  4. Des seuils simples classifient l'activité.
 *
 * Activités détectées :
 *  - Saut           : pic d'accélération > 10 m/s²
 *  - Marche         : écart-type > 1.2 (variation régulière)
 *  - Stable à plat  : axe Z dominant > 8 m/s²
 *  - Assis / debout : axe X ou Y dominant > 7 m/s²
 *  - Stable         : tout le reste
 */
public class ActivityRecognitionFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        accelerometer;
    private TextView      resultView;

    // Filtre passe-bas
    private final float[] gravity      = new float[3];
    private static final float ALPHA   = 0.8f;       // coefficient du filtre (0 = réactif, 1 = lisse)

    // Fenêtre glissante
    private final Queue<Float> movementWindow = new LinkedList<>();
    private static final int WINDOW_SIZE      = 30;  // nombre de mesures conservées

    // ────────────────────────────────────────────────────────────────────────────
    // Cycle de vie
    // ────────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // ── Layout ───────────────────────────────────────────────────────────
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
        root.setBackgroundColor(Color.parseColor("#F5F5F7"));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        // Titre
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText("Reconnaissance d'activité");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#2196F3"));
        tvTitle.setPadding(0, 0, 0, 40);
        tvTitle.setGravity(Gravity.CENTER);
        root.addView(tvTitle);

        // Carte résultat
        CardView card = new CardView(requireContext());
        card.setRadius(24f);
        card.setCardElevation(10f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        resultView = new TextView(requireContext());
        resultView.setTextSize(17f);
        resultView.setPadding(48, 48, 48, 48);
        resultView.setTextColor(Color.parseColor("#37474F"));
        resultView.setLineSpacing(0, 1.5f);
        card.addView(resultView);
        root.addView(card);

        // Capteur
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        } else {
            resultView.setText("Accéléromètre indisponible sur ce dispositif.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // SensorEventListener
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // ── Filtre passe-bas : estimation de la gravité ──────────────────────
        // gravity[i] ← ALPHA * gravity[i] + (1 - ALPHA) * mesure[i]
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        // ── Accélération linéaire (sans gravité) ─────────────────────────────
        float linearX = x - gravity[0];
        float linearY = y - gravity[1];
        float linearZ = z - gravity[2];

        // ── Intensité du mouvement (norme de l'accélération linéaire) ────────
        float movement = (float) Math.sqrt(
                linearX * linearX + linearY * linearY + linearZ * linearZ);

        // Mise à jour de la fenêtre glissante
        if (movementWindow.size() >= WINDOW_SIZE) {
            movementWindow.poll();
        }
        movementWindow.add(movement);

        // Classification
        String activity = classifyActivity(x, y, z);

        resultView.setText(String.format(Locale.US,
                "X : %.2f\nY : %.2f\nZ : %.2f\n\nMouvement : %.2f\n\nActivité détectée :\n%s",
                x, y, z, movement, activity));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // ────────────────────────────────────────────────────────────────────────────
    // Classification de l'activité
    // ────────────────────────────────────────────────────────────────────────────

    private String classifyActivity(float x, float y, float z) {
        if (movementWindow.size() < WINDOW_SIZE) {
            return "Calibration...";
        }

        // ── Statistiques sur la fenêtre ──────────────────────────────────────
        float sum = 0f, max = 0f;
        for (float v : movementWindow) {
            sum += v;
            max = Math.max(max, v);
        }
        float mean = sum / movementWindow.size();

        float varSum = 0f;
        for (float v : movementWindow) {
            varSum += (v - mean) * (v - mean);
        }
        float stdDev = (float) Math.sqrt(varSum / movementWindow.size());

        // ── Règles de classification ─────────────────────────────────────────
        if (max > 10f) {
            return "Saut 🦘";
        }
        if (stdDev > 1.2f) {
            return "Marche 🚶";
        }
        if (Math.abs(z) > 8f) {
            return "Stable / téléphone à plat 📱";
        }
        if (Math.abs(y) > 7f || Math.abs(x) > 7f) {
            return "Assis ou debout (selon l'orientation) 🪑";
        }
        return "Position stable 🧍";
    }
}

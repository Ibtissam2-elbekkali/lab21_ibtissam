package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.Locale;

import ensa.ma.sensors.views.LineChartView;

/**
 * Fragment générique utilisé pour afficher un seul capteur scalaire (ou sa norme).
 * Réutilisé pour : Température, Humidité, Proximité, Champ magnétique.
 *
 * Mode "FIRST_VALUE" → affiche values[0].
 * Mode "MAGNITUDE"   → affiche sqrt(x²+y²+z²).
 *
 * Si le capteur est absent du dispositif, une simulation est activée automatiquement.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class SensorGraphFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE        = "title";
    private static final String ARG_MODE         = "mode";

    private SensorManager sensorManager;
    private Sensor        sensor;

    private TextView    valueView;
    private LineChartView chartView;

    private int    sensorType;
    private String title;
    private String mode;

    private final Handler simulationHandler = new Handler(Looper.getMainLooper());
    private float  simulationTime  = 0f;
    private boolean dataReceived   = false;

    // ────────────────────────────────────────────────────────────────────────────
    // Factory
    // ────────────────────────────────────────────────────────────────────────────

    public static SensorGraphFragment newInstance(int sensorType, String title, String mode) {
        SensorGraphFragment fragment = new SensorGraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Cycle de vie
    // ────────────────────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        sensorType = requireArguments().getInt(ARG_SENSOR_TYPE);
        title      = requireArguments().getString(ARG_TITLE);
        mode       = requireArguments().getString(ARG_MODE);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        // ── Layout racine ────────────────────────────────────────────────────
        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
        root.setBackgroundColor(Color.parseColor("#F5F5F7"));

        // Titre
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#2196F3"));
        tvTitle.setPadding(0, 0, 0, 32);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(tvTitle);

        // Carte valeur courante
        CardView card = new CardView(requireContext());
        card.setRadius(24f);
        card.setCardElevation(10f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        valueView = new TextView(requireContext());
        valueView.setText("En attente des données...");
        valueView.setTextSize(20f);
        valueView.setPadding(40, 40, 40, 40);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setTextColor(Color.parseColor("#1976D2"));
        valueView.setGravity(Gravity.CENTER);
        card.addView(valueView);
        root.addView(card);

        // Graphe
        chartView = new LineChartView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 700);
        lp.setMargins(0, 40, 0, 0);
        chartView.setLayoutParams(lp);
        root.addView(chartView);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        dataReceived = false;

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            // Si le capteur ne répond pas en 2 s (émulateur sans données), activer la simulation
            simulationHandler.postDelayed(() -> {
                if (!dataReceived) {
                    valueView.setText("Capteur indisponible. Simulation activée.");
                    startSimulation();
                }
            }, 2000);
        } else {
            valueView.setText("Capteur indisponible. Simulation activée.");
            startSimulation();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        simulationHandler.removeCallbacksAndMessages(null);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // SensorEventListener
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = extractValue(event.values);
        dataReceived = true;
        updateUi(value, false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // ────────────────────────────────────────────────────────────────────────────
    // Méthodes internes
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Extrait la valeur à afficher selon le mode choisi.
     * FIRST_VALUE → values[0]
     * MAGNITUDE   → sqrt(x² + y² + z²)
     */
    private float extractValue(float[] values) {
        if ("MAGNITUDE".equals(mode)) {
            return (float) Math.sqrt(
                    values[0] * values[0]
                            + values[1] * values[1]
                            + values[2] * values[2]);
        }
        return values[0];
    }

    private void updateUi(float value, boolean simulated) {
        String label = simulated ? " (Simulation)" : "";
        valueView.setText(String.format(Locale.US, "Valeur détectée : %.3f%s", value, label));
        chartView.addValue(value);
    }

    /**
     * Simulation adaptée au type de capteur, activée lorsque le matériel est absent.
     * Produit des valeurs réalistes pour chaque type.
     */
    private void startSimulation() {
        simulationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulationTime += 0.2f;

                float value;
                if (sensorType == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    // Température : ~24 °C ± 3 °C
                    value = 24f + (float) Math.sin(simulationTime / 5f) * 3f;
                } else if (sensorType == Sensor.TYPE_RELATIVE_HUMIDITY) {
                    // Humidité : ~55 % ± 15 %
                    value = 55f + (float) Math.sin(simulationTime / 7f) * 15f;
                } else if (sensorType == Sensor.TYPE_PROXIMITY) {
                    // Proximité : alterne 0 (proche) et 5 (loin)
                    value = ((int)(simulationTime) % 6) < 3 ? 0f : 5f;
                } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                    // Champ magnétique : ~45 µT ± 10 µT
                    value = 45f + (float) Math.sin(simulationTime / 4f) * 10f;
                } else {
                    value = (float) Math.sin(simulationTime) * 5f;
                }

                updateUi(value, true);
                simulationHandler.postDelayed(this, 300);
            }
        }, 300);
    }
}

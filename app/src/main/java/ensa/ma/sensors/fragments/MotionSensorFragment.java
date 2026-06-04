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

import java.util.Locale;

import ensa.ma.sensors.views.LineChartView;

/**
 * Fragment pour les capteurs de mouvement à 3 axes :
 *   - Accéléromètre (TYPE_ACCELEROMETER)
 *   - Gravité        (TYPE_GRAVITY)
 *   - Gyroscope      (TYPE_GYROSCOPE)
 *
 * Affiche X, Y, Z et la norme du vecteur.
 * La norme est tracée sur le graphe.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class MotionSensorFragment extends Fragment implements SensorEventListener {

    private static final String ARG_SENSOR_TYPE = "sensor_type";
    private static final String ARG_TITLE        = "title";

    private SensorManager sensorManager;
    private Sensor        sensor;

    private TextView     valuesView;
    private LineChartView chartView;

    // ────────────────────────────────────────────────────────────────────────────
    // Factory
    // ────────────────────────────────────────────────────────────────────────────

    public static MotionSensorFragment newInstance(int sensorType, String title) {
        MotionSensorFragment fragment = new MotionSensorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SENSOR_TYPE, sensorType);
        args.putString(ARG_TITLE, title);
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

        int sensorType = requireArguments().getInt(ARG_SENSOR_TYPE);
        String title   = requireArguments().getString(ARG_TITLE);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);

        // ── Layout ───────────────────────────────────────────────────────────
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

        // Carte des valeurs
        CardView card = new CardView(requireContext());
        card.setRadius(24f);
        card.setCardElevation(10f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        valuesView = new TextView(requireContext());
        valuesView.setTextSize(16f);
        valuesView.setPadding(40, 40, 40, 40);
        valuesView.setTextColor(Color.parseColor("#333333"));
        valuesView.setLineSpacing(0, 1.4f);
        valuesView.setText("Initialisation du capteur...");
        card.addView(valuesView);
        root.addView(card);

        // Graphe (norme)
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
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            valuesView.setText("Capteur indisponible sur ce dispositif.");
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

        // Norme du vecteur (intensité globale)
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);

        valuesView.setText(String.format(Locale.US,
                "X : %.4f\nY : %.4f\nZ : %.4f\n\nNorme : %.4f",
                x, y, z, magnitude));

        chartView.addValue(magnitude);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}

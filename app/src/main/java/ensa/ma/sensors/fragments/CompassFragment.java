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

/**
 * Boussole numérique construite à partir de deux capteurs :
 *   - Accéléromètre → donne l'orientation par rapport à la gravité.
 *   - Magnétomètre  → donne la direction du champ magnétique terrestre.
 *
 * SensorManager.getRotationMatrix() combine les deux pour calculer
 * la matrice de rotation, puis getOrientation() extrait l'azimut.
 */
public class CompassFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        accelerometer;
    private Sensor        magnetometer;

    private TextView textView;

    private final float[] gravityValues  = new float[3];
    private final float[] magneticValues = new float[3];

    private boolean hasGravity  = false;
    private boolean hasMagnetic = false;

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
        tvTitle.setText("Boussole numérique");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#2196F3"));
        tvTitle.setPadding(0, 0, 0, 48);
        tvTitle.setGravity(Gravity.CENTER);
        root.addView(tvTitle);

        // Carte résultat
        CardView card = new CardView(requireContext());
        card.setRadius(30f);
        card.setCardElevation(12f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        textView = new TextView(requireContext());
        textView.setTextSize(22f);
        textView.setPadding(60, 60, 60, 60);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#1976D2"));
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLineSpacing(0, 1.4f);
        textView.setText("Calibration...\n\nTournez le téléphone pour calibrer.");
        card.addView(textView);
        root.addView(card);

        // Capteurs
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (accelerometer == null || magnetometer == null) {
            textView.setText("Boussole indisponible : capteur manquant.");
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

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravityValues, 0, 3);
            hasGravity = true;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticValues, 0, 3);
            hasMagnetic = true;
        }

        // Les deux sources de données doivent être disponibles
        if (hasGravity && hasMagnetic) {
            float[] rotationMatrix = new float[9];
            float[] orientation    = new float[3];

            boolean success = SensorManager.getRotationMatrix(
                    rotationMatrix, null, gravityValues, magneticValues);

            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);

                // orientation[0] = azimut en radians → conversion en degrés [0°, 360°)
                float azimuthDegrees = (float) Math.toDegrees(orientation[0]);
                if (azimuthDegrees < 0f) azimuthDegrees += 360f;

                textView.setText(String.format(Locale.US,
                        "Direction : %.1f°\n\n%s",
                        azimuthDegrees,
                        getDirectionName(azimuthDegrees)));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    // ────────────────────────────────────────────────────────────────────────────
    // Direction cardinale
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Convertit un azimut (en degrés) en direction cardinale française.
     */
    private String getDirectionName(float degree) {
        if (degree >= 337.5f || degree < 22.5f)  return "Nord";
        if (degree < 67.5f)                        return "Nord-Est";
        if (degree < 112.5f)                       return "Est";
        if (degree < 157.5f)                       return "Sud-Est";
        if (degree < 202.5f)                       return "Sud";
        if (degree < 247.5f)                       return "Sud-Ouest";
        if (degree < 292.5f)                       return "Ouest";
        return "Nord-Ouest";
    }
}

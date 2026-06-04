package ensa.ma.sensors.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Locale;

/**
 * Affiche le nombre de pas :
 *  - Pas totaux depuis le dernier redémarrage (TYPE_STEP_COUNTER).
 *  - Pas de la session (depuis l'ouverture du fragment).
 *
 * Demande la permission ACTIVITY_RECOGNITION sur Android 10+.
 * Désenregistre le listener dans onPause() pour préserver la batterie.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class StepCounterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor        stepCounterSensor;
    private TextView      textView;

    private float initialSteps = -1f;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startSensor();
                        } else {
                            textView.setText("Permission ACTIVITY_RECOGNITION refusée.\nImpossible d'accéder au compteur de pas.");
                        }
                    });

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
        tvTitle.setText("Compteur de pas");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(Color.parseColor("#2196F3"));
        tvTitle.setPadding(0, 0, 0, 48);
        tvTitle.setGravity(Gravity.CENTER);
        root.addView(tvTitle);

        // Carte résultat
        CardView card = new CardView(requireContext());
        card.setRadius(24f);
        card.setCardElevation(10f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        textView = new TextView(requireContext());
        textView.setTextSize(20f);
        textView.setPadding(48, 48, 48, 48);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.parseColor("#1B5E20"));
        textView.setTypeface(null, Typeface.BOLD);
        textView.setLineSpacing(0, 1.4f);
        textView.setText("Initialisation...");
        card.addView(textView);
        root.addView(card);

        // Initialisation du capteur
        sensorManager      = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor  = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (stepCounterSensor == null) {
            textView.setText("Capteur de pas indisponible sur ce dispositif.");
            return;
        }

        // Android 10+ : permission ACTIVITY_RECOGNITION obligatoire
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        } else {
            startSensor();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Désenregistrement important : préserve la batterie
        sensorManager.unregisterListener(this);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Méthodes internes
    // ────────────────────────────────────────────────────────────────────────────

    private void startSensor() {
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // SensorEventListener
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void onSensorChanged(SensorEvent event) {
        float totalStepsSinceBoot = event.values[0];

        // Mémoriser la valeur initiale à la première réception
        if (initialSteps < 0f) {
            initialSteps = totalStepsSinceBoot;
        }

        int sessionSteps = (int) (totalStepsSinceBoot - initialSteps);

        textView.setText(String.format(Locale.US,
                "Pas depuis le dernier redémarrage :\n%d\n\nPas de la session :\n%d",
                (int) totalStepsSinceBoot, sessionSteps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}

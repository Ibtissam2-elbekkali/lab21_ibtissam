package ensa.ma.sensors.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
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

/**
 * Écran d'accueil de l'application Sensors.
 * Présente l'objectif du lab et la liste des fonctionnalités accessibles.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(48, 60, 48, 48);
        root.setBackgroundColor(Color.parseColor("#F5F5F7"));

        // ── Titre ────────────────────────────────────────────────────────────
        TextView tvTitle = new TextView(requireContext());
        tvTitle.setText("Lab Capteurs Embarqués");
        tvTitle.setTextSize(26f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextColor(Color.parseColor("#2196F3"));
        tvTitle.setPadding(0, 0, 0, 12);
        root.addView(tvTitle);

        // ── Sous-titre ───────────────────────────────────────────────────────
        TextView tvSub = new TextView(requireContext());
        tvSub.setText("Android SensorManager — Lab 21");
        tvSub.setTextSize(15f);
        tvSub.setGravity(Gravity.CENTER);
        tvSub.setTextColor(Color.parseColor("#757575"));
        tvSub.setPadding(0, 0, 0, 40);
        root.addView(tvSub);

        // ── Carte descriptive ────────────────────────────────────────────────
        CardView card = new CardView(requireContext());
        card.setRadius(20f);
        card.setCardElevation(8f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(Color.WHITE);

        TextView tvDesc = new TextView(requireContext());
        tvDesc.setTextSize(15f);
        tvDesc.setPadding(40, 40, 40, 40);
        tvDesc.setTextColor(Color.parseColor("#37474F"));
        tvDesc.setLineSpacing(0, 1.5f);
        tvDesc.setText(
                "Fonctionnalités disponibles dans le menu :\n\n"
                        + "📋  Capteurs — liste complète avec caractéristiques\n\n"
                        + "🌡  Température — graphe en temps réel\n\n"
                        + "💧  Humidité — graphe en temps réel\n\n"
                        + "📡  Proximité — détection d'objet proche\n\n"
                        + "🧲  Champ magnétique — norme B (µT)\n\n"
                        + "📱  Accéléromètre — axes X, Y, Z\n\n"
                        + "⬇  Gravité — composante gravitationnelle\n\n"
                        + "🔄  Gyroscope — rotation (rad/s)\n\n"
                        + "👟  Compteur de pas — session & total\n\n"
                        + "🧭  Boussole — direction cardinale\n\n"
                        + "🏃  Reconnaissance d'activité — marche, saut…"
        );
        card.addView(tvDesc);
        root.addView(card);

        // ── Signature de l'auteur ────────────────────────────────────────────
        TextView tvAuthor = new TextView(requireContext());
        tvAuthor.setText("Conçu et développé par Ibtissam");
        tvAuthor.setTextSize(14f);
        tvAuthor.setTypeface(null, Typeface.ITALIC);
        tvAuthor.setGravity(Gravity.CENTER);
        tvAuthor.setTextColor(Color.parseColor("#9E9E9E"));
        tvAuthor.setPadding(0, 40, 0, 0);
        root.addView(tvAuthor);

        return root;
    }
}

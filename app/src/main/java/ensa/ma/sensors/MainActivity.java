package ensa.ma.sensors;

import android.hardware.Sensor;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import ensa.ma.sensors.fragments.ActivityRecognitionFragment;
import ensa.ma.sensors.fragments.CompassFragment;
import ensa.ma.sensors.fragments.HomeFragment;
import ensa.ma.sensors.fragments.MotionSensorFragment;
import ensa.ma.sensors.fragments.SensorGraphFragment;
import ensa.ma.sensors.fragments.SensorsListFragment;
import ensa.ma.sensors.fragments.StepCounterFragment;

/**
 * Activité principale de l'application Sensors.
 *
 * Gère un DrawerLayout contenant un NavigationView.
 * Chaque entrée du menu ouvre le fragment correspondant
 * dans le FrameLayout fragment_container.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    // ────────────────────────────────────────────────────────────────────────────
    // Cycle de vie
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Écran d'accueil au démarrage
        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
            navView.setCheckedItem(R.id.nav_home);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Navigation
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // ── Accueil ──────────────────────────────────────────────────────────
        if (id == R.id.nav_home) {
            openFragment(new HomeFragment());
        }

        // ── Liste des capteurs ───────────────────────────────────────────────
        else if (id == R.id.menu_sensors) {
            openFragment(new SensorsListFragment());
        }

        // ── Capteurs scalaires avec graphe ───────────────────────────────────
        else if (id == R.id.menu_temperature) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_AMBIENT_TEMPERATURE,
                    "Température ambiante",
                    "FIRST_VALUE"));
        }
        else if (id == R.id.menu_humidity) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_RELATIVE_HUMIDITY,
                    "Humidité relative",
                    "FIRST_VALUE"));
        }
        else if (id == R.id.menu_proximity) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_PROXIMITY,
                    "Capteur de proximité",
                    "FIRST_VALUE"));
        }
        else if (id == R.id.menu_magnetic) {
            openFragment(SensorGraphFragment.newInstance(
                    Sensor.TYPE_MAGNETIC_FIELD,
                    "Champ magnétique",
                    "MAGNITUDE"));
        }

        // ── Capteurs de mouvement (3 axes) ───────────────────────────────────
        else if (id == R.id.menu_accelerometer) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_ACCELEROMETER,
                    "Accéléromètre : x, y, z"));
        }
        else if (id == R.id.menu_gravity) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_GRAVITY,
                    "Gravité : x, y, z"));
        }
        else if (id == R.id.menu_gyroscope) {
            openFragment(MotionSensorFragment.newInstance(
                    Sensor.TYPE_GYROSCOPE,
                    "Gyroscope : rad/s"));
        }

        // ── Compteur de pas ──────────────────────────────────────────────────
        else if (id == R.id.menu_steps) {
            openFragment(new StepCounterFragment());
        }

        // ── Boussole ─────────────────────────────────────────────────────────
        else if (id == R.id.menu_compass) {
            openFragment(new CompassFragment());
        }

        // ── Reconnaissance d'activité ────────────────────────────────────────
        else if (id == R.id.menu_activity) {
            openFragment(new ActivityRecognitionFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Utilitaire
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Remplace le fragment courant dans fragment_container par le fragment fourni.
     * Utilise une animation fade pour la transition.
     */
    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

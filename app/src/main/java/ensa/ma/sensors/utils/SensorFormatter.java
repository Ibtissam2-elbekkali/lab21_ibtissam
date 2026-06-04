package ensa.ma.sensors.utils;

import android.hardware.Sensor;

/**
 * Formatte les données techniques d'un capteur Android.
 * Les champs sont affichés dans l'ordre exigé par le TP.
 *
 * @author Ibtissam
 * @version 1.0
 */

public class SensorFormatter {

    public static String format(Sensor sensor) {
        if (sensor == null) return "Capteur inconnu";

        return "Id : " + sensor.getId() + "\n"
                + "Name : " + sensor.getName() + "\n"
                + "Vendor : " + sensor.getVendor() + "\n"
                + "Version : " + sensor.getVersion() + "\n"
                + "Type : " + sensor.getStringType() + "\n"
                + "Int Type : " + sensor.getType() + "\n"
                + "Resolution : " + sensor.getResolution() + "\n"
                + "Power : " + sensor.getPower() + " mA\n"
                + "Maximum Range : " + sensor.getMaximumRange() + "\n"
                + "Min Delay : " + sensor.getMinDelay() + " µs\n";
    }
}

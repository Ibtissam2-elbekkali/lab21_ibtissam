package ensa.ma.sensors.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ensa.ma.sensors.R;
import ensa.ma.sensors.utils.SensorFormatter;

import java.util.List;

/**
 * Fragment qui affiche la liste complète des capteurs disponibles sur l'appareil.
 * Pour chaque capteur, affiche :
 *   Id, Name, Vendor, Version, Type, Int Type, Resolution, Power, Maximum Range, Min Delay.
 *
 * @author Ibtissam
 * @version 1.0
 */
public class SensorsListFragment extends Fragment {

    private SensorManager sensorManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_listsonsors_list, container, false);
        root.setBackgroundColor(Color.parseColor("#F0F2F5"));

        RecyclerView recyclerView = root.findViewById(R.id.sensor_recycler_view);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorsList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new SensorAdapter(sensorsList));

        return root;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // RecyclerView Adapter
    // ────────────────────────────────────────────────────────────────────────────

    private class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

        private final List<Sensor> items;

        SensorAdapter(List<Sensor> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            CardView card = new CardView(parent.getContext());
            card.setRadius(20f);
            card.setCardElevation(6f);
            card.setUseCompatPadding(true);
            card.setCardBackgroundColor(Color.WHITE);

            TextView tv = new TextView(parent.getContext());
            tv.setPadding(40, 40, 40, 40);
            tv.setTextSize(13f);
            tv.setTextColor(Color.parseColor("#333333"));
            tv.setLineSpacing(0, 1.3f);
            tv.setTypeface(Typeface.MONOSPACE);

            card.addView(tv);
            return new SensorViewHolder(card, tv);
        }

        @Override
        public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
            Sensor s = items.get(position);
            holder.textView.setText(SensorFormatter.format(s));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class SensorViewHolder extends RecyclerView.ViewHolder {
            final TextView textView;

            SensorViewHolder(View itemView, TextView tv) {
                super(itemView);
                this.textView = tv;
            }
        }
    }
}

package com.blukii.androidblukiitutorial;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Arnold on 24.02.14.
 */
public class ListViewBlukiiAdapter extends ArrayAdapter<String> {

    private List<String> items;
    private Context context;

    public ListViewBlukiiAdapter(Context context, int textViewResourceId, List<String> items) {

        super(context, textViewResourceId, items);
        this.items = items;
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String connectedDevice = sp.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");

        View v = convertView;

        // lade ein Layout, falls dies nötig ist
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) context.getApplicationContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listview_blukii_item, null);
        }

        String item = items.get(position);
        TextView tvAddress = (TextView) v.findViewById(R.id.device_address);

        if (tvAddress != null) {
            tvAddress.setText(item);
        }

        ImageView isConnectedImageView = (ImageView) v.findViewById(R.id.device_connection_status);

        if (connectedDevice.equals(item)) {
            isConnectedImageView.setVisibility(ImageView.VISIBLE);
            isConnectedImageView.setImageResource(R.drawable.ic_action_accept);
        } else {
            isConnectedImageView.setVisibility(ImageView.GONE);
        }
        return v;
    }

    public void addBlukii(String blukii) {
        items.add(blukii);
    }

}

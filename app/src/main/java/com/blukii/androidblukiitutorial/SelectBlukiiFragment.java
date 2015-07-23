package com.blukii.androidblukiitutorial;

import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiManagerService;
import com.blukii.android.blukiilibrary.Profile;

import java.util.ArrayList;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Use the {@link SelectBlukiiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectBlukiiFragment extends ListFragment implements View.OnClickListener {

    public static final String PREF_SELECTED_BLUKII = "pref_key_selectedBlukii";
    private final static String TAG = SelectBlukiiFragment.class.getSimpleName();

    /**
     * A broadcast receiver to receive updates on blukiis
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // erstmal die Action holen
            final String action = intent.getAction();

            // ein neuer Blukii wurde discovered
            if (Blukii.ACTION_DID_DISCOVER_DEVICE.equals(action)) {
                // device holen
                final BluetoothDevice device = intent.getParcelableExtra(Blukii.EXTRA_BLUKII_DEVICE);
                Log.d(TAG, "received device discovered: " + device.getAddress());

                // device dem Adapter hinzufügen
                adapter.addBlukii(device.getAddress());
                // adpater mitteilen, dass es eine Änderung gab
                adapter.notifyDataSetChanged();

                // wurde die App früher schon mal verwendet uns es damals wurde ein device gewählt?
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String previousDevice = sp.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");

                // wenn das gerade discovered device dem früher alsgewählten entspricht,
                // dann kann der Verbinden-Button enabled werden
                if (previousDevice.equals(device.getAddress())) {
                    getView().findViewById(R.id.btn_blukii_connect).setEnabled(true);
                }
                // ein Blukki hat sich verbunden
            } else if (Blukii.ACTION_DID_CONNECT_DEVICE.equals(action)) {
                String address = intent.getStringExtra(Blukii.EXTRA_BLUKII_DEVICE_ADDRESS);
                Log.d(TAG, "received broadcast: GATT connected to " + address);
                // jetzt alle verfügbaren Services laden

                // die Verbindung zum Blukii wurde getrennt
            } else if (Blukii.ACTION_DID_DISCONNECT_DEVICE.equals(action)) {
                // Benutzer informieren
                Log.d(TAG, "Verbindung getrennt");
                //updateStatus("Getrennt");
                updateConnectionStatus(getText(R.string.blukii_disconnected).toString());

                // Buttons sinnvoll enabled und disablen
                getView().findViewById(R.id.btn_blukii_disconnect).setEnabled(false);
                getView().findViewById(R.id.btn_blukii_connect).setEnabled(true);
                getView().findViewById(R.id.btn_blukii_discover).setEnabled(true);

                //Log.d(TAG, "Versuche Wiederverbindung");
                //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                //String connectedDevice = sp.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");
                //BlukiiManager.getInstance().connect(connectedDevice);
                // Blukii ist bereit, also es wurde eine Verbindung hergestellt und die Services wurden geladen
            } else if (Blukii.ACTION_BLUKII_DEVICE_IS_READY.equals(action)) {
                // Benutzer informieren
                //updateStatus("Verbunden");
                updateConnectionStatus(getText(R.string.blukii_connected).toString());

                getView().findViewById(R.id.btn_blukii_connect).setEnabled(false);
                getView().findViewById(R.id.btn_blukii_discover).setEnabled(false);
                // es gab einen Fehler beim Laden der Services
            } else if (Blukii.ACTION_ERROR_LOADING_SERVICES.equals(action)) {
                // Benutzer informieren
                //updateStatus("Getrennt");
                updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                getView().findViewById(R.id.btn_blukii_disconnect).setEnabled(false);
            }
        }
    };
    private ListViewBlukiiAdapter adapter;
    private BlukiiManagerService mService;

    public SelectBlukiiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectBlukiiFragment.
     */
    public static SelectBlukiiFragment newInstance() {
        SelectBlukiiFragment fragment = new SelectBlukiiFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_blukii, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Buttons registrieren damit wir auf Benutzereingaben reagieren können
        Button btnConnect = (Button) getView().findViewById(R.id.btn_blukii_connect);
        btnConnect.setOnClickListener(this);
        Button btnDisconnect = (Button) getView().findViewById(R.id.btn_blukii_disconnect);
        btnDisconnect.setOnClickListener(this);
        Button btnDiscover = (Button) getView().findViewById(R.id.btn_blukii_discover);
        btnDiscover.setOnClickListener(this);


    }

    @Override
    public void onStart() {
        super.onStart();

        // sind wir zum ersten Mal hier und es gibt noch keinen Adapter?
        if (adapter == null) {

            // Erstmal eine leere Liste erzeugen
            ArrayList<String> blukiiList = new ArrayList<String>();
            // Adapter setzen
            adapter = new ListViewBlukiiAdapter(getActivity(), R.layout.listview_blukii_item, blukiiList);
            // Adapter der Liste zuweisen
            getListView().setAdapter(adapter);

            // wir wollen auf Benutzereingaben in der Liste reagieren
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String address = ((TextView) view.findViewById(R.id.device_address)).getText().toString();

                    // speichere die Adresse des ausgewählten Blukiis
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    sp.edit().putString(PREF_SELECTED_BLUKII, address).commit();
                    // informiere den Adapter über Änderungen
                    adapter.notifyDataSetChanged();
                    // da ein Blukii ausgewählt wurde, kann der Benutzer nun den Verbinden-Button verwenden
                    getView().findViewById(R.id.btn_blukii_connect).setEnabled(true);
                }
            });
        }
        // registriere den UpdateReceiver
        IntentFilter intentFilter = new IntentFilter();
        Profile.addDefaultActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        // wenn das Fragment gestoppt wird, dann wollen wir auch nicht mehr auf Broadcasts reagieren, also wird der UpdateReceiver unregistriert
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {

        mService = ((MainActivity) getActivity()).getBlukiiManagerService();

        //Nach 10 sec ist die Suche beendet
        CountDownTimer countdown = new CountDownTimer(10000, 750) {

            int flag = 0;

            public void onTick(long millisUntilFinished) {
                switch(flag) {
                    case 0:
                        updateConnectionStatus(getText(R.string.blukii_discover).toString());
                        flag++;
                        break;
                    case 1:
                        updateConnectionStatus(getText(R.string.blukii_discover).toString() + getText(R.string.blukii_discover_1).toString());
                        flag++;
                        break;
                    case 2:
                        updateConnectionStatus(getText(R.string.blukii_discover).toString() + getText(R.string.blukii_discover_2).toString());
                        flag++;
                        break;
                    case 3:
                        updateConnectionStatus(getText(R.string.blukii_discover).toString() + getText(R.string.blukii_discover_3).toString());
                        flag = 0;
                        break;
                }
            }

            public void onFinish() {
                updateConnectionStatus(getText(R.string.blukii_discoveryStopped).toString());
                //buttons anpassen
                Button b = ((Button) getView().findViewById(R.id.btn_blukii_discover));
                b.setText(R.string.btn_startDiscover);
                b.setTag("start");
            }
        };

        // reagiere auf Button-Clicks
        switch (v.getId()) {

            // Benutzer hat den Verbinden-Button gedrückt
            case R.id.btn_blukii_connect:
                // Discovery stoppen
                mService.stopDiscovery();
                countdown.cancel();

                Button b = ((Button) getView().findViewById(R.id.btn_blukii_discover));
                b.setText(R.string.btn_startDiscover);
                b.setTag("start");

                getView().findViewById(R.id.btn_blukii_disconnect).setEnabled(true);
                getView().findViewById(R.id.btn_blukii_connect).setEnabled(false);
                getView().findViewById(R.id.btn_blukii_discover).setEnabled(false);

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String selectedDevice = sp.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");

                // prüfen ob vorher ein Blukii gewählt wurde
                if (selectedDevice != null) {
                    // zu Blukii verbinden
                    Blukii blukii = mService.getBlukiiByAddress(selectedDevice);
                    blukii.connect();
                    updateConnectionStatus(getText(R.string.blukii_connecting).toString());
                }

                break;
            // Benutzer hat Trennen-Button gedrückt
            case R.id.btn_blukii_disconnect:
                // Verbindung zu Blukii beenden
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String selected = pref.getString(SelectBlukiiFragment.PREF_SELECTED_BLUKII, "");

                // prüfen ob vorher ein Blukii gewählt wurde
                if (selected != null) {
                    // zu Blukii verbinden
                    Blukii blukii = mService.getBlukiiByAddress(selected);
                    blukii.disconnect();
                    //updateStatus("Trennen...");
                    updateConnectionStatus(getText(R.string.blukii_disconnecting).toString());
                }
                break;
            // Benutzer hat den Discover-Button gedrückt
            case R.id.btn_blukii_discover:
                // Der Button hat zwei Funktionen, Starten und Stoppen der Discovery,
                // über den Tag kann ausgelesen werden, welche Funktion vorher aktiv war
                if (v.getTag().equals("start")) {
                    // alle früheren Einträge entfernen
                    adapter.clear();
                    // die Änderungen dem Adapter mitteilen
                    adapter.notifyDataSetChanged();

                    // Discovery starten
                    mService.startDiscovery();
                    ((Button) v).setText(R.string.btn_stopDiscover);
                    v.setTag("stop");
                    updateConnectionStatus(getText(R.string.blukii_discover).toString());
                    countdown.start();

                } else {
                    // Discovery stoppen
                    mService.stopDiscovery();
                    ((Button) v).setText(R.string.btn_startDiscover);
                    v.setTag("start");
                    updateConnectionStatus(getText(R.string.blukii_discoveryStopped).toString());
                    countdown.cancel();
                }
                break;
        }
    }

    /**
     * Aktualisiert das TextView mit dem neuen Status
     *
     * @param newStatus Der neue Status
     */
    private void updateStatus(String newStatus) {
        //((TextView) getView().findViewById(R.id.tv_blukii_connection_status)).setText("Status: " + newStatus);
    }

    private void updateConnectionStatus(String status) {
        View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(status);
    }
}

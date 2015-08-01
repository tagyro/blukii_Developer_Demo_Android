package com.blukii.androidblukiitutorial;

import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
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
public class MenuFragment extends AbstractFragment implements View.OnClickListener {

    public static final String PREF_SELECTED_BLUKII = "pref_key_selectedBlukii";
    public final static String TAG = SelectBlukiiFragment.class.getSimpleName();


    private ListViewBlukiiAdapter adapter;
    private BlukiiManagerService mService;

    public MenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SelectBlukiiFragment.
     */
    public static MenuFragment newInstance() {
        MenuFragment fragment = new MenuFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Buttons registrieren damit wir auf Benutzereingaben reagieren können



    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onClick(View v) {

        // reagiere auf Button-Clicks
        switch (v.getId()) {

            // Benutzer hat den Verbinden-Button gedrückt
            case R.id.btn_gotoSelectBlukii:


                break;

        }
    }
}

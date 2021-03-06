package com.blukii.androidblukiitutorial;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BatteryProfile;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.HumidityProfile;
import com.blukii.android.blukiilibrary.Profile;


public class BasicSensorsFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = BasicSensorsFragment.class.getSimpleName();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            BatteryProfile batteryProfile = (BatteryProfile) MainActivity.getProfileById(getActivity(), BatteryProfile.ID);
            HumidityProfile humidityProfile = (HumidityProfile) MainActivity.getProfileById(getActivity(), HumidityProfile.ID);

            // Check status
            if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                Log.d(TAG, "onReceive(). Status is not ok, action=" + action);
                return;
            }

            // Check profile
            if (humidityProfile == null) {
                Log.d(TAG, "onReceive(). HumidityProfile is NULL");
                updateHumidityProfileStatus(getText(R.string.profile_inactive).toString());
                enableAllHumidity(false);
                return;
            }

            if (batteryProfile == null) {
                Log.d(TAG, "onReceive(). BatteryProfile is NULL");
                updateBatteryStatus(getText(R.string.profile_inactive).toString());
                enableAllBattery(false);
                return;
            }


            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:

                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    enableAllHumidity(false);
                    enableAllBattery(false);
                    break;

                case Blukii.ACTION_ERROR_LOADING_SERVICES:

                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    enableAllHumidity(false);
                    enableAllBattery(false);
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    //updateStatus("Verbunden");
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());

                    enableAllBattery(true);
                    enableAllHumidity(true);

                    ((Button) getView().findViewById(R.id.btn_humidity_update)).setEnabled(false);
                    ((Button) getView().findViewById(R.id.btn_humidity_activate)).setTag("activate");
                    ((Button) getView().findViewById(R.id.btn_humidity_activate)).setText(R.string.btn_activateProfile);

                    //if (humidityProfile != null) {
                    //    humidityProfile.readEnabled();
                    //}

                    break;
                /* **************
                * Battery
                * **************/
                case BatteryProfile.ACTION_READ_BATTERY_LEVEL:
                    Log.d(TAG, "onReceive() intent=" + action);

                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int level = intent.getIntExtra(BatteryProfile.EXTRA_BATTERY_VALUE, 0);
                        updateBatteryStatus(level + "%");
                        getView().findViewById(R.id.btn_battery_read).setEnabled(true);
                    } else {
                        updateConnectionStatus(getText(R.string.lbl_status_readError).toString());
                        getView().findViewById(R.id.btn_battery_read).setEnabled(true);
                    }

                    break;
                /* **************
                * Humidity
                * **************/
                case HumidityProfile.ACTION_SET_HUMIDITY_ENABLED:
                case HumidityProfile.ACTION_READ_HUMIDITY_ENABLED:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                        MainActivity.handleEnablerStatus(getActivity(), enablerStatus, "Humidity");
                        if (enablerStatus == EnablerStatus.Activated) {
                            Log.d(TAG, "Humidity profile enabled");
                            ((Button) getView().findViewById(R.id.btn_humidity_activate)).setText(getText(R.string.btn_deactivateProfile).toString());
                            ((Button) getView().findViewById(R.id.btn_humidity_activate)).setTag("deactivate");
                            enableAllHumidity(true);
                            updateHumidityProfileStatus(getText(R.string.lbl_humiditySensorActivated).toString());
                        } else if (enablerStatus == EnablerStatus.Deactivated) {
                            Log.d(TAG, "Humidity profile disabled");
                            //Button activate profile
                            ((Button) getView().findViewById(R.id.btn_humidity_activate)).setText(getText(R.string.btn_activateProfile).toString());
                            ((Button) getView().findViewById(R.id.btn_humidity_activate)).setTag("activate");
                            ((Button) getView().findViewById(R.id.btn_humidity_activate)).setEnabled(true);

                            //Button update Value
                            getView().findViewById(R.id.btn_humidity_update).setEnabled(false);

                            updateHumidityProfileStatus(getText(R.string.lbl_humiditySensorDeactivated).toString());
                        }
                    }
                    break;
                case HumidityProfile.ACTION_READ_HUMIDITY_VALUE:
                    Log.d(TAG, "ACTION_READ_HUMIDITY_VALUE, status=" + status);
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int value = intent.getIntExtra(HumidityProfile.EXTRA_HUMIDITY_VALUE, 0);
                        updateHumidityProfileStatus(String.valueOf(value) + "%");
                    } else {
                        updateHumidityProfileStatus(getText(R.string.lbl_status_readError).toString());
                    }
                    break;

            }
        }
    };

    public BasicSensorsFragment() {
        // Required empty public constructor
    }

    public static BasicSensorsFragment newInstance() {
        BasicSensorsFragment fragment = new BasicSensorsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Buttons registrieren damit wir auf Benutzereingaben reagieren können
        Button btnBatteryRead = (Button) getView().findViewById(R.id.btn_battery_read);
        btnBatteryRead.setOnClickListener(this);

        Button btnHumidityActivate = (Button) getView().findViewById(R.id.btn_humidity_activate);
        btnHumidityActivate.setOnClickListener(this);

        Button btnHumidityUpdate = (Button) getView().findViewById(R.id.btn_humidity_update);
        btnHumidityUpdate.setOnClickListener(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basic_sensors, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        HumidityProfile.addActions(intentFilter);
        BatteryProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {

        BatteryProfile batteryProfile = (BatteryProfile) MainActivity.getProfileById(getActivity(), BatteryProfile.ID);
        HumidityProfile humidityProfile = (HumidityProfile) MainActivity.getProfileById(getActivity(), HumidityProfile.ID);


        switch (v.getId()) {

            case R.id.btn_battery_read:
                if (batteryProfile != null) {
                    batteryProfile.readBatteryLevel();
                    updateBatteryStatus(getText(R.string.lbl_status_read).toString());
                    v.setEnabled(false);
                }
                break;
            case R.id.btn_humidity_activate:
                if (v.getTag().equals("activate")) {
                    if (humidityProfile != null) {
                        updateHumidityProfileStatus(getText(R.string.profile_activating).toString());
                        humidityProfile.setEnabled(true);
                    }
                } else {
                    if (humidityProfile != null) {
                        updateHumidityProfileStatus(getText(R.string.profile_deactivating).toString());
                        humidityProfile.setEnabled(false);
                    }
                }
                v.setEnabled(false);
                break;
            case R.id.btn_humidity_update:
                if (humidityProfile != null) {
                    humidityProfile.readHumidity();
                    updateHumidityProfileStatus(getText(R.string.lbl_status_read).toString());
                }
                break;

        }
    }

    private void updateBlukiiStatus(String newStatus) {
        //((TextView) getView().findViewById(R.id.tv_sensors_blukii_status)).setText(newStatus);
    }

    private void updateConnectionStatus(String status) {
        View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(status);
    }

    private void updateBatteryStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_battery)).setText(getText(R.string.lbl_stateOfCharge).toString() + newStatus);
    }

    private void updateHumidityProfileStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_humidity_status)).setText(getText(R.string.lbl_humiditySensor).toString() + newStatus);
    }

    private void enableAllHumidity(boolean enable) {
        getView().findViewById(R.id.btn_humidity_activate).setEnabled(enable);
        getView().findViewById(R.id.btn_humidity_update).setEnabled(enable);
    }

    private void enableAllBattery(boolean enable) {
        getView().findViewById(R.id.btn_battery_read).setEnabled(enable);
    }

    /*
    public void disableHumidity() {

        HumidityProfile humidityProfile = (HumidityProfile) MainActivity.getProfileById(getActivity(), HumidityProfile.ID);
        humidityProfile.setEnabled(false);

    }*/
}

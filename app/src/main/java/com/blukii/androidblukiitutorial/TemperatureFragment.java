package com.blukii.androidblukiitutorial;

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
import android.widget.EditText;
import android.widget.TextView;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.Profile;
import com.blukii.android.blukiilibrary.TemperatureEventState;
import com.blukii.android.blukiilibrary.TemperatureProfile;

/**
 * Created by bolatov on 18.12.2014.
 */
public class TemperatureFragment extends AbstractFragment implements View.OnClickListener {
    private static final String TAG = TemperatureFragment.class.getSimpleName();
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);
            TemperatureProfile temperatureProfile = (TemperatureProfile) MainActivity.getProfileById(getActivity(), TemperatureProfile.ID);

            // Check status
            if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                Log.d(TAG, "onReceive(). Status is not ok, action=" + action);
                return;
            }

            // Check profile
            if (temperatureProfile == null) {
                Log.d(TAG, "onReceive(). TemperatureProfile is NULL");
                updateStatus("Getrennt");
                enableAll(false);
                return;
            }

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateStatus("Getrennt");
                    enableAll(false);
                    break;

                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateStatus("Getrennt");
                    enableAll(false);
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateStatus("Verbunden");
                    ((Button) getView().findViewById(R.id.btn_temp_enabler)).setEnabled(true);
                    ((Button) getView().findViewById(R.id.btn_temp_enabler)).setTag("activate");
                    ((Button) getView().findViewById(R.id.btn_temp_enabler)).setText("Activate profile");
                    break;

                // TEMPERATURE ENABLER
                case TemperatureProfile.ACTION_ENABLED_TEMPERATURE_PROFILE:
                    EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                    if (enablerStatus == EnablerStatus.Activated) {
                        enableAll(true);
                        ((Button) getView().findViewById(R.id.btn_temp_enabler)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_temp_enabler)).setText("Deactivate profile");
                        updateStatus("Activated");

                    } else if (enablerStatus == EnablerStatus.Deactivated || enablerStatus == null) {
                        enableAll(false);
                        ((Button) getView().findViewById(R.id.btn_temp_enabler)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_temp_enabler)).setText("Activate profile");
                        ((Button) getView().findViewById(R.id.btn_temp_enabler)).setEnabled(true);
                        updateStatus("Deactivated");
                    }
                    break;

                // TEMPERATURE VALUE
                case TemperatureProfile.ACTION_READ_TEMPERATURE_VALUE:
                    double temperatureValue = intent.getDoubleExtra(TemperatureProfile.EXTRA_TEMPERATURE_VALUE, 0);
                    updateValue(String.format("%.1f %sC", temperatureValue, "\u00b0"));
                    break;


                // TEMPERATURE EVENT CONFIG
                case TemperatureProfile.ACTION_READ_TEMPERATURE_EVENT_CONFIG:
                    double minValue = intent.getDoubleExtra(TemperatureProfile.EXTRA_TEMPERATURE_EVENT_CONFIG_LOW, -1);
                    double maxValue = intent.getDoubleExtra(TemperatureProfile.EXTRA_TEMPERATURE_EVENT_CONFIG_HIGH, -1);
                    getEditText(R.id.et_temperature_eventcfg_low).setText(String.valueOf(minValue));
                    getEditText(R.id.et_temperature_eventcfg_high).setText(String.valueOf(maxValue));
                    break;

                // TEMPERATURE EVENT STATE
                case TemperatureProfile.ACTION_READ_TEMPERATURE_EVENT_STATE:
                    TemperatureEventState readTempEventState = (TemperatureEventState) intent.getSerializableExtra(TemperatureProfile.EXTRA_TEMPERATURE_EVENT_STATE);
                    updateEventState(String.valueOf(readTempEventState));
                    break;

                case TemperatureProfile.ACTION_SET_NOTIFY_TEMPERATURE_EVENT_STATE:
                    TemperatureEventState tes = (TemperatureEventState) intent.getSerializableExtra(TemperatureProfile.EXTRA_TEMPERATURE_EVENT_STATE);
                    Log.d(TAG, "ACTION_SET_TEMPERATURE_EVENT_STATE " + tes.toString());
                    if (tes != TemperatureEventState.Deactivated) {
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_read)).setEnabled(false);
                        updateEventState("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_temp_event_state_read)).setEnabled(true);
                        updateEventState("notify deactivated");
                    }
                    break;
            }
        }
    };

    public TemperatureFragment() {
        // Required empty public constructor
    }

    public static TemperatureFragment newInstance() {
        TemperatureFragment fragment = new TemperatureFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // newDataParser button click listeners
        getView().findViewById(R.id.btn_temp_enabler).setOnClickListener(this);
        getView().findViewById(R.id.btn_temp_value_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_temperature_eventcfg_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_temperature_eventcfg_set).setOnClickListener(this);
        getView().findViewById(R.id.btn_temp_event_state_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_temp_event_state_notify).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_temperature, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        TemperatureProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        TemperatureProfile temperatureProfile = (TemperatureProfile) MainActivity.getProfileById(getActivity(), TemperatureProfile.ID);
        if (temperatureProfile == null) return;

        switch (v.getId()) {
            // TEMPERATURE ENABLER
            case R.id.btn_temp_enabler:
                if (v.getTag().equals("activate")) {
                    temperatureProfile.setEnabled(true);
                    v.setEnabled(false);
                    updateStatus("activating...");
                } else {
                    temperatureProfile.setEnabled(false);
                    v.setEnabled(false);
                    updateStatus("deactivating...");
                }
                break;

            // TEMPERATURE VALUE
            case R.id.btn_temp_value_read:
                temperatureProfile.readValue();
                break;

            // TEMPERATURE EVENT CONFIG
            case R.id.btn_temperature_eventcfg_read:
                temperatureProfile.readEventConfig();
                break;

            case R.id.btn_temperature_eventcfg_set:
                String minText = ((EditText) getView().findViewById(R.id.et_temperature_eventcfg_low)).getText().toString();
                String maxText = ((EditText) getView().findViewById(R.id.et_temperature_eventcfg_high)).getText().toString();
                double minValue, maxValue;
                try {
                    minValue = Double.parseDouble(minText);
                    maxValue = Double.parseDouble(maxText);
                    temperatureProfile.setEventConfig(minValue, maxValue);
                } catch (Exception e) {
                    Log.e(TAG, String.format("Error parsing minText=%s or maxText=%s", String.valueOf(minText), String.valueOf(maxText)), e);
                    return;
                }

                break;

            // TEMPERATURE EVENT STATE
            case R.id.btn_temp_event_state_read:
                temperatureProfile.readEventState();
                break;

            case R.id.btn_temp_event_state_notify:
                if (v.getTag().equals("activate")) {
                    temperatureProfile.notifyEventState(true);
                    v.setEnabled(false);
                    updateEventState("activating...");
                } else {
                    temperatureProfile.notifyEventState(false);
                    v.setEnabled(false);
                    updateEventState("deactivating...");
                }
                break;
        }
    }

    private void enableAll(boolean enable) {
        // ENABLER
        getView().findViewById(R.id.btn_temp_enabler).setEnabled(enable);

        // VALUE
        getView().findViewById(R.id.btn_temp_value_read).setEnabled(enable);

        // EVENT CONFIG
        getView().findViewById(R.id.btn_temperature_eventcfg_read).setEnabled(enable);
        getView().findViewById(R.id.btn_temperature_eventcfg_set).setEnabled(enable);
        getView().findViewById(R.id.et_temperature_eventcfg_low).setEnabled(enable);
        getView().findViewById(R.id.et_temperature_eventcfg_high).setEnabled(enable);

        // EVENT STATE
        getView().findViewById(R.id.btn_temp_event_state_read).setEnabled(enable);
        getView().findViewById(R.id.btn_temp_event_state_notify).setEnabled(enable);

    }

    private void updateStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_temp_status)).setText(newStatus);
    }

    private void updateValue(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_temp_value)).setText("Value: " + newStatus);
    }

    private void updateEventState(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_temp_event_state)).setText("Event State: " + newStatus);
    }
}

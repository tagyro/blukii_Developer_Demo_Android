package com.blukii.androidblukiitutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.LightEventState;
import com.blukii.android.blukiilibrary.LightMode;
import com.blukii.android.blukiilibrary.LightProfile;
import com.blukii.android.blukiilibrary.Profile;

/**
 * Created by bolatov on 18.12.2014.
 */
public class LightFragment extends AbstractFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = LightFragment.class.getSimpleName();

    private LightMode lightMode = null;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);
            LightProfile lightProfile = (LightProfile) MainActivity.getProfileById(getActivity(), LightProfile.ID);

            // Check status
            if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                Log.d(TAG, "onReceive(). Status is not ok, action=" + action);
                return;
            }

            // Check profile
            if (lightProfile == null) {
                Log.d(TAG, "onReceive(). LightProfile is NULL");
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
                    ((Button) getView().findViewById(R.id.btn_light_enabler)).setEnabled(true);
                    ((Button) getView().findViewById(R.id.btn_light_enabler)).setTag("activate");
                    ((Button) getView().findViewById(R.id.btn_light_enabler)).setText("Activate profile");
                    break;


                // LIGHT ENABLER
                case LightProfile.ACTION_READ_LIGHT_ENABLED:
                case LightProfile.ACTION_SET_LIGHT_ENABLED:
                    EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                    if (enablerStatus == EnablerStatus.Activated) {
                        enableAll(true);
                        ((Button) getView().findViewById(R.id.btn_light_enabler)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_light_enabler)).setText("Deactivate profile");
                        updateStatus("Activated");

                    } else if (enablerStatus == EnablerStatus.Deactivated || enablerStatus == null) {
                        enableAll(false);
                        ((Button) getView().findViewById(R.id.btn_light_enabler)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_light_enabler)).setText("Activate profile");
                        ((Button) getView().findViewById(R.id.btn_light_enabler)).setEnabled(true);
                        updateStatus("Deactivated");
                    }
                    break;

                // LIGHT MODE
                case LightProfile.ACTION_READ_LIGHT_MODE:
                    lightMode = (LightMode) intent.getSerializableExtra(LightProfile.EXTRA_LIGHT_MODE);
                    if (lightMode != null) {
                        Spinner spinner = getSpinner(R.id.spin_light_mode);
                        spinner.setSelection(lightMode.ordinal() + 1);
                    }
                    break;

                // LIGHT VALUE
                case LightProfile.ACTION_READ_LIGHT_VALUE:
                    int lightValue = intent.getIntExtra(LightProfile.EXTRA_LIGHT_VALUE, -1);
//                    if (lightValue == -1) lightProfile.notifyValue(false);
                    updateValue("" + lightValue);
                    break;

                case LightProfile.ACTION_SET_NOTIFY_LIGHT_VALUE:
                    boolean isEnabled = intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false);
                    Log.d(TAG, "ACTION_SET_NOTIFY_LIGHT_VALUE " + (isEnabled ? "EXTRA_NOTIFICATIONS_ENABLED" : "DISABLED"));
                    if (isEnabled) {
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_light_value_read)).setEnabled(false);
                        updateValue("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_light_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_light_value_read)).setEnabled(true);
                        updateValue("notify deactivated");
                    }
                    break;

                // LIGHT EVENT CONFIG
                case LightProfile.ACTION_READ_LIGHT_EVENT_CONFIG:
                    int minValue = intent.getIntExtra(LightProfile.EXTRA_LIGHT_EVENT_CONFIG_LOW, -1);
                    int maxValue = intent.getIntExtra(LightProfile.EXTRA_LIGHT_EVENT_CONFIG_HIGH, -1);
                    getEditText(R.id.et_light_eventcfg_low).setText(String.valueOf(minValue));
                    getEditText(R.id.et_light_eventcfg_high).setText(String.valueOf(maxValue));
                    break;

                // LIGHT EVENT STATE
                case LightProfile.ACTION_READ_LIGHT_EVENT_STATE:
                    LightEventState readLightEventState = (LightEventState) intent.getSerializableExtra(LightProfile.EXTRA_LIGHT_EVENT_STATE);
                    updateEventState(String.valueOf(readLightEventState));
                    break;

                case LightProfile.ACTION_SET_NOTIFY_LIGHT_EVENT_STATE:
                    LightEventState lightEventState = (LightEventState) intent.getSerializableExtra(LightProfile.EXTRA_LIGHT_EVENT_STATE);
                    Log.d(TAG, "ACTION_SET_NOTIFY_LIGHT_EVENT_STATE " + lightEventState.toString());
                    if (lightEventState != LightEventState.Deactivated) {
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_light_event_state_read)).setEnabled(false);
                        updateEventState("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_light_event_state_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_light_event_state_read)).setEnabled(true);
                        updateEventState("notify deactivated");
                    }
                    break;
            }
        }
    };


    public LightFragment() {
        // Required empty public constructor
    }

    public static LightFragment newInstance() {
        LightFragment fragment = new LightFragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // newDataParser button click listeners
        getView().findViewById(R.id.btn_light_enabler).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_mode_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_mode_set).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_value_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_value_notify).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_eventcfg_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_eventcfg_set).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_event_state_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_light_event_state_notify).setOnClickListener(this);

        initSpinner(R.id.spin_light_mode, R.array.light_modes, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_light, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        LightProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        LightProfile lightProfile = (LightProfile) MainActivity.getProfileById(getActivity(), LightProfile.ID);
        if (lightProfile == null) {
            Log.e(TAG, "onClick() LightProfile is NULL");
            return;
        }

        switch (v.getId()) {
            // LIGHT ENABLER
            case R.id.btn_light_enabler:
                if (v.getTag().equals("activate")) {
                    lightProfile.setEnabled(true);
                    v.setEnabled(false);
                    updateStatus("activating...");
                } else {
                    lightProfile.setEnabled(false);
                    v.setEnabled(false);
                    updateStatus("deactivating...");
                }
                break;

            // LIGHT MODE
            case R.id.btn_light_mode_read:
                lightProfile.readMode();
                break;

            case R.id.btn_light_mode_set:
                if (lightMode != null) {
                    lightProfile.setMode(lightMode);
                } else {
                    toast("Please select a light mode");
                }

                break;

            // LIGHT VALUE
            case R.id.btn_light_value_read:
                lightProfile.readValue();
                break;

            case R.id.btn_light_value_notify:
                // test
                if (v.getTag().equals("activate")) {
                    lightProfile.notifyValue(true);
                    v.setEnabled(false);
                    updateValue("activating...");
                } else {
                    lightProfile.notifyValue(false);
                    v.setEnabled(false);
                    updateValue("deactivating...");
                }
                break;

            // LIGHT EVENT CONFIG
            case R.id.btn_light_eventcfg_read:
                lightProfile.readEventConfig();
                break;

            case R.id.btn_light_eventcfg_set:
                String minText = ((EditText) getView().findViewById(R.id.et_light_eventcfg_low)).getText().toString();
                String maxText = ((EditText) getView().findViewById(R.id.et_light_eventcfg_high)).getText().toString();
                int minValue, maxValue;
                try {
                    minValue = Integer.parseInt(minText);
                    maxValue = Integer.parseInt(maxText);
                } catch (Exception e) {
                    Log.e(TAG, String.format("Error parsing minText=%s or maxText=%s", String.valueOf(minText), String.valueOf(maxText)), e);
                    return;
                }
                lightProfile.setEventConfig(minValue, maxValue, lightMode);
                break;

            // LIGHT EVENT STATE
            case R.id.btn_light_event_state_read:
                lightProfile.readEventState();
                break;

            case R.id.btn_light_event_state_notify:
                if (v.getTag().equals("activate")) {
                    lightProfile.notifyEventState(true);
                    v.setEnabled(false);
                    updateEventState("activating...");
                } else {
                    lightProfile.notifyEventState(false);
                    v.setEnabled(false);
                    updateEventState("deactivating...");
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spin_light_mode:
                if (position > 0) {
                    lightMode = LightMode.values()[position - 1];
                } else {
                    lightMode = null;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void enableAll(boolean enable) {
        getView().findViewById(R.id.btn_light_enabler).setEnabled(enable);
        getView().findViewById(R.id.btn_light_mode_read).setEnabled(enable);
        getView().findViewById(R.id.btn_light_mode_set).setEnabled(enable);
        getView().findViewById(R.id.btn_light_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_light_value_notify).setEnabled(enable);
        getView().findViewById(R.id.btn_light_eventcfg_read).setEnabled(enable);
        getView().findViewById(R.id.btn_light_eventcfg_set).setEnabled(enable);
        getView().findViewById(R.id.btn_light_event_state_read).setEnabled(enable);
        getView().findViewById(R.id.btn_light_event_state_notify).setEnabled(enable);
        getView().findViewById(R.id.spin_light_mode).setEnabled(enable);
        getView().findViewById(R.id.et_light_eventcfg_low).setEnabled(enable);
        getView().findViewById(R.id.et_light_eventcfg_high).setEnabled(enable);
    }

    private void updateStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_light_status)).setText(newStatus);
    }

    private void updateValue(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_light_value)).setText("Value: " + newStatus);
    }

    private void updateEventState(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_light_event_state)).setText("Event State: " + newStatus);
    }
}

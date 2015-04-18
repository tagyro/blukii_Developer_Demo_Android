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
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.AltimeterEventState;
import com.blukii.android.blukiilibrary.AltimeterMode;
import com.blukii.android.blukiilibrary.AltimeterProfile;
import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.Profile;

public class AltimeterFragment extends AbstractFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = AltimeterFragment.class.getSimpleName();

    private AltimeterMode altimeterMode;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AltimeterProfile altimeterProfile = (AltimeterProfile) MainActivity.getProfileById(getActivity(), AltimeterProfile.ID);

            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, -1);

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;
                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    Button b = (Button) getView().findViewById(R.id.btn_altimeter_activate);
                    b.setTag("activate");
                    b.setText(R.string.btn_activateProfile);
                    b.setEnabled(true);
                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                case AltimeterProfile.ACTION_READ_ALTIMETER_ENABLED:
                case AltimeterProfile.ACTION_SET_ALTIMETER_ENABLED:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                        if (enablerStatus == EnablerStatus.Activated) {
                            Button btn = (Button) getView().findViewById(R.id.btn_altimeter_activate);
                            btn.setTag("deactivate");
                            btn.setText(R.string.btn_deactivateProfile);

                            ViewHelper.setEnabledOfAllControls(true, getView());
                            updateBlukiiStatus(R.string.profile_active);

                        } else if (enablerStatus == EnablerStatus.Deactivated || enablerStatus == null) {
                            Button btn = (Button) getView().findViewById(R.id.btn_altimeter_activate);
                            btn.setTag("activate");
                            btn.setText(R.string.btn_activateProfile);
                            ViewHelper.setEnabledOfAllControls(false, getView());
                            btn.setEnabled(true);
                            updateBlukiiStatus(R.string.profile_inactive);
                            if (enablerStatus == null) {
                                Log.e(TAG, String.format("Altimeter enabling error. enablerStatus=NULL"));
                            }
                        }
                    }
                    break;

                case AltimeterProfile.ACTION_SET_ALTIMETER_MODE:
                    toast("Altimeter mode set", status);
                case AltimeterProfile.ACTION_READ_ALTIMETER_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        altimeterMode = (AltimeterMode) intent.getSerializableExtra(AltimeterProfile.EXTRA_ALTIMETER_VALUE);
                        if (altimeterMode != null) {
                            Spinner spinner = getSpinner(R.id.spin_altimeter_mode);
                            spinner.setSelection(altimeterMode.ordinal() + 1);
                        }
                    }
                    break;
                case AltimeterProfile.ACTION_SET_ALTIMETER_EVENT_CONFIG:
                    toast("Altimeter event config set", status);
                case AltimeterProfile.ACTION_READ_ALTIMETER_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int threshold = intent.getIntExtra(AltimeterProfile.EXTRA_ALTIMETER_VALUE, 0);
                        getEditText(R.id.et_altimeter_eventcfg_threshold).setText(String.valueOf(threshold));
                    }
                    break;

                case AltimeterProfile.ACTION_SET_NOTIFY_ALTIMETER_EVENT_STATE:
                    toast("Altimeter event state set", status);
                case AltimeterProfile.ACTION_READ_ALTIMETER_EVENT_STATE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        AltimeterEventState state = (AltimeterEventState) intent.getSerializableExtra(AltimeterProfile.EXTRA_ALTIMETER_VALUE);
                        ((TextView) getView().findViewById(R.id.et_altimeter_event_state)).setText(state.name());
                        boolean on = (state == AltimeterEventState.Activated || state == AltimeterEventState.ActivatedWithEvent);
                        ToggleButton t = ((ToggleButton) getView().findViewById(R.id.btn_altimeter_event_state));
                        t.setChecked(on);
                        t.setEnabled(true);
                    }
                    break;

                case AltimeterProfile.ACTION_READ_ALTIMETER_VALUE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        double value = intent.getDoubleExtra(AltimeterProfile.EXTRA_ALTIMETER_VALUE, -1);
                        String measure = "";
                        String newStatus = String.format("%.2f %s", value, measure);
                        getTextView(R.id.tv_altimeter_value).setText(newStatus + " " + getUnit(altimeterMode));
                    }
                    break;
            }
        }
    };


    public AltimeterFragment() {
        // Required empty public constructor
    }

    public static AltimeterFragment newInstance() {
        AltimeterFragment fragment = new AltimeterFragment();
        return fragment;
    }

    private AltimeterProfile getAltimeterProfile() {
        return (AltimeterProfile) MainActivity.getProfileById(getActivity(), AltimeterProfile.ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_altimeter, container, false);
        ViewHelper.setEnabledOfAllControls(false, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewHelper.registerListener(this, getView());

        initSpinner(R.id.spin_altimeter_mode, R.array.altimeter_modes, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        AltimeterProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        AltimeterProfile altimeterProfile = getAltimeterProfile();
        if (altimeterProfile == null) return;

        boolean isActivate = v.getTag() != null && v.getTag().equals("activate");

        switch (v.getId()) {
            case R.id.btn_altimeter_activate:
                if (isActivate) {
                    altimeterProfile.setEnabled(true);
                    updateBlukiiStatus(R.string.profile_activating);
                    v.setEnabled(false);
                } else {
                    altimeterProfile.setEnabled(false);
                    updateBlukiiStatus(R.string.profile_deactivating);
                    v.setEnabled(false);
                }
                break;
            case R.id.btn_altimeter_mode_read:
                altimeterProfile.readMode();
                break;
            case R.id.btn_altimeter_mode_set:
                if (altimeterMode != null) {
                    altimeterProfile.setMode(altimeterMode);
                } else {
                    toast(getText(R.string.error_altiModeNotSet).toString());
                }
                break;
            case R.id.btn_altimeter_read_value:

                if (altimeterMode != null) {
                    altimeterProfile.readValue();
                } else {
                    toast(getText(R.string.error_altiModeNotSet).toString());
                }

                break;
            case R.id.btn_altimeter_event_state_read:
                altimeterProfile.readEventState();
                break;
            case R.id.btn_altimeter_event_state:
                altimeterProfile.notifyEventState(((ToggleButton) v).isChecked());
                break;
            case R.id.btn_altimeter_eventcfg_read:
                altimeterProfile.readEventConfig();
                break;
            case R.id.btn_altimeter_eventcfg_set:
                String thresholdStr = ((EditText) getView().findViewById(R.id.et_altimeter_eventcfg_threshold)).getText().toString();
                int threshold;
                if (altimeterMode != null) {
                    if ( thresholdStr.matches("") || thresholdStr == null || thresholdStr.isEmpty() ) {
                        switch (altimeterMode) {
                            case Altitude: default:
                                threshold = 0;
                                break;
                            case Barometer:
                                threshold = 2;
                                break;
                        }
                    } else {
                        threshold = Integer.parseInt(thresholdStr);
                    }
                    switch (altimeterMode) {
                        case Altitude:
                            if ( threshold >= -32768 && threshold <= 32767) {
                                altimeterProfile.setEventConfig(threshold, altimeterMode);
                            } else {
                                toast(getText(R.string.error_altitudeRange).toString());
                            }
                            break;
                        case Barometer:
                            if ( threshold >= 2 && threshold <= 131070 ) {
                                altimeterProfile.setEventConfig(threshold, altimeterMode);
                            } else {
                                toast(getText(R.string.error_barometerRange).toString());
                            }
                            break;
                    }
                } else {
                    toast(getText(R.string.error_altiModeNotSet).toString());
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spin_altimeter_mode:
                if (position > 0) {
                    altimeterMode = AltimeterMode.values()[position - 1];
                } else {
                    altimeterMode = null;
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    private String getUnit(AltimeterMode mode) {
        switch (mode) {
            case Altitude:
                return "m";
            case Barometer:
                return "Pa";
        }
        return "";
    }
}

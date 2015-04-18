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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.AccelerometerEventMode;
import com.blukii.android.blukiilibrary.AccelerometerEventState;
import com.blukii.android.blukiilibrary.AccelerometerProfile;
import com.blukii.android.blukiilibrary.AccelerometerRange;
import com.blukii.android.blukiilibrary.AccelerometerSwitch;
import com.blukii.android.blukiilibrary.AccelerometerSwitchSense;
import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.Profile;


public class AccelerometerFragment extends AbstractFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final static String TAG = AccelerometerFragment.class.getSimpleName();

    private AccelerometerRange accelerometerRange = null;
    /**
     * A broadcast receiver to receive updates on blukiis
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AccelerometerProfile profile = getAccelerometerProfile();

            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, -1);

            // die Verbindung zum Blukii wurde getrennt
            switch (action) {

                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;
                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    Button b = (Button) getView().findViewById(R.id.btn_accelerometer_activate);
                    b.setEnabled(true);
                    b.setTag("activate");
                    b.setText(R.string.btn_activateProfile);

                    ViewHelper.setEnabledOfAllControls(true, getView());

                    // update values
                    if (profile != null) {
                        profile.readEnabled();
                    }

                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_ENABLED:
                case AccelerometerProfile.ACTION_SET_ACCELEROMETER_ENABLED:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                        if (enablerStatus == EnablerStatus.Activated) {
                            Log.d(TAG, "Accelerometer enabled");
                            Button btn = (Button) getView().findViewById(R.id.btn_accelerometer_activate);
                            btn.setTag("deactivate");
                            btn.setText(R.string.btn_deactivateProfile);
                            ViewHelper.setEnabledOfAllControls(true, getView());
                            updateStatus(getText(R.string.profile_active).toString());
                        } else if (enablerStatus == EnablerStatus.Deactivated || enablerStatus == null) {
                            Log.d(TAG, "Accelerometer disabled");
                            Button btn = (Button) getView().findViewById(R.id.btn_accelerometer_activate);
                            btn.setTag("activate");
                            btn.setText(R.string.btn_activateProfile);
                            ViewHelper.setEnabledOfAllControls(false, getView());
                            btn.setEnabled(true);
                            updateStatus(getText(R.string.profile_inactive).toString());

                            if (enablerStatus == null) {
                                Log.e(TAG, String.format("Altimeter enabling error. enablerStatus=NULL"));
                            }
                        }
                    }
                    break;

                case AccelerometerProfile.ACTION_SET_ACCELEROMETER_RANGE:
                    toast("accelerometer range set", status);
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_RANGE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        accelerometerRange = (AccelerometerRange) intent.getSerializableExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_RANGE);
                        if (accelerometerRange != null) {
                            Spinner spinner = getSpinner(R.id.spin_accelerometer_range);
                            spinner.setSelection(accelerometerRange.ordinal() + 1);
                        }
                    }
                    break;

                // RAW X
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_RAW_X:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        double dx = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_RAW_VALUE, -1);
                        ((TextView) getView().findViewById(R.id.tv_acc_raw_x)).setText(String.format("%.4f", dx));
                    }
                    break;

                case AccelerometerProfile.ACTION_SET_NOTIFY_ACCELEROMETER_RAW_X:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        boolean xenabled = intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false);
                        ((ToggleButton) getView().findViewById(R.id.btn_acc_notify_raw_x)).setChecked(xenabled);
                        ((ImageButton) getView().findViewById(R.id.btn_acc_read_raw_x)).setEnabled(!xenabled);
                    }
                    break;

                // RAW Y
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_RAW_Y:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        double dy = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_RAW_VALUE, -1);
                        ((TextView) getView().findViewById(R.id.tv_acc_raw_y)).setText(String.format("%.4f", dy));
                    }
                    break;

                case AccelerometerProfile.ACTION_SET_NOTIFY_ACCELEROMETER_RAW_Y:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        boolean yenabled = intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false);
                        ((ToggleButton) getView().findViewById(R.id.btn_acc_notify_raw_y)).setChecked(yenabled);
                        ((ImageButton) getView().findViewById(R.id.btn_acc_read_raw_y)).setEnabled(!yenabled);
                    }
                    break;

                // RAW Z
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_RAW_Z:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        double dz = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_RAW_VALUE, -1);
                        ((TextView) getView().findViewById(R.id.tv_acc_raw_z)).setText(String.format("%.4f", dz));
                    }
                    break;

                case AccelerometerProfile.ACTION_SET_NOTIFY_ACCELEROMETER_RAW_Z:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        boolean zenabled = intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false);
                        ((ToggleButton) getView().findViewById(R.id.btn_acc_notify_raw_z)).setChecked(zenabled);
                        ((ImageButton) getView().findViewById(R.id.btn_acc_read_raw_z)).setEnabled(!zenabled);
                    }
                    break;

                // FILTER
                case AccelerometerProfile.ACTION_SET_ACCELEROMETER_FILTER:
                    toast("accelerometer filter set", status);
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_FILTER:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        Integer filter = intent.getIntExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_FILTER, -1);
                        ((TextView) getView().findViewById(R.id.et_accelerometer_filter)).setText(filter.toString());
                    }
                    break;

                // EVENT CONFIG
                case AccelerometerProfile.ACTION_SET_ACCELEROMETER_EVENT_CONFIG:
                    toast("accelerometer event cfg set", status);
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        Double high = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_CONFIG_HIGH, -1);
                        ((EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_high)).setText(high.toString());
                        Double highTime = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_CONFIG_HIGH_TIME, -1);
                        ((EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_high_time)).setText(highTime.toString());
                        Double low = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_CONFIG_LOW, -1);
                        ((EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_low)).setText(low.toString());
                        Double lowTime = intent.getDoubleExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_CONFIG_LOW_TIME, -1);
                        ((EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_low_time)).setText(lowTime.toString());
                    }
                    break;

                // EVENT MODE
                case AccelerometerProfile.ACTION_SET_ACCELEROMETER_EVENT_MODE:
                    toast("accelerometer event mode set", status);
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_EVENT_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        AccelerometerEventMode mode = (AccelerometerEventMode) intent.getSerializableExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_MODE);
                        boolean referenced = (mode == AccelerometerEventMode.ReferencedMode);
                        boolean lowEvent = intent.getBooleanExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_MODE_LOW_EVENT, false);
                        boolean highEvent = intent.getBooleanExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_MODE_HIGH_EVENT, false);
                        getToggleButton(R.id.btn_accelerometer_event_mode_ref).setChecked(referenced);
                        getToggleButton(R.id.btn_accelerometer_event_mode_low).setChecked(lowEvent);
                        getToggleButton(R.id.btn_accelerometer_event_mode_high).setChecked(highEvent);
                    }
                    break;

                // EVENT STATE
                case AccelerometerProfile.ACTION_SET_NOTIFY_ACCELEROMETER_EVENT_STATE:
                    toast("accelerometer event state set", status);
                case AccelerometerProfile.ACTION_READ_ACCELEROMETER_EVENT_STATE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        AccelerometerEventState eventState = (AccelerometerEventState) intent.getSerializableExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_EVENT_STATE);
                        ((TextView) getView().findViewById(R.id.tv_acc_event_state)).setText(eventState.toString());
                    }
                    break;

                // SWITCH SENSE
                case AccelerometerProfile.ACTION_SET_SWITCH_SENSE:
                    toast("accelerometer switch sense set", status);
                case AccelerometerProfile.ACTION_READ_SWITCH_SENSE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        AccelerometerSwitchSense switchSense = (AccelerometerSwitchSense) intent.getSerializableExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_SWITCH_SENSE);
                        String senseStatus = switchSense != null ? switchSense.getValue()+"" : "null";
                        ((EditText) getView().findViewById(R.id.et_accelerometer_switch_sense)).setText(senseStatus);
                    }
                    break;

                // SWITCH
                case AccelerometerProfile.ACTION_SET_NOTIFY_SWITCH:
                    toast("accelerometer switch enabler set", status);
                case AccelerometerProfile.ACTION_READ_SWITCH:
                    AccelerometerSwitch aSwitch = (AccelerometerSwitch) intent.getSerializableExtra(AccelerometerProfile.EXTRA_ACCELEROMETER_SWITCH);
                    ((TextView) getView().findViewById(R.id.tv_acc_switch)).setText(String.valueOf(aSwitch));
                    break;

            }

            if (intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE) != null) {
                String toast = intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE);
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public AccelerometerFragment() {
        // Required empty public constructor
    }

    public static AccelerometerFragment newInstance() {
        AccelerometerFragment fragment = new AccelerometerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_accelerometer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewHelper.registerListener(this, getView());

        Spinner spinner = (Spinner) getView().findViewById(R.id.spin_accelerometer_range);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.accelerometer_ranges, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        AccelerometerProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);

    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {

        AccelerometerProfile profile = getAccelerometerProfile();
        if (profile == null) return;

        // reagiere auf Button-Clicks
        switch (v.getId()) {
            case R.id.btn_accelerometer_activate:
                if (v.getTag().equals("activate")) {
                    Log.d(TAG, "aktiviere");
                    profile.setEnabled(true);
                    updateStatus(getText(R.string.profile_activating).toString());
                    v.setEnabled(false);
                } else {
                    Log.d(TAG, "deaktiviere");
                    profile.setEnabled(false);
                    updateStatus(getText(R.string.profile_deactivating).toString());
                }
                break;
            // RANGE
            case R.id.btn_accelerometer_range_read:
                profile.readRange();
                break;
            case R.id.btn_accelerometer_range_set:
                if (accelerometerRange != null) {
                    profile.setRange(accelerometerRange);
                } else {
                    toast(getText(R.string.error_accelRangeNotSet).toString());
                }
                break;
            // RAW X
            case R.id.btn_acc_read_raw_x:
                profile.readRawX();
                break;
            case R.id.btn_acc_notify_raw_x:
                profile.notifyRawX(((ToggleButton) v).isChecked());
                break;

            // RAW Y
            case R.id.btn_acc_read_raw_y:
                profile.readRawY();
                break;
            case R.id.btn_acc_notify_raw_y:
                profile.notifyRawY(((ToggleButton) v).isChecked());
                break;

            // RAW Z
            case R.id.btn_acc_read_raw_z:
                profile.readRawZ();
                break;
            case R.id.btn_acc_notify_raw_z:
                profile.notifyRawZ(((ToggleButton) v).isChecked());
                break;

            // FILTER
            case R.id.btn_accelerometer_read_filter:
                profile.readFilter();
                break;
            case R.id.btn_accelerometer_set_filter:
                int fint;
                EditText f = (EditText) getView().findViewById(R.id.et_accelerometer_filter);
                String fStr = f.getText().toString();

                if ( fStr == null || fStr.isEmpty() || fStr.matches("") ) {
                    fint = 0;
                } else {
                    fint = Integer.parseInt(f.getText().toString());
                }


                if ( fint >= 0 ) {
                    if (accelerometerRange == AccelerometerRange.Minus2To2G ) {
                        if ( fint <= 65 ) {
                            profile.setFilter(fint, accelerometerRange);
                        } else {
                            toast(getText(R.string.error_filter2).toString());
                        }
                    } else if (accelerometerRange == AccelerometerRange.Minus4To4G ) {
                        if ( fint <= 131 ) {
                            profile.setFilter(fint, accelerometerRange);
                        } else {
                            toast(getText(R.string.error_filter4).toString());
                        }
                    } else if (accelerometerRange == AccelerometerRange.Minus8To8G ) {
                        if ( fint <= 262 ) {
                            profile.setFilter(fint, accelerometerRange);
                        } else {
                            toast(getText(R.string.error_filter8).toString());
                        }
                    } else {
                        toast(getText(R.string.error_accelRangeNotSet).toString());
                    }
                } else {
                    toast(getText(R.string.error_filterDefault).toString());
                }

                break;

            // EVENT CONFIG
            case R.id.btn_accelerometer_eventcfg_read:
                profile.readEventConfig();
                break;
            case R.id.btn_accelerometer_eventcfg_set:

                double high     = 0.2;
                double highTime = 0.0;
                double low      = 0.1;
                double lowTime  = 0.0;

                EditText ech    = (EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_high);
                EditText echt   = (EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_high_time);
                EditText ecl    = (EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_low);
                EditText eclt   = (EditText) getView().findViewById(R.id.et_accelerometer_eventcfg_low_time);

                String echStr = ech.getText().toString();
                String echtStr = echt.getText().toString();
                String eclStr = ecl.getText().toString();
                String ecltStr = eclt.getText().toString();

                if ( !echStr.matches("") && !echStr.isEmpty() && echStr != null ) {
                    high = Double.parseDouble(echStr);
                }
                if ( !echtStr.matches("") && !echtStr.isEmpty() && echtStr != null ) {
                    highTime = Double.parseDouble(echtStr);
                }
                if ( !eclStr.matches("") && !eclStr.isEmpty() && eclStr != null ) {
                   low = Double.parseDouble(eclStr);
                }
                if ( !ecltStr.matches("") && !ecltStr.isEmpty() && ecltStr != null ) {
                   lowTime  = Double.parseDouble(ecltStr);
                }
                if ( accelerometerRange != null ) {
                    if ( high > low ) {
                        boolean flag = true;
                        switch (accelerometerRange) {
                            case Minus2To2G:
                                if ( !(high >= 0.2 && high <= 65.534) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigHigh2G).toString());
                                }
                                if ( !(low >= 0.1 && low <= 65.533) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigLow2G).toString());
                                }
                                break;
                            case Minus4To4G:
                                if ( !(high >= 0.2 && high <= 131.068) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigHigh4G).toString());
                                }
                                if ( !(low >= 0.1 && low <= 131.066) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigLow4G).toString());
                                }
                                break;
                            case Minus8To8G:
                                if ( !(high >= 0.2 && high <= 262.136) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigHigh8G).toString());
                                }
                                if ( !(low >= 0.1 && low <= 262.132) ) {
                                    flag = false;
                                    toast(getText(R.string.error_accelEventConfigLow8G).toString());
                                }
                                break;
                        }
                        if ( !(highTime >= 0.0 && highTime <= 10.08) ) {
                            flag = false;
                            toast(getText(R.string.error_accelEventConfigHighTime).toString());
                        }
                        if ( !(lowTime >= 0.0 && lowTime <= 10.08) ) {
                            flag = false;
                            toast(getText(R.string.error_accelEventConfigLowTime).toString());
                        }
                        if ( flag ) {
                            profile.setEventConfig(low, lowTime, high, highTime, accelerometerRange);
                        }
                    } else {
                        toast(getText(R.string.error_accelEventConfig).toString());
                    }
                } else {
                    toast(getText(R.string.error_accelRangeNotSet).toString());
                }
                break;

            // EVENT MODE
            case R.id.btn_accelerometer_event_mode_read:
                profile.readEventMode();
                break;
            case R.id.btn_accelerometer_event_mode_set:
                boolean isReferenced = ((ToggleButton) getView().findViewById(R.id.btn_accelerometer_event_mode_ref)).isChecked();
                boolean isLow = ((ToggleButton) getView().findViewById(R.id.btn_accelerometer_event_mode_low)).isChecked();
                boolean isHigh = ((ToggleButton) getView().findViewById(R.id.btn_accelerometer_event_mode_high)).isChecked();
                profile.setEventMode((isReferenced ? AccelerometerEventMode.ReferencedMode : AccelerometerEventMode.AbsoluteMode), isLow, isHigh);
                break;

            // EVENT STATE
            case R.id.btn_acc_read_event_state:
                profile.readEventState();
                break;
            case R.id.btn_acc_notify_event_state:
                profile.notifyEventState(((ToggleButton) v).isChecked());

            // SWITCH SENSE
            case R.id.btn_accelerometer_read_switch_sense:
                profile.readSwitchSense();
                break;
            case R.id.btn_accelerometer_set_switch_sense:
                EditText s = (EditText) getView().findViewById(R.id.et_accelerometer_switch_sense);
                String sStr = s.getText().toString();
                Integer i;

                if ( sStr == null || sStr.isEmpty() || sStr.matches("") ) {
                    i = 0;
                } else {
                    i = Integer.parseInt(sStr);
                }

                if ( i >= 0 && i <= 10 ) {
                    profile.setSwitchSense(AccelerometerSwitchSense.valueOf(i));
                } else {
                    toast(getText(R.string.error_switchSense).toString());
                }


                break;

            // SWITCH
            case R.id.btn_acc_read_switch:
                profile.readSwitch();
                break;
            case R.id.btn_acc_notify_switch:
                profile.notifySwitch(((ToggleButton) v).isChecked());
                break;
        }
    }

    private AccelerometerProfile getAccelerometerProfile() {
        return (AccelerometerProfile) MainActivity.getProfileById(getActivity(), AccelerometerProfile.ID);
    }

    private void updateStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_accelerometer_blukii_status)).setText(newStatus);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                accelerometerRange = null;
                break;
            case 1:
                accelerometerRange = AccelerometerRange.Minus2To2G;
                break;
            case 2:
                accelerometerRange = AccelerometerRange.Minus4To4G;
                break;
            case 3:
                accelerometerRange = AccelerometerRange.Minus8To8G;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }
}

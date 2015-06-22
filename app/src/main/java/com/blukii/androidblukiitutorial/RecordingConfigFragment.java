package com.blukii.androidblukiitutorial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.AccelerometerEventMode;
import com.blukii.android.blukiilibrary.AccelerometerRange;
import com.blukii.android.blukiilibrary.AltimeterMode;
import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.LightMode;
import com.blukii.android.blukiilibrary.MagnetometerMode;
import com.blukii.android.blukiilibrary.MagnetometerRange;
import com.blukii.android.blukiilibrary.Profile;
import com.blukii.android.blukiilibrary.RecordingMode;
import com.blukii.android.blukiilibrary.RecordingProfile;
import com.blukii.android.blukiilibrary.RecordingSensor;

import java.util.ArrayList;


public class RecordingConfigFragment extends AbstractFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private final static String TAG = "REC";
    private EnablerStatus enablerStatus = null;
    private RecordingMode recordingMode = null;
    private AccelerometerRange accelerometerRange = null;
    private LightMode lightMode = null;
    private MagnetometerRange magnetometerRange = null;
    private MagnetometerMode magnetometerMode = null;
    private AltimeterMode altimeterMode = null;
    private int numberOfEventSensors = 0;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            RecordingProfile profile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);
            ToggleButton btnEnabled = getToggleButton(R.id.btn_recording_enabled_set);

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    ViewHelper.setEnabledOfAllControls(true, getView());

                    // Exceptions
                    btnEnabled.setEnabled(false);
                    getCheckBox(R.id.cb_recording_mode_event).setEnabled(false);

                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                // ENABLE
                case RecordingProfile.ACTION_SET_RECORDING_ENABLED:
                case RecordingProfile.ACTION_READ_RECORDING_ENABLED:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                        btnEnabled.setEnabled(true);
                        switch (enablerStatus) {
                            case Activated:
                                btnEnabled.setChecked(true);
                                break;
                            case Deactivated:
                                btnEnabled.setChecked(false);
                                break;
                        }
                    }
                    break;
                // PASSWORD
                case RecordingProfile.ACTION_SET_RECORDING_PASSWORD:
                    toast("Password set", status);
                    setEnabledOfPasswordButtons(true);
                    break;
                case RecordingProfile.ACTION_SET_RECORDING_SENSORS:
                    toast("Sensors set", status);
                    profile.readSensors();
                    break;
                // SENSORS
                case RecordingProfile.ACTION_READ_RECORDING_SENSORS:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        setCheckedOfSensors(false);
                        RecordingSensor[] sensors = (RecordingSensor[]) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (sensors != null) {
                            for (RecordingSensor s : sensors) {
                                switch (s) {
                                    case Accelerometer:
                                        getCheckBox(R.id.cb_recording_sensor_accelerometer).setChecked(true);
                                        break;
                                    case Altimeter:
                                        getCheckBox(R.id.cb_recording_sensor_altimeter).setChecked(true);
                                        break;
                                    case Battery:
                                        getCheckBox(R.id.cb_recording_sensor_battery).setChecked(true);
                                        break;
                                    case Humidity:
                                        getCheckBox(R.id.cb_recording_sensor_humidity).setChecked(true);
                                        break;
                                    case Light:
                                        getCheckBox(R.id.cb_recording_sensor_light).setChecked(true);
                                        break;
                                    case Magneticfield:
                                        getCheckBox(R.id.cb_recording_sensor_magnetometer).setChecked(true);
                                        break;
                                    case StepCounter:
                                        getCheckBox(R.id.cb_recording_sensor_step_counter).setChecked(true);
                                        break;
                                    case Temperature:
                                        getCheckBox(R.id.cb_recording_sensor_temperature).setChecked(true);
                                        break;
                                }
                            }
                        }
                    }
                    setEnabledOfSensors(true);
                    break;
                // MODE
                case RecordingProfile.ACTION_SET_RECORDING_MODE:
                    toast("Mode set", status);
                    profile.readMode();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        recordingMode = (RecordingMode) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        CheckBox interval = getCheckBox(R.id.cb_recording_mode_interval);
                        CheckBox event = getCheckBox(R.id.cb_recording_mode_event);
                        if (recordingMode != null) {
                            switch (recordingMode) {
                                case Interval:
                                    interval.setChecked(true);
                                    event.setChecked(false);
                                    getView().findViewById(R.id.rec_config_elem_interval).setVisibility(View.VISIBLE);

                                    if (getCheckBox(R.id.cb_recording_sensor_accelerometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_accelRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventConfig).setVisibility(View.GONE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventMode).setVisibility(View.GONE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_altimeter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_altMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_altEventConfig).setVisibility(View.GONE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_light).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_lightEventConfig).setVisibility(View.GONE);
                                        getView().findViewById(R.id.rec_config_elem_lightMode).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_magnetometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_magRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magEventConfig).setVisibility(View.GONE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_step_counter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_step).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_temperature).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_tempEventConfig).setVisibility(View.GONE);
                                    }
                                    break;
                                case Event:
                                    interval.setChecked(false);
                                    event.setChecked(true);
                                    getView().findViewById(R.id.rec_config_elem_interval).setVisibility(View.GONE);

                                    if (getCheckBox(R.id.cb_recording_sensor_accelerometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_accelRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventConfig).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventMode).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_altimeter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_altMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_altEventConfig).setVisibility(View.VISIBLE);
                                    }

                                    if (getCheckBox(R.id.cb_recording_sensor_light).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_lightEventConfig).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_lightMode).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_magnetometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_magRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magEventConfig).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_step_counter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_step).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_temperature).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_tempEventConfig).setVisibility(View.VISIBLE);
                                    }
                                    break;
                                case IntervalAndEvent:
                                    interval.setChecked(true);
                                    event.setChecked(true);
                                    getView().findViewById(R.id.rec_config_elem_interval).setVisibility(View.VISIBLE);

                                    if (getCheckBox(R.id.cb_recording_sensor_accelerometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_accelRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventConfig).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_accelEventMode).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_altimeter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_altMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_altEventConfig).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_light).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_lightEventConfig).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_lightMode).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_magnetometer).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_magRange).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magMode).setVisibility(View.VISIBLE);
                                        getView().findViewById(R.id.rec_config_elem_magEventConfig).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_step_counter).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_step).setVisibility(View.VISIBLE);
                                    }
                                    if (getCheckBox(R.id.cb_recording_sensor_temperature).isChecked()) {
                                        getView().findViewById(R.id.rec_config_elem_tempEventConfig).setVisibility(View.VISIBLE);
                                    }

                                    break;
                            }
                            getView().findViewById(R.id.rec_config_elem_dataLimit).setVisibility(View.VISIBLE);
                        } else {
                            interval.setChecked(false);
                            event.setChecked(false);
                        }
                    }
                    break;
                // INTERVAL
                case RecordingProfile.ACTION_SET_RECORDING_INTERVAL:
                    toast("Interval set", status);
                    profile.readInterval();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_INTERVAL:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        EditText et = getEditText(R.id.et_recording_interval_value);
                        int v = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_VALUE, 0);
                        et.setText(String.valueOf(v));
                    }
                    break;
                // LIMIT
                case RecordingProfile.ACTION_SET_RECORDING_DATASET_LIMIT:
                    toast("Limit set", status);
                    profile.readDatasetLimit();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_DATASET_LIMIT:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        EditText et = getEditText(R.id.et_recording_limit_value);
                        int v = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_VALUE, 999);
                        et.setText(String.valueOf(v));
                    }
                    break;
                // ACCELEROMETER RANGE
                case RecordingProfile.ACTION_SET_RECORDING_ACCELEROMETER_RANGE:
                    toast("Accelerometer range set", status);
                    profile.readAccelerometerRange();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_ACCELEROMETER_RANGE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        accelerometerRange = (AccelerometerRange) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (accelerometerRange != null) {
                            Spinner spinner = getSpinner(R.id.spin_accelerometer_range);
                            spinner.setSelection(accelerometerRange.ordinal() + 1);
                        }
                    }
                    break;
                // ACCELEROMETER EVENT CONFIG
                case RecordingProfile.ACTION_SET_RECORDING_ACCELEROMETER_EVENT_CONFIG:
                    toast("Accelerometer Event Config set", status);
                    profile.readAccelerometerEventConfig();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_ACCELEROMETER_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        Double high = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_HIGH, -1);
                        getEditText(R.id.et_accelerometer_eventcfg_high).setText(high.toString());
                        Double highTime = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_HIGH_TIME, -1);
                        getEditText(R.id.et_accelerometer_eventcfg_high_time).setText(highTime.toString());
                        Double low = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_LOW, -1);
                        getEditText(R.id.et_accelerometer_eventcfg_low).setText(low.toString());
                        Double lowTime = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_LOW_TIME, -1);
                        getEditText(R.id.et_accelerometer_eventcfg_low_time).setText(lowTime.toString());
                    }
                    break;
                // ACCELEROMETER EVENT MODE
                case RecordingProfile.ACTION_SET_RECORDING_ACCELEROMETER_EVENT_MODE:
                    toast("accelerometer event mode set", status);
                    profile.readAccelerometerEventMode();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_ACCELEROMETER_EVENT_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        AccelerometerEventMode mode = (AccelerometerEventMode) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        boolean referenced = (mode == AccelerometerEventMode.ReferencedMode);
                        boolean lowEvent = intent.getBooleanExtra(RecordingProfile.EXTRA_RECORDING_LOW, false);
                        boolean highEvent = intent.getBooleanExtra(RecordingProfile.EXTRA_RECORDING_HIGH, false);
                        getToggleButton(R.id.btn_accelerometer_event_mode_ref).setChecked(referenced);
                        getToggleButton(R.id.btn_accelerometer_event_mode_low).setChecked(lowEvent);
                        getToggleButton(R.id.btn_accelerometer_event_mode_high).setChecked(highEvent);
                    }
                    break;

                // MAGNETOMETER RANGE
                case RecordingProfile.ACTION_SET_RECORDING_MAGNETOMETER_RANGE:
                    toast("magnetometer range set", status);
                    profile.readMagnetometerRange();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_MAGNETOMETER_RANGE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        magnetometerRange = (MagnetometerRange) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (magnetometerRange != null) {
                            Spinner spinner = getSpinner(R.id.spin_magnetometer_range);
                            spinner.setSelection(magnetometerRange.ordinal() + 1);
                        }
                    }
                    break;
                // MAGNETOMETER EVENT CONFIG
                case RecordingProfile.ACTION_SET_RECORDING_MAGNETOMETER_EVENT_CONFIG:
                    toast("magnetometer event config set", status);
                    profile.readMagnetometerEventConfig();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_MAGNETOMETER_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int threshold = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_THRESHOLD, 0);
                        getEditText(R.id.et_magnetometer_eventcfg_threshold).setText(String.valueOf(threshold));
                    }
                    break;
                // MAGNETOMETER MODE
                case RecordingProfile.ACTION_SET_RECORDING_MAGNETOMETER_MODE:
                    toast("magnetometer mode set", status);
                    profile.readMagnetometerMode();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_MAGNETOMETER_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        magnetometerMode = (MagnetometerMode) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (magnetometerMode != null) {
                            Spinner spinner = getSpinner(R.id.spin_magnetometer_mode);
                            spinner.setSelection(magnetometerMode.ordinal() + 1);
                        }
                    }
                    break;
                // LIGHT MODE
                case RecordingProfile.ACTION_SET_RECORDING_LIGHT_MODE:
                    toast("light mode set", status);
                    profile.readLightMode();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_LIGHT_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        lightMode = (LightMode) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (lightMode != null) {
                            Spinner spinner = getSpinner(R.id.spin_light_mode);
                            spinner.setSelection(lightMode.ordinal() + 1);
                        }
                    }
                    break;
                // LIGHT EVENT CONFIG
                case RecordingProfile.ACTION_SET_RECORDING_LIGHT_EVENT_CONFIG:
                    toast("light event config set", status);
                    profile.readLightEventConfig();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_LIGHT_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int low = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_LOW, 0);
                        int high = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_HIGH, 0);
                        getEditText(R.id.et_light_eventcfg_low).setText(String.valueOf(low));
                        getEditText(R.id.et_light_eventcfg_high).setText(String.valueOf(high));
                    }
                    break;
                // TEMPERATURE EVENT CONFIG
                case RecordingProfile.ACTION_SET_RECORDING_TEMPERATURE_EVENT_CONFIG:
                    toast("temperature event config set", status);
                    profile.readTemperatureEventConfig();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_TEMPERATURE_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        double low = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_LOW, 0);
                        double high = intent.getDoubleExtra(RecordingProfile.EXTRA_RECORDING_HIGH, 0);
                        getEditText(R.id.et_temperature_eventcfg_low).setText(String.valueOf(low));
                        getEditText(R.id.et_temperature_eventcfg_high).setText(String.valueOf(high));
                    }
                    break;
                // ALTIMETER MODE
                case RecordingProfile.ACTION_SET_RECORDING_ALTIMETER_MODE:
                    toast("altimeter mode set", status);
                    profile.readAltimeterMode();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_ALTIMETER_MODE:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        altimeterMode = (AltimeterMode) intent.getSerializableExtra(RecordingProfile.EXTRA_RECORDING_VALUE);
                        if (altimeterMode != null) {
                            Spinner spinner = getSpinner(R.id.spin_altimeter_mode);
                            spinner.setSelection(altimeterMode.ordinal() + 1);
                        }
                    }
                    break;
                // ALTIMETER MODE
                case RecordingProfile.ACTION_SET_RECORDING_ALTIMETER_EVENT_CONFIG:
                    toast("altimeter event config set", status);
                    profile.readAltimeterEventConfig();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_ALTIMETER_EVENT_CONFIG:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int threshold = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_THRESHOLD, 0);
                        getEditText(R.id.et_altimeter_eventcfg_threshold).setText(String.valueOf(threshold));
                    }
                    break;
                // STEP DETECTION
                case RecordingProfile.ACTION_SET_RECORDING_STEP_DETECTION_THRESHOLD:
                    toast("step detection threshold set", status);
                    profile.readStepDetection();
                    break;
                case RecordingProfile.ACTION_READ_RECORDING_STEP_DETECTION:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        int threshold = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_THRESHOLD, -1);
                        int counter = intent.getIntExtra(RecordingProfile.EXTRA_RECORDING_VALUE, -1);
                        EditText t = getEditText(R.id.et_step_detection_threshold);
                        TextView c = getTextView(R.id.tv_step_detection_value);
                        if (threshold > 0) {
                            t.setText(String.valueOf(threshold));
                        } else {
                            t.setText(R.string.undefined);
                        }
                        if (counter > 0) {
                            c.setText(String.valueOf(counter));
                        } else {
                            c.setText(R.string.undefined);
                        }
                    }
                    break;
            }

            if (intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE) != null) {
                toast(intent.getStringExtra(BlukiiConstants.EXTRA_ERROR_MESSAGE));
            }

        }
    };


    public RecordingConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DirectometerFragment.
     */
    public static RecordingConfigFragment newInstance() {
        RecordingConfigFragment fragment = new RecordingConfigFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ViewHelper.registerListener(this, getView());

        initSpinner(R.id.spin_accelerometer_range, R.array.accelerometer_ranges, this);
        initSpinner(R.id.spin_magnetometer_range, R.array.magnetometer_ranges, this);
        initSpinner(R.id.spin_magnetometer_mode, R.array.magnetometer_modes, this);
        initSpinner(R.id.spin_light_mode, R.array.light_modes, this);
        initSpinner(R.id.spin_altimeter_mode, R.array.altimeter_modes, this);

        ViewHelper.setEnabledOfAllControls(false, getView());
        //Hide unnecessary Elements
        setElementsGone();

        getView().findViewById(R.id.step_detection_value_view).setVisibility(View.INVISIBLE);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recording_config, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        RecordingProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }


    public void onCheck(View v) {

    }


    @Override
    public void onClick(View v) {

        RecordingProfile recProfile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);


        if (recProfile == null) return;

        switch (v.getId()) {
            // ENABLE
            case R.id.btn_recording_enabled_read:
                recProfile.readEnabled();
                break;
            case R.id.btn_recording_enabled_set:
                if (enablerStatus != null) {
                    v.setEnabled(false);
                    switch (enablerStatus) {
                        case Activated:
                            recProfile.setEnabled(false);
                            break;
                        case Deactivated:
                            recProfile.setEnabled(true);
                            break;
                    }
                }
                break;
            // PASSWORD
            case R.id.btn_recording_password:
                setEnabledOfPasswordButtons(false);
                recProfile.setPassword(42);
                break;
            case R.id.btn_recording_password_reset:
                setEnabledOfPasswordButtons(false);
                recProfile.resetPassword();
                break;


            // SENSORS
            case R.id.btn_recording_sensors_read:
                setEnabledOfSensors(false);
                recProfile.readSensors();
                break;

            //Checkboxes
            case R.id.cb_recording_sensor_accelerometer:
                if (getCheckBox(R.id.cb_recording_sensor_accelerometer).isChecked()) {
                    getCheckBox(R.id.cb_recording_sensor_step_counter).setEnabled(false);
                    numberOfEventSensors++;
                } else {
                    getCheckBox(R.id.cb_recording_sensor_step_counter).setEnabled(true);
                    numberOfEventSensors--;
                }
                break;

            case R.id.cb_recording_sensor_altimeter:
                if (getCheckBox(R.id.cb_recording_sensor_altimeter).isChecked()) {
                    numberOfEventSensors++;
                } else {
                    getCheckBox(R.id.cb_recording_sensor_altimeter).setEnabled(true);
                    numberOfEventSensors--;
                }
                break;

            case R.id.cb_recording_sensor_light:
                if (getCheckBox(R.id.cb_recording_sensor_light).isChecked()) {
                    numberOfEventSensors++;
                } else {
                    getCheckBox(R.id.cb_recording_sensor_light).setEnabled(true);
                    numberOfEventSensors--;
                }
                break;

            case R.id.cb_recording_sensor_magnetometer:
                if (getCheckBox(R.id.cb_recording_sensor_magnetometer).isChecked()) {
                    numberOfEventSensors++;
                } else {
                    getCheckBox(R.id.cb_recording_sensor_magnetometer).setEnabled(true);
                    numberOfEventSensors--;
                }
                break;

            case R.id.cb_recording_sensor_step_counter:
                if (getCheckBox(R.id.cb_recording_sensor_step_counter).isChecked()) {
                    getCheckBox(R.id.cb_recording_sensor_accelerometer).setEnabled(false);
                } else {
                    getCheckBox(R.id.cb_recording_sensor_accelerometer).setEnabled(true);
                }
                break;

            case R.id.cb_recording_sensor_temperature:
                if (getCheckBox(R.id.cb_recording_sensor_temperature).isChecked()) {
                    numberOfEventSensors++;
                } else {
                    getCheckBox(R.id.cb_recording_sensor_temperature).setEnabled(true);
                    numberOfEventSensors--;
                }
                break;



            case R.id.btn_recording_sensors_set:
                setEnabledOfSensors(false);
                ArrayList<RecordingSensor> sensors = new ArrayList<>();

                if (getCheckBox(R.id.cb_recording_sensor_accelerometer).isChecked()) {
                    sensors.add(RecordingSensor.Accelerometer);
                }
                if (getCheckBox(R.id.cb_recording_sensor_altimeter).isChecked()) {
                    sensors.add(RecordingSensor.Altimeter);
                }
                if (getCheckBox(R.id.cb_recording_sensor_battery).isChecked()) {
                    sensors.add(RecordingSensor.Battery);
                }
                if (getCheckBox(R.id.cb_recording_sensor_humidity).isChecked()) {
                    sensors.add(RecordingSensor.Humidity);
                }
                if (getCheckBox(R.id.cb_recording_sensor_light).isChecked()) {
                    sensors.add(RecordingSensor.Light);
                }
                if (getCheckBox(R.id.cb_recording_sensor_magnetometer).isChecked()) {
                    sensors.add(RecordingSensor.Magneticfield);
                }
                if (getCheckBox(R.id.cb_recording_sensor_step_counter).isChecked()) {
                    sensors.add(RecordingSensor.StepCounter);
                }
                if (getCheckBox(R.id.cb_recording_sensor_temperature).isChecked()) {
                    sensors.add(RecordingSensor.Temperature);
                }

                if ( !sensors.isEmpty() ) {
                    recProfile.setSensors(sensors.toArray(new RecordingSensor[sensors.size()]));
                } else {
                    toast(getText(R.string.error_recSensorConfig).toString());
                    setEnabledOfSensors(true);
                }


                break;
            // MODE
            case R.id.btn_recording_mode_read:
                recProfile.readMode();
                break;
            case R.id.btn_recording_mode_set:
                CheckBox interval = getCheckBox(R.id.cb_recording_mode_interval);
                CheckBox event = getCheckBox(R.id.cb_recording_mode_event);
                if (interval.isChecked() && event.isChecked()) {
                    recProfile.setMode(RecordingMode.IntervalAndEvent);
                } else if (interval.isChecked()) {
                    recProfile.setMode(RecordingMode.Interval);
                } else if (event.isChecked()) {
                    recProfile.setMode(RecordingMode.Event);
                } else {
                    toast(getText(R.string.error_recMode).toString());
                }
                break;
            // INTERVAL
            case R.id.btn_recording_interval_read:
                recProfile.readInterval();
                break;
            case R.id.btn_recording_interval_set:
                EditText etIntervalValue = getEditText(R.id.et_recording_interval_value);
                String etIntervalValueStr = etIntervalValue.getText().toString();
                if ( !etIntervalValueStr.matches("") && !etIntervalValueStr.isEmpty() && etIntervalValueStr != null ) {
                    int i = Integer.parseInt(etIntervalValueStr);
                    if ( i >= 100 && i <= 3600000) {
                        recProfile.setInterval(i);
                    } else {
                        toast(getText(R.string.error_recInterval).toString());
                    }
                } else {
                    toast(getText(R.string.error_enterNumber).toString());
                }
                break;
            // LIMIT
            case R.id.btn_recording_limit_read:
                recProfile.readDatasetLimit();
                break;
            case R.id.btn_recording_limit_set:
                EditText etLimitValue = getEditText(R.id.et_recording_limit_value);
                String etLimitValueStr = etLimitValue.getText().toString();

                if ( !etLimitValueStr.matches("") && !etLimitValueStr.isEmpty() && etLimitValueStr != null ) {
                    int l = Integer.parseInt(etLimitValueStr);
                    if ( l >= 0 && l <= 32767 ) {
                        recProfile.setDatasetLimit(l);
                    } else {
                        toast(getText(R.string.error_recDatasetLimit).toString());
                    }
                } else {
                    toast(getText(R.string.error_enterNumber).toString());
                }
                break;
            // ACCELEROMETER RANGE
            case R.id.btn_accelerometer_range_read:
                recProfile.readAccelerometerRange();
                break;
            case R.id.btn_accelerometer_range_set:
                if (accelerometerRange != null) {
                    recProfile.setAccelerometerRange(accelerometerRange);
                } else {
                    toast(getText(R.string.error_accelRangeNotSet).toString());
                }
                break;
            // ACCELEROMETER EVENT CONFIG
            case R.id.btn_accelerometer_eventcfg_read:
                recProfile.readAccelerometerEventConfig();
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
                            recProfile.setAccelerometerEventConfig(low, lowTime, high, highTime, accelerometerRange);
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
                recProfile.readAccelerometerEventMode();
                break;
            case R.id.btn_accelerometer_event_mode_set:
                boolean isReferenced = getToggleButton(R.id.btn_accelerometer_event_mode_ref).isChecked();
                boolean isLow = getToggleButton(R.id.btn_accelerometer_event_mode_low).isChecked();
                boolean isHigh = getToggleButton(R.id.btn_accelerometer_event_mode_high).isChecked();
                recProfile.setAccelerometerEventMode((isReferenced ? AccelerometerEventMode.ReferencedMode : AccelerometerEventMode.AbsoluteMode), isLow, isHigh);
                break;
            // MAGNETOMETER RANGE
            case R.id.btn_magnetometer_range_read:
                recProfile.readMagnetometerRange();
                break;
            case R.id.btn_magnetometer_range_set:
                if (magnetometerRange != null) {
                    recProfile.setMagnetometerRange(magnetometerRange);
                } else {
                    toast(getText(R.string.error_magRange).toString());
                }
                break;
            // MAGNETOMETER EVENT CONFIG
            case R.id.btn_magnetometer_eventcfg_read:
                recProfile.readMagnetometerEventConfig();
                break;
            case R.id.btn_magnetometer_eventcfg_set:
                EditText etThresholdValue = getEditText(R.id.et_magnetometer_eventcfg_threshold);
                String etThresholdValueStr = etThresholdValue.getText().toString();

                if ( !etThresholdValueStr.matches("") && !etThresholdValueStr.isEmpty() && etThresholdValueStr != null ) {
                    int threshold = Integer.parseInt(etThresholdValueStr);
                    if ( magnetometerRange != null ) {
                        switch (magnetometerRange) {
                            case Minus4To4Gauss:
                                if ( threshold >= 0 && threshold <= 4 ) {
                                    recProfile.setMagnetometerEventConfig(threshold, magnetometerRange);
                                } else {
                                    toast(getText(R.string.error_magEventConfig).toString() + getText(R.string.error_magEventConfig4G));
                                }
                                break;
                            case Minus8To8Gauss:
                                if ( threshold >= 0 && threshold <= 9 ) {
                                    recProfile.setMagnetometerEventConfig(threshold, magnetometerRange);
                                } else {
                                    toast(getText(R.string.error_magEventConfig).toString() + getText(R.string.error_magEventConfig8G));
                                }
                                break;
                            case Minus12To12Gauss:
                                if ( threshold >= 0 && threshold <= 14 ) {
                                    recProfile.setMagnetometerEventConfig(threshold, magnetometerRange);
                                } else {
                                    toast(getText(R.string.error_magEventConfig).toString() + getText(R.string.error_magEventConfig12G));
                                }
                                break;
                            case Minus16To16Gauss:
                                if ( threshold >= 0 && threshold <= 19 ) {
                                    recProfile.setMagnetometerEventConfig(threshold, magnetometerRange);
                                } else {
                                    toast(getText(R.string.error_magEventConfig).toString() + getText(R.string.error_magEventConfig16G));
                                }
                                break;
                        }
                    } else {
                        toast(getText(R.string.error_magRange).toString());
                    }
                } else {
                    toast(getText(R.string.error_enterNumber).toString());
                }
                break;
            // MAGNETOMETER MODE
            case R.id.btn_magnetometer_mode_read:
                recProfile.readMagnetometerMode();
                break;
            case R.id.btn_magnetometer_mode_set:
                if (magnetometerMode != null) {
                    recProfile.setMagnetometerMode(magnetometerMode);
                } else {
                    toast("Please select a magnetometer mode");
                }
                break;
            // LIGHT MODE
            case R.id.btn_light_mode_read:
                recProfile.readLightMode();
                break;
            case R.id.btn_light_mode_set:
                if (lightMode != null) {
                    recProfile.setLightMode(lightMode);
                } else {
                    toast("Please select a light mode");
                }
                break;
            // LIGHT EVENT CONFIG
            case R.id.btn_light_eventcfg_read:
                recProfile.readLightEventConfig();
                break;
            case R.id.btn_light_eventcfg_set:
                String minText = ((EditText) getView().findViewById(R.id.et_light_eventcfg_low)).getText().toString();
                String maxText = ((EditText) getView().findViewById(R.id.et_light_eventcfg_high)).getText().toString();
                int minValue = 1;
                int maxValue = 2;
                if ( lightMode != null ) {
                    if ( !minText.matches("") && !minText.isEmpty() && minText != null ) {
                        minValue = Integer.parseInt(minText);
                    }
                    if ( !maxText.matches("") && !maxText.isEmpty() && maxText != null ) {
                        maxValue = Integer.parseInt(maxText);
                    }
                    if ( minValue < maxValue ) {
                        switch (lightMode) {
                            case Als1000Lux: case Ir1000Lux:
                                if ( minValue > 0 && maxValue < 1000 ) {
                                    recProfile.setLightEventConfig(minValue, maxValue, lightMode);
                                } else {
                                    toast(getText(R.string.error_lightRange).toString() + getText(R.string.error_lightRange1000).toString());
                                }
                                break;
                            case Als4000Lux: case Ir4000Lux:
                                if ( minValue > 0 && maxValue < 4000 ) {
                                    recProfile.setLightEventConfig(minValue, maxValue, lightMode);
                                } else {
                                    toast(getText(R.string.error_lightRange).toString() + getText(R.string.error_lightRange4000).toString());
                                }
                                break;
                            case Als16000Lux: case Ir16000Lux:
                                if ( minValue > 0 && maxValue < 16000 ) {
                                    recProfile.setLightEventConfig(minValue, maxValue, lightMode);
                                } else {
                                    toast(getText(R.string.error_lightRange).toString() + getText(R.string.error_lightRange16000).toString());
                                }
                                break;
                            case Als64000Lux: case Ir64000Lux:
                                if ( minValue > 0 && maxValue < 64000 ) {
                                    recProfile.setLightEventConfig(minValue, maxValue, lightMode);
                                } else {
                                    toast(getText(R.string.error_lightRange).toString() + getText(R.string.error_lightRange64000).toString());
                                }
                                break;
                        }
                    } else {
                        toast(getText(R.string.error_lightHighLow).toString());
                    }
                } else {
                    toast(getText(R.string.error_lightMode).toString());
                }
                break;
            // TEMPERATURE EVENT CONFIG
            case R.id.btn_temperature_eventcfg_read:
                recProfile.readTemperatureEventConfig();
                break;
            case R.id.btn_temperature_eventcfg_set:

                String minTemp = ((EditText) getView().findViewById(R.id.et_temperature_eventcfg_low)).getText().toString();
                String maxTemp = ((EditText) getView().findViewById(R.id.et_temperature_eventcfg_high)).getText().toString();
                double tempLow = 0.0;
                double tempHigh = 1.0;
                if ( !minTemp.matches("") && !minTemp.isEmpty() && minTemp != null ) {
                    tempLow = Double.parseDouble(minTemp);
                }
                if ( !maxTemp.matches("") && !maxTemp.isEmpty() && maxTemp != null) {
                    tempHigh = Double.parseDouble(maxTemp);
                }
                if (tempLow < tempHigh) {
                    if (tempLow >= -2048 && tempHigh < 2048) {
                        recProfile.setTemperatureEventConfig(tempLow, tempHigh);
                    } else {
                        toast(getText(R.string.error_tempRange).toString());
                    }
                } else {
                    toast(getText(R.string.error_tempHighLow).toString());
                }

                break;
            // ALTIMETER MODE
            case R.id.btn_altimeter_mode_read:
                recProfile.readAltimeterMode();
                break;
            case R.id.btn_altimeter_mode_set:
                if (altimeterMode != null) {
                    recProfile.setAltimeterMode(altimeterMode);
                } else {
                    toast("Please select an altimeter mode");
                }
                break;
            // ALTIMETER EVENT CONFIG
            case R.id.btn_altimeter_eventcfg_read:
                recProfile.readAltimeterEventConfig();
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
                                recProfile.setAltimeterEventConfig(threshold, altimeterMode);
                            } else {
                                toast(getText(R.string.error_altitudeRange).toString());
                            }
                            break;
                        case Barometer:
                            if ( threshold >= 2 && threshold <= 131070 ) {
                                recProfile.setAltimeterEventConfig(threshold, altimeterMode);
                            } else {
                                toast(getText(R.string.error_barometerRange).toString());
                            }
                            break;
                    }
                } else {
                    toast(getText(R.string.error_altiModeNotSet).toString());
                }


                break;
            // STEP DETECTION
            case R.id.btn_step_detection_read:
                recProfile.readStepDetection();
                break;
            case R.id.btn_step_detection_set:
                String stepDetThresholdStr = getEditText(R.id.et_step_detection_threshold).getText().toString();
                if ( !stepDetThresholdStr.matches("") && !stepDetThresholdStr.isEmpty() && stepDetThresholdStr != null ) {
                    int stepDetThreshold = Integer.parseInt(stepDetThresholdStr);
                    if ( stepDetThreshold >= 1 && stepDetThreshold <= 50) {
                        recProfile.setStepDetectionThreshold(stepDetThreshold);
                    } else {
                        toast(getText(R.string.error_stepDetThreshold).toString());
                    }
                } else {
                    toast(getText(R.string.error_enterNumber).toString());
                }
                break;

            case R.id.btn_recording_resetConfig:
                setElementsGone();
                setCheckedOfSensors(false);
                getCheckBox(R.id.cb_recording_mode_interval).setChecked(false);
                getCheckBox(R.id.cb_recording_mode_event).setChecked(false);
                break;
        }


        if (numberOfEventSensors > 0) {
            getCheckBox(R.id.cb_recording_mode_event).setEnabled(true);
        } else {
            getCheckBox(R.id.cb_recording_mode_event).setEnabled(false);
        }
    }




    private void setEnabledOfPasswordButtons(boolean enabled) {
        getView().findViewById(R.id.btn_recording_password).setEnabled(enabled);
        getView().findViewById(R.id.btn_recording_password_reset).setEnabled(enabled);
    }

    private void setEnabledOfSensors(boolean enabled) {
        getView().findViewById(R.id.btn_recording_sensors_read).setEnabled(enabled);
        getView().findViewById(R.id.btn_recording_sensors_set).setEnabled(enabled);

        getCheckBox(R.id.cb_recording_sensor_accelerometer).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_altimeter).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_battery).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_humidity).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_light).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_magnetometer).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_step_counter).setEnabled(enabled);
        getCheckBox(R.id.cb_recording_sensor_temperature).setEnabled(enabled);
    }

    private void setCheckedOfSensors(boolean checked) {
        getCheckBox(R.id.cb_recording_sensor_accelerometer).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_altimeter).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_battery).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_humidity).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_light).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_magnetometer).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_step_counter).setChecked(checked);
        getCheckBox(R.id.cb_recording_sensor_temperature).setChecked(checked);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spin_accelerometer_range:
                if (position > 0) {
                    accelerometerRange = AccelerometerRange.values()[position - 1];
                } else {
                    accelerometerRange = null;
                }
                break;
            case R.id.spin_magnetometer_range:
                if (position > 0) {
                    magnetometerRange = MagnetometerRange.values()[position - 1];
                } else {
                    magnetometerRange = null;
                }
                break;
            case R.id.spin_magnetometer_mode:
                if (position > 0) {
                    magnetometerMode = MagnetometerMode.values()[position - 1];
                } else {
                    magnetometerMode = null;
                }
                break;
            case R.id.spin_light_mode:
                if (position > 0) {
                    lightMode = LightMode.values()[position - 1];
                } else {
                    lightMode = null;
                }
                break;
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

    private void setElementsGone() {
        getView().findViewById(R.id.rec_config_elem_dataLimit).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_interval).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_dataLimit).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_accelRange).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_accelEventConfig).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_accelEventMode).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_magRange).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_magEventConfig).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_magMode).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_lightMode).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_lightEventConfig).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_tempEventConfig).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_altMode).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_altEventConfig).setVisibility(View.GONE);
        getView().findViewById(R.id.rec_config_elem_step).setVisibility(View.GONE);
    }
}


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

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);

            RecordingProfile profile = (RecordingProfile) MainActivity.getProfileById(getActivity(), RecordingProfile.ID);
            ToggleButton btnEnabled = getToggleButton(R.id.btn_recording_enabled_set);

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    updateBlukiiStatus(R.string.blukii_disconnected);
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateBlukiiStatus(R.string.blukii_connected);
                    ViewHelper.setEnabledOfAllControls(true, getView());

                    // Exceptions
                    btnEnabled.setEnabled(false);

                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateBlukiiStatus(R.string.blukii_disconnected);
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
                                    break;
                                case Event:
                                    interval.setChecked(false);
                                    event.setChecked(true);
                                    break;
                                case IntervalAndEvent:
                                    interval.setChecked(true);
                                    event.setChecked(true);
                                    break;
                            }
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

                recProfile.setSensors(sensors.toArray(new RecordingSensor[sensors.size()]));
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
                    toast("Please select one mode at least");
                }
                break;
            // INTERVAL
            case R.id.btn_recording_interval_read:
                recProfile.readInterval();
                break;
            case R.id.btn_recording_interval_set:
                EditText etIntervalValue = getEditText(R.id.et_recording_interval_value);
                int i = Integer.parseInt(etIntervalValue.getText().toString());
                recProfile.setInterval(i);
                break;
            // LIMIT
            case R.id.btn_recording_limit_read:
                recProfile.readDatasetLimit();
                break;
            case R.id.btn_recording_limit_set:
                EditText etLimitValue = getEditText(R.id.et_recording_limit_value);
                int l = Integer.parseInt(etLimitValue.getText().toString());
                recProfile.setDatasetLimit(l);
                break;
            // ACCELEROMETER RANGE
            case R.id.btn_accelerometer_range_read:
                recProfile.readAccelerometerRange();
                break;
            case R.id.btn_accelerometer_range_set:
                if (accelerometerRange != null) {
                    recProfile.setAccelerometerRange(accelerometerRange);
                } else {
                    toast("Please select an accelerometer range");
                }
                break;
            // ACCELEROMETER EVENT CONFIG
            case R.id.btn_accelerometer_eventcfg_read:
                recProfile.readAccelerometerEventConfig();
                break;
            case R.id.btn_accelerometer_eventcfg_set:
                EditText ech = getEditText(R.id.et_accelerometer_eventcfg_high);
                EditText echt = getEditText(R.id.et_accelerometer_eventcfg_high_time);
                EditText ecl = getEditText(R.id.et_accelerometer_eventcfg_low);
                EditText eclt = getEditText(R.id.et_accelerometer_eventcfg_low_time);
                double high = Double.parseDouble(ech.getText().toString());
                double highTime = Double.parseDouble(echt.getText().toString());
                double low = Double.parseDouble(ecl.getText().toString());
                double lowTime = Double.parseDouble(eclt.getText().toString());
                recProfile.setAccelerometerEventConfig(low, lowTime, high, highTime, accelerometerRange);
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
                    toast("Please select a magnetometer range");
                }
                break;
            // MAGNETOMETER EVENT CONFIG
            case R.id.btn_magnetometer_eventcfg_read:
                recProfile.readMagnetometerEventConfig();
                break;
            case R.id.btn_magnetometer_eventcfg_set:
                int threshold = Integer.parseInt(getEditText(R.id.et_magnetometer_eventcfg_threshold).getText().toString());
                recProfile.setMagnetometerEventConfig(threshold, magnetometerRange);
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
                int lightLow = Integer.parseInt(getEditText(R.id.et_light_eventcfg_low).getText().toString());
                int lightHigh = Integer.parseInt(getEditText(R.id.et_light_eventcfg_high).getText().toString());
                recProfile.setLightEventConfig(lightLow, lightHigh, lightMode);
                break;
            // TEMPERATURE EVENT CONFIG
            case R.id.btn_temperature_eventcfg_read:
                recProfile.readTemperatureEventConfig();
                break;
            case R.id.btn_temperature_eventcfg_set:
                double tempLow = Double.parseDouble(getEditText(R.id.et_temperature_eventcfg_low).getText().toString());
                double tempHigh = Double.parseDouble(getEditText(R.id.et_temperature_eventcfg_high).getText().toString());
                recProfile.setTemperatureEventConfig(tempLow, tempHigh);
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
                int altimeterThreshold = Integer.parseInt(getEditText(R.id.et_altimeter_eventcfg_threshold).getText().toString());
                recProfile.setAltimeterEventConfig(altimeterThreshold, altimeterMode);
                break;
            // STEP DETECTION
            case R.id.btn_step_detection_read:
                recProfile.readStepDetection();
                break;
            case R.id.btn_step_detection_set:
                int stepDetThreshold = Integer.parseInt(getEditText(R.id.et_step_detection_threshold).getText().toString());
                recProfile.setStepDetectionThreshold(stepDetThreshold);
                break;
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
}


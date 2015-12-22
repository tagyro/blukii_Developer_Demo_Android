package com.blukii.androidblukiitutorial;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.EnablerStatus;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.DirectometerPositionMonitoring;
import com.blukii.android.blukiilibrary.DirectometerPositionMonitoringStatus;
import com.blukii.android.blukiilibrary.DirectometerProfile;
import com.blukii.android.blukiilibrary.Profile;
import com.blukii.android.blukiilibrary.ServiceMagnetometerCalibration;
import com.blukii.android.blukiilibrary.ServiceProfile;


public class DirectometerFragment extends Fragment implements View.OnClickListener {

    private final static String TAG = DirectometerFragment.class.getSimpleName();

    private Handler motionHandler;
    private Runnable motionRunnable;
    private int posCount = 0;

    /**
     * A broadcast receiver to receive updates on blukiis
     */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, 0);
            DirectometerProfile directometerProfile = (DirectometerProfile) MainActivity.getProfileById(getActivity(), DirectometerProfile.ID);

            // Check status
            if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                Log.d(TAG, "onReceive(). Status is not ok, action=" + action);
                return;
            }

            // Check profile
            if (directometerProfile == null) {
                Log.d(TAG, "onReceive(). DirectometerProfile is NULL");
                updateStatus(getText(R.string.profile_inactive).toString());

                enableAll(false);
                return;
            }

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    //updateStatus("Getrennt");
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    enableAll(false);
                    break;

                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    //updateStatus("Getrennt");
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    enableAll(false);
                    break;

                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    //updateStatus("Verbunden");
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    ((Button) getView().findViewById(R.id.btn_dir_enabler)).setEnabled(true);
                    ((Button) getView().findViewById(R.id.btn_dir_enabler)).setTag("activate");
                    ((Button) getView().findViewById(R.id.btn_dir_enabler)).setText(getText(R.string.btn_activateProfile).toString());


                    //calibration
                   /* getView().findViewById(R.id.btn_service_read_magnetometer_calibration).setEnabled(true);
                    getView().findViewById(R.id.btn_service_notify_magnetometer_calibration).setEnabled(true);*/

                    break;

                // ENABLER
                case DirectometerProfile.ACTION_ENABLED_DIRECTOMETER_PROFILE:
                    EnablerStatus enablerStatus = (EnablerStatus) intent.getSerializableExtra(Profile.EXTRA_ENABLER_STATUS);
                    MainActivity.handleEnablerStatus(getActivity(), enablerStatus, "Directometer");
                    if (enablerStatus == EnablerStatus.Activated) {
                        enableAll(true);

                        /*((ImageButton) getView().findViewById(R.id.btn_service_read_magnetometer_calibration)).setEnabled(false);
                        ((Button) getView().findViewById(R.id.btn_service_notify_magnetometer_calibration)).setEnabled(false);*/

                        ((Button) getView().findViewById(R.id.btn_dir_enabler)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_enabler)).setText(getText(R.string.btn_deactivateProfile).toString());
                        updateStatus(getText(R.string.profile_active).toString());

                    } else if (enablerStatus == EnablerStatus.Deactivated || enablerStatus == null) {
                        enableAll(false);

                       /* ((ImageButton) getView().findViewById(R.id.btn_service_read_magnetometer_calibration)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_service_notify_magnetometer_calibration)).setEnabled(true);*/

                        ((Button) getView().findViewById(R.id.btn_dir_enabler)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_enabler)).setText(getText(R.string.btn_activateProfile).toString());
                        ((Button) getView().findViewById(R.id.btn_dir_enabler)).setEnabled(true);
                        updateStatus(getText(R.string.profile_inactive).toString());
                    }
                    break;

                // X VALUE
               /* case DirectometerProfile.ACTION_READ_DIRECTOMETER_X_VALUE:
                    double x = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateX("" + x);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_X_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_read)).setEnabled(false);
                        updateX("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_x_value_read)).setEnabled(true);
                        updateX("notify deactivated");
                    }
                    break;

                // Y VALUE
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_Y_VALUE:
                    double y = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateY("" + y);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_Y_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_read)).setEnabled(false);
                        updateY("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_y_value_read)).setEnabled(true);
                        updateY("notify deactivated");
                    }
                    break;

                // Z VALUE
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_Z_VALUE:
                    double z = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateZ("" + z);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_Z_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_read)).setEnabled(false);
                        updateZ("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_z_value_read)).setEnabled(true);
                        updateZ("notify deactivated");
                    }
                    break;*/

                // X RAW VALUE
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_X_RAW_VALUE:
                    double xraw = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateXRaw("" + xraw);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_X_RAW_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_read)).setEnabled(false);
                        updateXRaw("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_x_raw_value_read)).setEnabled(true);
                        updateXRaw("notify deactivated");
                    }
                    break;

                // Y RAW VALUE
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_Y_RAW_VALUE:
                    double yraw = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateYRaw("" + yraw);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_Y_RAW_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_read)).setEnabled(false);
                        updateYRaw("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_y_raw_value_read)).setEnabled(true);
                        updateYRaw("notify deactivated");
                    }
                    break;

                // Z RAW VALUE
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_Z_RAW_VALUE:
                    double zraw = intent.getDoubleExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_MF_VALUE, -1.0);
                    updateZRaw("" + zraw);
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_Z_RAW_VALUE:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_read)).setEnabled(false);
                        updateZRaw("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_z_raw_value_read)).setEnabled(true);
                        updateZRaw("notify deactivated");
                    }
                    break;


                // POSITION MONITORING
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_POSITION_MONITORING:
                    DirectometerPositionMonitoring dpm = (DirectometerPositionMonitoring) intent.getSerializableExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_POSITION_MONITORING);

                    posCount++;

                    String s = dpm.getDescription() + " #" + posCount;
                    updatePositionMonitoring(s);
                    break;

                case DirectometerProfile.ACTION_SET_DIRECTOMETER_POSITION_MONITORING:
                    DirectometerPositionMonitoringStatus dpms = (DirectometerPositionMonitoringStatus) intent.getSerializableExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_POSITION_MONITORING_STATUS);
                    switch (dpms) {
                        case POSITION_1:
                            getView().findViewById(R.id.btn_dir_pos_mon_pos1).setEnabled(false);
                            break;
                        case POSITION_2:
                            getView().findViewById(R.id.btn_dir_pos_mon_pos2).setEnabled(false);
                            break;
                        case DISABLED:
                            getView().findViewById(R.id.btn_dir_pos_mon_pos1).setEnabled(true);
                            getView().findViewById(R.id.btn_dir_pos_mon_pos2).setEnabled(true);
                            posCount = 0;
                            break;
                    }
                    break;

                case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_POSITION_MONITORING:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_pos1)).setEnabled(false);
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_pos2)).setEnabled(false);
                        posCount = 0;
                        updatePositionMonitoring("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_pos1)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_pos_mon_pos2)).setEnabled(true);
                        posCount = 0;
                        updatePositionMonitoring("notify deactivated");
                        directometerProfile.setPositionMonitoring(DirectometerPositionMonitoringStatus.DISABLED);
                    }
                    break;

                // HEADING
                case DirectometerProfile.ACTION_READ_DIRECTOMETER_HEADING:
                    int heading = intent.getIntExtra(DirectometerProfile.EXTRA_DIRECTOMETER_DATA_HEADING, -1);
                    updateHeading("" + heading + " °");
                    break;

               /* case DirectometerProfile.ACTION_SET_NOTIFY_DIRECTOMETER_HEADING:
                    if (intent.getBooleanExtra(Profile.EXTRA_NOTIFICATIONS_ENABLED, false)) {
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setTag("deactivate");
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setText("Disable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_heading_read)).setEnabled(false);
                        updateHeading("notify activated");
                    } else {
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setTag("activate");
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setText("Enable Notify");
                        ((Button) getView().findViewById(R.id.btn_dir_heading_notify)).setEnabled(true);
                        ((Button) getView().findViewById(R.id.btn_dir_heading_read)).setEnabled(true);
                        updateHeading("notify deactivated");
                    }
                    break;*/

                // MAGNETOMETER CALIBRATION
                case ServiceProfile.ACTION_SET_NOTIFY_SERVICE_MAGNETOMETER_CALIBRATION:
                    String toast = "service magnetometer calibration set";
                    if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        toast = String.format("%s: %s (%d)", toast, "FAILED", status);
                    }
                    Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
                case ServiceProfile.ACTION_READ_SERVICE_MAGNETOMETER_CALIBRATION:
                    ServiceMagnetometerCalibration smc = (ServiceMagnetometerCalibration) intent.getSerializableExtra(ServiceProfile.EXTRA_SERVICE_MAGNETOMETER_CALIBRATION);
                    ((TextView) getView().findViewById(R.id.tv_service_magnetometer_calibration)).setText(String.valueOf(smc));
                    break;
                default:
                    break;
            }
        }
    };

    public DirectometerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DirectometerFragment.
     */
    public static DirectometerFragment newInstance() {
        DirectometerFragment fragment = new DirectometerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //// calibration
        //getView().findViewById(R.id.btn_service_read_magnetometer_calibration).setOnClickListener(this);
        //getView().findViewById(R.id.btn_service_notify_magnetometer_calibration).setOnClickListener(this);

        // enabler
        getView().findViewById(R.id.btn_dir_enabler).setOnClickListener(this);

//        // x
//        getView().findViewById(R.id.btn_dir_x_value_read).setOnClickListener(this);
//        getView().findViewById(R.id.btn_dir_x_value_notify).setOnClickListener(this);
//
//        // y
//        getView().findViewById(R.id.btn_dir_y_value_read).setOnClickListener(this);
//        getView().findViewById(R.id.btn_dir_y_value_notify).setOnClickListener(this);
//
//        // z
//        getView().findViewById(R.id.btn_dir_z_value_read).setOnClickListener(this);
//        getView().findViewById(R.id.btn_dir_z_value_notify).setOnClickListener(this);

        // x raw
        getView().findViewById(R.id.btn_dir_x_raw_value_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_x_raw_value_notify).setOnClickListener(this);

        // y raw
        getView().findViewById(R.id.btn_dir_y_raw_value_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_y_raw_value_notify).setOnClickListener(this);

        // z raw
        getView().findViewById(R.id.btn_dir_z_raw_value_read).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_z_raw_value_notify).setOnClickListener(this);

        // position monitoring
        //getView().findViewById(R.id.btn_dir_pos_mon_disable).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_pos_mon_pos1).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_pos_mon_pos2).setOnClickListener(this);
        getView().findViewById(R.id.btn_dir_pos_mon_notify).setOnClickListener(this);

//        // heading
//        getView().findViewById(R.id.btn_dir_heading_read).setOnClickListener(this);
//        getView().findViewById(R.id.btn_dir_heading_notify).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_directometer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        DirectometerProfile.addActions(intentFilter);
        ServiceProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        DirectometerProfile directometerProfile = (DirectometerProfile) MainActivity.getProfileById(getActivity(), DirectometerProfile.ID);
        if (directometerProfile == null) return;

        /*ServiceProfile serviceProfile = getServiceProfile();
        if (serviceProfile == null) return;*/




        switch (v.getId()) {
           /* // MAGNETOMETER CALIBRATION
            case R.id.btn_service_read_magnetometer_calibration:
                serviceProfile.readMagnetometerCalibration();
                break;

            case R.id.btn_service_notify_magnetometer_calibration:
                serviceProfile.notifyMagnetometerCalibration(((ToggleButton) v).isChecked());
                break;*/

            // ENABLER
            case R.id.btn_dir_enabler:
                if (v.getTag().equals("activate")) {
                    directometerProfile.setEnabled(true);
                    v.setEnabled(false);
                    updateStatus("activating...");
                } else {
                    directometerProfile.setEnabled(false);
                    v.setEnabled(false);
                    updateStatus("deactivating...");
                }
                break;

            // X
           /* case R.id.btn_dir_x_value_read:
                directometerProfile.readXValue();
                break;
            case R.id.btn_dir_x_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyXValue(true);
                    v.setEnabled(false);
                    updateX("activating...");
                } else {
                    directometerProfile.notifyXValue(false);
                    v.setEnabled(false);
                    updateX("deactivating...");
                }
                break;*/

            // Y
           /* case R.id.btn_dir_y_value_read:
                directometerProfile.readYValue();
                break;

            case R.id.btn_dir_y_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyYValue(true);
                    v.setEnabled(false);
                    updateY("activating...");
                } else {
                    directometerProfile.notifyYValue(false);
                    v.setEnabled(false);
                    updateY("deactivating...");
                }
                break;*/

            // Z
           /* case R.id.btn_dir_z_value_read:
                directometerProfile.readZValue();
                break;

            case R.id.btn_dir_z_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyZValue(true);
                    v.setEnabled(false);
                    updateZ("activating...");
                } else {
                    directometerProfile.notifyZValue(false);
                    v.setEnabled(false);
                    updateZ("deactivating...");
                }
                break;*/

            // X raw
            case R.id.btn_dir_x_raw_value_read:
                directometerProfile.readXRawValue();
                break;

            case R.id.btn_dir_x_raw_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyXRawValue(true);
                    v.setEnabled(false);
                    updateXRaw("activating...");
                } else {
                    directometerProfile.notifyXRawValue(false);
                    v.setEnabled(false);
                    updateXRaw("deactivating...");
                }
                break;

            // Y raw
            case R.id.btn_dir_y_raw_value_read:
                directometerProfile.readYRawValue();
                break;

            case R.id.btn_dir_y_raw_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyYRawValue(true);
                    v.setEnabled(false);
                    updateYRaw("activating...");
                } else {
                    directometerProfile.notifyYRawValue(false);
                    v.setEnabled(false);
                    updateYRaw("deactivating...");
                }
                break;

            // Z raw
            case R.id.btn_dir_z_raw_value_read:
                directometerProfile.readZRawValue();
                break;

            case R.id.btn_dir_z_raw_value_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyZRawValue(true);
                    v.setEnabled(false);
                    updateZRaw("activating...");
                } else {
                    directometerProfile.notifyZRawValue(false);
                    v.setEnabled(false);
                    updateZRaw("deactivating...");
                }
                break;

            // POSITION MONITORING
            /*case R.id.btn_dir_pos_mon_disable:
                directometerProfile.setPositionMonitoring(DirectometerPositionMonitoringStatus.DISABLED);
                getView().findViewById(R.id.btn_dir_pos_mon_pos1).setEnabled(false);
                getView().findViewById(R.id.btn_dir_pos_mon_pos2).setEnabled(false);
                break;*/

            case R.id.btn_dir_pos_mon_pos1:
                directometerProfile.setPositionMonitoring(DirectometerPositionMonitoringStatus.POSITION_1);
                break;

            case R.id.btn_dir_pos_mon_pos2:
                directometerProfile.setPositionMonitoring(DirectometerPositionMonitoringStatus.POSITION_2);
                break;

            case R.id.btn_dir_pos_mon_notify:

                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyPositionMonitoring(true);
                    v.setEnabled(false);
                    updatePositionMonitoring("activating...");
                    //getView().findViewById(R.id.btn_dir_pos_mon_disable).setEnabled(false);
                } else {
                    directometerProfile.notifyPositionMonitoring(false);
                    v.setEnabled(false);
                    updatePositionMonitoring("deactivating...");

                    //getView().findViewById(R.id.btn_dir_pos_mon_disable).setEnabled(true);
                }
                break;

            // HEADING
           /* case R.id.btn_dir_heading_read:
                directometerProfile.readHeading();
                break;

            case R.id.btn_dir_heading_notify:
                if (v.getTag().equals("activate")) {
                    directometerProfile.notifyHeading(true);
                    v.setEnabled(false);
                    updateHeading("activating...");
                } else {
                    directometerProfile.notifyHeading(false);
                    v.setEnabled(false);
                    updateHeading("deactivating...");
                }
                break;*/

            default:
                break;
        }
    }

    private void enableAll(boolean enable) {

        //calibration
       /* getView().findViewById(R.id.btn_service_read_magnetometer_calibration).setEnabled(enable);
        getView().findViewById(R.id.btn_service_notify_magnetometer_calibration).setEnabled(enable);*/

        getView().findViewById(R.id.btn_dir_enabler).setEnabled(enable);

        // x
      /*  getView().findViewById(R.id.btn_dir_x_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_x_value_notify).setEnabled(enable);
*/
        // y
        /*getView().findViewById(R.id.btn_dir_y_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_y_value_notify).setEnabled(enable);*/

        // z
        /*getView().findViewById(R.id.btn_dir_z_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_z_value_notify).setEnabled(enable);*/

        // x raw
        getView().findViewById(R.id.btn_dir_x_raw_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_x_raw_value_notify).setEnabled(enable);

        // y raw
        getView().findViewById(R.id.btn_dir_y_raw_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_y_raw_value_notify).setEnabled(enable);

        // z raw
        getView().findViewById(R.id.btn_dir_z_raw_value_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_z_raw_value_notify).setEnabled(enable);

        // position monitoring
        //getView().findViewById(R.id.btn_dir_pos_mon_disable).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_pos_mon_pos1).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_pos_mon_pos2).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_pos_mon_notify).setEnabled(enable);

        // heading
        /*getView().findViewById(R.id.btn_dir_heading_read).setEnabled(enable);
        getView().findViewById(R.id.btn_dir_heading_notify).setEnabled(enable);*/
    }

    private void updateStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_dir_status)).setText(newStatus);
    }

    private void updateConnectionStatus(String status) {
        View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(status);
    }

    private void updateX(String newStatus) {
       /* ((TextView) getView().findViewById(R.id.tv_dir_x)).setText("X: " + newStatus);*/
    }

    private void updateY(String newStatus) {
        /*((TextView) getView().findViewById(R.id.tv_dir_y)).setText("Y: " + newStatus);*/
    }

    private void updateZ(String newStatus) {
       /* ((TextView) getView().findViewById(R.id.tv_dir_z)).setText("Z: " + newStatus);*/
    }

    private void updateXRaw(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_dir_x_raw)).setText("X: " + newStatus);
    }

    private void updateYRaw(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_dir_y_raw)).setText("Y: " + newStatus);
    }

    private void updateZRaw(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_dir_z_raw)).setText("Z: " + newStatus);
    }

    private void updatePositionMonitoring(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_dir_position_monitoring)).setText("Position Monitoring: " + newStatus);
    }

    private void updateHeading(String newStatus) {
        /*((TextView) getView().findViewById(R.id.tv_dir_heading)).setText("Heading: " + newStatus);*/
    }

    private ServiceProfile getServiceProfile() {
        return (ServiceProfile) MainActivity.getProfileById(getActivity(), ServiceProfile.ID);
    }
}
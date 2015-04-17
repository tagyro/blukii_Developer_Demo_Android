package com.blukii.androidblukiitutorial;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blukii.android.blukiilibrary.Blukii;
import com.blukii.android.blukiilibrary.BlukiiConstants;
import com.blukii.android.blukiilibrary.DeviceInfoFirmwareBlukiiRevision;
import com.blukii.android.blukiilibrary.DeviceInfoFirmwareReleaseRevision;
import com.blukii.android.blukiilibrary.DeviceInfoProfile;

public class DeviceInfoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DeviceInfoFragment.class.getSimpleName();

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DeviceInfoProfile profile = (DeviceInfoProfile) MainActivity.getProfileById(getActivity(), DeviceInfoProfile.ID);

            final String action = intent.getAction();
            int status = intent.getIntExtra(BlukiiConstants.EXTRA_STATUS, -1);

            switch (action) {
                case Blukii.ACTION_DID_DISCONNECT_DEVICE:
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;
                case Blukii.ACTION_BLUKII_DEVICE_IS_READY:
                    updateConnectionStatus(getText(R.string.blukii_connected).toString());
                    ViewHelper.setEnabledOfAllControls(true, getView());
                    break;
                case Blukii.ACTION_ERROR_LOADING_SERVICES:
                    updateConnectionStatus(getText(R.string.blukii_disconnected).toString());
                    ViewHelper.setEnabledOfAllControls(false, getView());
                    break;

                // FIRMWARE REVISION
                case DeviceInfoProfile.ACTION_READ_DEVICE_INFO_FIRMWARE_REVISION:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        DeviceInfoFirmwareReleaseRevision releaseRevision = (DeviceInfoFirmwareReleaseRevision) intent.getSerializableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_RELEASE_REVISION);
                        DeviceInfoFirmwareBlukiiRevision blukiiRevision = (DeviceInfoFirmwareBlukiiRevision) intent.getSerializableExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_BLUKII_REVISION);
                        String feature = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_FEATURE_REVISION);
                        String bug = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_FIRMWARE_BUG_REVISION);
                        updateFirmware(releaseRevision.getDescription(), blukiiRevision.getDescription(), feature, bug);
                    }
                    break;

                // HARDWARE REVISION
                case DeviceInfoProfile.ACTION_READ_DEVICE_INFO_HARDWARE_REVISION:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {

                        String hardware = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_VALUE);
                        String sensors = getSensorsFromHardware(hardware);

                        updateHardware(hardware, sensors);
                    }
                    break;

                // MANUFACTURER NAME
                case DeviceInfoProfile.ACTION_READ_DEVICE_INFO_MANUFACTURE_NAME:
                    if (status == BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
                        String name = intent.getStringExtra(DeviceInfoProfile.EXTRA_DEVICE_INFO_VALUE);
                        String[] chars = name.split("");
                        String manufacture = "";
                        chars[21] = "ä";
                        for (int i = 0; i < chars.length; i++) {
                            manufacture += chars[i];
                        }
                        ((TextView) getView().findViewById(R.id.tv_device_info_manufacture)).setText(manufacture);
                    }
                    break;
            }
        }
    };


    public DeviceInfoFragment() {
        // Required empty public constructor
    }

    public static DeviceInfoFragment newInstance() {
        DeviceInfoFragment fragment = new DeviceInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_info, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewHelper.registerListener(this, getView());
        ViewHelper.setEnabledOfAllControls(false, getView());
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        DeviceInfoProfile.addActions(intentFilter);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mGattUpdateReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    public void onClick(View v) {
        DeviceInfoProfile profile = (DeviceInfoProfile) MainActivity.getProfileById(getActivity(), DeviceInfoProfile.ID);
        if (profile == null) return;

        switch (v.getId()) {
            // FIRMWARE REVISION
            case R.id.btn_device_info_read_firmware:
                profile.readFirmwareRevision();
                break;

            // HARDWARE REVISION
            case R.id.btn_device_info_read_hardware:
                profile.readHardwareRevision();
                break;

            // MANUFACTURE NAME
            case R.id.btn_device_info_read_manufacture:
                profile.readManufactureName();
                break;
        }

    }

    private void updateStatus(String newStatus) {
        ((TextView) getView().findViewById(R.id.tv_blukii_status)).setText(newStatus);
    }

    private void updateFirmware(String release, String blukii, String feature, String bug) {
        ((TextView) getView().findViewById(R.id.tv_device_info_firmware_release)).setText(release);
        ((TextView) getView().findViewById(R.id.tv_device_info_firmware_blukii)).setText(blukii);
        ((TextView) getView().findViewById(R.id.tv_device_info_firmware_feature)).setText(feature);
        ((TextView) getView().findViewById(R.id.tv_device_info_firmware_bug)).setText(bug);
    }

    private void updateHardware(String blukii, String sensors) {
        ((TextView) getView().findViewById(R.id.tv_device_info_hardware_blukii)).setText(blukii);
        ((TextView) getView().findViewById(R.id.tv_device_info_hardware_sensors)).setText(sensors);
    }

    private void toast(String toast, int status) {
        if (status != BlukiiConstants.BLUKII_DEVICE_STATUS_OK) {
            toast = String.format("%s: %s (%d)", toast, "FAILED", status);
        }
        Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private String getSensorsFromHardware (String hardware) {

        final int flagbit1 = 1;
        final int flagbit2 = 2;
        final int flagbit3 = 4;
        final int flagbit4 = 8;
        final int flagbit5 = 16;
        final int flagbit6 = 32;
        final int flagbit7 = 64;
        final int flagbit8 = 128;

        String[] sensorsArray = {"Accelerometer", "Magnetometer", "Humidity", "Light", "Temperature", "AirPressure", "Sensor 7", "Sensor 8"};
        String sensorsHex, sensors = "";
        byte[] byteArray;
        int n;

        String[] parts = hardware.split("-");
        sensorsHex = parts[2];
        byteArray = hexStringToByteArray(sensorsHex);
        n = byteArray[0];

        if ((n & flagbit1) == flagbit1) { sensors += sensorsArray[0] + " "; }
        if ((n & flagbit2) == flagbit2) { sensors += sensorsArray[1] + " "; }
        if ((n & flagbit3) == flagbit3) { sensors += sensorsArray[2] + " "; }
        if ((n & flagbit4) == flagbit4) { sensors += sensorsArray[3] + " "; }
        if ((n & flagbit5) == flagbit5) { sensors += sensorsArray[4] + " "; }
        if ((n & flagbit6) == flagbit6) { sensors += sensorsArray[5] + " "; }
        if ((n & flagbit7) == flagbit7) { sensors += sensorsArray[6] + " "; }
        if ((n & flagbit8) == flagbit8) { sensors += sensorsArray[7] + " "; }

        return sensors;
    }

    private void updateConnectionStatus(String status) {
        View v = (getActivity()).findViewById(R.id.statusbar);
        ((TextView) v).setText(status);
    }
}
